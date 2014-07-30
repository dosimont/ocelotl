package fr.inria.soctrace.tools.ocelotl.microdesc.ui;

import org.eclipse.swt.widgets.Shell;

public class HierarchicalStateDistributionView extends StateDistributionView{

	public HierarchicalStateDistributionView(Shell parent) {
		super(parent);
	}

	/**
	 * Check an element and all its parents.
	 * 
	 * @param element
	 *            The element to check.
	 */
	@Override
	protected void checkElement(Object element) {
		treeViewerEventProducer.setChecked(element, true);

		Object parent = new FilterTreeContentProvider().getParent(element);

		if (parent != null) {
			checkElement(parent);
		}
	}

	/**
	 * Uncheck an element and all its children.
	 * 
	 * @param element
	 *            The element to uncheck.
	 */
	@Override
	protected void uncheckElement(Object element) {
		treeViewerEventProducer.setChecked(element, false);

		for (Object child : new FilterTreeContentProvider()
				.getChildren(element)) {
			uncheckElement(child);
		}
	}
}
