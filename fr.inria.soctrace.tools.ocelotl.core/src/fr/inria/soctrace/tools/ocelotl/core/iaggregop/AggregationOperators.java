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

package fr.inria.soctrace.tools.ocelotl.core.iaggregop;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.TraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.PajeConfig;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeNormalizedStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.aggregop.PajeStateTypeSum;
import fr.inria.soctrace.tools.ocelotl.core.paje.query.Query;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;


public class AggregationOperators {
	
HashMap<String,IAggregationOperator> List;
HashMap<String, TraceTypeConfig> Config;
ArrayList<String> Names;
Query query;

public AggregationOperators(Query query) {
	super();
	this.query=query;
	try {
		init();
		initConfig();
	} catch (SoCTraceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}

private void init() throws SoCTraceException{
	List = new HashMap<String, IAggregationOperator>();
	List.put(PajeStateSum.descriptor, new PajeStateSum());
	List.put(PajeNormalizedStateSum.descriptor, new PajeNormalizedStateSum());
	List.put(PajeStateTypeSum.descriptor, new PajeStateTypeSum());
	
}

private void initConfig() throws SoCTraceException{
	Config = new HashMap<String, TraceTypeConfig>();
	Config.put(PajeConstants.PajeFormatName, new PajeConfig());
}

public Collection<IAggregationOperator> getList(){
	return List.values();
}

public IAggregationOperator getOperator(String name) throws SoCTraceException{
			IAggregationOperator op=List.get(name);
			op.setQueries(query);
			return op;
}

public TraceTypeConfig config(IAggregationOperator op){
		return Config.get(op.traceType());
}

public TraceTypeConfig config(String op){
	return Config.get(List.get(op).traceType());
}

public String getType(String op) {
	return List.get(op).traceType();
}


 
}


