/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

    int getGroupPriority();

    // a protocol definition has a lesser priority (+1) than port (+2) or address definition (+3)
    default int calculatePriority() {
        int priority = 0;
        if (hasSrcAddressDefinition()) priority += 9;
        if (hasSrcPortDefinition()) priority += 3;
        if (hasDstAddressDefinition()) priority += 9;
        if (hasDstPortDefinition()) priority += 3;
        if (hasProtocolDefinition()) priority += 1;
        if (hasExportFilterDefinition()) priority += 1;
        return priority;
    }

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

    static boolean isDefined(String value) {
        return !Strings.isNullOrEmpty(value);
    }
}
