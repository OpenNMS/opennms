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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LayerDefinition {

    public static class Mapping {

        private final Layer layer;
        private final String restriction;

        public Mapping(Layer layer, String restriction) {
            this.layer = Objects.requireNonNull(layer);
            this.restriction = restriction;
        }

        public String getRestriction() {
            return restriction;
        }

        public Layer getLayer() {
            return layer;
        }
    }

    final Map<String, Mapping> mapping = new HashMap<>();

    public LayerDefinition() {
        for (Layers layer : Layers.values()) {
            try {
                final Field enumField = layer.getClass().getField(layer.name());
                final Key key = enumField.getAnnotation(Key.class);
                final Restriction restriction = enumField.getAnnotation(Restriction.class);
                mapping.put(key == null ? layer.getLayer().getId() : key.value(), new Mapping(layer.getLayer(), restriction == null ? null : restriction.hql()));
            } catch (NoSuchFieldException e) {
                // swallow, should not happen
            }
        }
    }

    public List<Mapping> getMapping(List<String> layerKeys) {
        return layerKeys.stream().map(key -> mapping.get(key)).filter(definition -> definition != null).collect(Collectors.toList());
    }
}
