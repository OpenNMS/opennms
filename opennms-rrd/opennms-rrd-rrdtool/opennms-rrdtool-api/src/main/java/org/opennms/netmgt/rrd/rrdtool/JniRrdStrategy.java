/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd.rrdtool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.springframework.util.FileCopyUtils;

/**
 * Provides an rrdtool based implementation of RrdStrategy. It uses the existing
 * JNI based single-threaded interface to write the rrdtool compatibile RRD
 * files.
 *
 * The JNI interface takes command-like arguments and doesn't provide open files
 * so the the Objects used to represent open files are really partial command
 * strings
 *
 * See the individual methods for more details
 *
 * @author ranger
 * @version $Id: $
 */
public class JniRrdStrategy implements RrdStrategy<JniRrdStrategy.CreateCommand ,StringBuffer> {
	
	private final static String IGNORABLE_LIBART_WARNING_STRING = "*** attempt to put segment in horiz list twice";
	private final static String IGNORABLE_LIBART_WARNING_REGEX = "\\*\\*\\* attempt to put segment in horiz list twice\r?\n?";

    private Properties m_configurationProperties;
    
    public static class CreateCommand {
    	
    	String filename;
    	final String operation = "create";
    	String parameter;
    	
		public CreateCommand(String filename, String parameter) {
			super();
			this.filename = filename;
			this.parameter = parameter;
		}
		
            @Override
		public String toString() {
			return operation + " " + filename + " " + parameter;
		}
		
    }

    /**
     * <p>getConfigurationProperties</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public Properties getConfigurationProperties() {
        return m_configurationProperties;
    }

    /** {@inheritDoc} */
        @Override
    public void setConfigurationProperties(Properties configurationParameters) {
        this.m_configurationProperties = configurationParameters;
    }

    /**
     * The 'closes' the rrd file. This is where the actual work of writing the
     * RRD files takes place. The passed in rrd is actually an rrd command
     * string containing updates. This method executes this command.
     *
     * @param rrd a {@link java.lang.StringBuffer} object.
     * @throws java.lang.Exception if any.
     */
        @Override
    public void closeFile(StringBuffer rrd) throws Exception {
        String command = rrd.toString();
        String[] results = Interface.launch(command);
        if (results[0] != null) {
            throw new Exception(results[0]);
        }
    }

    /** {@inheritDoc} */
        @Override
    public CreateCommand createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception {
        File f = new File(directory);
        f.mkdirs();

        String fileName = directory + File.separator + rrdName + RrdUtils.getExtension();
        
        if (new File(fileName).exists()) {
			log().debug(
					"createDefinition: filename [" + fileName
							+ "] already exists returning null as definition");
            return null;
        }

        StringBuffer parameter = new StringBuffer();

        parameter.append(" --start=" + (System.currentTimeMillis() / 1000L - 10L));

        parameter.append(" --step=" + step);
        
        for (RrdDataSource dataSource : dataSources) {
        	parameter.append(" DS:");
        	parameter.append(dataSource.getName()).append(':');
        	parameter.append(dataSource.getType()).append(":");
        	parameter.append(dataSource.getHeartBeat()).append(':');
        	parameter.append(dataSource.getMin()).append(':');
        	parameter.append(dataSource.getMax());
        }


        for (String rra : rraList) {
        	parameter.append(' ');
        	parameter.append(rra);
        }

        return new CreateCommand(fileName, parameter.toString());
    }


