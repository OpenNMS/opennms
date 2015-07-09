/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
