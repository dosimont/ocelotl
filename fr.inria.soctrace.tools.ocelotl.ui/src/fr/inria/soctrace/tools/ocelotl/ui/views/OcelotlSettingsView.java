package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;

public class OcelotlSettingsView extends Dialog {
	
	private OcelotlView							ocelotlView;
	private Button								btnDeleteDataCache;
	private Text								datacacheDirectory;
	private Button								btnChangeCacheDirectory;
	private Button								btnCacheEnabled;
	private Button								btnRadioButton, btnRadioButton_1, btnRadioButton_2, btnRadioButton_3;
	private HashMap<DatacachePolicy, Button>	cachepolicy	= new HashMap<DatacachePolicy, Button>();
	private Spinner								cacheTimeSliceValue;
	private TabFolder							tabFolder;
	private Button								btnNormalize;
	private Button								btnGrowingQualities;
	private Button								btnDecreasingQualities;
	private Spinner								spinnerEventSize;
	private Spinner								spinnerDivideDbQuery;
	private Spinner								spinnerThread;
	private Spinner								dataCacheSize;
	private Font								cantarell8;
	private Text								textThreshold;
	
	public OcelotlSettingsView(final OcelotlView ocelotlView) {
		super(ocelotlView.getSite().getShell());
		this.ocelotlView = ocelotlView;
	}

	public void openDialog() {
		this.open();
	}

