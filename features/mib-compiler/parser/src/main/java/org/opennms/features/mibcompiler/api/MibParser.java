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
package org.opennms.features.mibcompiler.api;

import java.io.File;
import java.util.List;

import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.xml.eventconf.Events;

/**
 * The Interface MibParser.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public interface MibParser {

    /**
     * Sets the MIB directory.
     *
     * @param mibDirectory the MIB directory
     */
    void setMibDirectory(File mibDirectory);

    /**
     * Parses the MIB.
     *
     * @param mibFile the MIB file
     * @return true, if successful
     */
    boolean parseMib(File mibFile);

    /**
     * Gets the formatted errors.
     *
     * @return the formatted errors
     */
    String getFormattedErrors();

    /**
     * Gets the missing dependencies.
     *
     * @return the missing dependencies
     */
    List<String> getMissingDependencies();

    /**
     * Gets the MIB name.
     * 
     * @return the MIB name.
     */
    String getMibName();

    /**
     * Gets the event list.
     *
     * @param ueibase the UEI base
     * @return the event list
     */
    Events getEvents(String ueibase);

    /**
     * Gets the data collection.
     *
     * @return the data collection group
     */
    DatacollectionGroup getDataCollection();

    /**
     * Gets the prefab graph templates.
     *
     * @return the prefab graph templates.
     */
    List<PrefabGraph> getPrefabGraphs();

}
