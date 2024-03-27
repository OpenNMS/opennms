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
package org.opennms.netmgt.rrd.jrobin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jrobin.graph.RrdGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @version $Id: $
 */
public class JRobinRrdGraphDetails implements RrdGraphDetails {
    private static final Logger LOG = LoggerFactory.getLogger(JRobinRrdGraphDetails.class);
    
    private RrdGraph m_rrdGraph;
    private String m_graphCommand;

    /**
     * <p>Constructor for JRobinRrdGraphDetails.</p>
     *
     * @param rrdGraph a {@link org.jrobin.graph.RrdGraph} object.
     * @param graphCommand a {@link java.lang.String} object.
     */
    public JRobinRrdGraphDetails(RrdGraph rrdGraph, String graphCommand) {
        m_rrdGraph = rrdGraph;
        m_graphCommand = graphCommand;
    }
    
    /**
     * <p>getRrdGraph</p>
     *
     * @return a {@link org.jrobin.graph.RrdGraph} object.
     */
    public RrdGraph getRrdGraph() {
        return m_rrdGraph;
    }
    
    /**
     * <p>getGraphCommand</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGraphCommand() {
        return m_graphCommand;
    }
    
    /**
     * <p>getInputStream</p>
     *
     * @return a {@link java.io.InputStream} object.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    @Override
    public InputStream getInputStream() throws RrdException {
        assertGraphProduced();

        return new ByteArrayInputStream(m_rrdGraph.getRrdGraphInfo().getBytes());
    }

    /**
     * <p>getPrintLines</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    @Override
    public String[] getPrintLines() {
        return m_rrdGraph.getRrdGraphInfo().getPrintLines();
    }

    /**
     * <p>getHeight</p>
     *
     * @return a int.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    @Override
    public int getHeight() throws RrdException {
        assertGraphProduced();
        
        return m_rrdGraph.getRrdGraphInfo().getHeight();
    }

    /**
     * <p>getWidth</p>
     *
     * @return a int.
     * @throws org.opennms.netmgt.rrd.RrdException if any.
     */
    @Override
    public int getWidth() throws RrdException {
        assertGraphProduced();
        
        return m_rrdGraph.getRrdGraphInfo().getWidth();
    }

    private void assertGraphProduced() throws RrdException {
        if (m_rrdGraph.getRrdGraphInfo().getBytes() == null) {
            String message = "no graph was produced by JRobin for command '" + getGraphCommand() + "'.  Does the command have any drawing commands (e.g.: LINE1, LINE2, LINE3, AREA, STACK, GPRINT)?";
            LOG.error(message);
            throw new RrdException(message);
        }
    }
}
