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

package fr.inria.soctrace.tools.ocelotl.ui.color;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public class OcelotlColor {

	private static int	light		= 180;
	private static int	tooLight	= 180;

	public static int getLight() {
		return light;
	}

	public static int getTooLight() {
		return tooLight;
	}

	public static void setLight(final int light) {
		OcelotlColor.light = light;
	}

	public static void setTooLight(final int tooLight) {
		OcelotlColor.tooLight = tooLight;
	}

	private Color	bg;

	private Color	fg;

	public OcelotlColor(final Color bg) {
		super();
		this.bg = bg;
		setFg();
	}

	public OcelotlColor(final int r, final int g, final int b) {
		super();
		final Device device = Display.getCurrent();
		bg = new Color(device, r % 255, g % 255, b % 255);
		setFg();
	}

	public Color getBg() {
		return bg;
	}

	public Color getFg() {
		return fg;
	}

	public boolean isTooLight() {
		if (bg.getBlue() > tooLight && bg.getGreen() > tooLight && bg.getRed() > tooLight)
			return true;
		else
			return false;
	}

	public void setBg(final Color bg) {
		this.bg = bg;
	}

	public void setFg() {
		if (bg.getBlue() > light && bg.getGreen() > light || bg.getBlue() > light && bg.getRed() > light || bg.getGreen() > light && bg.getRed() > light)
			fg = ColorConstants.black;
		else
			fg = ColorConstants.white;
	}

	public void setFg(final Color fg) {
		this.fg = fg;
	}

}
