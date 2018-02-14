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

package org.opennms.features.topology.plugins.topo.asset.layers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayerDefinitionRepository {

    private static Logger LOG = LoggerFactory.getLogger(LayerDefinitionRepository.class);

    public LayerDefinitionRepository() {

    }

    public List<LayerDefinition> getDefinitions(Collection<String> layerKeys) {
        final List<LayerDefinition> layerDefinitions = new ArrayList<>();
        for (String layerKey : layerKeys) {
            LayerDefinition layerDefinition = getLayerDefinition(layerKey);
            layerDefinitions.add(layerDefinition);
        }
        return layerDefinitions;
    }

    private LayerDefinition getLayerDefinition(String layerKey) {
        for (Layers layerEnum : Layers.values()) {
            try {
                final Field enumField = layerEnum.getClass().getField(layerEnum.name());
                final Key key = enumField.getAnnotation(Key.class);
                final Restriction restriction = enumField.getAnnotation(Restriction.class);
                final String enumLayerKey = key != null ? key.value() : layerEnum.getLayer().getId();
                if (enumLayerKey.equals(layerKey)) {
                    return new LayerDefinition(layerKey, layerEnum.getLayer(), restriction != null ? restriction.hql() : null);
                }
            } catch (NoSuchFieldException e) {
                // swallow, should not happen
            }

        }
        LOG.warn("No layer definition found for layer key {}. Skipping.", layerKey);
        return null;
    }
}
