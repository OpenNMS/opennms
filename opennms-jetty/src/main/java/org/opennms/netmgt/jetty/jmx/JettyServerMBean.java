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
package org.opennms.netmgt.jetty.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>JettyServerMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface JettyServerMBean extends BaseOnmsMBean {

    /**
     * 
     * @return The total number of HTTPS connections since the JettyServer
     *         was started
     */
    public long getHttpsConnectionsTotal();
    
    /**
     * 
     * @return The current number of HTTPS connections to the JettyServer
     */
    public long getHttpsConnectionsOpen();
    
    /**
     * 
     * @return The maximum number of concurrent HTTPS connections to
     *         the JettyServer since it was started
     */
    public long getHttpsConnectionsOpenMax();

    /**
     * 
     * @return The total number of plain-HTTP connections since the
     *         JettyServer was started
     */
    public long getHttpConnectionsTotal();
    
    /**
     * 
     * @return The current number of plain-HTTP connections to the
     *         JettyServer
     */
    public long getHttpConnectionsOpen();

    /**
     *  
     * @return The maximum number of concurrent plain-HTTP connections
     *         to the JettyServer since it was started
     */
    public long getHttpConnectionsOpenMax();
    
}
