/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultNameProvider implements NameProvider {

    private final SelectionManager selectionManager;

    public DefaultNameProvider(SelectionManager selectionManager) {
        Objects.requireNonNull(selectionManager);
        this.selectionManager = selectionManager;
    }

    @Override
    public Map<Object, String> getNamesMap() {
        Map<Object, String> objectToNameMap = new HashMap<>();
        for (Mbean bean : selectionManager.getSelectedMbeans()) {
            for (Attrib att : selectionManager.getSelectedAttributes(bean)) {
                objectToNameMap.put(att, att.getAlias());
            }
            for (CompAttrib compAttrib : selectionManager.getSelectedCompositeAttributes(bean)) {
                for (CompMember compMember : selectionManager.getSelectedCompositeMembers(compAttrib)) {
                    objectToNameMap.put(compMember, compMember.getAlias());
                }
            }
        }
        return Collections.unmodifiableMap(objectToNameMap);
    }
}
