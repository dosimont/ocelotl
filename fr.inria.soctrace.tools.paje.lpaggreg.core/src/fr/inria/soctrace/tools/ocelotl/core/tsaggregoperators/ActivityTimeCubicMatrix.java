/* ===========================================================
 * LPAggreg core module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to visualize a Paje
 * trace across an aggregated representation.
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
 */

package fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public class ActivityTimeCubicMatrix implements ITimeSliceCubicMatrix{

	protected Query						query;
	protected List<HashMap<String, HashMap<String, Long>>>	matrix	= new ArrayList<HashMap<String, HashMap<String, Long>>>();
	protected int							eventsNumber;
	protected TimeSliceManager				timeSliceManager;

	public ActivityTimeCubicMatrix(Query query) throws SoCTraceException {
		super();
		this.query = query;
		query.checkTimeStamps();
		timeSliceManager = new TimeSliceManager(query.getLpaggregParameters().getTimeRegion(), query.getLpaggregParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}
	
	protected void computeSubMatrix(List<EventProducer> eventProducers) throws SoCTraceException {
		DeltaManager dm = new DeltaManager();
		dm.start();
		List<Event> fullEvents = query.getEvents(eventProducers);
		eventsNumber = fullEvents.size();
		dm.end("QUERIES : " + eventProducers.size() + " Event Producers : " + fullEvents.size() + " Events");
		dm = new DeltaManager();
		dm.start();
		Map<Integer, List<Event>> eventList = new HashMap<Integer, List<Event>>();
		for (EventProducer ep : eventProducers)
			eventList.put(ep.getId(), new ArrayList<Event>());
		for (Event e : fullEvents)
			eventList.get(e.getEventProducer().getId()).add(e);
		for (EventProducer ep : eventProducers) {
			List<State> state = new ArrayList<State>();
			List<Event> events = eventList.get(ep.getId());
			for (int i=0; i<events.size()-1; i++){
				state.add(new State(events.get(i), events.get(i + 1), timeSliceManager));
				if (query.getLpaggregParameters().getSleepingStates().contains(state.get(state.size()-1).getStateType()))
					state.remove(state.size()-1);
				else{
					Map<Long, Long> distrib = state.get(state.size()-1).getTimeSlicesDistribution();
					if (!matrix.get(0).get(ep.getName()).containsKey(state.get(state.size()-1).getStateType())){
						System.out.println("Adding " + state.get(state.size()-1).getStateType() + " state");
						for (int incr = 0; incr < matrix.size(); incr++){
							for (EventProducer epname : eventProducers){
								matrix.get(incr).get(epname.getName()).put(state.get(state.size()-1).getStateType(), 0L);
							}
						}
					}
					for (long it : distrib.keySet()){
						matrix.get((int) it).get(ep.getName()).put(state.get(state.size()-1).getStateType(), matrix.get((int) it).get(ep.getName()).get(state.get(state.size()-1).getStateType()) + distrib.get(it));
					}
				}
			}
		}
		dm.end("VECTORS COMPUTATION : " + query.getLpaggregParameters().getTimeSlicesNumber() + " timeslices");
	}


	public void computeMatrix() throws SoCTraceException {
		eventsNumber = 0;
		DeltaManager dm = new DeltaManager();
		dm.start();	
		int epsize = query.getLpaggregParameters().getEventProducers().size();
		if ((query.getLpaggregParameters().getMaxEventProducers()==0) || (epsize<query.getLpaggregParameters().getMaxEventProducers())){
			computeSubMatrix(query.getLpaggregParameters().getEventProducers());
		}else{
			List<EventProducer> producers = (query.getLpaggregParameters().getEventProducers().size() == 0) ? query.getAllEventProducers() : query.getLpaggregParameters().getEventProducers();
			for (int i=0; i<epsize; i=i+query.getLpaggregParameters().getMaxEventProducers()){
				computeSubMatrix(producers.subList(i, Math.min(epsize-1, i+query.getLpaggregParameters().getMaxEventProducers())));
			}
		}

		dm.end("TOTAL (QUERIES + COMPUTATION) : " + epsize + " Event Producers, " + eventsNumber + " Events");
	}

	public List<HashMap<String, HashMap<String, Long>>> getMatrix() {
		return matrix;
	}

	public Query getQueries() {
		return query;
	}

	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	public int getVectorSize() {
		return matrix.get(0).size();
	}

	public int getVectorsNumber() {
		return matrix.size();
	}

	public void initVectors() throws SoCTraceException {
		List<EventProducer> producers = query.getLpaggregParameters().getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<String, HashMap<String, Long>>());

			for (EventProducer ep : producers)
				matrix.get((int) i).put(ep.getName(), new HashMap<String, Long>());
		}
	}

	public void print() {
		System.out.println();
		System.out.println("Distribution Vectors");
		int i = 0;
		for (HashMap<String, HashMap<String, Long>> it : matrix) {
			System.out.println();
			System.out.println("slice " + i++);
			System.out.println();
			for (String ep : it.keySet())
				System.out.println(ep + " = " + it.get(ep));
		}
	}

	public void setQueries(Query query) {
		this.query = query;
	}

	@Override
	public void computeVectors() {
		// TODO Auto-generated method stub
		
	}

}
