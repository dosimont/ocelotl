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

import java.util.ArrayList;
import java.util.List;

public class TimeAggregationOperatorResource {

	String operatorClass;

	String name;

	boolean generic;
	List<String> traceFormats = new ArrayList<String>();
	List<String> spaceCompatibility = new ArrayList<String>();
	String paramWinClass;
	String paramConfig;
	String bundle;

	public TimeAggregationOperatorResource() {
		// TODO Auto-generated constructor stub
	}

	public TimeAggregationOperatorResource(final String operatorClass,
			final String name, final boolean generic,
			final List<String> traceFormats,
			final List<String> spaceCompatibility, final String paramWinClass,
			final String paramConfig, final String bundle) {
		super();
		this.operatorClass = operatorClass;
		this.name = name;
		this.generic = generic;
		this.traceFormats = traceFormats;
		this.spaceCompatibility = spaceCompatibility;
		this.paramWinClass = paramWinClass;
		this.paramConfig = paramConfig;
		this.bundle = bundle;
	}

	public TimeAggregationOperatorResource(final String operatorClass,
			final String name, final boolean generic,
			final String traceFormats, final String spaceCompatibility,
			final String paramWinClass, final String paramConfig,
			final String bundle) {
		super();
		this.operatorClass = operatorClass;
		this.name = name;
		this.generic = generic;
		setTraceFormats(traceFormats);
		setSpaceCompatibility(spaceCompatibility);
		this.paramWinClass = paramWinClass;
		this.paramConfig = paramConfig;
		this.bundle = bundle;
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

	public String getParamConfig() {
		return paramConfig;
	}

	public String getParamWinClass() {
		return paramWinClass;
	}

	public List<String> getSpaceCompatibility() {
		return spaceCompatibility;
	}

	public List<String> getTraceFormats() {
		return traceFormats;
	}

	public boolean isGeneric() {
		return generic;
	}

	public void setBundle(final String bundle) {
		this.bundle = bundle;
	}

	public void setGeneric(final boolean generic) {
		this.generic = generic;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOperatorClass(final String operatorClass) {
		this.operatorClass = operatorClass;
	}

	public void setParamConfig(final String paramConfig) {
		this.paramConfig = paramConfig;
	}

	public void setParamWinClass(final String paramWinClass) {
		this.paramWinClass = paramWinClass;
	}

	public void setSpaceCompatibility(final List<String> spaceCompatibility) {
		this.spaceCompatibility = spaceCompatibility;
	}

	public void setSpaceCompatibility(final String spaceCompatibility) {
		final String[] tmp = spaceCompatibility.split(", ");
		for (final String s : tmp)
			this.spaceCompatibility.add(s);
	}

	public void setTraceFormats(final List<String> traceFormats) {
		this.traceFormats = traceFormats;
	}

	public void setTraceFormats(final String traceFormats) {
		final String[] tmp = traceFormats.split(", ");
		for (final String s : tmp)
			this.traceFormats.add(s);

	}

}
