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
package org.opennms.netmgt.correlation.drools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Affliction class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Affliction implements Serializable {
    private static final long serialVersionUID = -6417353487212711644L;

    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    
    public static enum Type {
        UNDECIDED,
        ISOLATED,
        WIDE_SPREAD
    }
    
    private List<Integer> m_reporters = new ArrayList<>();
    private Type m_type  = Type.UNDECIDED;
    
    /**
     * <p>Constructor for Affliction.</p>
     *
     * @param nodeId a {@link java.lang.Long} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param reporter a {@link java.lang.Integer} object.
     */
    public Affliction(final Long nodeId, final String ipAddr, final String svcName, final Integer reporter) {
        m_nodeid = nodeId;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_reporters.add(reporter);
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    /**
     * <p>setIpAddr</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     */
    public void setIpAddr(final String ipAddr) {
        m_ipAddr = ipAddr;
    }

    /**
     * <p>getNodeid</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getNodeid() {
        return m_nodeid;
    }

    /**
     * <p>setNodeid</p>
     *
     * @param nodeid a {@link java.lang.Long} object.
     */
    public void setNodeid(final Long nodeid) {
        m_nodeid = nodeid;
    }

    /**
     * <p>getReporters</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Integer> getReporters() {
        return m_reporters;
    }

    /**
     * <p>setReporters</p>
     *
     * @param reporters a {@link java.util.List} object.
     */
    public void setReporters(final List<Integer> reporters) {
        m_reporters = reporters;
    }

    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSvcName() {
        return m_svcName;
    }

    /**
     * <p>setSvcName</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    public void setSvcName(final String svcName) {
        m_svcName = svcName;
    }
    
    /**
     * <p>getReporterCount</p>
     *
     * @return a int.
     */
    public int getReporterCount() {
        return m_reporters.size();
    }
    
    /**
     * <p>addReporter</p>
     *
     * @param reporter a {@link java.lang.Integer} object.
     */
    public void addReporter(final Integer reporter) {
        m_reporters.add( reporter );
    }
    
    /**
     * <p>removeReporter</p>
     *
     * @param reporter a {@link java.lang.Integer} object.
     */
    public void removeReporter(final Integer reporter) {
        m_reporters.remove(reporter);
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link org.opennms.netmgt.correlation.drools.Affliction.Type} object.
     */
    public Type getType() {
        return m_type;
    }
    
    /**
     * <p>setType</p>
     *
     * @param type a {@link org.opennms.netmgt.correlation.drools.Affliction.Type} object.
     */
    public void setType(final Type type) {
        m_type = type;
    }
    
}
