/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.support;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.ConfigurableIconRepository;
import org.opennms.features.topology.api.IconManager;
import org.opennms.features.topology.api.IconRepository;
import org.opennms.features.topology.api.topo.Vertex;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.server.Page;

public class IconRepositoryManager implements IconManager {

    private Set<IconRepository> m_iconRepositories = Sets.newHashSet();

    public synchronized void onBind(IconRepository iconRepo) {
        try {
            m_iconRepositories.add(iconRepo);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
        }
    }

    public synchronized void onUnbind(IconRepository iconRepo) {
        try {
            m_iconRepositories.remove(iconRepo);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
        }
    }

    public synchronized void onBind(ConfigurableIconRepository iconRepo) {
        try {
            m_iconRepositories.add(iconRepo);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onBind()", e);
        }
    }

    public synchronized void onUnbind(ConfigurableIconRepository iconRepo) {
        try {
            m_iconRepositories.remove(iconRepo);
        } catch (Throwable e) {
            LoggerFactory.getLogger(this.getClass()).warn("Exception during onUnbind()", e);
        }
    }

    private String lookupSVGIconIdForExactKey(String key) {
        for(IconRepository iconRepo : m_iconRepositories) {
            if(iconRepo.contains(key)) {
                return iconRepo.getSVGIconId(key);
            }
        }
        return null;
    }

    private String createIconKey(Vertex vertex) {
        return String.format("%s.%s", vertex.getNamespace(), vertex.getId());
    }

    @Override
    public String setIconMapping(Vertex vertex, String newIconId) {
        // We look for a IconRepository with the old icon key as mapping
        final ConfigurableIconRepository iconRepository = findRepositoryByIconKey(vertex.getIconKey());
        final String oldIconId = getSVGIconId(vertex.getIconKey());
        if (iconRepository != null && !oldIconId.equals(newIconId)) {
            String iconKey = createIconKey(vertex);
            // now we set the new mapping: vertex-id => icon-id
            iconRepository.addIconMapping(iconKey, newIconId);
            iconRepository.save();
            return iconKey;
        }
        return null;
    }

    @Override
    public boolean removeIconMapping(Vertex vertex) {
        // We look for a IconRepository with the old icon key as mapping
        final String iconKey = createIconKey(vertex);
        final ConfigurableIconRepository iconRepository = findRepositoryByIconKey(iconKey);
        if (iconRepository != null) { // we may not have a icon repository due to no custom mapping
            iconRepository.removeIconMapping(iconKey);
            iconRepository.save();
            return true;
        }
        return false;
    }

    @Override
    public String getSVGIconId(Vertex vertex) {
        // If there is a direct mapping for the vertex, use that mapping (overwrites icon key)
        final String iconId = lookupSVGIconIdForExactKey(createIconKey(vertex));
        if (iconId != null) {
            return iconId;
        }
        // Otherwise resolve the icon key assigned by the topology provider for that vertex
        return getSVGIconId(vertex.getIconKey());
    }

    @Override
    public String getSVGIconId(String iconKey) {
        if(iconKey != null) {
        	// if the exact key is configured then use it
        	String iconUrl = lookupSVGIconIdForExactKey(iconKey);
        	if (iconUrl != null) {
        		return iconUrl;
        	}
            if ("default".equals(iconKey)) {
            	// we got here an no default icon was registered!!
                LoggerFactory.getLogger(this.getClass()).warn("No icon with key 'default' found.");
                return null;
            }
            int lastColon = iconKey.lastIndexOf('.');
            if (lastColon == -1) {
            	// no colons in key so just return 'default' icon
            	return getSVGIconId("default");
            } else {
            	// remove the segment following the last colon
            	String newKey = iconKey.substring(0, lastColon);
                return getSVGIconId(newKey);
            }
        } else {
            return getSVGIconId("default");
        }
    }

    @Override
    public List<String> getSVGIconFiles() {
        List<String> svgUrls = Lists.newArrayList();
        try {
            URI location = Page.getCurrent().getLocation();
            URL url = new URL(location.getScheme(), location.getHost(), location.getPort(), "/opennms");
            Path path = Paths.get(System.getProperty("opennms.home", ""), "jetty-webapps", "opennms", "svg");
            File[] files = path.toFile().listFiles((file) -> file.isFile() && file.getName().endsWith(".svg"));
            for (File eachFile : files) {
                svgUrls.add(String.format("%s/svg/%s", url, eachFile.getName()));
            }
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(this.getClass()).error("Error while loading SVG definitions", e);
        }
        return svgUrls;
    }

    @Override
    public ConfigurableIconRepository findRepositoryByIconKey(String iconKey) {
        // only consider configurable Repositories
        final List<ConfigurableIconRepository> configurableIconRepositories = m_iconRepositories.stream()
                .filter(e -> e instanceof ConfigurableIconRepository)
                .map(e -> (ConfigurableIconRepository) e)
                .collect(Collectors.toList());
        // look up the key in each repository
        for (ConfigurableIconRepository eachRepository : configurableIconRepositories) {
            if (eachRepository.contains(iconKey)) {
                return eachRepository;
            }
        }
        // Key not found, yet. If reducible, reduce key and try again
        if (iconKey != null && iconKey.lastIndexOf('.') > 0) {
            return findRepositoryByIconKey(iconKey.substring(0, iconKey.lastIndexOf('.')));
        }
        // No Repository with the iconKey exists
        return null;
    }
}
