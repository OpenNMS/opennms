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
package org.opennms.netmgt.statsd.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>StatsdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface StatsdMBean extends BaseOnmsMBean {
    /**
     * @return The number of report runs started since Statsd was last started
     */
    public long getReportsStarted();
    
    /**
     * @return The number of report runs completed since Statsd was last started
     */
    public long getReportsCompleted();
    
    /**
     * @return The number of reports persisted since Statsd was last started
     */
    public long getReportsPersisted();
    
    /**
     * @return The time, in milliseconds, spent running reports since Statsd was last started
     */
    public long getReportRunTime();
    }
