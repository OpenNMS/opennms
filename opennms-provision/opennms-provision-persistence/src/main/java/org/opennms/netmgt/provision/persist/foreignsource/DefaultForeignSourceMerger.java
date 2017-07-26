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

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.ForeignSourceDao;
import org.opennms.netmgt.model.foreignsource.DetectorPluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigType;
import org.opennms.netmgt.model.foreignsource.PolicyPluginConfigEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Merges the incoming objects with already persisted objects.
 * If the objects are not persisted yet, a new instance is created of that entity (but not persisted)
 */
public class DefaultForeignSourceMerger implements ForeignSourceMerger {

    @Autowired
    private ForeignSourceDao foreignSourceDao;

    @Override
    public ForeignSourceEntity createOrMerge(ForeignSource input) {
        final ForeignSourceEntity persistedEntity = getOrCreateForeignSource(input.getName());
        persistedEntity.setName(input.getName());
        persistedEntity.setDate(input.getDateStampAsDate());
        persistedEntity.setDefault(input.isDefault());
        persistedEntity.setScanInterval(input.getScanInterval().getMillis());

        // Note: In order to help hibernate to keep track of new and updated objects, objects must be merged with existing objects
        // update existing or add new policies
        input.getPolicies().forEach(p -> createOrMerge(persistedEntity, PluginConfigType.Policy, p));
        input.getDetectors().forEach(d -> createOrMerge(persistedEntity, PluginConfigType.Detector, d));

        // remove not existing policies
        persistedEntity.getPolicies().stream()
                .filter(p -> input.getPolicy(p.getName()) == null) // find not existing one
                .collect(Collectors.toList())
                .forEach(p -> persistedEntity.removePolicy(p)); // and remove

        // remove not existing detectors
        persistedEntity.getDetectors().stream()
                .filter(d -> input.getDetector(d.getName()) == null) // find not existing one
                .collect(Collectors.toList())
                .forEach(d -> persistedEntity.removeDetector(d)); // and remove

        // The order of policies/detectors is automatically set when persisting the entity and it is
        // based on the index of the list they are in, therefore the order is applied manually here
        final List<DetectorPluginConfigEntity> detectors = input.getDetectors().stream().map(d -> persistedEntity.getDetector(d.getName())).collect(Collectors.toList());
        final List<PolicyPluginConfigEntity> policies = input.getPolicies().stream().map(p -> persistedEntity.getPolicy(p.getName())).collect(Collectors.toList());
        persistedEntity.setDetectors(detectors);
        persistedEntity.setPolicies(policies);

        return persistedEntity;
    }

    private PluginConfigEntity createOrMerge(ForeignSourceEntity parent, PluginConfigType pluginType, PluginConfig input) {
        final PluginConfigEntity persistedPluginConfig = getOrCreatePluginConfig(parent, pluginType, input.getName());
        persistedPluginConfig.setName(input.getName());
        persistedPluginConfig.setPluginClass(input.getPluginClass());
        persistedPluginConfig.setParameters(
                input.getParameters().stream()
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())));
        return persistedPluginConfig;
    }

    private ForeignSourceEntity getOrCreateForeignSource(String name) {
        ForeignSourceEntity foreignSourceEntity = foreignSourceDao.get(name);
        if (foreignSourceEntity == null) {
            foreignSourceEntity = new ForeignSourceEntity();
        }
        return foreignSourceEntity;
    }

    private PluginConfigEntity getOrCreatePluginConfig(ForeignSourceEntity parentEntity, PluginConfigType pluginType, String name) {
        PluginConfigEntity pluginConfigEntity = parentEntity.getPlugin(name, pluginType);
        if (pluginConfigEntity == null) {
            pluginConfigEntity = pluginType.newInstance();
            parentEntity.addPlugin(pluginConfigEntity);
        }
        return pluginConfigEntity;
    }
}
