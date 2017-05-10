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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.Duration;
import org.opennms.netmgt.model.foreignsource.DetectorPluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigType;
import org.opennms.netmgt.model.foreignsource.PolicyPluginConfigEntity;

/**
 * Simply converts rest and persistence objects.
 * No merging with existing entities is performed.
 */
public class ForeignSourceMapper {

    // Attention, this does not merge with existing data in the database. Use with caution
    public static ForeignSourceEntity toPersistenceModel(ForeignSource input) {
        if (input == null) {
            return null;
        }
        ForeignSourceEntity output = new ForeignSourceEntity();
        output.setName(input.getName());
        output.setScanInterval(input.getScanInterval().getMillis());
        output.setDate(input.getDateStampAsDate());
        output.setDefault(input.isDefault());
        input.getDetectors()
                .stream()
                .map(d -> (DetectorPluginConfigEntity) toPersistenceModel(d, PluginConfigType.Detector))
                .collect(Collectors.toList())
                .forEach(p -> output.addDetector(p));
        input.getPolicies()
                .stream()
                .map(d -> (PolicyPluginConfigEntity) toPersistenceModel(d, PluginConfigType.Policy))
                .forEach(p -> output.addPolicy(p));
        return output;
    }

    // Be careful when using, as there is no merging with existing database entities
    public static <T extends PluginConfigEntity> T toPersistenceModel(PluginConfig input, PluginConfigType type) {
        if (input == null) {
            return null;
        }
        PluginConfigEntity output = type.newInstance();
        output.setName(input.getName());
        output.setPluginClass(input.getPluginClass());
        output.setParameters(input.getParameters().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())));
        return (T) output;
    }

    public static List<ForeignSource> toRestModel(Collection<ForeignSourceEntity> input) {
        if (input == null) {
            return null;
        }
        return input.stream().map(it -> toRestModel(it)).collect(Collectors.toList());
    }

    public static ForeignSource toRestModel(ForeignSourceEntity input) {
        if (input == null) {
            return null;
        }
        ForeignSource output = new ForeignSource();
        output.setName(input.getName());
        output.setDefault(input.isDefault());
        output.setScanInterval(Duration.millis(input.getScanInterval()));
        output.setDate(input.getDate());
        output.setDetectors(input.getDetectors().stream()
                .sorted(Comparator.comparing(PluginConfigEntity::getPosition))
                .map(d -> toRestModel(d))
                .collect(Collectors.toList()));
        output.setPolicies(input.getPolicies().stream()
                .sorted(Comparator.comparing(PluginConfigEntity::getPosition))
                .map(p -> toRestModel(p))
                .collect(Collectors.toList()));
        return output;
    }

    public static PluginConfig toRestModel(PluginConfigEntity input) {
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
