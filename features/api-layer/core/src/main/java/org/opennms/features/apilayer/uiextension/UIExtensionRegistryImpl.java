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
package org.opennms.features.apilayer.uiextension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.integration.api.v1.ui.UIExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIExtensionRegistryImpl implements UIExtensionRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(UIExtensionRegistryImpl.class);
    private final Map<String, UIExtension> extensionMap = new HashMap<>();

    public synchronized void onBind(UIExtension  extension, Map properties) {
        LOG.debug("Extension registry bind called with {}: {}", extension, properties);
        if(extension != null) {
            extensionMap.put(extension.getExtensionId(), extension);
        }
    }

    public synchronized void onUnbind(UIExtension extension, Map properties) {
        LOG.debug("Extension registry unBind called with {}: {}", extension, properties);
        if(extension != null) {
            extensionMap.remove(extension.getExtensionId());
        }
    }

    @Override
    public Set<String> getExtensionNames() {
        return extensionMap.keySet();
    }

    @Override
    public UIExtension getExtensionByID(String id) {
        return extensionMap.get(id);
    }

    @Override
    public List<UIExtension> listExtensions() {
        return new ArrayList<>(extensionMap.values());
    }
}

