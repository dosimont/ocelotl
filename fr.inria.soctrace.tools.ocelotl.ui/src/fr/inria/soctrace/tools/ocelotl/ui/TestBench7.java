package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Testbench class used for the benchmarking of the new query system (filtering
 * done in java instead of in DB)
 */
public class TestBench7 extends TestBench {

	public final int	TraceNamePos			= 0;
	public final int	TraceIDPos				= 1;
	public final int	TimeAggregatorPos		= 2;
	public final int	StartTimestampPos		= 3;
	public final int	EndTimeStampPos			= 4;
	public final int	numberOfThreadPos		= 5;
	public final int	eventPerthreadPos		= 6;
	public final int	filteredEventTypePos	= 7;
	public final int	filteredEventProdPos	= 8;
	
	public final int	CacheActivatedPos		= 2;
	public final int	NumberOfTimeSlicePos	= 4;
	public final int	DataAggregatorPos		= 3;
	public final int	NumberOfRepetetionPos	= 8;
	public final int	ParameterPos			= 9;
	public final int	testbenchHeaderSize		= 10;

	public TestBench7(String aFilePath, OcelotlView aView) {
		super(aFilePath, aView);
	}

	// Header: "TRACE; CACHE_USED; TS; MICROMODEL_TIME; QUALITY_TIME;
	// DICHO_TIME; PART_DISPLAY_TIME; CACHE_SIZE

	@Override
	public void parseFile() {

		LinkedList<String> microModels = new LinkedList<String>();
		microModels.add("State");
		microModels.add("State Query");
		microModels.add("State Hybrid");

		LinkedList<Integer> nbThreadParam = new LinkedList<Integer>();
		nbThreadParam.add(4);
		nbThreadParam.add(8);
		nbThreadParam.add(16);

		LinkedList<Integer> nbEventPerThreadParam = new LinkedList<Integer>();
		nbEventPerThreadParam.add(100);
		nbEventPerThreadParam.add(1000);
		nbEventPerThreadParam.add(10000);
		nbEventPerThreadParam.add(100000);

		LinkedList<Double> duration = new LinkedList<Double>();
		duration.add(1.0);
		duration.add(0.1);
		duration.add(0.25);
		duration.add(0.5);
		duration.add(0.66);
		duration.add(0.9);

		LinkedList<Integer> nbProd = new LinkedList<Integer>();
		nbProd.add(1111);
		nbProd.add(111);
		nbProd.add(255);
		nbProd.add(555);
		nbProd.add(700);
		nbProd.add(1000);

		LinkedList<Integer> nbET = new LinkedList<Integer>();
		nbProd.add(10);
		nbProd.add(1);
		nbProd.add(3);
		nbProd.add(5);
		nbProd.add(7);
		nbProd.add(9);

		for (Trace aTrace : theView.getConfDataLoader().getTraces()) {
			for (String aMicroModel : microModels) {
				for (Integer nbThread : nbThreadParam) {
					for (Integer nbEventThread : nbEventPerThreadParam) {
						for (Double aDuration : duration) {
							for (Integer aNbProd : nbProd) {
								for (Integer aNbET : nbET) {
									TestParameters params = new TestParameters();

									// Name
									params.setTraceName(aTrace.getAlias());
									// Database unique ID
									params.setTraceID(aTrace.getId());
									// Cache activation
									params.setActivateCache(false);
									// Time Aggregation Operator
									params.setMicroModelType(aMicroModel);
									// Data Aggregation Operator
									params.setDataAggOperator("Temporal Aggregation");
									params.setStartTimestamp(aTrace.getMinTimestamp());
									params.setEndTimestamp((long) (aTrace.getMaxTimestamp() * aDuration));
									params.setNumberOfThreads(nbThread);
									params.setEventPerThread(nbEventThread);

									params.setNumberOfEventProd(aNbProd);
									params.setNumberOfEventType(aNbET);

									testParams.add(params);
								}
							}
						}
					}
				}
			}
		}
	}

	public void launchTest() {

		if (!testParams.isEmpty()) {
			statData = "TRACE; NB_EVENTS; QUERY_TYPE; NB_THREAD; NB_EVENT_PER_THREAD; TS_START; TS_END; Filtered ET; filtered EP; MICROMODEL_TIME\n";
			String fileDir = aConfFile.substring(0, aConfFile.lastIndexOf("/") + 1);
			Date aDate = new Date(System.currentTimeMillis());
			String dirName = testParams.get(0).getTraceName() + "_" + aDate.toString();
			dirName = dirName.replace(" ", "_");
			testDirectory = fileDir + dirName;
			File dir = new File(testDirectory);
			dir.mkdirs();

			for (TestParameters aTest : testParams) {
				aTest.setDirectory(dir.getAbsolutePath());
				aTest.getParameters().add(1.0);
				aTest.setVisuOperator("null");
				aTest.setDatacacheStrat(DatacacheStrategy.DATACACHE_PROPORTIONAL);
				aTest.setNbTimeSlice(100);
				
				theView.loadFromParam(aTest, false);
				
				statData = statData + getStatData(aTest);
				writeStat();
			}

			// Call the script to compare the image
			try {
				Process p = new ProcessBuilder("/home/youenn/projects/testBenchOcelotl/compare.sh", testDirectory).start();
				p.waitFor();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Write the stat data for the current test
	 */
	public String getStatData(TestParameters aTest) {
		String stat = "";
		BufferedReader bufFileReader;
		String line;

		int microscopicModel = -1;

		try {
			bufFileReader = new BufferedReader(new FileReader("/home/youenn/traces/eclipse_output.txt"));

			while ((line = bufFileReader.readLine()) != null) {
				if (line.isEmpty())
					continue;

				if (line.contains("[Total Time for Microscopic Rebuilding]")) {
					String computation = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					microscopicModel = Integer.valueOf(computation);
				}
			}
			
			// TRACE; NB_EVENTS; QUERY_TYPE; NB_THREAD; NB_EVENT_PER_THREAD; TS_START; TS_END; Filtered ET; filtered EP; MICROMODEL_TIME
			stat = theView.aTestTrace.getAlias() + ";" + theView.getCurrentShownTrace().getNumberOfEvents() + ";" + theView.getOcelotlParameters().getMicroModelType() + ";" + theView.getOcelotlParameters().getNumberOfThread() + ";" + theView.getOcelotlParameters().getEventsPerThread() + ";"
					+ theView.getTimeRegion().getTimeStampStart() + ";" + theView.getTimeRegion().getTimeStampEnd() + ";" 
					+ aTest.getNumberOfEventType() + ";" + aTest.getNumberOfEventProd() + ";"
					//+ aTest.getFilteredEventType().toString() + ";" + aTest.getFilteredEventProducer().toString() + ";"
					+ microscopicModel + "\n";
			bufFileReader.close();

			// Delete the output file
			PrintWriter writer = new PrintWriter(new File("/home/youenn/traces/eclipse_output.txt"));
			writer.print("");
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stat;
	}
}