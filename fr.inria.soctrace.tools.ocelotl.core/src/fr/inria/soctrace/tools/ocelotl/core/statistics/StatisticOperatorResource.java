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

package fr.inria.soctrace.tools.ocelotl.core.statistics;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;

public class StatisticOperatorResource {

	String operatorClass;
	String name;
	List<String> timeCompatibility = new ArrayList<String>();
	List<String> aggregatorCompatibility = new ArrayList<String>();
	String paramWinClass;
	String paramConfig;
	String visualization;
	String bundle;
	String unit;
	int selectionPriority;
	boolean leaveAggregationCompatiblity;

	public StatisticOperatorResource() {
		// TODO Auto-generated constructor stub
	}

	public StatisticOperatorResource(final String operatorClass,
			final String name, final List<String> timeCompatibility,
			final String visualization, String paramWinClass,
			String paramConfig, final int selectionPriority,
			final boolean leaveAggregationCompatiblity, final String bundle, final String unit) {
	super();
		this.operatorClass = operatorClass;
		this.name = name;
		this.timeCompatibility = timeCompatibility;
		this.visualization = visualization;
		this.paramWinClass = paramWinClass;
		this.paramConfig = paramConfig;
		this.selectionPriority = selectionPriority;
		this.leaveAggregationCompatiblity = leaveAggregationCompatiblity;
		this.bundle = bundle;
		this.unit=unit;
	}

	public String getBundle() {
		return bundle;
	}

	public String getName() {
		return name;
	}

	public String getOperatorClass() {
		return operatorClass;
	}

	public List<String> getTimeCompatibility() {
		return timeCompatibility;
	}
	
	public List<String> getAggregatorCompatibility() {
		return aggregatorCompatibility;
	}

	public String getVisualization() {
		return visualization;
	}

	public void setBundle(final String bundle) {
		this.bundle = bundle;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getParamConfig() {
		return paramConfig;
	}

	public String getParamWinClass() {
		return paramWinClass;
	}

	public void setOperatorClass(final String operatorClass) {
		this.operatorClass = operatorClass;
	}

	public void setTimeCompatibility(final String string) {
		final String[] tmp = string.split(OcelotlConstants.MultipleValueExtensionSeparator);
		for (final String s : tmp)
			timeCompatibility.add(s);
	}
	
	public void setAggregatorCompatibility(final String string) {
		final String[] tmp = string.split(OcelotlConstants.MultipleValueExtensionSeparator);
		for (final String s : tmp)
			aggregatorCompatibility.add(s);
	}

	public void setVisualization(final String visualization) {
		this.visualization = visualization;
	}

	public void setParamConfig(final String paramConfig) {
		this.paramConfig = paramConfig;
	}

	public void setParamWinClass(final String paramWinClass) {
		this.paramWinClass = paramWinClass;
	}

	public int getSelectionPriority() {
		return selectionPriority;
	}

	public void setSelectionPriority(int selectionPriority) {
		this.selectionPriority = selectionPriority;
	}

	public boolean isLeaveAggregationCompatiblity() {
		return leaveAggregationCompatiblity;
	}

	public void setLeaveAggregationCompatiblity(boolean leaveAggregationCompatiblity) {
		this.leaveAggregationCompatiblity = leaveAggregationCompatiblity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
}
