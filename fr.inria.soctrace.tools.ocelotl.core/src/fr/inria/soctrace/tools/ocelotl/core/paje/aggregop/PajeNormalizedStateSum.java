/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

package fr.inria.soctrace.tools.ocelotl.core.paje.aggregop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.Matrix;
import fr.inria.soctrace.tools.ocelotl.core.paje.PajeState;
import fr.inria.soctrace.tools.ocelotl.core.query.EventProxy;
import fr.inria.soctrace.tools.ocelotl.core.query.ReducedEventCache;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.query.ReducedEvent;
import fr.inria.soctrace.tools.ocelotl.core.ts.IState;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

public class PajeNormalizedStateSum extends Matrix {
	
	public final static String 									descriptor								="State Sum (normalized)";
	public final static String 									traceType								=PajeConstants.PajeFormatName;

	public PajeNormalizedStateSum(final Query query) throws SoCTraceException {
		super(query);
		System.out.println(descriptor);
	}


	public PajeNormalizedStateSum() throws SoCTraceException {
		super();
	}


	protected void computeSubMatrixNonCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<ReducedEvent> fullEvents = query.getReducedEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		final Map<Integer, List<ReducedEvent>> eventList = new HashMap<Integer, List<ReducedEvent>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<ReducedEvent>());
		for (final ReducedEvent e : fullEvents)
			eventList.get(e.EP).add(e);
		List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < query.getOcelotlParameters().getThread(); t++) {
			threadlist.add(new OcelotlThread(eventProducers, eventList, query.getOcelotlParameters().getThread(), t, false));
		}
		for (Thread thread : threadlist)
			thread.join();
		dm.end("VECTOR COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	protected void computeSubMatrixCached(final List<EventProducer> eventProducers) throws SoCTraceException, InterruptedException {
		dm = new DeltaManager();
		dm.start();
		final List<EventProxy> fullEvents = query.getEventsProxy(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		final Map<Integer, List<EventProxy>> eventList = new HashMap<Integer, List<EventProxy>>();
		for (final EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<EventProxy>());
		for (final EventProxy e : fullEvents)
			eventList.get(e.EP).add(e);
		List<OcelotlThread> threadlist = new ArrayList<OcelotlThread>();
		for (int t = 0; t < query.getOcelotlParameters().getThread(); t++) {
			threadlist.add(new OcelotlThread(eventProducers, eventList, query.getOcelotlParameters().getThread(), t, false));
		}
		for (Thread thread : threadlist)
			thread.join();
		dm.end("VECTOR COMPUTATION : " + query.getOcelotlParameters().getTimeSlicesNumber() + " timeslices");
	}

	class OcelotlThread extends Thread {

		List<EventProducer>					eventProducers;
		Map<Integer, List<EventProxy>>		eventProxyList;
		Map<Integer, List<ReducedEvent>>	eventList;
		int									threadNumber;
		int									thread;
		boolean								cached;

		@Override
		public void run() {
			if (cached)
				try {
					cacheRun();
				} catch (SoCTraceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				noCacheRun();
		}

		private void cacheRun() throws SoCTraceException {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				ReducedEventCache cache;
				cache = new ReducedEventCache(query.getOcelotlParameters());
				EventProducer ep = eventProducers.get(t);
					cache = new ReducedEventCache(query.getOcelotlParameters());
					IState state;
					final List<EventProxy> events = eventProxyList.get(ep.getId());
					for (int i = 0; i < events.size() - 1; i++) {
						state = (new PajeState(cache.getEventMultiPageEPCache(events.get(i)), cache.getEventMultiPageEPCache(events.get(i + 1)), timeSliceManager));
						if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())) {
							final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
							for (final long it : distrib.keySet())
								matrixWrite(it, ep, distrib);
						}
					}
					long total = 0;
					for (int i = 0; i < matrix.size(); i++)
						total = matrix.get(i).get(ep.getName()) + total;
					if (total != 0)
						for (int i = 0; i < matrix.size(); i++)
							matrix.get(i).put(ep.getName(), matrix.get(i).get(ep.getName()) * 1000000000 / total);
					
					cache.close();
					int c = getCount();
					if (c % EPCOUNT == 0)
						total(c);
					
				 
			}

		}

		private void noCacheRun() {
			for (int t = getEP(); t < eventProducers.size(); t = getEP()) {
				EventProducer ep = eventProducers.get(t);
				final List<ReducedEvent> events = eventList.get(ep.getId());
				IState state;
				for (int i = 0; i < events.size() - 1; i++) {
					state = (new PajeState(events.get(i), events.get(i + 1), timeSliceManager));
					if (!query.getOcelotlParameters().getSleepingStates().contains(state.getStateType())) {
						final Map<Long, Long> distrib = state.getTimeSlicesDistribution();
						for (final long it : distrib.keySet())
							matrixWrite(it, ep, distrib);
					}
				}
				long total = 0;
				for (int i = 0; i < matrix.size(); i++)
					total = matrix.get(i).get(ep.getName()) + total;
				if (total != 0)
					for (int i = 0; i < matrix.size(); i++)
						matrix.get(i).put(ep.getName(), matrix.get(i).get(ep.getName()) * 1000000000 / total);
				int c = getCount();
				if (c % EPCOUNT == 0)
					total(c);
			}

		}

		@SuppressWarnings("unchecked")
		public OcelotlThread(List<EventProducer> eventProducers, Object eventList, int threadNumber, int thread, boolean cached) {
			super();
			this.eventProducers = eventProducers;
			this.cached = cached;
			if (cached)
				this.eventProxyList = (Map<Integer, List<EventProxy>>) eventList;
			else
				this.eventList = (Map<Integer, List<ReducedEvent>>) eventList;
			this.threadNumber = threadNumber;
			this.thread = thread;

			start();
		}
	}
	
	@Override
	public String descriptor() {
		return descriptor;
	}

	@Override
	public String traceType() {
		return traceType;
	}




	}
