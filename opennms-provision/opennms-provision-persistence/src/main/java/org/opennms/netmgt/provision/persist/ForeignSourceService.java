/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.support.PluginWrapper;

public interface ForeignSourceService {

    void setDeployedForeignSourceRepository(ForeignSourceRepository repo);
    void setPendingForeignSourceRepository(ForeignSourceRepository repo);

    Set<ForeignSource> getAllForeignSources();

    ForeignSource getForeignSource(String name);
    ForeignSource saveForeignSource(String name, ForeignSource fs);
    ForeignSource cloneForeignSource(String name, String target);
    void          deleteForeignSource(String name);

    ForeignSource deletePath(String foreignSourceName, String dataPath);
    ForeignSource addParameter(String foreignSourceName, String dataPath);

    ForeignSource addDetectorToForeignSource(String foreignSource, String name);
    ForeignSource deleteDetector(String foreignSource, String name);

    ForeignSource addPolicyToForeignSource(String foreignSource, String name);
    ForeignSource deletePolicy(String foreignSource, String name);

    Map<String,String> getDetectorTypes();
    Map<String,String> getPolicyTypes();
    Map<String,PluginWrapper> getWrappers();
    
}
