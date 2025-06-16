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
