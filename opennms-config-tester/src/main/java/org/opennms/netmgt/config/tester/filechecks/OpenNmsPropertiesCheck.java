/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.tester.filechecks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.config.tester.checks.ConfigCheck;
import org.opennms.netmgt.config.tester.checks.ConfigEntryDefinition;
import org.opennms.netmgt.config.tester.checks.NotEmptyConfigEntryDefinition;


public class OpenNmsPropertiesCheck implements ConfigCheck {

    @Override
    public String getFilename() {
        return "opennms.properties";
    }

    @Override
    public Collection<ConfigEntryDefinition> getChecks() {
        List<ConfigEntryDefinition> list = new ArrayList<>();
        list.add(new NotEmptyConfigEntryDefinition("org.snmp4j.smisyntaxes"));
        // TODO: we need to define the rest of the keys
        return list;
    }

}
