/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
