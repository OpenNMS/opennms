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
package org.opennms.netmgt.daemon;

/**
 * SimpleSpringContextJmxServiceDaemon
 *
 * @author brozow
 * @version $Id: $
 */
public class SimpleSpringContextJmxServiceDaemon extends
        AbstractSpringContextJmxServiceDaemon<SpringServiceDaemon> implements SimpleSpringContextJmxServiceDaemonMBean {
    
    private String m_loggingPrefix;
    private String m_springContext;
    /**
     * <p>getLoggingPrefix</p>
     *
     * @return the loggingPrefix
     */
    @Override
    public String getLoggingPrefix() {
        return m_loggingPrefix;
    }
    /** {@inheritDoc} */
    @Override
    public void setLoggingPrefix(String loggingPrefix) {
        m_loggingPrefix = loggingPrefix;
    }
    /**
     * <p>getSpringContext</p>
     *
     * @return the springContext
     */
    @Override
    public String getSpringContext() {
        return m_springContext;
    }
    /** {@inheritDoc} */
    @Override
    public void setSpringContext(String springContext) {
        m_springContext = springContext;
    }


}