    /**
     * Creates a the rrd file from the rrdDefinition. Since this definition is
     * really just the create command string it just executes it.
     *
     * @param createCommand a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
        @Override
    public void createFile(CreateCommand createCommand, Map<String, String> attributeMappings) throws Exception {
        if (createCommand == null) {
        	log().debug("createRRD: skipping RRD file");
        	return;
        }
        log().debug("Executing: rrdtool "+createCommand.toString());
        Interface.launch(createCommand.toString());
        
        String filenameWithoutExtension = createCommand.filename.replace(RrdUtils.getExtension(), "");
        int lastIndexOfSeparator = filenameWithoutExtension.lastIndexOf(File.separator);
        
		RrdUtils.createMetaDataFile(
				filenameWithoutExtension.substring(0, lastIndexOfSeparator),
				filenameWithoutExtension.substring(lastIndexOfSeparator),
				attributeMappings);
    }

    /**
     * {@inheritDoc}
     *
     * The 'opens' the given rrd file. In actuality since the JNI interface does
     * not provide files that may be open, this constructs the beginning portion
     * of the rrd command to update the file.
     */
        @Override
    public StringBuffer openFile(String fileName) throws Exception {
        return new StringBuffer("update " + fileName);
    }

    /**
     * {@inheritDoc}
     *
     * This 'updates' the given rrd file by providing data. Since the JNI
     * interface does not provide files that can be open, this just appends the
     * data to the command string constructed so far. The data is not
     * immediately written to the file since this would eliminate the
     * possibility of getting performance benefit by doing more than one write
     * per open. The updates are all performed at once in the closeFile method.
     */
        @Override
    public void updateFile(StringBuffer rrd, String owner, String data) throws Exception {
        rrd.append(' ');
        rrd.append(data);
    }

    /**
     * Initialized the JNI Interface
     *
     * @throws java.lang.Exception if any.
     */
    public JniRrdStrategy() throws Exception {
        Interface.init();
    }

    /**
     * {@inheritDoc}
     *
     * Fetches the last value directly from the rrd file using the JNI
     * Interface.
     */
        @Override
    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException, RrdException {
        return fetchLastValue(rrdFile, ds, "AVERAGE", interval);
    }

