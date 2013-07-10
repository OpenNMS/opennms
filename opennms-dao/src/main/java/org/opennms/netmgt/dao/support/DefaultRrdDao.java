/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.InputStream;

import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>DefaultRrdDao class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DefaultRrdDao implements RrdDao, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRrdDao.class);
    private RrdStrategy<?, ?> m_rrdStrategy;
    private File m_rrdBaseDirectory;
    private String m_rrdBinaryPath;

    /** {@inheritDoc} */
    @Override
    public double getPrintValue(OnmsAttribute attribute, String cf, long start, long end) {
        return getPrintValues(attribute, cf, start, end)[0];
    }

    /**
     * <p>getPrintValues</p>
     *
     * @param attribute a {@link org.opennms.netmgt.model.OnmsAttribute} object.
     * @param rraConsolidationFunction a {@link java.lang.String} object.
     * @param startTimeInMillis a long.
     * @param endTimeInMillis a long.
     * @param printFunctions a {@link java.lang.String} object.
     * @return an array of double.
     */
    @Override
    public double[] getPrintValues(OnmsAttribute attribute, String rraConsolidationFunction, long startTimeInMillis, long endTimeInMillis, String... printFunctions) {
        Assert.notNull(attribute, "attribute argument must not be null");
        Assert.notNull(rraConsolidationFunction, "rraConsolicationFunction argument must not be null");
        Assert.isTrue(endTimeInMillis > startTimeInMillis, "end argument must be after start argument");
        Assert.isAssignable(attribute.getClass(), RrdGraphAttribute.class, "attribute argument must be assignable to RrdGraphAttribute");

        // if no printFunctions are given just use the rraConsolidationFunction
        if (printFunctions.length < 1) {
            printFunctions = new String[] { rraConsolidationFunction };
        }
        
        RrdGraphAttribute rrdAttribute = (RrdGraphAttribute) attribute;
        
        String[] command = new String[] {
                m_rrdBinaryPath,
                "graph",
                "-",
                "--start=" + (startTimeInMillis / 1000),
                "--end=" + (endTimeInMillis / 1000),
                "DEF:ds=" + RrdFileConstants.escapeForGraphing(rrdAttribute.getRrdRelativePath()) + ":" + attribute.getName() + ":" + rraConsolidationFunction,
        };
        
        String[] printDefs = new String[printFunctions.length];
        for (int i = 0; i < printFunctions.length; i++) {
            printDefs[i] = "PRINT:ds:" + printFunctions[i] + ":\"%le\""; 
        }
        
        String commandString = StringUtils.arrayToDelimitedString(command, " ") + ' ' + StringUtils.arrayToDelimitedString(printDefs, " ");

        LOG.debug("commandString: {}", commandString);
        RrdGraphDetails graphDetails;
        try {
            graphDetails = m_rrdStrategy.createGraphReturnDetails(commandString, m_rrdBaseDirectory);
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Failure when generating graph to get data with command '" + commandString + "'", e);
        }
        
        String[] printLines;
        try {
            printLines = graphDetails.getPrintLines();
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Failure to get print lines from graph after graphing with command '" + commandString + "'", e);
        }
      
        if (printLines.length != printFunctions.length) {
            throw new DataAccessResourceFailureException("Returned number of print lines should be "+printFunctions.length+", but was " + printLines.length + " from command: " + commandString);
        }

        double[] values = new double[printLines.length];
        
        for (int i = 0; i < printLines.length; i++) {
            if (printLines[i].endsWith("nan")) {
                values[i] = Double.NaN;
            } else {
                try {
                    values[i] = Double.parseDouble(printLines[i]);
                } catch (NumberFormatException e) {
                    throw new DataAccessResourceFailureException("Value of line " + (i + 1) + " of output from RRD is not a valid floating point number: '" + printLines[i] + "'");
                }
            }
        }
        
        return values;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_rrdStrategy != null, "property rrdStrategy must be set and be non-null");
        Assert.state(m_rrdBaseDirectory != null, "property rrdBaseDirectory must be set and be non-null");
        Assert.state(m_rrdBinaryPath != null, "property rrdBinaryPath must be set and be non-null");
    }

    /**
     * <p>getRrdStrategy</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public RrdStrategy<?, ?> getRrdStrategy() {
        return m_rrdStrategy;
    }

    /**
     * <p>setRrdStrategy</p>
     *
     * @param rrdStrategy a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public void setRrdStrategy(RrdStrategy<?, ?> rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }

    /**
     * <p>getRrdBaseDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getRrdBaseDirectory() {
        return m_rrdBaseDirectory;
    }

    /**
     * <p>setRrdBaseDirectory</p>
     *
     * @param rrdBaseDirectory a {@link java.io.File} object.
     */
    public void setRrdBaseDirectory(File rrdBaseDirectory) {
        m_rrdBaseDirectory = rrdBaseDirectory;
    }

    /**
     * <p>getRrdBinaryPath</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRrdBinaryPath() {
        return m_rrdBinaryPath;
    }

    /**
     * <p>setRrdBinaryPath</p>
     *
     * @param rrdBinaryPath a {@link java.lang.String} object.
     */
    public void setRrdBinaryPath(String rrdBinaryPath) {
        m_rrdBinaryPath = rrdBinaryPath;
    }

    /**
     * {@inheritDoc}
     *
     * Create an RRD graph.
     * @see org.opennms.netmgt.dao.api.RrdDao#createGraph(java.lang.String, java.io.File)
     */
    @Override
    public InputStream createGraph(String command, File workDir) throws DataRetrievalFailureException {
       try {
           return m_rrdStrategy.createGraph(command, workDir);
       } catch (Throwable e) {
           throw new DataRetrievalFailureException("Could not create graph: " + e, e);
       }
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @see org.opennms.netmgt.dao.api.RrdDao#getGraphTopOffsetWithText()
     * @return a int.
     */
    @Override
    public int getGraphTopOffsetWithText() {
        return m_rrdStrategy.getGraphTopOffsetWithText();
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @see org.opennms.netmgt.dao.api.RrdDao#getGraphLeftOffset()
     * @return a int.
     */
    @Override
    public int getGraphLeftOffset() {
        return m_rrdStrategy.getGraphLeftOffset();
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @see org.opennms.netmgt.dao.api.RrdDao#getGraphRightOffset()
     * @return a int.
     */
    @Override
    public int getGraphRightOffset() {
        return m_rrdStrategy.getGraphRightOffset();
    }

    /** {@inheritDoc} */
    @Override
    public Double getLastFetchValue(OnmsAttribute attribute, int interval) throws DataAccessResourceFailureException {
        Assert.notNull(attribute, "attribute argument must not be null");
        Assert.isTrue(interval > 0, "interval argument must be greater than zero");
        Assert.isAssignable(attribute.getClass(), RrdGraphAttribute.class, "attribute argument must be assignable to RrdGraphAttribute");
        
        RrdGraphAttribute rrdAttribute = (RrdGraphAttribute) attribute;

        File rrdFile = new File(m_rrdBaseDirectory, rrdAttribute.getRrdRelativePath());
        try {
            return m_rrdStrategy.fetchLastValue(rrdFile.getAbsolutePath(), attribute.getName(), interval);
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Failure to fetch last value from file '" + rrdFile + "' with interval " + interval, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Double getLastFetchValue(OnmsAttribute attribute, int interval, int range) throws DataAccessResourceFailureException {
        Assert.notNull(attribute, "attribute argument must not be null");
        Assert.isTrue(interval > 0, "interval argument must be greater than zero");
        Assert.isTrue(range > 0, "range argument must be greater than zero");
        Assert.isAssignable(attribute.getClass(), RrdGraphAttribute.class, "attribute argument must be assignable to RrdGraphAttribute");
        
        RrdGraphAttribute rrdAttribute = (RrdGraphAttribute) attribute;

        File rrdFile = new File(m_rrdBaseDirectory, rrdAttribute.getRrdRelativePath());
        try {
            return m_rrdStrategy.fetchLastValueInRange(rrdFile.getAbsolutePath(), attribute.getName(), interval, range);
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Failure to fetch last value from file '" + rrdFile + "' with interval " + interval + " and range " + range, e);
        }
    }
}
