/* ===========================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * under an aggregated representation form.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlCore;
import fr.inria.soctrace.tools.ocelotl.core.OcelotlParameters;
import fr.inria.soctrace.tools.ocelotl.core.TraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.iaggregop.IAggregationOperator;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.ui.Activator;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;

import org.eclipse.wb.swt.ResourceManager;

/**
 * Main view for LPAggreg Paje Tool
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlView extends ViewPart {

	private class AddAllEventProducersAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			producers.clear();
			producers.addAll(confDataLoader.getProducers());
			listViewerEventProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class AddEventProducersAdapter extends SelectionAdapter {

		// all - input
		java.util.List<Object> diff(final java.util.List<EventProducer> all, final java.util.List<EventProducer> input) {
			final java.util.List<Object> tmp = new LinkedList<>();
			for (final Object oba : all)
				tmp.add(oba);
			tmp.removeAll(input);
			return tmp;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getSite().getShell(), new EventProducerLabelProvider());
			dialog.setTitle("Select Event Producers");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(diff(confDataLoader.getProducers(), producers).toArray());
			dialog.setMultipleSelection(true);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				producers.add((EventProducer) o);
			listViewerEventProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class AddResultsEventProducersAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getSite().getShell(), new AnalysisResultLabelProvider());
			dialog.setTitle("Select a Result");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(confDataLoader.getResults().toArray());
			dialog.setMultipleSelection(false);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				try {
					for (final EventProducer ep : confDataLoader.getProducersFromResult((AnalysisResult) o))
						if (!producers.contains(ep))
							producers.add(ep);
				} catch (final SoCTraceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			listViewerEventProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class AnalysisResultLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((AnalysisResult) element).getDescription();
		}
	}

	private class ConfModificationListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			hasChanged = HasChanged.ALL;
			if (confDataLoader.getCurrentTrace() == null)
				return;
			try {
				if (Long.parseLong(textTimestampEnd.getText()) > confDataLoader.getMaxTimestamp() || Long.parseLong(textTimestampEnd.getText()) < confDataLoader.getMinTimestamp())
					textTimestampEnd.setText(String.valueOf(confDataLoader.getMaxTimestamp()));
			} catch (final NumberFormatException err) {
				textTimestampEnd.setText("0");
			}
			try {
				if (Long.parseLong(textTimestampStart.getText()) < confDataLoader.getMinTimestamp() || Long.parseLong(textTimestampStart.getText()) > confDataLoader.getMaxTimestamp())
					textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
			} catch (final NumberFormatException err) {
				textTimestampStart.setText("0");
			}
		}

	}

	private class ResetListener extends SelectionAdapter {
		public void widgetSelected(final SelectionEvent e) {
			textTimestampStart.setText(Long.toString(confDataLoader.getMinTimestamp()));
			textTimestampEnd.setText(Long.toString(confDataLoader.getMaxTimestamp()));
			matrixView.resizeDiagram();
			timeAxisView.resizeDiagram();
		}
	}

	private class EventProducerLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventProducer) element).getName();
		}
	}

	private class GetAggregationAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.PARAMETER;
			else
				textRun.setText("1.0");
			setConfiguration();
			final String title = "Computing Aggregated View";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {
						if (hasChanged != HasChanged.PARAMETER) {
							ocelotlCore.computeDichotomy(hasChanged);
							// textRun.setText(String.valueOf(ocelotlCore.getLpaggregManager().getParameters().get(ocelotlCore.getLpaggregManager().getParameters().size()
							// - 1)));
							// setConfiguration();
						}
						hasChanged = HasChanged.PARAMETER;
						ocelotlCore.computeParts(hasChanged);
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								// MessageDialog.openInformation(getSite().getShell(),
								// "Parts", "Parts processing finished");
								hasChanged = HasChanged.NOTHING;
								matrixView.deleteDiagram();
								matrixView.createDiagram(ocelotlCore.getLpaggregManager().getParts(), ocelotlParameters.getTimeRegion(), btnMergeAggregatedParts.getSelection(), btnShowNumbers.getSelection());
								timeAxisView.createDiagram(ocelotlParameters.getTimeRegion());
								qualityView.createDiagram();
							}
						});
					} catch (final Exception e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();

		}
	}

	private class ParameterDownAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			float p = Float.parseFloat(textRun.getText());
			for (float f : ocelotlCore.getLpaggregManager().getParameters())
				if (f > p) {
					textRun.setText(Float.toString(f));
					break;
				}
			btnRun.notifyListeners(SWT.Selection, new Event());

		}

	}

	private class ParameterUpAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			float p = Float.parseFloat(textRun.getText());
			for (int f = ocelotlCore.getLpaggregManager().getParameters().size() - 1; f >= 0; f--)
				if (ocelotlCore.getLpaggregManager().getParameters().get(f) < p) {
					textRun.setText(Float.toString(ocelotlCore.getLpaggregManager().getParameters().get(f)));
					break;
				}
			btnRun.notifyListeners(SWT.Selection, new Event());
		}

	}

	private class NormalizeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (hasChanged != HasChanged.ALL)
				hasChanged = HasChanged.NORMALIZE;
			btnRun.notifyListeners(SWT.Selection, new Event());
		}
	}

	private class GrowingQualityRadioSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (btnGrowingQualities.getSelection()) {
				btnDecreasingQualities.setSelection(false);
				ocelotlParameters.setGrowingQualities(true);
				qualityView.createDiagram();
			}
		}
	}

	private class DecreasingQualityRadioSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (btnDecreasingQualities.getSelection()) {
				btnGrowingQualities.setSelection(false);
				ocelotlParameters.setGrowingQualities(false);
				qualityView.createDiagram();
			}
		}
	}

	private class ParameterModifyListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			try {
				if (Float.parseFloat(textRun.getText()) < 0 || Float.parseFloat(textRun.getText()) > 1)
					textRun.setText("0");
			} catch (final NumberFormatException err) {
				textRun.setText("0");
			}
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ)
				hasChanged = HasChanged.PARAMETER;
		}

	}

	private class ResetSelectionAdapter extends SelectionAdapter {

		private final ListViewer	viewer;

		public ResetSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.clear();
			viewer.refresh(false);
			hasChanged = HasChanged.ALL;
		}
	}

	private class ThresholdModifyListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			if (confDataLoader.getCurrentTrace() == null)
				return;
			try {
				if (Float.parseFloat(textThreshold.getText()) < Float.MIN_VALUE || Float.parseFloat(textThreshold.getText()) > 1)
					textThreshold.setText("0.001");
			} catch (final NumberFormatException err) {
				textThreshold.setText("0.001");
			}
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.THRESHOLD;
		}
	}

	public static final String					ID				= "fr.inria.soctrace.tools.ocelotl.ui.OcelotlView"; //$NON-NLS-1$
	public static final String					PLUGIN_ID		= Activator.PLUGIN_ID;
	private Button								btnMergeAggregatedParts;
	private Button								btnCache;
	private Button								btnGetParameters;
	private Button								btnNormalize;
	private Button								btnRun;
	private Button								btnShowNumbers;
	private Button								btnGrowingQualities;
	private Button								btnDecreasingQualities;
	private Combo								comboAggregationOperator;
	private Combo								comboTraces;
	private final ConfDataLoader				confDataLoader	= new ConfDataLoader();
	private HasChanged							hasChanged		= HasChanged.ALL;

	private ListViewer							listViewerEventProducers;

	private MatrixView							matrixView;
	private final OcelotlCore					ocelotlCore;
	private final OcelotlParameters				ocelotlParameters;
	private Text								textRun;
	private final java.util.List<EventProducer>	producers		= new LinkedList<EventProducer>();
	private QualityView							qualityView;
	private Spinner								spinnerDivideDbQuery;
	private Spinner								spinnerThread;
	private Spinner								spinnerPageSize;
	private Spinner								spinnerEPPageSize;
	private Spinner								spinnerTSNumber;
	private Text								textThreshold;
	private TimeAxisView						timeAxisView;
	private Text								textTimestampEnd;
	private Text								textTimestampStart;
	private TraceTypeConfig						currentTraceTypeConfig;

	final Map<Integer, Trace>					traceMap		= new HashMap<Integer, Trace>();

	private Button								buttonDown;
	private Button								buttonUp;

	/** @throws SoCTraceException */
	public OcelotlView() throws SoCTraceException {
		try {
			confDataLoader.loadTraces();
		} catch (final SoCTraceException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
		ocelotlParameters = new OcelotlParameters();
		ocelotlCore = new OcelotlCore(ocelotlParameters);
	}

	public void setTimeRegion(TimeRegion time) {
		textTimestampStart.setText(String.valueOf(time.getTimeStampStart()));
		textTimestampEnd.setText(String.valueOf(time.getTimeStampEnd()));
	}

	public TimeRegion getTimeRegion() {
		return new TimeRegion(Long.parseLong(textTimestampStart.getText()), Long.parseLong(textTimestampEnd.getText()));
	}

	public TimeAxisView getTimeAxisView() {
		return timeAxisView;
	}

	private void cleanAll() {
		hasChanged = HasChanged.ALL;
		textThreshold.setText("0.001");
		textTimestampStart.setText("0");
		textTimestampEnd.setText("0");
		btnNormalize.setSelection(false);
		btnGrowingQualities.setSelection(true);
		btnDecreasingQualities.setSelection(false);
		btnCache.setSelection(false);
		spinnerTSNumber.setSelection(200);
		spinnerDivideDbQuery.setSelection(0);
		spinnerPageSize.setSelection(50);
		spinnerEPPageSize.setSelection(100);
		spinnerThread.setSelection(5);
		textRun.setText("1.0");
		btnMergeAggregatedParts.setSelection(true);
		producers.clear();
		// types.clear();
		// idles.clear();
		listViewerEventProducers.setInput(producers);
		// TODO config paje
	}

	public ConfDataLoader getConfDataLoader() {
		return confDataLoader;
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		matrixView = new MatrixView(this);
		timeAxisView = new TimeAxisView(this);
		qualityView = new QualityView(this);
		final SashForm sashFormView = new SashForm(sashFormGlobal, SWT.VERTICAL);
		sashFormView.setSashWidth(0);
		final Composite compositeMatrixView = new Composite(sashFormView, SWT.NONE);
		compositeMatrixView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeMatrixView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		final GridLayout gl_compositeMatrixView = new GridLayout();
		gl_compositeMatrixView.horizontalSpacing = 0;
		gl_compositeMatrixView.marginHeight = 0;
		compositeMatrixView.setLayout(gl_compositeMatrixView);
		compositeMatrixView.setSize(500, 500);
		final Canvas canvasMatrixView = matrixView.initDiagram(compositeMatrixView);
		canvasMatrixView.setLayoutData(new GridData(GridData.FILL_BOTH));
		final Composite compositeTimeAxisView = new Composite(sashFormView, SWT.NONE);
		compositeTimeAxisView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		final GridLayout gl_compositeTimeAxisView = new GridLayout();
		gl_compositeTimeAxisView.horizontalSpacing = 0;
		gl_compositeTimeAxisView.marginHeight = 0;
		compositeTimeAxisView.setLayout(gl_compositeTimeAxisView);
		final Canvas canvasTimeAxisView = timeAxisView.initDiagram(compositeTimeAxisView);
		sashFormView.setWeights(new int[] { 220, 57 });

		Group groupTime = new Group(sashFormGlobal, SWT.NONE);
		groupTime.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTime.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTime.setLayout(new GridLayout(7, false));

		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblStartTimestamp.setText("Start");

		textTimestampStart = new Text(groupTime, SWT.BORDER);
		textTimestampStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		textTimestampStart.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblEndTimestamp.setText("End");

		textTimestampEnd = new Text(groupTime, SWT.BORDER);
		textTimestampEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		textTimestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		Button btnReset = new Button(groupTime, SWT.NONE);
		btnReset.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		btnReset.setFont(SWTResourceManager.getFont("Cantarell", 7, SWT.NORMAL));
		btnReset.setText("Reset");

		final Label lblTSNumber = new Label(groupTime, SWT.NONE);
		lblTSNumber.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblTSNumber.setText("Timeslice Number");

		spinnerTSNumber = new Spinner(groupTime, SWT.BORDER);
		spinnerTSNumber.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		spinnerTSNumber.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerTSNumber.setMaximum(10000);
		spinnerTSNumber.setMinimum(1);
		spinnerTSNumber.setSelection(200);
		spinnerTSNumber.addModifyListener(new ConfModificationListener());
		btnReset.addSelectionListener(new ResetListener());
		textTimestampEnd.addModifyListener(new ConfModificationListener());
		textTimestampStart.addModifyListener(new ConfModificationListener());

		final TabFolder tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final TabItem tbtmTimeAggregationParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setText("Time Aggregation");

		final SashForm sashFormTimeAggregationParameters = new SashForm(tabFolder, SWT.NONE);
		tbtmTimeAggregationParameters.setControl(sashFormTimeAggregationParameters);

		SashForm sashFormTSandCurve = new SashForm(sashFormTimeAggregationParameters, SWT.VERTICAL);
		sashFormTSandCurve.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final Group groupTSParameters = new Group(sashFormTSandCurve, SWT.NONE);
		groupTSParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTSParameters.setLayout(new GridLayout(1, false));
		comboTraces = new Combo(groupTSParameters, SWT.READ_ONLY);
		GridData gd_comboTraces = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_comboTraces.widthHint = 180;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		comboTraces.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				cleanAll();
				try {
					confDataLoader.load(traceMap.get(comboTraces.getSelectionIndex()));
					textTimestampStart.setText(String.valueOf(confDataLoader.getMinTimestamp()));
					textTimestampEnd.setText(String.valueOf(confDataLoader.getMaxTimestamp()));
					comboAggregationOperator.removeAll();
					for (final IAggregationOperator op : ocelotlCore.getOperators().getList()) {
						if (op.traceType().equals(confDataLoader.getCurrentTrace().getType().getName()))
							comboAggregationOperator.add(op.descriptor());
					}
					comboAggregationOperator.setText("");
				} catch (final SoCTraceException e1) {
					MessageDialog.openError(getSite().getShell(), "Exception", e1.getMessage());
				}
			}
		});

		final Group groupAggregationOperator = new Group(groupTSParameters, SWT.NONE);
		groupAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupAggregationOperator.setText("Aggregation Operator");
		groupAggregationOperator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		groupAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite compositeAggregationOperator = new Composite(groupAggregationOperator, SWT.NONE);
		compositeAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		compositeAggregationOperator.setLayout(new GridLayout(2, false));
		final GridData gd_compositeAggregationOperator = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_compositeAggregationOperator.widthHint = 85;
		compositeAggregationOperator.setLayoutData(gd_compositeAggregationOperator);

		comboAggregationOperator = new Combo(compositeAggregationOperator, SWT.READ_ONLY);
		GridData gd_comboAggregationOperator = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_comboAggregationOperator.widthHint = 170;
		comboAggregationOperator.setLayoutData(gd_comboAggregationOperator);
		comboAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		comboAggregationOperator.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (confDataLoader.getCurrentTrace() == null)
					return;
				hasChanged = HasChanged.ALL;
			}
		});
		comboAggregationOperator.setText("");

		Button btnSettings = new Button(compositeAggregationOperator, SWT.NONE);
		btnSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnSettings.setText("Settings");
		btnSettings.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				hasChanged = HasChanged.ALL;
				// TODO à faire demain!
			}
		});
		;

		final Group groupEventProducers = new Group(groupTSParameters, SWT.NONE);
		groupEventProducers.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		groupEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupEventProducers.setText("Event Producers");
		final GridLayout gl_groupEventProducers = new GridLayout(2, false);//
		gl_groupEventProducers.horizontalSpacing = 0;
		groupEventProducers.setLayout(gl_groupEventProducers);

		listViewerEventProducers = new ListViewer(groupEventProducers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventProducers.setContentProvider(new ArrayContentProvider());
		listViewerEventProducers.setLabelProvider(new EventProducerLabelProvider());
		listViewerEventProducers.setComparator(new ViewerComparator());
		final List listEventProducers = listViewerEventProducers.getList();
		listEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final GridData gd_listEventProducers = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_listEventProducers.heightHint = 79;
		gd_listEventProducers.widthHint = 120;
		listEventProducers.setLayoutData(gd_listEventProducers);

		final ScrolledComposite scrCompositeEventProducerButtons = new ScrolledComposite(groupEventProducers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventProducerButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrCompositeEventProducerButtons.setExpandHorizontal(true);
		scrCompositeEventProducerButtons.setExpandVertical(true);

		final Composite compositeEventProducerButtons = new Composite(scrCompositeEventProducerButtons, SWT.NONE);
		compositeEventProducerButtons.setLayout(new GridLayout(1, false));
		final Button btnAddEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnAddEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddEventProducer.setText("Add");
		btnAddEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddEventProducer.setImage(null);

		final Button btnAddAllEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnAddAllEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddAllEventProducer.setText("Add All");
		btnAddAllEventProducer.setImage(null);
		btnAddAllEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddAllEventProducer.addSelectionListener(new AddAllEventProducersAdapter());

		final Button btnAddResult = new Button(compositeEventProducerButtons, SWT.NONE);
		btnAddResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddResult.setText("Add Result");
		btnAddResult.setImage(null);
		btnAddResult.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddResult.addSelectionListener(new AddResultsEventProducersAdapter());
		final Button btnRemoveEventProducer = new Button(compositeEventProducerButtons, SWT.NONE);
		btnRemoveEventProducer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnRemoveEventProducer.setText("Reset");
		btnRemoveEventProducer.addSelectionListener(new ResetSelectionAdapter(listViewerEventProducers));
		btnRemoveEventProducer.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveEventProducer.setImage(null);
		scrCompositeEventProducerButtons.setContent(compositeEventProducerButtons);
		scrCompositeEventProducerButtons.setMinSize(compositeEventProducerButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnAddEventProducer.addSelectionListener(new AddEventProducersAdapter());

		Group groupQualityCurveSettings = new Group(sashFormTSandCurve, SWT.NONE);
		groupQualityCurveSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupQualityCurveSettings.setText("Quality Curve Settings");
		groupQualityCurveSettings.setLayout(new GridLayout(3, false));

		btnNormalize = new Button(groupQualityCurveSettings, SWT.CHECK);
		btnNormalize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnNormalize.setSelection(false);
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());

		btnGrowingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnGrowingQualities.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnGrowingQualities.setText("Complexity (green), Information (red)");
		btnGrowingQualities.setSelection(true);
		btnGrowingQualities.addSelectionListener(new GrowingQualityRadioSelectionAdapter());
		btnGrowingQualities.setSelection(false);

		btnDecreasingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnDecreasingQualities.setText("Complexity reduction (green), Information loss (red)");
		btnDecreasingQualities.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		sashFormTSandCurve.setWeights(new int[] { 265, 46 });
		btnDecreasingQualities.addSelectionListener(new DecreasingQualityRadioSelectionAdapter());

		SashForm sashForm = new SashForm(sashFormTimeAggregationParameters, SWT.VERTICAL);

		final Composite compositeQualityView = new Composite(sashForm, SWT.NONE);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		final GridLayout gl_compositeQualityView = new GridLayout();
		compositeQualityView.setLayout(gl_compositeQualityView);
		final Canvas canvasQualityView = qualityView.initDiagram(compositeQualityView);

		final Group groupLPAParameters = new Group(sashForm, SWT.NONE);
		groupLPAParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupLPAParameters.setLayout(new GridLayout(1, false));

		final Composite compositeGetParameters = new Composite(groupLPAParameters, SWT.NONE);
		compositeGetParameters.setLayout(new GridLayout(8, false));
		compositeGetParameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		final Label lblThreshold = new Label(compositeGetParameters, SWT.NONE);
		lblThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblThreshold.setText("Threshold");

		textThreshold = new Text(compositeGetParameters, SWT.BORDER);
		final GridData gd_textThreshold = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_textThreshold.widthHint = 82;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		Label lblParameter = new Label(compositeGetParameters, SWT.NONE);
		lblParameter.setText("Parameter");
		lblParameter.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblParameter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		// btnGetParameters = new Button(compositeGetParameters, SWT.NONE);
		// btnGetParameters.setFont(SWTResourceManager.getFont("Cantarell", 8,
		// SWT.NORMAL));
		// btnGetParameters.setText("Get");

		textRun = new Text(compositeGetParameters, SWT.BORDER);
		textRun.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		buttonDown = new Button(compositeGetParameters, SWT.NONE);
		buttonDown.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonDown.setText("<");
		buttonDown.addSelectionListener(new ParameterDownAdapter());

		buttonUp = new Button(compositeGetParameters, SWT.NONE);
		buttonUp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonUp.setText(">");
		buttonUp.addSelectionListener(new ParameterUpAdapter());
		btnRun = new Button(compositeGetParameters, SWT.NONE);
		btnRun.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/1366759976_white_tiger.png"));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRun.setText("RUN!");
		new Label(compositeGetParameters, SWT.NONE);
		sashForm.setWeights(new int[] { 249, 46 });
		sashFormTimeAggregationParameters.setWeights(new int[] { 241, 344 });
		btnRun.addSelectionListener(new GetAggregationAdapter());
		textRun.addModifyListener(new ParameterModifyListener());

		// btnGetParameters.addSelectionListener(new GetParametersAdapter());
		textThreshold.addModifyListener(new ThresholdModifyListener());
		canvasQualityView.setLayoutData(new GridData(GridData.FILL_BOTH));

		final TabItem tbtmTraceParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTraceParameters.setText("Trace Parameters");

		final SashForm sashFormTraceParameter = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmTraceParameters.setControl(sashFormTraceParameter);

		int index = 0;
		for (final Trace t : confDataLoader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}
		;

		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Advanced Parameters");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);

		final Group grpCacheManagement = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpCacheManagement.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpCacheManagement.setText("Cache Management");
		grpCacheManagement.setLayout(new GridLayout(5, false));

		btnCache = new Button(grpCacheManagement, SWT.CHECK);
		btnCache.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		// btnCache.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
		// false, 1, 1));
		btnCache.setText("Activate Cache");
		btnCache.setSelection(false);

		final Label lblPageSize = new Label(grpCacheManagement, SWT.NONE);
		lblPageSize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblPageSize.setText("Cache Page Size");

		spinnerPageSize = new Spinner(grpCacheManagement, SWT.BORDER);
		spinnerPageSize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerPageSize.addModifyListener(new ConfModificationListener());
		spinnerPageSize.setMinimum(1);
		spinnerPageSize.setMaximum(1000000);
		spinnerPageSize.setSelection(50);

		final Label lblEPPageSize = new Label(grpCacheManagement, SWT.NONE);
		lblEPPageSize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblEPPageSize.setText("EP Cache Page Size");

		spinnerEPPageSize = new Spinner(grpCacheManagement, SWT.BORDER);
		spinnerEPPageSize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerEPPageSize.addModifyListener(new ConfModificationListener());
		spinnerEPPageSize.setMinimum(1);
		spinnerEPPageSize.setMaximum(1000000);
		spinnerEPPageSize.setSelection(100);

		final Group grpDivideDbQuery = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpDivideDbQuery.setText("Query Management");
		grpDivideDbQuery.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpDivideDbQuery, SWT.NONE);
		lblDivideDbQueries.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblDivideDbQueries.setText("Event Producers per query (0=All)");

		spinnerDivideDbQuery = new Spinner(grpDivideDbQuery, SWT.BORDER);
		spinnerDivideDbQuery.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerDivideDbQuery.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		spinnerDivideDbQuery.addModifyListener(new ConfModificationListener());
		spinnerDivideDbQuery.setMinimum(0);
		spinnerDivideDbQuery.setMaximum(1000000);
		spinnerDivideDbQuery.setSelection(0);

		final Group grpMultiThread = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpMultiThread.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpMultiThread.setText("Multi Threading");
		grpMultiThread.setLayout(new GridLayout(2, false));

		final Label lblThread = new Label(grpMultiThread, SWT.NONE);
		lblThread.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblThread.setText("Working Threads");

		spinnerThread = new Spinner(grpMultiThread, SWT.BORDER);
		spinnerThread.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerThread.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		spinnerThread.addModifyListener(new ConfModificationListener());
		spinnerThread.setMinimum(1);
		spinnerThread.setMaximum(1000000);
		spinnerThread.setSelection(5);

		final Group grpVisualizationSettings = new Group(sashFormAdvancedParameters, SWT.NONE);
		grpVisualizationSettings.setText("Visualization settings");
		grpVisualizationSettings.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpVisualizationSettings.setLayout(new GridLayout(1, false));

		btnMergeAggregatedParts = new Button(grpVisualizationSettings, SWT.CHECK);
		btnMergeAggregatedParts.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnMergeAggregatedParts.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnMergeAggregatedParts.setText("Merge Aggregated Parts");
		btnMergeAggregatedParts.setSelection(true);

		btnShowNumbers = new Button(grpVisualizationSettings, SWT.CHECK);
		btnShowNumbers.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
			}
		});
		btnShowNumbers.setText("Show Part Numbers");
		btnShowNumbers.setSelection(false);
		btnShowNumbers.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		sashFormGlobal.setWeights(new int[] { 254, 41, 368 });
		// sashFormAdvancedParameters.setWeights(new int[] { 112, 374 });
		// sashFormGlobal.setWeights(new int[] { 172, 286 });
		canvasTimeAxisView.setLayoutData(new GridData(GridData.FILL_BOTH));

		// clean all
		cleanAll();

	}

	public Button getBtnRun() {
		return btnRun;
	}

	public OcelotlCore getCore() {
		return ocelotlCore;
	}

	public Text getParam() {
		return textRun;
	}

	public OcelotlParameters getParams() {
		return ocelotlParameters;
	}

	public void setConfiguration() {

		ocelotlParameters.setTrace(confDataLoader.getCurrentTrace());
		ocelotlParameters.setEventProducers(producers);
		// ocelotlParameters.setEventTypes(types);
		// ocelotlParameters.setSleepingStates(idles);
		ocelotlParameters.setNormalize(btnNormalize.getSelection());
		ocelotlParameters.setTimeSlicesNumber(spinnerTSNumber.getSelection());
		ocelotlParameters.setMaxEventProducers(spinnerDivideDbQuery.getSelection());
		ocelotlParameters.setAggOperator(comboAggregationOperator.getText());
		ocelotlParameters.setCache(btnCache.getSelection());
		ocelotlParameters.setEpCache(spinnerEPPageSize.getSelection());
		ocelotlParameters.setPageCache(spinnerPageSize.getSelection());
		ocelotlParameters.setThread(spinnerThread.getSelection());
		// TODO manage number format exception
		try {
			ocelotlParameters.setThreshold(Double.valueOf(textThreshold.getText()).floatValue());
			ocelotlParameters.setParameter(Double.valueOf(textRun.getText()).floatValue());
			ocelotlParameters.setTimeRegion(new TimeRegion(Long.valueOf(textTimestampStart.getText()), Long.valueOf(textTimestampEnd.getText())));
		} catch (final NumberFormatException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}