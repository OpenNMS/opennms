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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.MoreObjects;

/**
 * <p>FlapCount class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class FlapCount implements Serializable {
    private static final long serialVersionUID = 8550223065319878158L;

    private static final Logger LOG = LoggerFactory.getLogger(FlapCount.class);
    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    Integer m_locationMonitor;
    boolean m_alerted;

    Integer m_count;
    
    /**
     * <p>Constructor for FlapCount.</p>
     *
     * @param nodeid a {@link java.lang.Long} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param locationMonitor a {@link java.lang.Integer} object.
     */
    public FlapCount(final Long nodeid, final String ipAddr, final String svcName, final Integer locationMonitor) {
        m_nodeid = nodeid;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_locationMonitor = locationMonitor;
        m_count = 1;
        m_alerted = false;
        
        LOG.debug("FlapCount created.");
    }
    
    /**
     * <p>increment</p>
     */
    public void increment() {
        m_count += 1;
        LOG.debug("FlapCount incremented ({}).", m_count);
    }
    
    /**
     * <p>decrement</p>
     */
    public void decrement() {
        m_count -= 1;
        LOG.debug("FlapCount decremented ({}).", m_count);
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getCount() {
        return m_count;
    }

    /**
     * <p>setCount</p>
     *
     * @param count a {@link java.lang.Integer} object.
     */
    public void setCount(final Integer count) {
        m_count = count;
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
     * <p>isAlerted</p>
     *
     * @return a boolean.
     */
    public boolean isAlerted() {
        return m_alerted;
    }

    /**
     * <p>setAlerted</p>
     *
     * @param alerted a boolean.
     */
    public void setAlerted(final boolean alerted) {
        m_alerted = alerted;
    }

    /**
     * <p>getLocationMonitor</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLocationMonitor() {
        return m_locationMonitor;
    }

    /**
     * <p>setLocationMonitor</p>
     *
     * @param locationMonitor a {@link java.lang.Integer} object.
     */
    public void setLocationMonitor(final Integer locationMonitor) {
        m_locationMonitor = locationMonitor;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
		.add("nodeid", m_nodeid)
		.add("ipAddr", m_ipAddr)
		.add("svcName", m_svcName)
		.add("locMon", m_locationMonitor)
		.add("count", m_count)
		.toString();
    }
}
