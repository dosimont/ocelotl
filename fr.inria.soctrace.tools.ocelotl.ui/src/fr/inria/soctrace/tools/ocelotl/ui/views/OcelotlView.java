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
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.core.tsaggregoperators.AggregationOperators;
import fr.inria.soctrace.tools.ocelotl.ui.Activator;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;

/**
 * Main view for LPAggreg Paje Tool
 * 
 * @author "Damien Dosimont <damien.dosimont@imag.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlView extends ViewPart {

	private class AllFilterAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			producers.clear();
			producers.addAll(loader.getProducers());
			listViewerProducers.setInput(producers);
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
			if (loader.getCurrentTrace() == null)
				return;
			try {
				if (Long.parseLong(timestampEnd.getText()) > loader.getMaxTimestamp() || Long.parseLong(timestampEnd.getText()) < loader.getMinTimestamp())
					timestampEnd.setText(String.valueOf(loader.getMaxTimestamp()));
			} catch (final NumberFormatException err) {
				timestampEnd.setText("0");
			}
			try {
				if (Long.parseLong(timestampStart.getText()) < loader.getMinTimestamp() || Long.parseLong(timestampStart.getText()) > loader.getMaxTimestamp())
					timestampStart.setText(String.valueOf(loader.getMinTimestamp()));
			} catch (final NumberFormatException err) {
				timestampStart.setText("0");
			}
		}

	}

	private class DichotomySelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.PARAMETER || hasChanged == HasChanged.EQ)
				hasChanged = HasChanged.THRESHOLD;
			setConfiguration();
			final String title = "Getting parameters...";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {
						core.computeDichotomy(hasChanged);

						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								// MessageDialog.openInformation(getSite().getShell(),
								// "Parameters", "Parameters retrieved");
								hasChanged = HasChanged.NOTHING;
								list.removeAll();
								for (int i =core.getLpaggregManager().getParameters().size()-1; i>=0; i--)
									list.add(Float.toString(core.getLpaggregManager().getParameters().get(i)));
								list.select(0);
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

	private class EventProducerLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventProducer) element).getName();
		}
	}

	private class EventTypeLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventType) element).getName();
		}
	}

	private class IdlesSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final InputDialog dialog = new InputDialog(getSite().getShell(), "Type Idle State", "Select Idle state", "", null);
			if (dialog.open() == Window.CANCEL)
				return;
			idles.add(dialog.getValue());
			listViewerIdles.setInput(idles);
			hasChanged = HasChanged.ALL;
		}
	}

	private class NormalizeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (hasChanged != HasChanged.ALL)
				hasChanged = HasChanged.NORMALIZE;
		}
	}

	private class ParamModificationListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			try {
				if (Float.parseFloat(param.getText()) < 0 || Float.parseFloat(param.getText()) > 1)
					param.setText("0");
			} catch (final NumberFormatException err) {
				param.setText("0");
			}
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ)
				hasChanged = HasChanged.PARAMETER;
		}

	}

	private class ProducersFilterAdapter extends SelectionAdapter {

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
			if (loader.getCurrentTrace() == null)
				return;

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getSite().getShell(), new EventProducerLabelProvider());
			dialog.setTitle("Select Event Producers");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(diff(loader.getProducers(), producers).toArray());
			dialog.setMultipleSelection(true);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				producers.add((EventProducer) o);
			listViewerProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class RemoveSelectionAdapter extends SelectionAdapter {

		private final ListViewer	viewer;

		public RemoveSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			final Object obj = selection.getFirstElement();
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.remove(obj);
			viewer.refresh(false);
			hasChanged = HasChanged.ALL;
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

	private class ResultsFilterAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (loader.getCurrentTrace() == null)
				return;

			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getSite().getShell(), new AnalysisResultLabelProvider());
			dialog.setTitle("Select a Result");
			dialog.setMessage("Select a String (* = any string, ? = any char):");
			dialog.setElements(loader.getResults().toArray());
			dialog.setMultipleSelection(false);
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				try {
					for (final EventProducer ep : loader.getProducersFromResult((AnalysisResult) o))
						if (!producers.contains(ep))
							producers.add(ep);
				} catch (final SoCTraceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			listViewerProducers.setInput(producers);
			hasChanged = HasChanged.ALL;
		}
	}

	private class RunSelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.PARAMETER;
			else
				list.removeAll();
			setConfiguration();
			final String title = "Computing parts...";
			final Job job = new Job(title) {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask(title, IProgressMonitor.UNKNOWN);
					try {
						core.computeParts(hasChanged);
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								// MessageDialog.openInformation(getSite().getShell(),
								// "Parts", "Parts processing finished");
								hasChanged = HasChanged.NOTHING;
								matrix.deleteDiagram();
								matrix.createDiagram(core.getLpaggregManager().getParts(), params.getTimeRegion(), btnMergeAggregatedParts.getSelection(), btnShowNumbers.getSelection());
								timeAxis.createDiagram(params.getTimeRegion());
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

	private class SelectSelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			try {
				if (loader.getCurrentTrace() == null)
					return;
				if (list.getSelectionCount() > 0)
					param.setText(list.getSelection()[0]);
				if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
					hasChanged = HasChanged.PARAMETER;
				else
					list.removeAll();
				setConfiguration();
				final String title = "Computing parts...";
				final Job job = new Job(title) {

					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						monitor.beginTask(title, IProgressMonitor.UNKNOWN);
						try {
							core.computeParts(hasChanged);
							monitor.done();
							Display.getDefault().syncExec(new Runnable() {

								@Override
								public void run() {
									// MessageDialog.openInformation(getSite().getShell(),
									// "Parts", "Parts processing finished");
									hasChanged = HasChanged.NOTHING;
									matrix.deleteDiagram();
									timeAxis.createDiagram(params.getTimeRegion());
									matrix.createDiagram(core.getLpaggregManager().getParts(), params.getTimeRegion(), btnMergeAggregatedParts.getSelection(), btnShowNumbers.getSelection());
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
			} catch (final NumberFormatException e1) {

			}

		}
	}

	private class ThresholdModificationListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			try {
				if (Float.parseFloat(threshold.getText()) < Float.MIN_VALUE || Float.parseFloat(threshold.getText()) > 1)
					threshold.setText("0.001");
			} catch (final NumberFormatException err) {
				threshold.setText("0.001");
			}
		}
	}

	private class TypesSelectionAdapter extends SelectionAdapter {

		// all - input
		java.util.List<Object> diff(final java.util.List<EventType> all, final java.util.List<EventType> input) {
			final java.util.List<Object> tmp = new LinkedList<>();
			for (final Object oba : all)
				tmp.add(oba);
			tmp.removeAll(input);
			return tmp;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (loader.getCurrentTrace() == null)
				return;
			final ListSelectionDialog dialog = new ListSelectionDialog(getSite().getShell(), diff(loader.getTypes(), types), new ArrayContentProvider(), new EventTypeLabelProvider(), "Select Event Types");
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				types.add((EventType) o);
			listViewerTypes.setInput(types);
			hasChanged = HasChanged.ALL;
		}
	}

	public static final String					PLUGIN_ID	= Activator.PLUGIN_ID;
	public static final String					ID			= "fr.inria.soctrace.tools.ocelotl.ui.Ocelotl"; //$NON-NLS-1$
	private HasChanged							hasChanged	= HasChanged.ALL;
	/** Loader to interact with the DB */
	private final ConfDataLoader				loader		= new ConfDataLoader();
	private Text								timestampStart;
	private Text								timestampEnd;
	private Text								threshold;
	private Text								param;
	private ListViewer							listViewerProducers;
	private ListViewer							listViewerTypes;
	private ListViewer							listViewerIdles;
	private List								list;

	private final java.util.List<EventProducer>	producers	= new LinkedList<EventProducer>();
	private final java.util.List<EventType>		types		= new LinkedList<EventType>();
	private final java.util.List<String>		idles		= new LinkedList<String>();
	private Button								btnNormalize;
	private Spinner								spinnerTimeSlices;
	private Spinner								maxEventProducers;
	private Combo								comboAggOperator;
	private Button								btnRun;
	private Button								btnMergeAggregatedParts;
	private Button								btnShowNumbers;
	private final OcelotlCore					core;
	private final OcelotlParameters				params;
	private MatrixView							matrix;
	private TimeAxisView						timeAxis;
	private QualityView							qualityView;
	private Combo								comboTraces;

	final Map<Integer, Trace>					traceMap	= new HashMap<Integer, Trace>();

	/** @throws SoCTraceException */
	public OcelotlView() throws SoCTraceException {
		try {
			loader.loadTraces();
		} catch (final SoCTraceException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
		params = new OcelotlParameters();
		core = new OcelotlCore(params);
	}

	private void cleanAll() {
		hasChanged = HasChanged.ALL;
		threshold.setText("0.001");
		timestampStart.setText("0");
		timestampEnd.setText("0");
		btnNormalize.setSelection(false);
		spinnerTimeSlices.setSelection(20);
		maxEventProducers.setSelection(0);
		param.setText("0");
		btnMergeAggregatedParts.setSelection(true);
		// lists
		producers.clear();
		types.clear();
		idles.clear();
		listViewerProducers.setInput(producers);
		listViewerTypes.setInput(types);
		listViewerIdles.setInput(idles);
	}

	@Override
	public void createPartControl(final Composite parent) {

		// Highest Component
		final SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

		matrix = new MatrixView();

		timeAxis = new TimeAxisView();

		qualityView = new QualityView(this);
		// sashForm.setWeights(new int[] {220, 295});

		final SashForm sashForm_5 = new SashForm(sashForm, SWT.BORDER | SWT.VERTICAL);
		sashForm_5.setSashWidth(1);
		// @SuppressWarnings("unused")
		final Composite compositeVisu = new Composite(sashForm_5, SWT.NONE);
		compositeVisu.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeVisu.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		// compositeVisu.setToolTipText("test");
		final GridLayout gl_compositeVisu = new GridLayout();
		compositeVisu.setLayout(gl_compositeVisu);
		compositeVisu.setSize(500, 500);
		final Canvas canvas = matrix.initDiagram(compositeVisu);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		final Composite composite_4 = new Composite(sashForm_5, SWT.NONE);
		composite_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		// TODO right proportions
		final GridLayout gl_compositeTime = new GridLayout();
		composite_4.setLayout(gl_compositeTime);
		final Canvas canvas2 = timeAxis.initDiagram(composite_4);
		sashForm_5.setWeights(new int[] { 125, 36 });

		final TabFolder tabFolder = new TabFolder(sashForm, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		final TabItem tbtmTraceParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmTraceParameters.setText("Trace Parameters");

		final SashForm sashForm_3 = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmTraceParameters.setControl(sashForm_3);

		final Composite composite_2 = new Composite(sashForm_3, SWT.NONE);
		composite_2.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		composite_2.setLayout(new GridLayout(1, false));
		comboTraces = new Combo(composite_2, SWT.READ_ONLY);
		final GridData gd_comboTraces = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_comboTraces.widthHint = 327;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		int index = 0;
		for (final Trace t : loader.getTraces()) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}
		;

		final SashForm sashForm_6 = new SashForm(sashForm_3, SWT.NONE);
		final Group groupProducers = new Group(sashForm_6, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		groupProducers.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupProducers.setText("Event Producers");
		final GridLayout gl_groupProducers = new GridLayout(2, false);//
		gl_groupProducers.horizontalSpacing = 0;
		groupProducers.setLayout(gl_groupProducers);

		listViewerProducers = new ListViewer(groupProducers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerProducers.setContentProvider(new ArrayContentProvider());
		listViewerProducers.setLabelProvider(new EventProducerLabelProvider());
		listViewerProducers.setComparator(new ViewerComparator());
		final List listProd = listViewerProducers.getList();
		listProd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final GridData gd_listProd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_listProd.heightHint = 79;
		gd_listProd.widthHint = 120;
		listProd.setLayoutData(gd_listProd);

		final ScrolledComposite scrolledComposite = new ScrolledComposite(groupProducers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Composite composite_1 = new Composite(scrolledComposite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		final Button btnAddProdFilter = new Button(composite_1, SWT.NONE);
		btnAddProdFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddProdFilter.setText("Add");
		btnAddProdFilter.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddProdFilter.setImage(null);

		final Button btnAddAll = new Button(composite_1, SWT.NONE);
		btnAddAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddAll.setText("Add All");
		btnAddAll.setImage(null);
		btnAddAll.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddAll.addSelectionListener(new AllFilterAdapter());

		final Button btnAddResult = new Button(composite_1, SWT.NONE);
		btnAddResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddResult.setText("Add Result");
		btnAddResult.setImage(null);
		btnAddResult.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddResult.addSelectionListener(new ResultsFilterAdapter());
		final Button btnRemoveProd = new Button(composite_1, SWT.NONE);
		btnRemoveProd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnRemoveProd.setText("Reset");
		btnRemoveProd.addSelectionListener(new ResetSelectionAdapter(listViewerProducers));
		btnRemoveProd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveProd.setImage(null);
		scrolledComposite.setContent(composite_1);
		scrolledComposite.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnAddProdFilter.addSelectionListener(new ProducersFilterAdapter());
		final Group groupTypes = new Group(sashForm_6, SWT.NONE);
		groupTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTypes.setText("Event Types");
		final GridLayout gl_groupTypes = new GridLayout(2, false);
		gl_groupTypes.horizontalSpacing = 0;
		groupTypes.setLayout(gl_groupTypes);

		listViewerTypes = new ListViewer(groupTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerTypes.setContentProvider(new ArrayContentProvider());
		listViewerTypes.setLabelProvider(new EventTypeLabelProvider());
		listViewerTypes.setComparator(new ViewerComparator());
		final List listTypes = listViewerTypes.getList();
		listTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		listTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final ScrolledComposite scrolledComposite_1 = new ScrolledComposite(groupTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);

		final Composite compositeBtnTypes = new Composite(scrolledComposite_1, SWT.NONE);
		compositeBtnTypes.setLayout(new GridLayout(1, false));

		final Button btnAddTypes = new Button(compositeBtnTypes, SWT.NONE);
		btnAddTypes.setText("Add");
		btnAddTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddTypes.setImage(null);
		btnAddTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddTypes.addSelectionListener(new TypesSelectionAdapter());

		final Button btnRemoveTypes = new Button(compositeBtnTypes, SWT.NONE);
		btnRemoveTypes.setText("Remove");
		btnRemoveTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveTypes.setImage(null);
		btnRemoveTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrolledComposite_1.setContent(compositeBtnTypes);
		scrolledComposite_1.setMinSize(compositeBtnTypes.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveTypes.addSelectionListener(new RemoveSelectionAdapter(listViewerTypes));
		final Group groupIdle = new Group(sashForm_6, SWT.NONE);
		groupIdle.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupIdle.setText("Idle States");
		final GridLayout gl_groupIdle = new GridLayout(2, false);
		gl_groupIdle.horizontalSpacing = 0;
		gl_groupIdle.verticalSpacing = 0;
		groupIdle.setLayout(gl_groupIdle);

		listViewerIdles = new ListViewer(groupIdle, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerIdles.setContentProvider(new ArrayContentProvider());
		listViewerIdles.setComparator(new ViewerComparator());
		final List listIdle = listViewerIdles.getList();
		final GridData gd_listIdle = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_listIdle.widthHint = 203;
		listIdle.setLayoutData(gd_listIdle);
		listIdle.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));

		final ScrolledComposite scrolledComposite_2 = new ScrolledComposite(groupIdle, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrolledComposite_2.setExpandHorizontal(true);
		scrolledComposite_2.setExpandVertical(true);

		final Composite compositeBtnIdle = new Composite(scrolledComposite_2, SWT.NONE);
		compositeBtnIdle.setLayout(new GridLayout(1, false));

		final Button btnAddIdle = new Button(compositeBtnIdle, SWT.NONE);
		btnAddIdle.setText("Add");
		btnAddIdle.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddIdle.setImage(null);
		btnAddIdle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddIdle.addSelectionListener(new IdlesSelectionAdapter());

		final Button btnRemoveIdle = new Button(compositeBtnIdle, SWT.NONE);
		btnRemoveIdle.setText("Remove");
		btnRemoveIdle.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveIdle.setImage(null);
		btnRemoveIdle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrolledComposite_2.setContent(compositeBtnIdle);
		scrolledComposite_2.setMinSize(compositeBtnIdle.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveIdle.addSelectionListener(new RemoveSelectionAdapter(listViewerIdles));
		sashForm_6.setWeights(new int[] { 1, 1, 1 });
		sashForm_3.setWeights(new int[] { 23, 226 });
		comboTraces.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				cleanAll();
				try {
					loader.load(traceMap.get(comboTraces.getSelectionIndex()));
					timestampStart.setText(String.valueOf(loader.getMinTimestamp()));
					timestampEnd.setText(String.valueOf(loader.getMaxTimestamp()));
					for (int i = 0; i < loader.getTypes().size(); i++)
						if (loader.getTypes().get(i).getName().contains("PajeSetState")) {
							types.add(loader.getTypes().get(i));
							break;
						}
					listViewerTypes.setInput(types);
					idles.add("IDLE");
					listViewerIdles.setInput(idles);
				} catch (final SoCTraceException e1) {
					MessageDialog.openError(getSite().getShell(), "Exception", e1.getMessage());
				}
			}
		});

		final TabItem tbtmAggregationParameters = new TabItem(tabFolder, SWT.NONE);
		tbtmAggregationParameters.setText("Time Aggregation Parameters");

		final SashForm sashForm_1 = new SashForm(tabFolder, SWT.NONE);
		tbtmAggregationParameters.setControl(sashForm_1);

		final Group groupParameters = new Group(sashForm_1, SWT.NONE);
		groupParameters.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupParameters.setText("Time Slicing Parameters ");
		groupParameters.setLayout(new GridLayout(1, false));

		final Group grpAggregationOperator = new Group(groupParameters, SWT.NONE);
		grpAggregationOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpAggregationOperator.setText("Aggregation Operator");
		grpAggregationOperator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		grpAggregationOperator.setLayout(new GridLayout(1, false));

		final Composite composite = new Composite(grpAggregationOperator, SWT.NONE);
		composite.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		composite.setLayout(new GridLayout(1, false));
		final GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gd_composite.widthHint = 85;
		composite.setLayoutData(gd_composite);

		comboAggOperator = new Combo(composite, SWT.READ_ONLY);
		comboAggOperator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		comboAggOperator.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		comboAggOperator.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (loader.getCurrentTrace() == null)
					return;
				hasChanged = HasChanged.ALL;
			}
		});
		for (final String op : AggregationOperators.List)
			comboAggOperator.add(op);
		comboAggOperator.setText(AggregationOperators.List.get(0));

		final Group groupTime = new Group(groupParameters, SWT.NONE);
		final GridData gd_groupTime = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_groupTime.widthHint = 200;
		groupTime.setLayoutData(gd_groupTime);
		groupTime.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupTime.setText("Time Interval");
		groupTime.setLayout(new GridLayout(2, false));

		final Label lblStartTimestamp = new Label(groupTime, SWT.NONE);
		lblStartTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblStartTimestamp.setText("Start Timestamp");

		timestampStart = new Text(groupTime, SWT.BORDER);
		timestampStart.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		timestampStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timestampStart.addModifyListener(new ConfModificationListener());

		final Label lblEndTimestamp = new Label(groupTime, SWT.NONE);
		lblEndTimestamp.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblEndTimestamp.setText("End Timestamp");

		timestampEnd = new Text(groupTime, SWT.BORDER);
		timestampEnd.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		timestampEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timestampEnd.addModifyListener(new ConfModificationListener());
		final Composite compositeNormalize = new Composite(groupParameters, SWT.NONE);
		compositeNormalize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		compositeNormalize.setLayout(new GridLayout(2, false));
		compositeNormalize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		final Label lblNumberOfTime = new Label(compositeNormalize, SWT.NONE);
		lblNumberOfTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
		lblNumberOfTime.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblNumberOfTime.setText("Number of time slices");

		spinnerTimeSlices = new Spinner(compositeNormalize, SWT.BORDER);
		final GridData gd_spinnerTimeSlices = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_spinnerTimeSlices.widthHint = 36;
		spinnerTimeSlices.setLayoutData(gd_spinnerTimeSlices);
		spinnerTimeSlices.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		spinnerTimeSlices.setMaximum(10000);
		spinnerTimeSlices.setMinimum(1);
		spinnerTimeSlices.setSelection(200);
		spinnerTimeSlices.addModifyListener(new ConfModificationListener());
		final Group groupDichotomy = new Group(sashForm_1, SWT.NONE);
		groupDichotomy.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupDichotomy.setLayout(new GridLayout(1, false));
		groupDichotomy.setText("Get Best-Cut Partition Gain/Loss Parameter List");

		btnNormalize = new Button(groupDichotomy, SWT.CHECK);
		btnNormalize.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnNormalize.setSelection(false);
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());

		final Composite composite_3 = new Composite(groupDichotomy, SWT.NONE);
		composite_3.setLayout(new GridLayout(3, false));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		final Label lblThreshold = new Label(composite_3, SWT.NONE);
		lblThreshold.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblThreshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblThreshold.setText("Threshold");

		threshold = new Text(composite_3, SWT.BORDER);
		final GridData gd_threshold = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_threshold.widthHint = 342;
		threshold.setLayoutData(gd_threshold);
		threshold.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final Button btnRun_1 = new Button(composite_3, SWT.NONE);
		btnRun_1.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRun_1.setText("Get");

		final Group group = new Group(groupDichotomy, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		final GridData gd_group = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_group.heightHint = 119;
		gd_group.widthHint = 277;
		group.setLayoutData(gd_group);

		list = new List(group, SWT.BORDER | SWT.V_SCROLL);
		list.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		final GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_list.heightHint = 132;
		gd_list.widthHint = 330;
		list.setLayoutData(gd_list);
		new Label(group, SWT.NONE);
		final Group groupAggreg = new Group(group, SWT.NONE);
		groupAggreg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		groupAggreg.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupAggreg.setLayout(new GridLayout(3, false));
		groupAggreg.setText("Perform Best-Cut Partition");

		final Label lblParam = new Label(groupAggreg, SWT.NONE);
		lblParam.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblParam.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblParam.setText("Gain/loss parameter");

		param = new Text(groupAggreg, SWT.BORDER);
		param.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		param.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		param.addModifyListener(new ParamModificationListener());
		btnRun = new Button(groupAggreg, SWT.NONE);
		btnRun.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnRun.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRun.setText("Process");
		btnRun.addSelectionListener(new RunSelectionListener());
		new Label(group, SWT.NONE);
		list.addSelectionListener(new SelectSelectionListener());

		final Composite composite_5 = new Composite(sashForm_1, SWT.NONE);
		composite_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		composite_5.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		// compositeVisu.setToolTipText("test");
		final GridLayout gl_compositeQuality = new GridLayout();
		composite_5.setLayout(gl_compositeQuality);
		final Canvas canvas3 = qualityView.initDiagram(composite_5);
		canvas3.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm_1.setWeights(new int[] { 97, 153, 332 });

		btnRun_1.addSelectionListener(new DichotomySelectionListener());
		threshold.addModifyListener(new ThresholdModificationListener());

		final TabItem tbtmSystemConfiguration = new TabItem(tabFolder, 0);
		tbtmSystemConfiguration.setText("Advanced Parameters");

		final SashForm sashForm_4 = new SashForm(tabFolder, SWT.VERTICAL);
		sashForm_4.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		tbtmSystemConfiguration.setControl(sashForm_4);

		final Group grpEventProducersMax = new Group(sashForm_4, SWT.NONE);
		grpEventProducersMax.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		grpEventProducersMax.setText("Memory Management");
		grpEventProducersMax.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpEventProducersMax, SWT.NONE);
		lblDivideDbQueries.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		lblDivideDbQueries.setText("Divide DB query (Event Producers per query, inactive if 0)");

		maxEventProducers = new Spinner(grpEventProducersMax, SWT.BORDER);
		maxEventProducers.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
		final GridData gd_maxEventProducers = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_maxEventProducers.widthHint = 99;
		maxEventProducers.setLayoutData(gd_maxEventProducers);
		maxEventProducers.addModifyListener(new ConfModificationListener());
		maxEventProducers.setMinimum(0);
		maxEventProducers.setSelection(0);

		final Group grpVisualizationSettings = new Group(sashForm_4, SWT.NONE);
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
		sashForm_4.setWeights(new int[] { 112, 374 });
		sashForm.setWeights(new int[] { 172, 286 });
		canvas2.setLayoutData(new GridData(GridData.FILL_BOTH));

		// clean all
		cleanAll();

	}

	public Button getBtnRun() {
		return btnRun;
	}

	public OcelotlCore getCore() {
		return core;
	}

	public List getList() {
		return list;
	}

	public Text getParam() {
		return param;
	}

	public OcelotlParameters getParams() {
		return params;
	}

	public void setConfiguration() {

		params.setTrace(loader.getCurrentTrace());
		params.setEventProducers(producers);
		params.setEventTypes(types);
		params.setSleepingStates(idles);
		params.setNormalize(btnNormalize.getSelection());
		params.setTimeSlicesNumber(spinnerTimeSlices.getSelection());
		params.setMaxEventProducers(maxEventProducers.getSelection());
		params.setAggOperator(comboAggOperator.getText());
		// TODO manage number format exception
		try {
			params.setThreshold(Double.valueOf(threshold.getText()).floatValue());
			params.setParameter(Double.valueOf(param.getText()).floatValue());
			params.setTimeRegion(new TimeRegion(Long.valueOf(timestampStart.getText()), Long.valueOf(timestampEnd.getText())));
		} catch (final NumberFormatException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}