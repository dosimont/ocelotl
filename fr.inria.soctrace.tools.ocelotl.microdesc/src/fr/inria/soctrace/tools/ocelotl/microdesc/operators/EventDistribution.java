/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.microdesc.operators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop._3DMicroDescription;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.queries.OcelotlQueries;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;
import fr.inria.soctrace.tools.ocelotl.core.utils.DeltaManagerOcelotl;
import fr.inria.soctrace.tools.ocelotl.microdesc.config.DistributionConfig;

public class EventDistribution extends _3DMicroDescription {

	private static final Logger logger = LoggerFactory.getLogger(EventDistribution.class);
	private TimeSliceStateManager timeSliceManager;
	
	class OcelotlThread extends Thread {

		List<EventProducer> eventProducers;
		int threadNumber;
		int thread;
		int size;
		IProgressMonitor monitor;


		public OcelotlThread(final int threadNumber, final int thread,
				final int size, IProgressMonitor monitor) {
			super();
			this.threadNumber = threadNumber;
			this.thread = thread;
			this.size = size;
			this.monitor = monitor;
			
			start();
		}

		private void matrixWrite(final long slice, final EventProducer ep,
				String type) {
			synchronized (matrix) {
				matrix.get((int) slice)
						.get(ep)
						.put(type,
								matrix.get((int) slice).get(ep).get(type) + 1);
			}
		}

		private void matrixUpdate(final Event event, final EventProducer ep) {
			synchronized (matrix) {
				if (!matrix.get(0).get(ep)
						.containsKey(event.getType().getName())) {
					logger.debug("Adding " + event.getType().getName()
							+ " event");
					// addKey(state.getStateType());
					for (int incr = 0; incr < matrix.size(); incr++)
						for (final EventProducer epset : matrix.get(incr)
								.keySet())
							matrixPushType(incr, epset, event.getType()
									.getName());
				}
				final long slice = timeSliceManager.getTimeSlice(event
						.getTimestamp());
				matrixWrite(slice, ep, event.getType().getName());
			}
		}

		@Override
		public void run() {
			while (true) {
				long eventCount = 0;
				final List<Event> events = getEvents(size, monitor);
				if (monitor.isCanceled()) {
					return;
				}
				if (events.size() == 0)
					break;
				for (final Event event : events) {
					// final Map<Long, Long> distrib =
					// state.getTimeSlicesDistribution();
					matrixUpdate(event, event.getEventProducer());
					eventCount++;
					if (eventCount % 30 == 0) {
						if (monitor.isCanceled()) {
							return;
						}
					}
				}
			}
		}
	}

	public EventDistribution() throws SoCTraceException {
		super();
	}

	public EventDistribution(final OcelotlParameters parameters, IProgressMonitor monitor)
			throws SoCTraceException, OcelotlException {
		super(parameters, monitor);
	}

	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers, IProgressMonitor monitor)
			throws SoCTraceException, InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitor.subTask("Query events");
		eventIterator = ocelotlQueries.getEventIterator(eventProducers);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}
	
		timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
		.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		getOcelotlParameters().setTimeSliceManager(timeSliceManager);
		
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		
		monitor.subTask("Fill the matrix");
		for (int t = 0; t < ((DistributionConfig) getOcelotlParameters()
				.getTraceTypeConfig()).getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getThreadNumber(), t,
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getEventsPerThread(), monitor));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		
		if(monitor.isCanceled())
			return;
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	@Override
	protected void computeSubMatrix(final List<EventProducer> eventProducers,
			List<IntervalDesc> time, IProgressMonitor monitor) throws SoCTraceException,
			InterruptedException, OcelotlException {
		dm = new DeltaManagerOcelotl();
		dm.start();
		monitor.subTask("Query events");
		eventIterator = ocelotlQueries.getEventIterator(eventProducers, time);
		if (monitor.isCanceled()) {
			ocelotlQueries.closeIterator();
			return;
		}
		timeSliceManager = new TimeSliceStateManager(getOcelotlParameters()
				.getTimeRegion(), getOcelotlParameters().getTimeSlicesNumber());
		getOcelotlParameters().setTimeSliceManager(timeSliceManager);
		
		final List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		monitor.subTask("Fill the matrix");
		for (int t = 0; t < ((DistributionConfig) getOcelotlParameters()
				.getTraceTypeConfig()).getThreadNumber(); t++)
			threadlist.add(new OcelotlThread(
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getThreadNumber(), t,
					((DistributionConfig) getOcelotlParameters()
							.getTraceTypeConfig()).getEventsPerThread(), monitor));
		for (final Thread thread : threadlist)
			thread.join();
		ocelotlQueries.closeIterator();
		if(monitor.isCanceled())
			return;
		dm.end("VECTORS COMPUTATION: "
				+ getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}
	
	@Override
	public void initQueries() {
		try {
			ocelotlQueries = new OcelotlQueries(parameters);
		} catch (final SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
