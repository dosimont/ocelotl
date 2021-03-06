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

package fr.inria.soctrace.tools.ocelotl.ui.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.TimestampFormat;
import fr.inria.soctrace.lib.model.utils.TimestampFormat.TickDescriptor;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;

/**
 * Time Axis View : part representation, according to LP algorithm result
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 */
public class TimeAxisView {

	private class SelectFigure extends RectangleFigure {

		public SelectFigure() {
			super();
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			setLayoutManager(layout);
			setAlpha(50);
		}

		public void draw(final TimeRegion timeRegion, final boolean active) {
			if (active) {
				setForegroundColor(AggregatedView.activeColorFG);
				setBackgroundColor(AggregatedView.activeColorBG);
				setAlpha(AggregatedView.activeColorAlpha);
			} else {
				setForegroundColor(AggregatedView.potentialColorFG);
				setBackgroundColor(AggregatedView.potentialColorBG);
				setAlpha(AggregatedView.potentialColorAlpha);
			}
			root.add(this,
					new Rectangle(new Point((int) ((timeRegion.getTimeStampStart() - time.getTimeStampStart()) * (root.getSize().width - 2 * Border) / time.getTimeDuration() + Border), root.getSize().height - 2), new Point(
							(int) ((timeRegion.getTimeStampEnd() - time.getTimeStampStart()) * (root.getSize().width - 2 * Border) / time.getTimeDuration() + Border), -1)));
		}
	}

	Figure				root;
	Canvas				canvas;
	TimeRegion			time;
	TimeRegion			selectTime;
	OcelotlView			ocelotlView;
	final static int	Height				= 100;
	final static int	Border				= 10;
	final static int	TimeAxisWidth		= 1;
	final static long	Divide				= 10;
	double				GradNumber			= 10.0;
	double				GradDuration		= 10.0;
	final static long	GradWidthMin		= 50;
	double				GradWidth			= 50;
	final static int	GradHeight			= 8;
	final static int	TextWidth			= 50;
	final static int	TextHeight			= 20;
	final static long	MiniDivide			= 5;
	final static int	MiniGradHeight		= 4;
	final static int	TextPositionOffset	= 2;
	int					Space				= 6;
	SelectFigure		selectFigure;
	TimestampFormat		timeFormatter;
	
	public TimeAxisView(OcelotlView theView) {
		super();
		selectFigure = new SelectFigure();
		ocelotlView = theView;
	}

	public void createDiagram(final TimeRegion time) {
		root.removeAll();
		this.time = time;
		if (time != null) {
			drawMainLine();
			drawGrads();
		}
		root.validate();
		canvas.update();
		unselect();
	}

	public void createDiagram(final TimeRegion time, final TimeRegion timeRegion, final boolean active) {
		root.removeAll();
		this.time = time;
		if (time != null) {
			drawMainLine();
			drawGrads();
			if (timeRegion != null) {
				selectTime = timeRegion;
				selectFigure.draw(timeRegion, active);
			}
		}
		root.validate();
		canvas.update();
	}

