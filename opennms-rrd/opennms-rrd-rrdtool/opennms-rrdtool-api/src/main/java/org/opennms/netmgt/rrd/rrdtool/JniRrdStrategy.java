/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd.rrdtool;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdMetaDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an rrdtool based implementation of RrdStrategy. It uses the existing
 * JNI based single-threaded interface to write the rrdtool compatible RRD
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
public class JniRrdStrategy extends AbstractJniRrdStrategy<JniRrdStrategy.CreateCommand ,StringBuffer> {
    private static final Logger LOG = LoggerFactory.getLogger(JniRrdStrategy.class);

    private Properties m_configurationProperties;
    
    public static class CreateCommand {
    	
    	private static final String OPERATION = "create";
    	String filename;
    	String parameter;
    	
		public CreateCommand(String filename, String parameter) {
			super();
			this.filename = filename;
			this.parameter = parameter;
		}
		
            @Override
		public String toString() {
			return OPERATION + " " + filename + " " + parameter;
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
        if (!f.exists()) {
            if (!f.mkdirs()) {
        	       LOG.warn("Could not make directory: {}", f.getPath());
            }
        }

        String fileName = directory + File.separator + rrdName + getDefaultFileExtension();

        if (new File(fileName).exists()) {
            LOG.debug("createDefinition: filename [{}] already exists returning null as definition", fileName);
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
     * @param createCommand a {@link String} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public void createFile(CreateCommand createCommand) throws Exception {
        if (createCommand == null) {
            LOG.debug("createRRD: skipping RRD file");
            return;
        }
        LOG.debug("Executing: rrdtool {}", createCommand.toString());
        Interface.launch(createCommand.toString());
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

        LOG.debug("fetch: Issuing RRD command: {}", fetchCmd);

        String[] fetchStrings = Interface.launch(fetchCmd);

        // Sanity check the returned string array
        if (fetchStrings == null) {
            LOG.error("fetch: Unexpected error issuing RRD 'fetch' command, no error text available.");
            return null;
        }

        // Check error string at index 0, will be null if 'fetch' was successful
        if (fetchStrings[0] != null) {
            LOG.error("fetch: RRD database 'fetch' failed, reason: {}", fetchStrings[0]);
            return null;
        }

        // Sanity check
        if (fetchStrings[1] == null || fetchStrings[2] == null) {
            LOG.error("fetch: RRD database 'fetch' failed, no data retrieved.");
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
        if (dsValues[dsIndex].trim().toLowerCase().endsWith("nan")) {
            dsValue = new Double(Double.NaN);
        } else {
            try {
                dsValue = new Double(dsValues[dsIndex].trim());
            } catch (NumberFormatException nfe) {
                LOG.warn("fetch: Unable to convert fetched value ({}) to Double for data source {}", dsValues[dsIndex].trim(), dsName);
                throw nfe;
            }
        }

        LOG.debug("fetch: fetch successful: {}={}", dsName, dsValue);

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
        
        LOG.debug("fetchInRange: fetching data from {} to {}", earliestUpdateTime, latestUpdateTime);
        
        String fetchCmd = "fetch " + rrdFile + " AVERAGE -s " + earliestUpdateTime + " -e " + latestUpdateTime;

        String[] fetchStrings = Interface.launch(fetchCmd);

        // Sanity check the returned string array
        if (fetchStrings == null) {
            LOG.error("fetchInRange: Unexpected error issuing RRD 'fetch' command, no error text available.");
            return null;
        }

        // Check error string at index 0, will be null if 'fetch' was successful
        if (fetchStrings[0] != null) {
            LOG.error("fetchInRange: RRD database 'fetch' failed, reason: {}", fetchStrings[0]);
            return null;
        }

        // Sanity check
        if (fetchStrings[1] == null || fetchStrings[2] == null) {
            LOG.error("fetchInRange: RRD database 'fetch' failed, no data retrieved.");
            return null;
        }
        
        int numFetched = fetchStrings.length;
        
        LOG.debug("fetchInRange: got {} strings from RRD", numFetched);

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
            if (dsValues[dsIndex].trim().toLowerCase().endsWith("nan")) {
        	    LOG.debug("fetchInRange: Got a NaN value - continuing back in time");
        	} else {
        		try {
                    dsValue = new Double(dsValues[dsIndex].trim());
                    LOG.debug("fetchInRange: fetch successful: {}= {}", dsName, dsValue);
                    return dsValue;
                } catch (NumberFormatException nfe) {
                    LOG.warn("fetchInRange: Unable to convert fetched value ({}) to Double for data source {}", dsValues[dsIndex].trim(), dsName);
                    throw nfe;
                }
          	}
        }
        
        return null;
    }

}
