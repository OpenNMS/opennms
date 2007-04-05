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
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultRrdDao implements RrdDao, InitializingBean {
    private RrdStrategy m_rrdStrategy;
    private File m_rrdBaseDirectory;
    private String m_rrdBinaryPath;

    public double getPrintValue(OnmsAttribute attribute, String cf, long start, long end) {
        Assert.notNull(attribute, "attribute argument must not be null");
        Assert.notNull(cf, "cf argument must not be null");
        Assert.isTrue(end > start, "end argument must be after start argument");
        Assert.isAssignable(attribute.getClass(), RrdGraphAttribute.class, "attribute argument must be assignable to RrdGraphAttribute");
        
        RrdGraphAttribute rrdAttribute = (RrdGraphAttribute) attribute;
        
        String[] command = new String[] {
                m_rrdBinaryPath,
                "graph",
                "-",
                "--start=" + (start / 1000),
                "--end=" + (end / 1000),
                "DEF:ds=" + rrdAttribute.getRrdRelativePath() + ":" + attribute.getName() + ":" + cf,
                "PRINT:ds:" + cf + ":\"%le\""
        };
        String commandString = StringUtils.arrayToDelimitedString(command, " ");

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
            throw new DataAccessResourceFailureException("Failure to get print lines from grpah after graphing with command '" + commandString + "'", e);
        }
      
        if (printLines.length != 1) {
            throw new ObjectRetrievalFailureException("Returned number of print lines should be 1, but was " + printLines.length, commandString);
        }

        return Double.parseDouble(printLines[0]);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_rrdStrategy != null, "property rrdStrategy must be set and be non-null");
        Assert.state(m_rrdBaseDirectory != null, "property rrdBaseDirectory must be set and be non-null");
        Assert.state(m_rrdBinaryPath != null, "property rrdBinaryPath must be set and be non-null");
    }

    public RrdStrategy getRrdStrategy() {
        return m_rrdStrategy;
    }

    public void setRrdStrategy(RrdStrategy rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }

    public File getRrdBaseDirectory() {
        return m_rrdBaseDirectory;
    }

    public void setRrdBaseDirectory(File rrdBaseDirectory) {
        m_rrdBaseDirectory = rrdBaseDirectory;
    }

    public String getRrdBinaryPath() {
        return m_rrdBinaryPath;
    }

    public void setRrdBinaryPath(String rrdBinaryPath) {
        m_rrdBinaryPath = rrdBinaryPath;
    }

    /**
     * Create an RRD graph.
     * 
     * @see org.opennms.netmgt.dao.RrdDao#createGraph(java.lang.String, java.io.File)
     * @throws DataRetrievalFailureException if the graph could not be created
     */
    public InputStream createGraph(String command, File workDir) throws DataRetrievalFailureException {
       try {
           return m_rrdStrategy.createGraph(command, workDir);
       } catch (Exception e) {
           throw new DataRetrievalFailureException("Could not create graph: " + e, e);
       }
    }

    /**
     * @see org.opennms.netmgt.dao.RrdDao#getGraphTopOffsetWithText()
     */
    public int getGraphTopOffsetWithText() {
        return m_rrdStrategy.getGraphTopOffsetWithText();
    }

    /**
     * @see org.opennms.netmgt.dao.RrdDao#setGraphRightOffset()
     */
    public int setGraphRightOffset() {
        return m_rrdStrategy.getGraphRightOffset();
    }
}
