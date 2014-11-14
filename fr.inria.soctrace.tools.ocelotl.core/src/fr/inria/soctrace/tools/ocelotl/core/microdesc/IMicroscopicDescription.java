package fr.inria.soctrace.tools.ocelotl.core.microdesc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;

public interface IMicroscopicDescription {

	void rebuildClean(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException;

	void rebuildApproximate(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException;

	void rebuildDirty(File aCacheFile,
			HashMap<String, EventProducer> eventProducers,
			IProgressMonitor monitor) throws IOException, SoCTraceException,
			InterruptedException, OcelotlException;
	
	public int getVectorSize();

	public int getVectorNumber();

	public void print();
}