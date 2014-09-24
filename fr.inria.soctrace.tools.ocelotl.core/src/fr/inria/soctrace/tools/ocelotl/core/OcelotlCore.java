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

package fr.inria.soctrace.tools.ocelotl.core;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.ISpaceAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.ispaceaggregop.SpaceAggregationOperatorManager;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.ITimeAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.itimeaggregop.TimeAggregationOperatorManager;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.time.PartManager;

public class OcelotlCore {

	private static final boolean DEBUG = true;
	private static final boolean TEST = true;

	public static boolean isDebug() {
		return DEBUG;
	}

	public static boolean isTest() {
		return TEST;
	}

	OcelotlParameters ocelotlParameters;
	IMicroDescManager lpaggregManager;
	PartManager partManager;
	TimeAggregationOperatorManager timeOperators;
	ITimeAggregationOperator timeOperator;
	SpaceAggregationOperatorManager spaceOperators;
	ISpaceAggregationOperator spaceOperator;

	public OcelotlCore() {
		super();
	}

	public OcelotlCore(final OcelotlParameters ocelotlParameters)
			throws SoCTraceException {
		super();
		init(ocelotlParameters);
	}

	public void initTimeOperator(IProgressMonitor monitor) throws OcelotlException {
		setTimeOperator(monitor);
		if(monitor.isCanceled())
			return;
		try {
			lpaggregManager = timeOperator.createManager(monitor);
			if(monitor.isCanceled())
				return;
		} catch (UnsatisfiedLinkError e) {
			throw new OcelotlException(OcelotlException.JNI);
		}
	}

	public void computeQualities() {
		lpaggregManager.computeQualities();
	}

	public void computeDichotomy() throws OcelotlException {
		lpaggregManager.computeDichotomy();
		lpaggregManager.printParameters();
	}

	public void computeParts() {
		lpaggregManager.computeParts();
		// lpaggregManager.printParts();
		setSpaceOperator();
		lpaggregManager.print(this);
	}

	public IMicroDescManager getLpaggregManager() {
		return lpaggregManager;
	}

	public OcelotlParameters getOcelotlParameters() {
		return ocelotlParameters;
	}

	public PartManager getPartManager() {
		return partManager;
	}

	public PartManager getPartsManager() {
		return partManager;
	}

	public ISpaceAggregationOperator getSpaceOperator() {
		return spaceOperator;
	}

	public SpaceAggregationOperatorManager getSpaceOperators() {
		return spaceOperators;
	}

	public ITimeAggregationOperator getTimeOperator() {
		return timeOperator;
	}

	public TimeAggregationOperatorManager getTimeOperators() {
		return timeOperators;
	}

	public void init(final OcelotlParameters ocelotlParameters)
			throws SoCTraceException {
		setOcelotlParameters(ocelotlParameters);
		timeOperators = new TimeAggregationOperatorManager(ocelotlParameters);
		spaceOperators = new SpaceAggregationOperatorManager(this);
	}

	public void setOcelotlParameters(final OcelotlParameters ocelotlParameters) {
		this.ocelotlParameters = ocelotlParameters;
	}

	public void setSpaceOperator() {
		spaceOperators.activateSelectedOperator();
		spaceOperator = spaceOperators.getSelectedOperator();
	}
	
	public void setTimeOperator(IProgressMonitor monitor) throws OcelotlException {
		timeOperators.activateSelectedOperator(monitor);
		timeOperator = timeOperators.getSelectedOperator();
	}

}
