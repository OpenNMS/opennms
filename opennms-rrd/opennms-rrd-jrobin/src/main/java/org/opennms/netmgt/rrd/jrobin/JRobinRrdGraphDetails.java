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
package org.opennms.netmgt.rrd.jrobin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Category;
import org.jrobin.graph.RrdGraph;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;

/**
 * Container for details from a JRobin RRD graph.  Stores the same details
 * as RrdGraphDetails, in addition to the JRobin RrdGraph object itself and
 * the graph command String used to generate the graph.  We keep the graph
 * command string around so we can generate a detailed error if
 * getInputStream() is called, but no graph was produced.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class JRobinRrdGraphDetails implements RrdGraphDetails {
    
    private RrdGraph m_rrdGraph;
    private String m_graphCommand;

    public JRobinRrdGraphDetails(RrdGraph rrdGraph, String graphCommand) {
        m_rrdGraph = rrdGraph;
        m_graphCommand = graphCommand;
    }
    
    public RrdGraph getRrdGraph() {
        return m_rrdGraph;
    }
    
    public String getGraphCommand() {
        return m_graphCommand;
    }
    
    public InputStream getInputStream() throws RrdException {
        assertGraphProduced();

        return new ByteArrayInputStream(m_rrdGraph.getRrdGraphInfo().getBytes());
    }

    public String[] getPrintLines() {
        return m_rrdGraph.getRrdGraphInfo().getPrintLines();
    }

    public int getHeight() throws RrdException {
        assertGraphProduced();
        
        return m_rrdGraph.getRrdGraphInfo().getHeight();
    }

    public int getWidth() throws RrdException {
        assertGraphProduced();
        
        return m_rrdGraph.getRrdGraphInfo().getWidth();
    }

    private void assertGraphProduced() throws RrdException {
        if (m_rrdGraph.getRrdGraphInfo().getBytes() == null) {
            String message = "no graph was produced by JRobin for command '" + getGraphCommand() + "'.  Does the command have any drawing commands (e.g.: LINE1, LINE2, LINE3, AREA, STACK, GPRINT)?";
            log().error(message);
            throw new RrdException(message);
        }
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
