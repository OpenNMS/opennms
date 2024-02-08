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
