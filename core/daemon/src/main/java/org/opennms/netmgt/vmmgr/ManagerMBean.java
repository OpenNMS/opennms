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
package org.opennms.netmgt.vmmgr;

import java.util.List;

public interface ManagerMBean {

    /**
     * <p>init</p>
     */
    public void init();
    /**
     * <p>dumpThreads</p>
     */
    public void dumpThreads();

    /**
     * <p>status</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> status();

    /**
     * <p>stop</p>
     */
    public void stop();

    /**
     * <p>doSystemExit</p>
     */
    public void doSystemExit();
    
    /**
     * <p>doTestLoadLibraries</p>
     */
    public void doTestLoadLibraries();
    
    /**
     * <p>getUptime</p>
     * 
     * @return a {@link java.lang.Long} expressing the time, in milliseconds,
     * since the manager first started 
     */
    public Long getUptime();
}
