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

package fr.inria.soctrace.tools.ocelotl.core.timeslice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class TimeSliceStateManager {

	protected final List<TimeSlice> timeSlices = new ArrayList<TimeSlice>();
	protected final TimeRegion timeRegion;
	protected long slicesNumber;

	protected long sliceDuration;
	private static final Logger logger = LoggerFactory.getLogger(TimeSliceStateManager.class);

	public TimeSliceStateManager(final TimeRegion timeRegion, final long slicesNumber) {// TODO
		// use
		// region
		super();
		this.timeRegion = timeRegion;
		this.slicesNumber = slicesNumber;
		sliceDuration = timeRegion.getTimeDuration() / slicesNumber;
		if (timeRegion.getTimeDuration() % slicesNumber != 0)
			sliceDuration++;
		timeSlicesInit();
	}

	public long getSliceDuration() {
		return sliceDuration;
	}

	public long getSlicesNumber() {
		return slicesNumber;
	}

	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	public long getTimeSlice(final long timeStamp) {
		// final Map<Long, Long> timeSlicesDistribution = new HashMap<Long,
		// Long>();
		long slice = Math.max(0, (timeStamp - timeRegion.getTimeStampStart())
				/ sliceDuration - 1);
		for (long i = slice; i < timeSlices.size(); i++) {
			final TimeSlice it = timeSlices.get((int) i);
			if (it.startIsInsideMe(timeStamp)) {
				slice = it.getNumber();
				break;
			}
		}
		return slice;
	}
	
	public TimeSlice getATimeSlice(final long timeStamp) {
		TimeSlice slice = null;

		long presumeTimeSlice = Math.max(0,
				(timeStamp - timeRegion.getTimeStampStart()) / sliceDuration
						- 1);
		for (long i = presumeTimeSlice; i < timeSlices.size(); i++) {
			final TimeSlice it = timeSlices.get((int) i);
			if (it.startIsInsideMe(timeStamp)) {
				slice = it;
				break;
			}
		}
		return slice;
	}

	public List<TimeSlice> getTimeSlices() {
		return timeSlices;
	}

	public Map<Long, Double> getStateDistribution(
			final TimeRegion testedTimeRegion) {
		final Map<Long, Double> timeSlicesDistribution = new HashMap<Long, Double>();
		long startSlice = Math.max(
				0,
				(testedTimeRegion.getTimeStampStart() - timeRegion
						.getTimeStampStart()) / sliceDuration - 1);
		double temp = 0;
		if (testedTimeRegion.getTimeStampStart()
				- timeRegion.getTimeStampStart() >= 0)
			for (long i = startSlice; i < timeSlices.size(); i++) {
				final TimeSlice it = timeSlices.get((int) i);
				if (it.startIsInsideMe(testedTimeRegion.getTimeStampStart())) {
					startSlice = it.getNumber();
					break;
				}
			}
		for (long i = startSlice; i < slicesNumber; i++) {
			temp = timeSlices.get((int) i).regionInsideMe(testedTimeRegion);
			if (temp == 0)
				break;
			else
				timeSlicesDistribution.put(i, (double) temp);
		}
		return timeSlicesDistribution;
	}

	public void printInfos() {
		logger.info("TimeSliceManager: " + slicesNumber + " slices, "
				+ sliceDuration + " ns duration");
	}

	public void setValues(final List<Integer> values) {
		for (int i = 0; i < values.size(); i++)
			timeSlices.get(i).setValue(values.get(i));
	}

	public void timeSlicesInit() {
		int i = 0;
		long currentTime = timeRegion.getTimeStampStart();
		while (currentTime < timeRegion.getTimeStampEnd()) {
			timeSlices.add(new TimeSlice(new TimeRegion(currentTime,
					currentTime + sliceDuration), i));
			currentTime += sliceDuration;
			i++;
		}
		slicesNumber = i;
	}

}