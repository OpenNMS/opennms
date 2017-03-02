/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.provision.support.PluginWrapper;

public interface ForeignSourceService {

    String DEFAULT_FOREIGNSOURCE_NAME = "default";

    Set<String> getActiveForeignSourceNames();

    int getForeignSourceCount();

    Set<ForeignSourceEntity> getAllForeignSources();

    ForeignSourceEntity getForeignSource(String name);

    void saveForeignSource(ForeignSourceEntity foreignSource);

    ForeignSourceEntity getDefaultForeignSource();

    void resetDefaultForeignSource();

    void deleteForeignSource(String name);

    Map<String,String> getDetectorTypes();

    Map<String,String> getPolicyTypes();

    Map<String,PluginWrapper> getWrappers();
    
}
