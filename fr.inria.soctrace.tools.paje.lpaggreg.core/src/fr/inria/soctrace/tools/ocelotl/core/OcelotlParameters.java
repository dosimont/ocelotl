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

package fr.inria.soctrace.tools.ocelotl.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class OcelotlParameters {

	private List<EventProducer>	eventProducers		= new ArrayList<EventProducer>();
	private List<EventType>		eventTypes			= new ArrayList<EventType>();		;
	private List<String>		sleepingStates		= new ArrayList<String>();			;
	private int					timeSlicesNumber	= 1;
	private TimeRegion			timeRegion;
	private float				parameter			= 0;
	private boolean				normalize			= false;
	private float				threshold			= (float) 0.001;
	private Trace				trace				= null;
	private int					maxEventProducers	= 0;
	private String				aggOperator;

	public OcelotlParameters() {
		super();
	}

	public String getAggOperator() {
		return aggOperator;
	}

	public List<EventProducer> getEventProducers() {
		return eventProducers;
	}

	public List<EventType> getEventTypes() {
		return eventTypes;
	}

	public int getMaxEventProducers() {
		return maxEventProducers;
	}

	public float getParameter() {
		return parameter;
	}

	public List<String> getSleepingStates() {
		return sleepingStates;
	}

	public float getThreshold() {
		return threshold;
	}

	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	public int getTimeSlicesNumber() {
		return timeSlicesNumber;
	}

	public Trace getTrace() {
		return trace;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setAggOperator(final String aggOperator) {
		this.aggOperator = aggOperator;
	}

	public void setEventProducers(final List<EventProducer> eventProducers) {
		this.eventProducers = eventProducers;
	}

	public void setEventTypes(final List<EventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public void setMaxEventProducers(final int maxEventProducers) {
		this.maxEventProducers = maxEventProducers;
	}

	public void setNormalize(final boolean normalize) {
		this.normalize = normalize;
	}

	public void setParameter(final float parameter) {
		this.parameter = parameter;
	}

	public void setSleepingStates(final List<String> sleepingStates) {
		this.sleepingStates = sleepingStates;
	}

	public void setThreshold(final float threshold) {
		this.threshold = threshold;
	}

	public void setTimeRegion(final TimeRegion timeRegion) {
		this.timeRegion = timeRegion;
	}

	public void setTimeSlicesNumber(final int timeSlicesNumber) {
		this.timeSlicesNumber = timeSlicesNumber;
	}

	public void setTrace(final Trace trace) {
		this.trace = trace;
	}

	@Override
	public String toString() { // TODO update this
		final StringBuilder builder = new StringBuilder();
		builder.append("OcelotlParameters \n[\neventProducers=").append(eventProducers).append("\neventTypes=").append(eventTypes).append("\nsleepingStates=").append(sleepingStates).append("\ntimeSlicesNumber=").append(timeSlicesNumber)
				.append("\ntimeRegion=").append(timeRegion).append("\nparameter=").append(parameter).append("\nnormalize=").append(normalize).append("\nthreshold=").append(threshold).append("\ntrace=").append(trace).append("\n]");
		return builder.toString();
	}

}