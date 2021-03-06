/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a Framesoc plug in that enables to visualize a trace 
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

package fr.inria.soctrace.tools.ocelotl.core.ivisuop;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.time.ITimeManager;

abstract public class VisuTOperator implements IVisuTOperator {

	protected List<Part> parts;
	protected OcelotlCore ocelotlCore;
	protected int timeSliceNumber;
	protected long timeSliceDuration;
	protected ITimeManager timeManager;

	public VisuTOperator() {
		super();
	}

	public VisuTOperator(final OcelotlCore ocelotlCore) {
		super();
		setOcelotlCore(ocelotlCore);
	}

	abstract protected void computeParts();

	@Override
	public OcelotlCore getOcelotlCore() {
		return ocelotlCore;
	}

	@Override
	public Part getPart(final int i) {
		return parts.get(i);
	}

	@Override
	public int getPartNumber() {
		return parts.size();
	}

	@Override
	public int getSliceNumber() {
		return timeSliceNumber;
	}

	// Initialize the number of parts for the current aggregation parameter
	protected void initParts() {
		int oldPart = 0;
		// First part
		parts.add(new Part(0, 1, null));
		for (int i = 0; i < timeManager.getParts().size(); i++)
			// If we are still in the same part
			if (timeManager.getParts().get(i) == oldPart)
				// Extend the current part
				parts.get(parts.size() - 1).setEndPart(i + 1);
			else {
				// Start a new part
				oldPart = timeManager.getParts().get(i);
				parts.add(new Part(i, i + 1, null));
			}
	}

	@Override
	public void setOcelotlCore(final OcelotlCore ocelotlCore) {
		this.ocelotlCore = ocelotlCore;
		timeManager = (ITimeManager) ocelotlCore.getLpaggregManager();
		timeSliceNumber = ocelotlCore.getOcelotlParameters()
				.getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion()
				.getTimeDuration()
				/ timeSliceNumber;
		parts = new ArrayList<Part>();
		initParts();
		computeParts();
	}

	@Override
	public void initManager(OcelotlCore ocelotlCore, IDataAggregManager aManager) {
		this.ocelotlCore = ocelotlCore;
		timeManager = (ITimeManager) aManager;
		timeSliceNumber = ocelotlCore.getOcelotlParameters()
				.getTimeSlicesNumber();
		timeSliceDuration = ocelotlCore.getOcelotlParameters().getTimeRegion()
				.getTimeDuration()
				/ timeSliceNumber;
		parts = new ArrayList<Part>();
		initParts();
		computeParts();
	}

	public double getMaxValue(){
		return -1.0;
	}
}
