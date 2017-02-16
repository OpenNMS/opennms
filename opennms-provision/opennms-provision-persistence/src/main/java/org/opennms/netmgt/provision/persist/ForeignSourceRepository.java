/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

import java.util.Set;

import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.model.requisition.OnmsRequisition;

public interface ForeignSourceRepository {

    String DEFAULT_FOREIGNSOURCE_NAME = "default";

    Set<String> getActiveForeignSourceNames();
    
    int getForeignSourceCount();

    Set<OnmsForeignSource> getForeignSources();

    OnmsForeignSource getForeignSource(String foreignSourceName);

    void save(OnmsForeignSource foreignSource);

    void delete(OnmsForeignSource foreignSource);

    OnmsForeignSource getDefaultForeignSource();

    void putDefaultForeignSource(OnmsForeignSource foreignSource);

    void resetDefaultForeignSource();

    Set<OnmsRequisition> getRequisitions();

    OnmsRequisition getRequisition(String foreignSourceName);

    void save(OnmsRequisition requisition);

    void delete(OnmsRequisition requisition);

    // TODO MVR ?
    void validate(OnmsForeignSource foreignSource);

    // TODO MVR ?
    void validate(OnmsRequisition requisition);

    void triggerImport(ImportRequest web);
}
