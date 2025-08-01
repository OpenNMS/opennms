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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

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
