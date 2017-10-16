/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator;

import java.util.Collections;
import java.util.Map;

import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

public class TestHelper {

    public static NameProvider DUMMY_NAME_PROVIDER = new NameProvider() {
        @Override
        public Map<Object, String> getNamesMap() {
            return Collections.emptyMap();
        }
    };

    public static Attrib createAttrib(String name, String alias) {
        Attrib attrib = new Attrib();
        attrib.setName(name);
        attrib.setAlias(alias);
        return attrib;
    }

    public static CompMember createCompMember(String name, String alias) {
        CompMember compMember = new CompMember();
        compMember.setName(name);
        compMember.setAlias(alias);
        return compMember;
    }

    public static Mbean createMbean(String name) {
        Mbean mbean = new Mbean();
        mbean.setName(name);
        return mbean;
    }

    public static CompAttrib createCompAttrib(String name, CompMember... compMember) {
        CompAttrib compAttrib = new CompAttrib();
        compAttrib.setName(name);
        for (CompMember eachMember : compMember) {
            compAttrib.addCompMember(eachMember);
        }
        return compAttrib;
    }
}
