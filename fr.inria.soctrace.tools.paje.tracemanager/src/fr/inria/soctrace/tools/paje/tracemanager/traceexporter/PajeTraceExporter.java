/* ===========================================================
 * Paje Trace Manager module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to import, export and
 * process Pajé trace files
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

package fr.inria.soctrace.tools.paje.tracemanager.traceexporter;

import java.io.IOException;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.paje.tracemanager.traceexporter.managers.ExporterArgumentsManager;

public class PajeTraceExporter {

	private static final boolean	DEBUG	= true;

	public static void main(final String[] args) throws IOException {

		final DeltaManager dm = new DeltaManager();
		dm.start();

		try {
			final ExporterArgumentsManager arguments = new ExporterArgumentsManager(args);
			arguments.parseArgs();
			arguments.processArgs();
			if (DEBUG)
				arguments.debugArgs();

			final SystemDBObject sysDB = new SystemDBObject(arguments.getSysDbName(), DBMode.DB_OPEN);
			final TraceDBObject traceDB = new TraceDBObject(arguments.getTraceDbName(), DBMode.DB_OPEN);

			/** Export trace from database*/

			final Writer exporter = new Writer(arguments, traceDB);
			exporter.exportTrace();

			/** DB close (commit) */

			traceDB.close();
			sysDB.close();

		} catch (final SoCTraceException e) {
			e.printStackTrace();
		} finally {
			dm.end("Paje Trace Exporter end");
		}

	}

}
