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

import org.opennms.netmgt.config.vmware.cim.VmwareCimCollection;
import org.opennms.netmgt.config.vmware.cim.VmwareCimDatacollectionConfig;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * The Interface VmwareCimDatacollectionConfigDao
 * <p/>
 * This class is used for defining the methods for accessing the configuration data for the Vmware Cim Data Collection
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public interface VmwareCimDatacollectionConfigDao {

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    VmwareCimDatacollectionConfig getConfig();

    /**
     * This method returns a subset of the configuration data for a given collection name.
     *
     * @param collectionName the collection's name
     * @return the Cim collection object
     */
    VmwareCimCollection getVmwareCimCollection(String collectionName);

    /**
     * Returns the Rrd repository for a given collection name.
     *
     * @param collectionName the collection's name
     * @return the repository
     */
    public RrdRepository getRrdRepository(String collectionName);

    /**
     * Returns the base Rrd's path.
     *
     * @return the Rrd's path
     */
    public String getRrdPath();
}
