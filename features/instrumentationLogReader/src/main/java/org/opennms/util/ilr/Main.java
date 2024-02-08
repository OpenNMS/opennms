/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.util.ilr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.opennms.util.ilr.Collector.SortColumn;

public class Main {

    /**
     * @param args
     * @throws IOException  	
     */
    Collector c = new Collector();
    boolean processedFiles = false;
    public static void main(String[] args) throws IOException {
        new Main().execute(args, System.out);
    }
    public void execute(String[] args, OutputStream out) {
        ArgumentParser argParser = new ArgumentParser("ILR", this);
        try {
            argParser.processArgs(args);
        } catch (Exception e) {
            reportErrAndExit(argParser, "Unable to parse Arguments."+ e);
        }
        if (!processedFiles) {
            reportErrAndExit(argParser, "At least one instrumentation log file must be passed in.");
        }
        
        PrintWriter writer = new PrintWriter(out,true);
        c.printReport(writer);

    }
	private void reportErrAndExit(ArgumentParser argParser, String errMsg) {
		System.err.println(errMsg);
		argParser.printHelpOptions();
		System.exit(1);
	}
    @Option (shortName ="tpt", longName = "totalPersistTime", help = "Sorts by total persist time")
    public void sortByTotalPersistTime() {
        c.setSortColumn(SortColumn.TOTALPERSISTTIME);
    }
    @Option (shortName ="apt", longName = "averagePersistTime", help = "Sorts by average persist time")
    public void sortByAveragePersistTime() {
        c.setSortColumn(SortColumn.AVERAGEPERSISTTIME);
    }
    @Option (shortName ="tct", longName = "totalCollectionTime", help = "Sorts by total collection time")
    public void sortByTotalCollectiontime() {
        c.setSortColumn(SortColumn.TOTALCOLLECTTIME);
    }
    @Option (shortName ="up", longName = "unsuccesfulPercentage", help = "Sorts by unsuccessful percentage")
    public void sortByUnsuccessfulPercentage() {
        c.setSortColumn(SortColumn.UNSUCCESSPERCENTAGE);
    }
    @Option (shortName ="auct", longName = "averageUnsuccessfulCollectionTime", help = "Sorts by average unsuccessful collection time")
    public void sortByAverageUnsuccessfulCollectionTime() {
        c.setSortColumn(SortColumn.AVGUNSUCCESSCOLLECTTIME);
    }
    @Option (shortName ="sp", longName = "successfulPercentage", help = "Sorts by successful percentage")
    public void sortBySuccessfulPercentage() {
        c.setSortColumn(SortColumn.SUCCESSPERCENTAGE);
    }
    @Option (shortName ="asct", longName = "averageSuccessfulCollectionTime", help = "Sorts by average successful collection time")
    public void sortByAverageSuccessfulCollectionTime() {
        c.setSortColumn(SortColumn.AVGSUCCESSCOLLECTTIME);
    }
    @Option (shortName ="atbc", longName = "averageTimeBetweenCollections", help = "Sorts by average time between collections")
    public void sortByAverageTimeBetweenCollections() {
        c.setSortColumn(SortColumn.AVGTIMEBETWEENCOLLECTS);
    }
    @Option (shortName ="act", longName = "averageCollectionTime", help = "Sorts by average collection time")
    public void sortByAverageCollectionTime() {
        c.setSortColumn(SortColumn.AVGCOLLECTTIME);
    }
    @Option (shortName ="tc", longName = "totalCollections", help = "Sorts by total collections")
    public void sortByTotalCollections() {
        c.setSortColumn(SortColumn.TOTALCOLLECTS);
    }
    @Option (shortName ="ms", longName = "msDurations", help = "Outputs all durations in milliseconds")
    public void setDurationsMs() {
        Collector.setDurationsMs(true);
    }
    public Collector getCollector(){
        return c;
    }
    @Arguments(help = "One or more instrumentation log files (with INFO logging enabled)")
    public void processLogFile(String fileName) {
        try {

            c.readLogMessagesFromFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        processedFiles = true;
    }
    public void parseHelpOption(String [] args, int i) {
        if (args[i].equals( "-h") || args[i].equals( "--help")){
            System.out.println("Usage: Main.java <input> <sort method>");
            System.out.println("<input> is your input file, <sort method> is a");
            System.out.println("column sorting method chosen from the following list:");
            System.out.println("-tc or --totalCollections : Sorts by total collections");
            System.out.println("-act or --averageCollectionTime : Sorts by average collection time");
            System.out.println("-atbc or --averageTimeBetweenCollections : Sorts by average time between collections");
            System.out.println("-asct or --averageSuccessfulCollectionTime : Sorts by average sucessful collection time");
            System.out.println("-sp or --successfulPercentage : Sorts by successful percentage");
            System.out.println("-auct or --averageUnsuccessfulCollectionTime : Sorts by average unsuccessful collection time");
            System.out.println("-up or --unsuccessfulPercentage : Sorts by unsuccessful percentage");
            System.out.println("-tct or --totalCollectionTime : Sorts by total collection time");
            System.out.println("-tpt or --totalPersistTime : Sorts by total persist time");
        }
    }
    
    
}
