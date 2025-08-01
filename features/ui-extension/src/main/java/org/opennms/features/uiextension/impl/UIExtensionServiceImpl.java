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
package org.opennms.features.uiextension.impl;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opennms.features.apilayer.uiextension.UIExtensionRegistry;
import org.opennms.features.uiextension.api.UIExtensionService;
import org.opennms.integration.api.v1.ui.UIExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;


public class UIExtensionServiceImpl implements UIExtensionService {
    private static final String UI_PATH = "web/components";
    private final UIExtensionRegistry extensionRegistry;

    public UIExtensionServiceImpl(UIExtensionRegistry registry) {
        this.extensionRegistry = registry;
    }

    @Override
    public List<UIExtension> listPlugins() {
        return extensionRegistry.listExtensions();
    }

    @Override
    public String getExtensionJSFile(String id, String resourcePath) throws IOException {
        return readExtensionFile(id, resourcePath);
    }

    @Override
    public String getExtensionCSSFile(String id) throws IOException {
        UIExtension extension = extensionRegistry.getExtensionByID(id);
        return readExtensionFile(id, extension.getResourceRootPath() + "/style.css");
    }

    private String readExtensionFile(String id, String filePath) throws IOException {
        UIExtension extension = extensionRegistry.getExtensionByID(id);
        Bundle bundle = FrameworkUtil.getBundle(extension.getExtensionClass());
        if(bundle == null) {
            return null;
        }
        URL url = bundle.getResource(filePath);
        if(url == null) {
            return null;
        }
        return new String(IOUtils.toByteArray(url.openStream()));
    }
}
