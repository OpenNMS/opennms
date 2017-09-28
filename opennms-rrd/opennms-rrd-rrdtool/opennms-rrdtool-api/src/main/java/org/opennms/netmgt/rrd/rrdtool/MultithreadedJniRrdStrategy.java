/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is Copyright (C) 2002-2015 The OpenNMS Group, Inc.  All rights
 * reserved.  OpenNMS(R) is a derivative work, containing both original code,
 * included code and modified code that was published under the GNU General
 * Public License.  Copyrights for modified and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License with the Classpath
 * Exception; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.rrd.rrdtool;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdMetaDataUtils;
import org.opennms.netmgt.rrd.jrrd2.api.FetchResults;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2;
import org.opennms.netmgt.rrd.jrrd2.api.JRrd2Exception;
import org.opennms.netmgt.rrd.jrrd2.impl.JRrd2Jni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A multi-threaded JNI-based RRD strategy using jrrd2.
 *
 * @author jwhite <jesse@opennms.com>
 */
public class MultithreadedJniRrdStrategy extends AbstractJniRrdStrategy<MultithreadedJniRrdStrategy.CreateCommand, MultithreadedJniRrdStrategy.UpdateCommand> {
    private static final Logger LOG = LoggerFactory.getLogger(MultithreadedJniRrdStrategy.class);

	private final JRrd2 jrrd2 = new JRrd2Jni();
	
    private Properties m_configurationProperties;

    public static class CreateCommand {
        private final String m_filename;
        private final long m_step;
        private final long m_start;
        private final String[] m_argv;

		public CreateCommand(String filename, long step, long start, String[] argv) {
		    m_filename = filename;
		    m_step = step;
		    m_start = start;
		    m_argv = argv;
		}

		public String getFilename() {
		    return m_filename;
		}

		public void execute(JRrd2 jrrd2) throws JRrd2Exception {
		    jrrd2.create(m_filename, m_step, m_start, m_argv);
		}

		public String toString() {
		    return String.format("Filename: %s, Argv: %s",
		            m_filename, Arrays.toString(m_argv));
		}
    }

    public static class UpdateCommand {
        private final String m_filename;
        private final List<String> m_arguments = new LinkedList<String>();

        public UpdateCommand(String filename) {
            m_filename = filename;
        }

        public void execute(JRrd2 jrrd2) throws JRrd2Exception {
            jrrd2.update(m_filename, null, m_arguments.toArray(new String[m_arguments.size()]));
        }

        public String toString() {
            return String.format("Filename: %s Arguments: %s",
                    m_filename, m_arguments);
        }

        public void append(String argument) {
            m_arguments.add(argument);
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
    public void closeFile(UpdateCommand update) throws Exception {
            update.execute(jrrd2);
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

        int k = 0;
        final String[] arguments = new String[dataSources.size() + rraList.size()];

        final long start = (System.currentTimeMillis() / 1000L - 10L);

        for (RrdDataSource dataSource : dataSources) {
            arguments[k++] = String.format("DS:%s:%s:%d:%s:%s",
                    dataSource.getName(), dataSource.getType(), dataSource.getHeartBeat(),
                    dataSource.getMin(), dataSource.getMax());
        }

        for (String rra : rraList) {
            arguments[k++] = rra;
        }

        return new CreateCommand(fileName, step, start, arguments);
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
        
        LOG.debug("Executing: rrdtool {}", createCommand);
        createCommand.execute(jrrd2);
    }

    /**
     * {@inheritDoc}
     *
     * The 'opens' the given rrd file. In actuality since the JNI interface does
     * not provide files that may be open, this constructs the beginning portion
     * of the rrd command to update the file.
     */
    @Override
    public UpdateCommand openFile(String fileName) throws Exception {
        return new UpdateCommand(fileName);
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
    public void updateFile(UpdateCommand update, String owner, String data) throws Exception {
        update.append(data);
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
        try {
            final long now = new Date().getTime();
            final long start = now - interval / 1000;
            final long end = now + interval / 1000;
            FetchResults results = jrrd2.fetch(rrdFile, consolidationFunction, start, end, 1);

            // Determine the index of the requested data-source
            final String[] dsNames = results.getColumns();
            int dsIndex = 0;
            for (int i = 0; i < dsNames.length; i++) {
                if (dsNames[i].equals(ds)) {
                    dsIndex = i;
                }
            }

            // Grab the last value from that column
            final double[][] dsValues = results.getValues();
            final int numRows = dsValues[dsIndex].length;

            return dsValues[dsIndex][numRows-1];
        } catch (JRrd2Exception e) {
            LOG.error("Fetch failed", e);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException, RrdException {
        try {
            final long now = System.currentTimeMillis();
            final long latestUpdateTime = (now - (now % interval)) / 1000L;
            final long earliestUpdateTime = ((now - (now % interval)) - range) / 1000L;
            FetchResults results = jrrd2.fetch(rrdFile, "AVERAGE", earliestUpdateTime, latestUpdateTime, 1);

            // Determine the index of the requested data-source
            final String[] dsNames = results.getColumns();
            int dsIndex = 0;
            for (int i = 0; i < dsNames.length; i++) {
                if (dsNames[i].equals(ds)) {
                    dsIndex = i;
                }
            }

            final double[][] dsValues = results.getValues();
            final int numRows = dsValues[dsIndex].length;

            for (int i = numRows - 1; i >= 0; i--) {
                final double value = dsValues[dsIndex][i];
                if (!Double.isNaN(value)) {
                    LOG.debug("fetchInRange: fetch successful: {}= {}", ds, value);
                    return value;
                } else {
                    LOG.debug("fetchInRange: Got a NaN value - continuing back in time");
                }
            }
            return dsValues[dsIndex][numRows-1];
        } catch (JRrd2Exception e) {
            LOG.error("Fetch failed", e);
        }

        return null;
    }
}
