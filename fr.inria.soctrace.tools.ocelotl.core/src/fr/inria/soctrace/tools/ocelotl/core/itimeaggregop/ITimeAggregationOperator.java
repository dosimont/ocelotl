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

package fr.inria.soctrace.tools.ocelotl.core.itimeaggregop;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.timeaggregmanager.IMicroDescManager;
import fr.inria.soctrace.tools.ocelotl.core.timeslice.TimeSliceStateManager;

public interface ITimeAggregationOperator {

	public IMicroDescManager createManager() throws OcelotlException;

	public OcelotlParameters getOcelotlParameters();

	//public TimeSliceStateManager getTimeSlicesManager();

	public int getVectorSize();

	public int getVectorNumber();

	public void initVectors() throws SoCTraceException;

	public void print();

	public void setOcelotlParameters(OcelotlParameters parameters)
			throws SoCTraceException, InterruptedException, OcelotlException;

}
