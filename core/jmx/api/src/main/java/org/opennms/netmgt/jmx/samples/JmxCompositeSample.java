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

import javax.management.Attribute;
import javax.management.openmbean.CompositeData;

import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

/**
 * A {@link JmxCompositeSample} should be created each time the {@link org.opennms.netmgt.jmx.JmxCollector}
 * collects a MBean Composite Member.
 */
public class JmxCompositeSample extends AbstractJmxSample {

    /**
     * The Composite Member the Composite Data belongs to.
     */
    private final CompMember compositeMember;

    /**
     * The collected CompositeData
     */
    private final CompositeData compositeData;

    public JmxCompositeSample(Mbean mbean, Attribute attribute, CompositeData compositeData, CompMember compositeMember) {
        super(mbean, attribute);
        this.compositeData = compositeData;
        this.compositeMember = compositeMember;
    }

    public CompMember getCompositeMember() {
        return compositeMember;
    }

    public String getCompositeKey() {
        return compositeMember.getName();
    }

    @Override
    public String getCollectedValueAsString() {
        Object value = compositeData.get(getCompositeKey());
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
