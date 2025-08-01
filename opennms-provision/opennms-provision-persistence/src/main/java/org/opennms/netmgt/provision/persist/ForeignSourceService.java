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
package org.opennms.netmgt.provision.persist;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.support.PluginWrapper;

/**
 * <p>ForeignSourceService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ForeignSourceService {

    /**
     * <p>setDeployedForeignSourceRepository</p>
     *
     * @param repo a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    void setDeployedForeignSourceRepository(ForeignSourceRepository repo);
    /**
     * <p>setPendingForeignSourceRepository</p>
     *
     * @param repo a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    void setPendingForeignSourceRepository(ForeignSourceRepository repo);

    /**
     * <p>getAllForeignSources</p>
     *
     * @return a {@link java.util.Set} object.
     */
    Set<ForeignSource> getAllForeignSources();

    /**
     * <p>getForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource getForeignSource(String name);
    /**
     * <p>saveForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param fs a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource saveForeignSource(String name, ForeignSource fs);
    /**
     * <p>cloneForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param target a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource cloneForeignSource(String name, String target);
    /**
     * <p>deleteForeignSource</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    void          deleteForeignSource(String name);

    /**
     * <p>deletePath</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @param dataPath a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource deletePath(String foreignSourceName, String dataPath);
    /**
     * <p>addParameter</p>
     *
     * @param foreignSourceName a {@link java.lang.String} object.
     * @param dataPath a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource addParameter(String foreignSourceName, String dataPath);

    /**
     * <p>addDetectorToForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource addDetectorToForeignSource(String foreignSource, String name);
    /**
     * <p>deleteDetector</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource deleteDetector(String foreignSource, String name);

    /**
     * <p>addPolicyToForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource addPolicyToForeignSource(String foreignSource, String name);
    /**
     * <p>deletePolicy</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     */
    ForeignSource deletePolicy(String foreignSource, String name);

    /**
     * <p>getDetectorTypes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, Class<?>> getDetectorTypes();

    /**
     * <p>getPolicyTypes</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String,String> getPolicyTypes();
    /**
     * <p>getWrappers</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String,PluginWrapper> getWrappers();
    
}
