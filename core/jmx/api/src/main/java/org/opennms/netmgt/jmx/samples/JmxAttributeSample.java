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
package org.opennms.netmgt.jmx.samples;

import java.util.Objects;

import javax.management.Attribute;

import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

/**
 * A {@link JmxAttributeSample} should be created each time the {@link org.opennms.netmgt.jmx.JmxCollector}
 * collects a MBean Attribute.
 */
public class JmxAttributeSample extends AbstractJmxSample {

    public JmxAttributeSample(Mbean mbean, Attribute attribute) {
        super(mbean, attribute);
    }

    public Attrib getAttrib() {
        for (Attrib eachAttrib : getMbean().getAttribList()) {
            if (Objects.equals(getCollectedAttribute().getName(), eachAttrib.getName())) {
                return eachAttrib;
            }
        }
        return null;
    }

    @Override
    public String getCollectedValueAsString() {
        final Object value = getCollectedAttribute().getValue();
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
