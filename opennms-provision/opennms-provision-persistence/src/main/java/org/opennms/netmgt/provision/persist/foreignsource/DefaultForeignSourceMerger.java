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

import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.ForeignSourceDao;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.PluginConfigType;
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

        return persistedEntity;
    }

    private PluginConfigEntity createOrMerge(ForeignSourceEntity parent, PluginConfigType pluginType, PluginConfig input) {
        final PluginConfigEntity persistedPolicy = getOrCreatePluginConfig(parent, pluginType, input.getName());
        persistedPolicy.setName(input.getName());
        persistedPolicy.setPluginClass(input.getPluginClass());
        persistedPolicy.setParameters(
                input.getParameters().stream()
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())));
        return persistedPolicy;
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
