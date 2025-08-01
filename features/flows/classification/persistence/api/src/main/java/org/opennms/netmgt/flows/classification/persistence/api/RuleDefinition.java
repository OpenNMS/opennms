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
package org.opennms.netmgt.flows.classification.persistence.api;

import com.google.common.base.Strings;

public interface RuleDefinition {

    String getName();

    String getDstAddress();

    String getDstPort();

    String getSrcPort();

    String getSrcAddress();

    String getProtocol();

    String getExporterFilter();

    int getGroupPosition();

    /** Defines the order in which the rules are evaluated. Lower positions go first */
    int getPosition();

    default boolean hasProtocolDefinition() {
        return isDefined(getProtocol());
    }

    default boolean hasDstAddressDefinition() {
        return isDefined(getDstAddress());
    }

    default boolean hasDstPortDefinition() {
        return isDefined(getDstPort());
    }

    default boolean hasSrcAddressDefinition() {
        return isDefined(getSrcAddress());
    }

    default boolean hasSrcPortDefinition() {
        return isDefined(getSrcPort());
    }

    default boolean hasExportFilterDefinition() {
        return isDefined(getExporterFilter());
    }

    default boolean hasDefinition() {
        return hasProtocolDefinition()
               || hasDstAddressDefinition()
               || hasDstPortDefinition()
               || hasSrcAddressDefinition()
               || hasSrcPortDefinition()
               || hasExportFilterDefinition();
    }

    default RuleDefinition reversedRule() {
        final DefaultRuleDefinition result = new DefaultRuleDefinition();
        result.setName(getName());
        result.setDstAddress(getSrcAddress());
        result.setDstPort(getSrcPort());
        result.setSrcAddress(getDstAddress());
        result.setSrcPort(getDstPort());
        result.setProtocol(getProtocol());
        result.setExporterFilter(getExporterFilter());
        result.setGroupPosition(getGroupPosition());
        result.setPosition(getPosition());
        return result;
    }

    static boolean isDefined(String value) {
        return !Strings.isNullOrEmpty(value);
    }
}