	public void drawGrads() {
		if (ocelotlView != null && ocelotlView.getCurrentShownTrace() != null)
			timeFormatter = new TimestampFormat(TimeUnit.getTimeUnit(ocelotlView.getCurrentShownTrace().getTimeUnit()));
		else
			timeFormatter = new TimestampFormat();
		
		timeFormatter.setContext(time.getTimeStampStart(), time.getTimeStampEnd());

		// Set the number of grads and their properties
		grads();

		timeFormatter.setMaximumIntegerDigits(3);

		final int linePosition = root.getSize().height() - TextHeight / 2 - TextPositionOffset - Border;
		for (int i = 0; i < (int) GradNumber + 1; i++) {
			final RectangleFigure rectangle = new RectangleFigure();
			root.add(rectangle, new Rectangle(new Point((int) (i * GradWidth) + Border, linePosition), new Point(new Point((int) (i * GradWidth) + Border + TimeAxisWidth, linePosition - GradHeight))));
			rectangle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			rectangle.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			rectangle.setLineWidth(1);

			final long value = (long) (i * GradDuration + time.getTimeStampStart());
			final String text = timeFormatter.format(value);
			final Label label = new Label(text);
			label.setLabelAlignment(SWT.CENTER);
			label.setToolTip(new Label(" "+text+" "));
			label.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
			label.setFont(SWTResourceManager.getFont("Cantarell", TextHeight / 2, SWT.NORMAL));
			final ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
			
			if (i != (int) GradNumber)
				root.add(label, new Rectangle(new Point((int) (i * GradWidth) - Border, linePosition + TextPositionOffset), new Point(new Point((int) (i * GradWidth) + TimeAxisWidth + TextWidth, linePosition + TextPositionOffset + TextHeight))));
			else
				root.add(label, new Rectangle(new Point((int) (i * GradWidth) - Border * 3, linePosition + TextPositionOffset), new Point(new Point((int) (i * GradWidth) + TimeAxisWidth + TextWidth, linePosition + TextPositionOffset + TextHeight))));

			label.setLayoutManager(layout);
			for (int j = 1; j < 5; j++) {
				final RectangleFigure rectangle2 = new RectangleFigure();
				if ((int) (i * GradWidth) + Border + (int) (j * GradWidth / MiniDivide) > root.getSize().width() - Border)
					break;
				root.add(rectangle2, new Rectangle(new Point((int) (i * GradWidth) + Border + (int) (j * GradWidth / MiniDivide), linePosition), new Point(new Point((int) (i * GradWidth) + Border + (int) (j * GradWidth / MiniDivide), linePosition
						- MiniGradHeight))));
				rectangle2.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
				rectangle2.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
				rectangle2.setLineWidth(1);
			}
		}
	}

	public void drawMainLine() {
		final int linePosition = root.getSize().height() - TextHeight / 2 - TextPositionOffset - Border;
		final RectangleFigure rectangle = new RectangleFigure();
		root.add(rectangle, new Rectangle(new Point(Border, linePosition + TimeAxisWidth), new Point(root.getSize().width() - Border, linePosition)));
		rectangle.setBackgroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		rectangle.setForegroundColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
		rectangle.setLineWidth(1);
	}

	public void grads() {
		TickDescriptor ticks = timeFormatter.getTickDescriptor(time.getTimeStampStart(), time.getTimeStampEnd(), 10);
		GradDuration = ticks.delta;
		GradNumber = time.getTimeDuration() / GradDuration;
		GradWidth = (root.getSize().width - 2 * Border - 1) / GradNumber;

		while (GradWidth < GradWidthMin && GradNumber > 2) {
			GradNumber /= 2;
			GradWidth *= 2;
			GradDuration *= 2;
		}
	}

	public Canvas initDiagram(final Composite parent) {
		root = new Figure();
		root.setFont(parent.getFont());
		final XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.gray);
		canvas.setSize(parent.getSize());
		final LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(root);
		lws.setControl(canvas);
		root.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		root.setSize(parent.getSize().x, parent.getSize().y);
		canvas.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(final ControlEvent arg0) {
				// TODO Auto-generated method stub
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}

			@Override
			public void controlResized(final ControlEvent arg0) {
				canvas.redraw();
				root.repaint();
				resizeDiagram();
			}
		});

		return canvas;
	}
	
	public Figure getRoot() {
		return root;
	}

	public void resizeDiagram() {
		createDiagram(time, selectTime, true);
		root.repaint();
	}

	public void select(final TimeRegion timeRegion, final boolean active) {
		createDiagram(time, timeRegion, active);
		root.repaint();
	}

	public void unselect() {
		selectTime = null;
		resizeDiagram();
	}
	
	public void deleteDiagram() {
		root.removeAll();
		root.repaint();
		time = null;
	}

	public OcelotlView getOcelotlView() {
		return ocelotlView;
	}

	public void setOcelotlView(OcelotlView ocelotlView) {
		this.ocelotlView = ocelotlView;
	}

}