    /** {@inheritDoc} */
        @Override
    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) {
        /*
         * Generate rrd_fetch() command through jrrd JNI interface in order to
         * retrieve LAST pdp for the datasource stored in the specified RRD
         * file.
         *
         * String array returned from launch() native method format:
         *      String[0] - If success is null, otherwise contains reason
         *                  for failure
         *      String[1] - All data source names contained in the RRD (space
         *                  delimited)
         *      String[2 ... n] - RRD fetch data in the following format:
         *                      <timestamp> <value1> <value2> ... <valueX>
         *                  X is the total number of data sources.
         *
         * NOTE: Specifying start time of 'now-<interval>' and end time of
         * 'now-<interval>' where <interval> is the configured thresholding
         * interval (and should be the same as the RRD step size) in order to
         * guarantee that we don't get a 'NaN' value from the fetch command.
         * This is necessary because the collection is being done by collectd at
         * effectively random times and there is nothing keeping us in sync.
         * 
         * interval argument is in milliseconds so must convert to seconds
         */
        
        // TODO: Combine fetchLastValueInRange and fetchLastValue
        String fetchCmd = "fetch " + rrdFile + " "+consolidationFunction+" -s now-" + interval / 1000 + " -e now-" + interval / 1000;

        if (log().isDebugEnabled()) {
            log().debug("fetch: Issuing RRD command: " + fetchCmd);
        }

        String[] fetchStrings = Interface.launch(fetchCmd);

        // Sanity check the returned string array
        if (fetchStrings == null) {
            log().error("fetch: Unexpected error issuing RRD 'fetch' command, no error text available.");
            return null;
        }

        // Check error string at index 0, will be null if 'fetch' was successful
        if (fetchStrings[0] != null) {
            log().error("fetch: RRD database 'fetch' failed, reason: " + fetchStrings[0]);
            return null;
        }

        // Sanity check
        if (fetchStrings[1] == null || fetchStrings[2] == null) {
            log().error("fetch: RRD database 'fetch' failed, no data retrieved.");
            return null;
        }

        // String at index 1 contains the RRDs datasource names
        //
        String[] dsNames = fetchStrings[1].split("\\s");
        int dsIndex = 0;
        for (int i = 0; i < dsNames.length; i++) {
        	if (dsNames[i].equals(ds)) dsIndex = i;
        }
        String dsName = dsNames[dsIndex].trim();

        // String at index 2 contains fetched values for the current time
        // Convert value string into a Double
        //
        String[] dsValues = fetchStrings[2].split("\\s");
        Double dsValue = null;
        if (dsValues[dsIndex].trim().equalsIgnoreCase("nan")) {
            dsValue = new Double(Double.NaN);
        } else {
            try {
                dsValue = new Double(dsValues[dsIndex].trim());
            } catch (NumberFormatException nfe) {
                log().warn("fetch: Unable to convert fetched value (" + dsValues[dsIndex].trim() + ") to Double for data source " + dsName);
                throw nfe;
            }
        }

        if (log().isDebugEnabled()) {
            log().debug("fetch: fetch successful: " + dsName + "= " + dsValue);
        }

        return dsValue;
    }

    /** {@inheritDoc} */
        @Override
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException, RrdException {
        // Generate rrd_fetch() command through jrrd JNI interface in order to
        // retrieve
        // LAST pdp for the datasource stored in the specified RRD file
        //
        // String array returned from launch() native method format:
        // String[0] - If success is null, otherwise contains reason for failure
        // String[1] - All data source names contained in the RRD (space
        // delimited)
        // String[2]...String[n] - RRD fetch data in the following format:
        // <timestamp> <value1> <value2> ... <valueX> where X is
        // the total number of data sources
        //
        // NOTE: Specifying start time of 'now-<interval>' and
        // end time of 'now-<interval>' where <interval> is the
        // configured thresholding interval (and should be the
        // same as the RRD step size) in order to guarantee that
        // we don't get a 'NaN' value from the fetch command. This
        // is necessary because the collection is being done by collectd
        // and there is nothing keeping us in sync.
        // 
        // interval argument is in milliseconds so must convert to seconds
        //
        
        // TODO: Combine fetchLastValueInRange and fetchLastValue
        
    	long now = System.currentTimeMillis();
        long latestUpdateTime = (now - (now % interval)) / 1000L;
        long earliestUpdateTime = ((now - (now % interval)) - range) / 1000L;
        
        if (log().isDebugEnabled()) {
        	log().debug("fetchInRange: fetching data from " + earliestUpdateTime + " to " + latestUpdateTime);
        }
        
        String fetchCmd = "fetch " + rrdFile + " AVERAGE -s " + earliestUpdateTime + " -e " + latestUpdateTime;

        String[] fetchStrings = Interface.launch(fetchCmd);

        // Sanity check the returned string array
        if (fetchStrings == null) {
            log().error("fetchInRange: Unexpected error issuing RRD 'fetch' command, no error text available.");
            return null;
        }

        // Check error string at index 0, will be null if 'fetch' was successful
        if (fetchStrings[0] != null) {
            log().error("fetchInRange: RRD database 'fetch' failed, reason: " + fetchStrings[0]);
            return null;
        }

        // Sanity check
        if (fetchStrings[1] == null || fetchStrings[2] == null) {
            log().error("fetchInRange: RRD database 'fetch' failed, no data retrieved.");
            return null;
        }
        
        int numFetched = fetchStrings.length;
        
        if (log().isDebugEnabled()) {
        	log().debug("fetchInRange: got " + numFetched + " strings from RRD");
        }

        // String at index 1 contains the RRDs datasource names
        //
        String[] dsNames = fetchStrings[1].split("\\s");
        int dsIndex = 0;
        for (int i = 0; i < dsNames.length; i++) {
        	if (dsNames[i].equals(ds)) dsIndex = i;
        }
        String dsName = dsNames[dsIndex].trim();

        Double dsValue;

        // Back through the RRD output until I get something interesting
        
        for(int i = fetchStrings.length - 2; i > 1; i--) {
            String[] dsValues = fetchStrings[i].split("\\s");
        	if ( dsValues[dsIndex].trim().equalsIgnoreCase("nan") ) {
        	    log().debug("fetchInRange: Got a NaN value - continuing back in time");
        	} else {
        		try {
                    dsValue = new Double(dsValues[dsIndex].trim());
                    if (log().isDebugEnabled()) {
                        log().debug("fetchInRange: fetch successful: " + dsName + "= " + dsValue);
                    }
                    return dsValue;
                } catch (NumberFormatException nfe) {
                    log().warn("fetchInRange: Unable to convert fetched value (" + dsValues[dsIndex].trim() + ") to Double for data source " + dsName);
                    throw nfe;
                }
          	}
        }
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     *
     * Executes the given graph command as process with workDir as the current
     * directory. The output stream of the command (a PNG image) is copied to a
     * the InputStream returned from the method.
     */
        @Override
    public InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        byte[] byteArray = createGraphAsByteArray(command, workDir);
        return new ByteArrayInputStream(byteArray);
    }

    private byte[] createGraphAsByteArray(String command, File workDir) throws IOException, RrdException {
        String[] commandArray = StringUtils.createCommandArray(command, '@');
        Process process;
        try {
             process = Runtime.getRuntime().exec(commandArray, null, workDir);
        } catch (IOException e) {
            IOException newE = new IOException("IOException thrown while executing command '" + command + "' in " + workDir.getAbsolutePath() + ": " + e);
            newE.initCause(e);
            throw newE;
        }
        
        // this closes the stream when its finished
        byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());
        
        // this close the stream when its finished
        String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
        
        // one particular warning message that originates in libart should be ignored
        if (errors.length() > 0 && errors.contains(IGNORABLE_LIBART_WARNING_STRING)) {
        	log().debug("Ignoring libart warning message in rrdtool stderr stream: " + IGNORABLE_LIBART_WARNING_STRING);
        	errors = errors.replaceAll(IGNORABLE_LIBART_WARNING_REGEX, "");
        }
        if (errors.length() > 0) {
            throw new RrdException(errors);
        }
        return byteArray;
    }

    /**
     * No stats are kept for this implementation.
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getStats() {
        return "";
    }
    
    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    private final ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    // These offsets work perfectly for ranger@ with rrdtool 1.2.23 and Firefox
    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
        @Override
    public int getGraphLeftOffset() {
        return 65;
    }
    
    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
        @Override
    public int getGraphRightOffset() {
        return -30;
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
        @Override
    public int getGraphTopOffsetWithText() {
        return -75;
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getDefaultFileExtension() {
        return ".rrd";
    }
    
    /** {@inheritDoc} */
        @Override
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException, org.opennms.netmgt.rrd.RrdException {
        // Creating Temp PNG File
        File pngFile = File.createTempFile("opennms.rrdtool.", ".png");
        command = command.replaceFirst("graph - ", "graph " + pngFile.getAbsolutePath() + " ");

        int width;
        int height;
        String[] printLines;
        InputStream pngStream;

        try {
            // Executing RRD Command
            InputStream is = createGraph(command, workDir);
            
            // Processing Command Output
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            try {
                String s[] = reader.readLine().split("x");
                width = Integer.parseInt(s[0]);
                height = Integer.parseInt(s[1]);
                
                List<String> printLinesList = new ArrayList<String>();
                
                String line = null;
                while ((line = reader.readLine()) != null) {
                    printLinesList.add(line);
                }
                
                printLines = printLinesList.toArray(new String[printLinesList.size()]);

            } finally {
                reader.close();
            }

            // Creating PNG InputStream
            byte[] byteArray = FileCopyUtils.copyToByteArray(pngFile);
            pngStream = new ByteArrayInputStream(byteArray);
        } catch (Throwable e) {
            throw new RrdException("Can't execute command " + command, e);
        } finally {
            pngFile.delete();
        }

        // Creating Graph Details
        RrdGraphDetails details = new JniGraphDetails(width, height, printLines, pngStream);
        return details;
    }

    /** {@inheritDoc} */
        @Override
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // no need to do anything since this strategy doesn't queue
    }
}
