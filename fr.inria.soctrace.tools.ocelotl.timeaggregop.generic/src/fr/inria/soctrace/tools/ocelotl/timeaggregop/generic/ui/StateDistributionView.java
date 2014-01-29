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

package fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.ui;

import org.eclipse.swt.widgets.Shell;


public class StateDistributionView extends StateDistributionBaseView {


	public StateDistributionView(Shell shell) {
		super(shell);
	}

	@Override
	public void setParameters() {
		if (config.getTypes().isEmpty()){
//		for (int i = 0; i < ocelotlView.getConfDataLoader().getTypes().size(); i++)
//			if (ocelotlView.getConfDataLoader().getTypes().get(i).getName().contains(PajeExternalConstants.PajeSetState)) {
//				if (!config.getTypes().contains(ocelotlView.getConfDataLoader().getTypes().get(i)))
//					config.getTypes().add(ocelotlView.getConfDataLoader().getTypes().get(i));
//				break;
//			}
		if (!config.getIdles().contains("IDLE"))
			config.getIdles().add("IDLE");
		}
		listViewerEventTypes.setInput(config.getTypes());
		listViewerIdleStates.setInput(config.getIdles());
		}
		
	}


