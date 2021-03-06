/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.spacetime.EventProducerHierarchy.EventProducerNode;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class HierarchyView {

	public final static int		Height	= 700;
	public final static int		Width	= 500;
	private EventProducerNode	eventProducerNode;
	private Shell				dialog;
	private Canvas				canvas;
	private Figure				root;
	private Tree				tree;
	private OcelotlView			ocelotlView;

	
	public HierarchyView(EventProducerNode eventProducerNode) {
		super();
		this.eventProducerNode = eventProducerNode;
	}

	public EventProducerNode getEventProducerNode() {
		return eventProducerNode;
	}

	public void setEventProducerNode(EventProducerNode eventProducerNode) {
		this.eventProducerNode = eventProducerNode;
	}

	/**
	 * Display the content of the aggregation in a new window
	 * 
	 * @param ocelotlview
	 *            the current ocelotl view
	 */
	public void display(OcelotlView ocelotlview) {
		this.ocelotlView = ocelotlview;
		
		try {
			// New window
			dialog = new Shell(ocelotlView.getSite().getShell().getDisplay());
			dialog.setText(eventProducerNode.getMe().getName());
			dialog.setSize(Width, Height);
			// Set location of the new window centered around the center of the
			// eclipse window
			dialog.setLocation(ocelotlView.getSite().getShell().getLocation().x + ocelotlView.getSite().getShell().getSize().x / 2 - Width / 2, ocelotlview.getSite().getShell().getLocation().y + ocelotlview.getSite().getShell().getSize().y / 2 - Height / 2);
			dialog.setLayout(new FillLayout());

			// Init the tree
			tree = new Tree(dialog, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			tree.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
			tree.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));

			root = new Figure();
			root.setFont(tree.getFont());
			final XYLayout layout = new XYLayout();
			root.setLayoutManager(layout);
			canvas = new Canvas(tree, SWT.DOUBLE_BUFFERED);
			canvas.setSize(tree.getSize());
			final LightweightSystem lws = new LightweightSystem(canvas);
			lws.setContents(root);
			lws.setControl(canvas);
			root.setFont(SWTResourceManager.getFont("Cantarell", 24, SWT.NORMAL));
			root.setSize(tree.getSize().x, tree.getSize().y);

			dialog.addShellListener(new DialogShellListener());

			// Init tree content
			buildTree();
			dialog.open();

		} catch (IllegalArgumentException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the construction of the hierarchy tree
	 */
	private void buildTree() {
		TreeItem rootItem = new TreeItem(tree, 0);
		rootItem.setText(eventProducerNode.getMe().getName());
		
		for (EventProducerNode epn : eventProducerNode.getChildrenNodes()) {
			buildTreeChild(epn, rootItem);
		}
	}

	/**
	 * Recursively build all the node of the hierarchy
	 * 
	 * @param epn
	 *            the current event producer node
	 * @param parent
	 *            the parent tree item
	 */
	private void buildTreeChild(EventProducerNode epn, TreeItem parent) {
		TreeItem treeItem = new TreeItem(parent, 0);
		treeItem.setText(epn.getMe().getName());
		
		for (EventProducerNode epnChild : epn.getChildrenNodes()) {
			buildTreeChild(epnChild, treeItem);
		}
	}

	private class DialogShellListener implements ShellListener {

		@Override
		public void shellActivated(ShellEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void shellClosed(ShellEvent e) {
			// Remove the highlight rectangle
			UnitAxisView graphDisplayView = ocelotlView.getUnitAxisView();

			if (graphDisplayView.getHighLightDisplayedProducer() != null)
				graphDisplayView.getHighLightDisplayedProducer().delete();
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void shellDeiconified(ShellEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void shellIconified(ShellEvent e) {
			// TODO Auto-generated method stub

		}

	}
}
