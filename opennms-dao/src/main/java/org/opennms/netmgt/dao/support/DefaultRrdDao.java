/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jan 10: Improve error checking on print lines in getPrintValues.
 *              - dj@opennms.org
 * 2007 May 16: Added fetch methods. - dj@opennms.org
 * 2007 Apr 05: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.InputStream;

import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
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
    private RrdStrategy m_rrdStrategy;
    private File m_rrdBaseDirectory;
    private String m_rrdBinaryPath;

    /** {@inheritDoc} */
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
                "DEF:ds=" + rrdAttribute.getRrdRelativePath() + ":" + attribute.getName() + ":" + rraConsolidationFunction,
        };
        
        String[] printDefs = new String[printFunctions.length];
        for (int i = 0; i < printFunctions.length; i++) {
            printDefs[i] = "PRINT:ds:" + printFunctions[i] + ":\"%le\""; 
        }
        
        String commandString = StringUtils.arrayToDelimitedString(command, " ") + ' ' + StringUtils.arrayToDelimitedString(printDefs, " ");

        RrdGraphDetails graphDetails;
        try {
            graphDetails = m_rrdStrategy.createGraphReturnDetails(commandString, m_rrdBaseDirectory);
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Failure when generating graph to get data with command '" + commandString + "'", e);
        }
        
        String[] printLines;
        try {
            printLines = graphDetails.getPrintLines();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Failure to get print lines from graph after graphing with command '" + commandString + "'", e);
        }
      
        if (printLines.length != printFunctions.length) {
            throw new DataAccessResourceFailureException("Returned number of print lines should be "+printFunctions.length+", but was " + printLines.length + " from command: " + commandString);
        }

        double[] values = new double[printLines.length];
        
        for (int i = 0; i < printLines.length; i++) {
            if ("nan".equals(printLines[i])) {
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
    public RrdStrategy getRrdStrategy() {
        return m_rrdStrategy;
    }

    /**
     * <p>setRrdStrategy</p>
     *
     * @param rrdStrategy a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public void setRrdStrategy(RrdStrategy rrdStrategy) {
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
     * @see org.opennms.netmgt.dao.RrdDao#createGraph(java.lang.String, java.io.File)
     */
    public InputStream createGraph(String command, File workDir) throws DataRetrievalFailureException {
       try {
           return m_rrdStrategy.createGraph(command, workDir);
       } catch (Exception e) {
           throw new DataRetrievalFailureException("Could not create graph: " + e, e);
       }
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @see org.opennms.netmgt.dao.RrdDao#getGraphTopOffsetWithText()
     * @return a int.
     */
    public int getGraphTopOffsetWithText() {
        return m_rrdStrategy.getGraphTopOffsetWithText();
    }

    /**
     * <p>getGraphLeftOffset</p>
     *
     * @see org.opennms.netmgt.dao.RrdDao#getGraphLeftOffset()
     * @return a int.
     */
    public int getGraphLeftOffset() {
        return m_rrdStrategy.getGraphLeftOffset();
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @see org.opennms.netmgt.dao.RrdDao#getGraphRightOffset()
     * @return a int.
     */
    public int getGraphRightOffset() {
        return m_rrdStrategy.getGraphRightOffset();
    }

    /** {@inheritDoc} */
    public Double getLastFetchValue(OnmsAttribute attribute, int interval) throws DataAccessResourceFailureException {
        Assert.notNull(attribute, "attribute argument must not be null");
        Assert.isTrue(interval > 0, "interval argument must be greater than zero");
        Assert.isAssignable(attribute.getClass(), RrdGraphAttribute.class, "attribute argument must be assignable to RrdGraphAttribute");
        
        RrdGraphAttribute rrdAttribute = (RrdGraphAttribute) attribute;

        File rrdFile = new File(m_rrdBaseDirectory, rrdAttribute.getRrdRelativePath());
        try {
            return m_rrdStrategy.fetchLastValue(rrdFile.getAbsolutePath(), attribute.getName(), interval);
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Failure to fetch last value from file '" + rrdFile + "' with interval " + interval, e);
        }
    }

    /** {@inheritDoc} */
    public Double getLastFetchValue(OnmsAttribute attribute, int interval, int range) throws DataAccessResourceFailureException {
        Assert.notNull(attribute, "attribute argument must not be null");
        Assert.isTrue(interval > 0, "interval argument must be greater than zero");
        Assert.isTrue(range > 0, "range argument must be greater than zero");
        Assert.isAssignable(attribute.getClass(), RrdGraphAttribute.class, "attribute argument must be assignable to RrdGraphAttribute");
        
        RrdGraphAttribute rrdAttribute = (RrdGraphAttribute) attribute;

        File rrdFile = new File(m_rrdBaseDirectory, rrdAttribute.getRrdRelativePath());
        try {
            return m_rrdStrategy.fetchLastValueInRange(rrdFile.getAbsolutePath(), attribute.getName(), interval, range);
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Failure to fetch last value from file '" + rrdFile + "' with interval " + interval + " and range " + range, e);
        }
    }
}
