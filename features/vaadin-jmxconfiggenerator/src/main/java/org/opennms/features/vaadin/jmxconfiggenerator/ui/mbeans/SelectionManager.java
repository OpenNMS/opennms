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

import java.util.Collection;
import java.util.Collections;

import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

public interface SelectionManager {

    Collection<Attrib> getSelectedAttributes(Mbean mbean);

    Collection<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib);

    Collection<CompAttrib> getSelectedCompositeAttributes(Mbean mbean);

    Collection<Mbean> getSelectedMbeans();

    SelectionManager EMPTY = new SelectionManager() {
        @Override
        public Collection<Attrib> getSelectedAttributes(Mbean mbean) {
            return Collections.emptyList();
        }

        @Override
        public Collection<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib) {
            return Collections.emptyList();
        }

        @Override
        public Collection<CompAttrib> getSelectedCompositeAttributes(Mbean mbean) {
            return Collections.emptyList();
        }

        @Override
        public Collection<Mbean> getSelectedMbeans() {
            return Collections.emptyList();
        }
    };
}
