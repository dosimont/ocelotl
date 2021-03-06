/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.microdesc.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.events.IState;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.microdesc.genericevents.GenericState;

public class StateAverageDistribution extends StateDistribution {

	private static final Logger logger = LoggerFactory
			.getLogger(StateAverageDistribution.class);
	protected List<HashMap<EventProducer, HashMap<String, Integer>>> stateCounter;
	
	public StateAverageDistribution() {
		super();
		stateCounter = new ArrayList<HashMap<EventProducer, HashMap<String, Integer>>>();
	}

	@Override
	public void computeSubMatrix(final List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitorMessageDatabaseQuery(monitor);
		eventIterator = ocelotlQueries.getStateIterator(eventProducers, time,
				monitor);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}
		
		setTimeSliceManager(new TimeSliceStateManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber()));
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		monitorMessageDatabaseQuery(monitor);
		for (int t = 0; t < getOcelotlParameters().getNumberOfThreads(); t++)
			threadlist.add(new OcelotlThread(getOcelotlParameters()
					.getNumberOfThreads(), t, getOcelotlParameters()
					.getEventsPerThread(), monitor));
		for (final Thread thread : threadlist)
			thread.join();
		
		computeAverage();
		
		ocelotlQueries.closeIterator();
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	/**
	 * Compute the average duration of a state for each time slice
	 */
	private void computeAverage() {
		// For each time slice
		for (int i = 0; i < matrix.size(); i++) {
			// For each event producer
			for (final EventProducer ep : matrix.get(i).keySet()) {
				// For each state
				for (String aState : matrix.get(i).get(ep).keySet()) {
					if (stateCounter.get(i).get(ep).get(aState) != 0)
						// Divide the current value by the number of state in
						// the time slice
						matrix.get(i)
								.get(ep)
								.put(aState,
										matrix.get(i).get(ep).get(aState)
												/ stateCounter.get(i).get(ep)
														.get(aState));
				}
			}
		}
	}

	class OcelotlThread extends Thread {

		List<EventProducer> localActiveEventProducers;
		int threadNumber;
		int thread;
		int size;
		IProgressMonitor monitor;

		public OcelotlThread() {
			super();
		}

		public OcelotlThread(final int threadNumber, final int thread,
				final int size, IProgressMonitor monitor) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;
			this.monitor = monitor;
			localActiveEventProducers = new ArrayList<EventProducer>();
			
			start();
		}

		protected void matrixUpdate(final IState state, final EventProducer ep,
				final Map<Long, Double> distrib) {
			// Mutex
			synchronized (getMatrix()) {
				// If the event type is not in the matrix yet
				if (!getMatrix().get(0).get(ep)
						.containsKey(state.getType())) {
					logger.debug("Adding " + state.getType() + " state");

					// Add the type for each slice and ep and init to zero
					for (int incr = 0; incr < getMatrix().size(); incr++)
						for (final EventProducer epset : getMatrix().get(incr)
								.keySet()) {
							matrixPushType(incr, epset, state.getType());
							stateCounter.get(incr).get(epset)
									.put(state.getType(), 0);
						}
				}
				for (final long it : distrib.keySet()) {
					matrixWrite(it, ep, state.getType(), distrib);
					stateCounter
							.get((int) it)
							.get(ep)
							.put(state.getType(),
									(stateCounter.get((int) it).get(ep)
											.get(state.getType())) + 1);
				}
			}
		}

		@Override
		public void run() {
			EventProducer currentEP = null;
			while (true) {
				final List<Event> events = getEvents(size, monitor);
				if (events.size() == 0)
					break;
				if (monitor.isCanceled())
					return;

				IState state;
				// For each event
				for (final Event event : events) {
					// Convert to state
					state = new GenericState(event, (TimeSliceStateManager) timeSliceManager);
					// Get duration of the state for every time slice it is in
					final Map<Long, Double> distrib = state
							.getTimeSlicesDistribution();		
					EventProducer eventEP = event.getEventProducer();
					
					if(aggregatedProducers.containsKey(event.getEventProducer()))
						eventEP = aggregatedProducers.get(event.getEventProducer());
					
					
					matrixUpdate(state, eventEP, distrib);
					if (currentEP != eventEP) {
						currentEP = eventEP;

						// If the event producer is not in the active producers list
						if (!localActiveEventProducers.contains(eventEP)) {
							// Add it
							localActiveEventProducers.add(eventEP);
						}
					}
					if (monitor.isCanceled())
						return;
				}
				monitor.worked(events.size());
			}
			// Merge local active event producers to the global one
			synchronized (activeProducers) {
				for (EventProducer ep : localActiveEventProducers) {
					if (!activeProducers.contains(ep))
						activeProducers.add(ep);
				}
			}
		}
	}
	
	@Override
	public void initMatrix() throws SoCTraceException {
		matrix = new ArrayList<HashMap<EventProducer, HashMap<String, Double>>>();
		final List<EventProducer> producers = parameters.getCurrentProducers();

		for (long i = 0; i < parameters.getTimeSlicesNumber(); i++) {
			matrix.add(new HashMap<EventProducer, HashMap<String, Double>>());
			stateCounter
					.add(new HashMap<EventProducer, HashMap<String, Integer>>());

			for (final EventProducer ep : producers) {
				if (!aggregatedProducers.containsKey(ep)) {
					matrix.get((int) i).put(ep, new HashMap<String, Double>());
					stateCounter.get((int) i).put(ep,
							new HashMap<String, Integer>());
				}
			}
		}
	}

}
