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

package fr.inria.soctrace.tools.ocelotl.core.generic.query;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class GenericQuery {

	private final OcelotlParameters	ocelotlParameters;

	public GenericQuery(final OcelotlParameters ocelotlParameters) throws SoCTraceException {
		super();
		this.ocelotlParameters = ocelotlParameters;
	}

	public void checkTimeStamps() {
		// TODO calculer le max
	}

	public List<EventProducer> getAllEventProducers() throws SoCTraceException {
		final TraceDBObject traceDB = new TraceDBObject(ocelotlParameters.getTrace().getDbName(), DBMode.DB_OPEN);
		final EventProducerQuery eventProducerQuery = new EventProducerQuery(traceDB);
		final List<EventProducer> eplist = eventProducerQuery.getList();
		traceDB.close();
		return eplist;
	}

	public List<Event> getAllEvents() throws SoCTraceException {
		final GenericTraceSearch traceSearch = (GenericTraceSearch) new GenericTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<Event> elist = traceSearch.getEventsByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}

	public List<EventProxy> getAllEventsProxy() throws SoCTraceException {
		final GenericTraceSearch traceSearch = (GenericTraceSearch) new GenericTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<EventProxy> elist = traceSearch.getEventProxysByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}

	public List<GenericReducedEvent> getAllReducedEvents() throws SoCTraceException {
		final GenericTraceSearch traceSearch = (GenericTraceSearch) new GenericTraceSearch().initialize();
		final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
		time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
		final List<GenericReducedEvent> elist = traceSearch.getReducedEventsByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, null);
		traceSearch.uninitialize();
		return elist;
	}

	public List<Event> getEvents(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllEvents();
		else {
			final GenericTraceSearch traceSearch = (GenericTraceSearch) new GenericTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<Event> elist = traceSearch.getEventsByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}

	public List<EventProxy> getEventsProxy(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllEventsProxy();
		else {
			final GenericTraceSearch traceSearch = (GenericTraceSearch) new GenericTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<EventProxy> elist = traceSearch.getEventProxysByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}

	public OcelotlParameters getOcelotlParameters() {
		return ocelotlParameters;
	}

	public List<GenericReducedEvent> getReducedEvents(final List<EventProducer> eventProducers) throws SoCTraceException {
		if (eventProducers.size() == getAllEventProducers().size())
			return getAllReducedEvents();
		else {
			final GenericTraceSearch traceSearch = (GenericTraceSearch) new GenericTraceSearch().initialize();
			final List<IntervalDesc> time = new ArrayList<IntervalDesc>();
			time.add(new IntervalDesc(ocelotlParameters.getTimeRegion().getTimeStampStart(), ocelotlParameters.getTimeRegion().getTimeStampEnd()));
			final List<GenericReducedEvent> elist = traceSearch.getReducedEventsByEventTypesAndIntervalsAndEventProducers(ocelotlParameters.getTrace(), ocelotlParameters.getTraceTypeConfig().getTypes(), time, eventProducers);
			traceSearch.uninitialize();
			return elist;
		}
	}

}
