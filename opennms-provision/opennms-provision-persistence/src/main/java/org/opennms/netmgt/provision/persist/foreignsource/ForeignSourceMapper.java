/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.Duration;
import org.opennms.netmgt.model.requisition.DetectorPluginConfig;
import org.opennms.netmgt.model.requisition.OnmsForeignSource;
import org.opennms.netmgt.model.requisition.OnmsPluginConfig;
import org.opennms.netmgt.model.requisition.PluginType;
import org.opennms.netmgt.model.requisition.PolicyPluginConfig;

// TODO MVR verify license headers
public class ForeignSourceMapper {

    // Attention, this does not merge with existing data in the database. Use with caution
    public static OnmsForeignSource toPersistenceModel(ForeignSource input) {
        if (input == null) {
            return null;
        }
        OnmsForeignSource output = new OnmsForeignSource();
        output.setName(input.getName());
        output.setScanInterval(input.getScanInterval().getMillis());
        output.setDate(input.getDateStampAsDate());
        output.setDefault(input.isDefault());
        input.getDetectors()
                .stream()
                .map(d -> (DetectorPluginConfig) toPersistenceModel(d, PluginType.Detector))
                .collect(Collectors.toList())
                .forEach(p -> output.addPlugin(p));
        input.getPolicies()
                .stream()
                .map(d -> (PolicyPluginConfig) toPersistenceModel(d, PluginType.Policy))
                .forEach(p -> output.addPlugin(p));
        return output;
    }

    // Be careful when using, as there is no merging with existing database entities
    public static <T extends OnmsPluginConfig> T toPersistenceModel(PluginConfig input, PluginType type) {
        if (input == null) {
            return null;
        }
        OnmsPluginConfig output = type.newInstance();
        output.setName(input.getName());
        output.setPluginClass(input.getPluginClass());
        output.setParameters(input.getParameters().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())));
        return (T) output;
    }

    public static List<ForeignSource> toRestModel(Collection<OnmsForeignSource> input) {
        if (input == null) {
            return null;
        }
        return input.stream().map(it -> toRestModel(it)).collect(Collectors.toList());
    }

    public static ForeignSource toRestModel(OnmsForeignSource input) {
        if (input == null) {
            return null;
        }
        ForeignSource output = new ForeignSource();
        output.setName(input.getName());
        output.setDefault(input.isDefault());
        output.setScanInterval(Duration.millis(input.getScanInterval()));
        output.setDateStamp(null); // TODO MVR
        output.setDetectors(input.getDetectors().stream().map(d -> toRestModel(d)).collect(Collectors.toList()));
        output.setPolicies(input.getPolicies().stream().map(p -> toRestModel(p)).collect(Collectors.toList()));
        return output;
    }

    public static PluginConfig toRestModel(OnmsPluginConfig input) {
        if (input == null) {
            return null;
        }
        PluginConfig output = new PluginConfig();
        output.setName(input.getName());
        output.setPluginClass(input.getPluginClass());
        output.setParameterMap(input.getParameters());
        return output;
    }
}
