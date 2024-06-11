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
package org.opennms.netmgt.dao.vmware;

import org.opennms.netmgt.config.vmware.VmwareConfig;
import org.opennms.netmgt.config.vmware.VmwareServer;

import java.util.Map;

/**
 * The Interface VmwareConfigDao
 * <p/>
 * This class is used for defining the methods for accessing the configuration data for the Vmware Accounts
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public interface VmwareConfigDao {

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    VmwareConfig getConfig();

    /**
     * Returns the map of server entries from the configuration object.
     *
     * @return the map of server entries
     */
    Map<String, VmwareServer> getServerMap();
}