	private class CacheTimeSliceListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlView.getParams().getOcelotlSettings().setCacheTimeSliceNumber(Integer.valueOf(cacheTimeSliceValue.getText()));
		}
	}

	private class ThreadNumberListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlView.getParams().getOcelotlSettings().setNumberOfThread(Integer.valueOf(spinnerThread.getText()));
		}
	}

	private class MaxEventProducerListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlView.getParams().getOcelotlSettings().setMaxEventProducersPerQuery(Integer.valueOf(spinnerDivideDbQuery.getText()));
		}
	}

	private class EventPerThreadListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlView.getParams().getOcelotlSettings().setEventsPerThread(Integer.valueOf(spinnerEventSize.getText()));
		}
	}

	private class cachePolicyListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (btnRadioButton.getSelection()) {
				ocelotlView.getParams().getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_SLOW);
			}
			if (btnRadioButton_1.getSelection()) {
				ocelotlView.getParams().getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_FAST);
			}
			if (btnRadioButton_2.getSelection()) {
				ocelotlView.getParams().getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_ASK);
			}
			if (btnRadioButton_3.getSelection()) {
				ocelotlView.getParams().getOcelotlSettings().setCachePolicy(DatacachePolicy.CACHEPOLICY_AUTO);
			}
		}
	}

	private class DataCacheSizeListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			try {
				if (Integer.valueOf(dataCacheSize.getText()) < 0) {
					ocelotlView.getParams().getDataCache().setCacheMaxSize(-1);
				} else {
					// Set the cache size at the entered value converted from
					// Megabytes to bytes
					ocelotlView.getParams().getDataCache().setCacheMaxSize(Long.valueOf(dataCacheSize.getText()) * 1000000);
				}
			} catch (final NumberFormatException err) {
				dataCacheSize.setSelection((int) ocelotlView.getParams().getDataCache().getCacheMaxSize());
			} catch (OcelotlException e1) {
				MessageDialog.openInformation(getShell(), "Error", e1.getMessage());
			}
		}
	}

	private class ThresholdModifyListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			ocelotlView.getParams().getOcelotlSettings().setThresholdPrecision(Float.parseFloat(textThreshold.getText()));

			try {
				if (Float.parseFloat(textThreshold.getText()) < Float.MIN_VALUE || Float.parseFloat(textThreshold.getText()) > 1)
					textThreshold.setText(String.valueOf(OcelotlDefaultParameterConstants.Threshold));
			} catch (final NumberFormatException err) {
				textThreshold.setText(String.valueOf(OcelotlDefaultParameterConstants.Threshold));
			}
		/*	if (confDataLoader.getCurrentTrace() == null)
				return;

			if (hasChanged == HasChanged.NOTHING || hasChanged == HasChanged.EQ || hasChanged == HasChanged.PARAMETER)
				hasChanged = HasChanged.THRESHOLD;
		}*/
	}
	}

	private class DeleteDataCache extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			// Ask user confirmation
			if (MessageDialog.openConfirm(getShell(), "Delete cached data", "This will delete all cached data. Do you want to continue ?"))
				ocelotlView.getParams().getDataCache().deleteCache();
		}
	}

	private class ModifyDatacacheDirectory extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			String newCacheDir = dialog.open();
			if (newCacheDir != null) {
				// Update the current datacache path
				ocelotlView.getParams().getDataCache().setCacheDirectory(newCacheDir);
				// Update the displayed path
				datacacheDirectory.setText(ocelotlView.getParams().getDataCache().getCacheDirectory());
			}
		}
	}

	private class EnableCacheListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			ocelotlView.getParams().getDataCache().setCacheActive(btnCacheEnabled.getSelection());
			boolean cacheActivation = ocelotlView.getParams().getDataCache().isCacheActive();

			btnDeleteDataCache.setEnabled(cacheActivation);
			datacacheDirectory.setEnabled(cacheActivation);
			btnChangeCacheDirectory.setEnabled(cacheActivation);
			btnRadioButton.setEnabled(cacheActivation);
			btnRadioButton_1.setEnabled(cacheActivation);
			btnRadioButton_2.setEnabled(cacheActivation);
			btnRadioButton_3.setEnabled(cacheActivation);
			cacheTimeSliceValue.setEnabled(cacheActivation);
			dataCacheSize.setEnabled(cacheActivation);
		}
	}

	private class IncreasingQualityRadioSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			btnDecreasingQualities.setSelection(!btnGrowingQualities.getSelection());
			ocelotlView.getParams().setGrowingQualities(btnGrowingQualities.getSelection());
			ocelotlView.getParams().getOcelotlSettings().setIncreasingQualities(btnGrowingQualities.getSelection());
		//	qualityView.createDiagram();
		}
	}

	private class NormalizeSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
		//	if (hasChanged != HasChanged.ALL)
		//		hasChanged = HasChanged.NORMALIZE;

		ocelotlView.getParams().getOcelotlSettings().setNormalizedCurve(btnNormalize.getSelection());

		//	if (confDataLoader.getCurrentTrace() == null || comboSpace.getText().equals("") || comboTime.getText().equals(""))
		//		return;
		//	btnRun.notifyListeners(SWT.Selection, new Event());
		}
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite all = (Composite) super.createDialogArea(parent);

		final SashForm sashFormGlobal = new SashForm(all, SWT.VERTICAL);
		sashFormGlobal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		cantarell8 = new Font(sashFormGlobal.getDisplay(), new FontData("Cantarell", 8, SWT.NORMAL));

		tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		// Quality curves settings
		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Quality curves");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(cantarell8);
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);

		final Group groupQualityCurveSettings = new Group(sashFormAdvancedParameters, SWT.NONE);
		groupQualityCurveSettings.setFont(cantarell8);
		groupQualityCurveSettings.setText("Quality Curve Settings");
		groupQualityCurveSettings.setLayout(new GridLayout(4, false));
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnNormalize = new Button(groupQualityCurveSettings, SWT.CHECK);
		btnNormalize.setFont(cantarell8);
		btnNormalize.setSelection(ocelotlView.getParams().getOcelotlSettings().isNormalizedCurve());
		btnNormalize.setText("Normalize Qualities");
		btnNormalize.addSelectionListener(new NormalizeSelectionAdapter());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnGrowingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnGrowingQualities.setFont(cantarell8);
		btnGrowingQualities.setText("Complexity gain (green)\nInformation gain (red)");
		btnGrowingQualities.addSelectionListener(new IncreasingQualityRadioSelectionAdapter());
		btnGrowingQualities.setSelection(ocelotlView.getParams().getOcelotlSettings().getIncreasingQualities());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnDecreasingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnDecreasingQualities.setText("Complexity reduction (green)\nInformation loss (red)");
		btnDecreasingQualities.setSelection(!ocelotlView.getParams().getOcelotlSettings().getIncreasingQualities());
		btnDecreasingQualities.setFont(cantarell8);
		btnDecreasingQualities.addSelectionListener(new IncreasingQualityRadioSelectionAdapter());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		final Label lblThreshold = new Label(groupQualityCurveSettings, SWT.NONE);
		lblThreshold.setFont(cantarell8);
		lblThreshold.setText("X Axis Maximal Precision");

		textThreshold = new Text(groupQualityCurveSettings, SWT.BORDER);
		GridData gd_textThreshold = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textThreshold.widthHint = 100;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(cantarell8);
		textThreshold.addModifyListener(new ThresholdModifyListener());
		textThreshold.setText(String.valueOf(ocelotlView.getParams().getOcelotlSettings().getThresholdPrecision()));
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		sashFormAdvancedParameters.setWeights(new int[] { 1 });

		// Datacache settings
		final TabItem tbtmOcelotlSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmOcelotlSettings.setText("Cache");

		final SashForm sashFormSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormSettings.setFont(cantarell8);
		tbtmOcelotlSettings.setControl(sashFormSettings);

		final Group groupDataCacheSettings = new Group(sashFormSettings, SWT.NONE);
		groupDataCacheSettings.setFont(cantarell8);
		groupDataCacheSettings.setText("Data Cache Settings");
		groupDataCacheSettings.setLayout(new GridLayout(3, false));

		btnCacheEnabled = new Button(groupDataCacheSettings, SWT.CHECK);
		btnCacheEnabled.setFont(cantarell8);
		btnCacheEnabled.setText("Cache Enabled");
		btnCacheEnabled.setSelection(ocelotlView.getParams().getOcelotlSettings().isCacheActivated());
		btnCacheEnabled.addSelectionListener(new EnableCacheListener());

		btnDeleteDataCache = new Button(groupDataCacheSettings, SWT.PUSH);
		btnDeleteDataCache.setToolTipText("Empty Cache");
		btnDeleteDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/delete_obj.gif"));
		btnDeleteDataCache.setText("Empty Cache");
		btnDeleteDataCache.setFont(cantarell8);
		btnDeleteDataCache.addSelectionListener(new DeleteDataCache());
		new Label(groupDataCacheSettings, SWT.NONE);

		final Label lblDataCacheDirectory = new Label(groupDataCacheSettings, SWT.NONE);
		lblDataCacheDirectory.setFont(cantarell8);
		lblDataCacheDirectory.setText("Data cache directory:");

		final GridData gd_dataCacheDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dataCacheDir.widthHint = 100;

		datacacheDirectory = new Text(groupDataCacheSettings, SWT.BORDER);
		datacacheDirectory.setLayoutData(gd_dataCacheDir);
		datacacheDirectory.setFont(cantarell8);
		datacacheDirectory.setEditable(false);
		datacacheDirectory.setText(ocelotlView.getParams().getDataCache().getCacheDirectory());

		btnChangeCacheDirectory = new Button(groupDataCacheSettings, SWT.PUSH);
		btnChangeCacheDirectory.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnChangeCacheDirectory.setToolTipText("Change Cache Directory");
		btnChangeCacheDirectory.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/fldr_obj.gif"));
		btnChangeCacheDirectory.setFont(cantarell8);
		btnChangeCacheDirectory.addSelectionListener(new ModifyDatacacheDirectory());

		final Label lblDataCacheSize = new Label(groupDataCacheSettings, SWT.NONE);
		lblDataCacheSize.setFont(cantarell8);
		lblDataCacheSize.setText("MB Data cache size (-1=unlimited):");

		dataCacheSize = new Spinner(groupDataCacheSettings, SWT.BORDER);
		dataCacheSize.setValues(0, -1, 99999999, 0, 1, 10);
		dataCacheSize.setFont(cantarell8);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 100;
		dataCacheSize.setLayoutData(gd_text);
		dataCacheSize.addModifyListener(new DataCacheSizeListener());
		new Label(groupDataCacheSettings, SWT.NONE);

		Label lblCacheTimeSlices = new Label(groupDataCacheSettings, SWT.NONE);
		lblCacheTimeSlices.setText("Cache time slices:");
		lblCacheTimeSlices.setFont(cantarell8);
		lblCacheTimeSlices.setToolTipText("Number of time slices used when generating cache");

		cacheTimeSliceValue = new Spinner(groupDataCacheSettings, SWT.BORDER);
		cacheTimeSliceValue.setValues(0, 0, 99999999, 0, 1, 10);
		cacheTimeSliceValue.setFont(cantarell8);
		GridData gd_cacheTimeSliceValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_cacheTimeSliceValue.widthHint = 100;
		cacheTimeSliceValue.setLayoutData(gd_cacheTimeSliceValue);
		cacheTimeSliceValue.setSelection(ocelotlView.getParams().getOcelotlSettings().getCacheTimeSliceNumber());
		cacheTimeSliceValue.addModifyListener(new CacheTimeSliceListener());
		new Label(groupDataCacheSettings, SWT.NONE);

		Label lblCachePolicy = new Label(groupDataCacheSettings, SWT.NONE);
		lblCachePolicy.setText("Cache policy");
		lblCachePolicy.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);
		new Label(groupDataCacheSettings, SWT.NONE);

		btnRadioButton = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton.addSelectionListener(new cachePolicyListener());
		btnRadioButton.setText("Precise (slow)");
		btnRadioButton.setFont(cantarell8);

		btnRadioButton_1 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_1.addSelectionListener(new cachePolicyListener());
		btnRadioButton_1.setText("Fast");
		btnRadioButton_1.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);

		btnRadioButton_2 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_2.addSelectionListener(new cachePolicyListener());
		btnRadioButton_2.setText("Ask me");
		btnRadioButton_2.setFont(cantarell8);

		btnRadioButton_3 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_3.addSelectionListener(new cachePolicyListener());
		btnRadioButton_3.setText("Auto");
		btnRadioButton_3.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);

		cachepolicy.put(DatacachePolicy.CACHEPOLICY_SLOW, btnRadioButton);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_FAST, btnRadioButton_1);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_ASK, btnRadioButton_2);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_AUTO, btnRadioButton_3);
		cachepolicy.get(ocelotlView.getParams().getOcelotlSettings().getCachePolicy()).setSelection(true);

		if (ocelotlView.getParams().getOcelotlSettings().getCacheSize() > 0) {
			dataCacheSize.setSelection((int) (ocelotlView.getParams().getOcelotlSettings().getCacheSize() / 1000000));
		} else {
			dataCacheSize.setSelection((int) ocelotlView.getParams().getOcelotlSettings().getCacheSize());
		}
		sashFormSettings.setWeights(new int[] { 1 });

		// Thread settings
		final TabItem tbtmAdvancedSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvancedSettings.setText("Advanced");

		SashForm advancedSettingsSashForm = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmAdvancedSettings.setControl(advancedSettingsSashForm);
		final Group grpCacheManagement = new Group(advancedSettingsSashForm, SWT.NONE);
		grpCacheManagement.setFont(cantarell8);
		grpCacheManagement.setText("Iterator Management");
		grpCacheManagement.setLayout(new GridLayout(2, false));

		final Label lblPageSize = new Label(grpCacheManagement, SWT.NONE);
		lblPageSize.setFont(cantarell8);
		lblPageSize.setText("Event Number Retrieved by Threads");

		spinnerEventSize = new Spinner(grpCacheManagement, SWT.BORDER);
		spinnerEventSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerEventSize.setFont(cantarell8);
		spinnerEventSize.setMinimum(OcelotlDefaultParameterConstants.MIN_EVENTS_PER_THREAD);
		spinnerEventSize.setMaximum(OcelotlDefaultParameterConstants.MAX_EVENTS_PER_THREAD);
		spinnerEventSize.setSelection(ocelotlView.getParams().getOcelotlSettings().getEventsPerThread());
		spinnerEventSize.addModifyListener(new EventPerThreadListener());

		final Group grpDivideDbQuery = new Group(advancedSettingsSashForm, SWT.NONE);
		grpDivideDbQuery.setFont(cantarell8);
		grpDivideDbQuery.setText("Query Management");
		grpDivideDbQuery.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpDivideDbQuery, SWT.NONE);
		lblDivideDbQueries.setFont(cantarell8);
		lblDivideDbQueries.setText("Event Producers per Query (0=All)");

		spinnerDivideDbQuery = new Spinner(grpDivideDbQuery, SWT.BORDER);
		spinnerDivideDbQuery.setFont(cantarell8);
		spinnerDivideDbQuery.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerDivideDbQuery.setMinimum(OcelotlDefaultParameterConstants.MIN_EVENT_PRODUCERS_PER_QUERY);
		spinnerDivideDbQuery.setMaximum(OcelotlDefaultParameterConstants.MAX_EVENT_PRODUCERS_PER_QUERY);
		spinnerDivideDbQuery.setSelection(ocelotlView.getParams().getOcelotlSettings().getMaxEventProducersPerQuery());
		spinnerDivideDbQuery.addModifyListener(new MaxEventProducerListener());

		final Group grpMultiThread = new Group(advancedSettingsSashForm, SWT.NONE);
		grpMultiThread.setFont(cantarell8);
		grpMultiThread.setText("Multi Threading");
		grpMultiThread.setLayout(new GridLayout(2, false));

		final Label lblThread = new Label(grpMultiThread, SWT.NONE);
		lblThread.setFont(cantarell8);
		lblThread.setText("Working Threads");

		spinnerThread = new Spinner(grpMultiThread, SWT.BORDER);
		spinnerThread.setFont(cantarell8);
		spinnerThread.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerThread.setMinimum(OcelotlDefaultParameterConstants.MIN_NUMBER_OF_THREAD);
		spinnerThread.setMaximum(OcelotlDefaultParameterConstants.MAX_NUMBER_OF_THREAD);
		spinnerThread.setSelection(ocelotlView.getParams().getOcelotlSettings().getNumberOfThread());
		spinnerThread.addModifyListener(new ThreadNumberListener());
		advancedSettingsSashForm.setWeights(new int[] { 1, 1, 1 });

		return sashFormGlobal;
	}
	
	@Override
	protected void okPressed() {
		saveSettings();
		super.okPressed();
	}

	
	void saveSettings()
	{
		
	}
	
	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

}
