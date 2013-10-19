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

package fr.inria.soctrace.tools.ocelotl.core.iaggregop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.VLPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.paje.PajeState;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.Query;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.PajeReducedEvent;
import fr.inria.soctrace.tools.ocelotl.core.ts.IState;
import fr.inria.soctrace.tools.ocelotl.core.ts.State;
import fr.inria.soctrace.tools.ocelotl.core.ts.TimeSliceManager;

public abstract class CubicMatrix implements ICubicMatrix {

	protected Query											query;
	protected List<HashMap<String, HashMap<String, Long>>>	matrix;
	protected int											eventsNumber;
	protected TimeSliceManager								timeSliceManager;

	public CubicMatrix(final Query query) throws SoCTraceException {
		super();
		setQueries(query);
	}

	public CubicMatrix() {
		super();
	}

	public abstract void computeMatrix() throws SoCTraceException;

	@Override
	public List<HashMap<String, HashMap<String, Long>>> getMatrix() {
		return matrix;
	}

	@Override
	public Query getQueries() {
		return query;
	}

	@Override
	public TimeSliceManager getTimeSlicesManager() {
		return timeSliceManager;
	}

	@Override
	public int getVectorSize() {
		return matrix.get(0).size();
	}

	@Override
	public int getVectorsNumber() {
		return matrix.size();
	}

	@Override
	public void initVectors() throws SoCTraceException {
		final List<EventProducer> producers = query.getOcelotlParameters().getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<String, HashMap<String, Long>>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep.getName(), new HashMap<String, Long>());
		}
	}

	@Override
	public void print() {
		System.out.println();
		System.out.println("Distribution Vectors");
		int i = 0;
		for (final HashMap<String, HashMap<String, Long>> it : matrix) {
			System.out.println();
			System.out.println("slice " + i++);
			System.out.println();
			for (final String ep : it.keySet())
				System.out.println(ep + " = " + it.get(ep));
		}
	}

	@Override
	public void setQueries(final Query query) throws SoCTraceException {
		this.query = query;
		query.checkTimeStamps();
		matrix = new ArrayList<HashMap<String, HashMap<String, Long>>>();
		timeSliceManager = new TimeSliceManager(query.getOcelotlParameters().getTimeRegion(), query.getOcelotlParameters().getTimeSlicesNumber());
		initVectors();
		computeMatrix();
	}
	
	public MLPAggregManager createManager(){
		return new MLPAggregManager(this);
		
	}

}
