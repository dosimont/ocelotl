/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
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

package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.core.lpaggreg.MLPAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public abstract class _3DCacheMicroDescription extends CacheTimeAggregationOperator implements I3DMicroDescription {

	protected List<HashMap<String, HashMap<String, Long>>>	matrix;
	List<String>											keys;

	public _3DCacheMicroDescription() {
		super();
	}

	public _3DCacheMicroDescription(final OcelotlParameters parameters) throws SoCTraceException {
		super();
		try {
			setOcelotlParameters(parameters);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void computeMatrix() throws SoCTraceException {
		keys = new ArrayList<String>();
		eventsNumber = 0;
		final DeltaManager dm = new DeltaManager();
		dm.start();
		final int epsize = getOcelotlParameters().getEventProducers().size();
		if (getOcelotlParameters().getMaxEventProducers() == 0 || epsize < getOcelotlParameters().getMaxEventProducers())
			try {
				computeSubMatrix(getOcelotlParameters().getEventProducers());
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else {
			final List<EventProducer> producers = getOcelotlParameters().getEventProducers().size() == 0 ? ocelotlQueries.getAllEventProducers() : getOcelotlParameters().getEventProducers();
			for (int i = 0; i < epsize; i = i + getOcelotlParameters().getMaxEventProducers())
				try {
					computeSubMatrix(producers.subList(i, Math.min(epsize - 1, i + getOcelotlParameters().getMaxEventProducers())));
				} catch (final InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		dm.end("TOTAL (QUERIES + COMPUTATION) : " + epsize + " Event Producers, " + eventsNumber + " Events");
	}

	@Override
	public MLPAggregManager createManager() {
		return new MLPAggregManager(this);

	}

	@Override
	public List<HashMap<String, HashMap<String, Long>>> getMatrix() {
		return matrix;
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
		matrix = new ArrayList<HashMap<String, HashMap<String, Long>>>();
		final List<EventProducer> producers = getOcelotlParameters().getEventProducers();
		for (long i = 0; i < timeSliceManager.getSlicesNumber(); i++) {
			matrix.add(new HashMap<String, HashMap<String, Long>>());

			for (final EventProducer ep : producers)
				matrix.get((int) i).put(ep.getName(), new HashMap<String, Long>());
		}
	}

	public void matrixPushType(final int incr, final String epstring, final String key, final Map<Long, Long> distrib) {
		matrix.get(incr).get(epstring).put(key, 0L);
	}

	public void matrixWrite(final long it, final EventProducer ep, final String key, final Map<Long, Long> distrib) {
		matrix.get((int) it).get(ep.getName()).put(key, matrix.get((int) it).get(ep.getName()).get(key) + distrib.get(it));
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

}