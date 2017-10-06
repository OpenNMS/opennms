/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.data.util.BeanItemContainer;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.opennms.features.vaadin.dashboard.model.Wallboards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * This class is used for loading, holding and saving of {@link Wallboard} definitions.
 */
public class WallboardProvider {
    private static Logger LOG = LoggerFactory.getLogger(WallboardProvider.class);

    /**
     * Instance variable for this singleton object
     */
    private static WallboardProvider m_wallboardProvider = new WallboardProvider();
    /**
     * A variable fro holding a {@link Wallboards} instance
     */
    private Wallboards m_wallboards = null;
    /**
     * The configuration {@link File} to be used.
     */
    private File m_cfgFile = new File("etc/dashboard-config.xml");

    /**
     * Private default constructor used to instantiate this class.
     */
    private WallboardProvider() {
        load();
    }

    /**
     * Method to return the {@link BeanItemContainer} associated with this object.
     *
     * @return the {@link BeanItemContainer}
     */
    public BeanItemContainer<Wallboard> getBeanContainer() {
        return new BeanItemContainer<Wallboard>(Wallboard.class, m_wallboards.getWallboards());
    }

    /**
     * Method to return the instance of this singleton.
     *
     * @return the singleton instance
     */
    public static WallboardProvider getInstance() {
        return m_wallboardProvider;
    }

    /**
     * This method saves the data represented by this object to disk.
     */
    public synchronized void save() {
        if (m_wallboards == null) {
            load();
        }

        try {
            JaxbUtils.marshal(m_wallboards, m_cfgFile);
        } catch (final IOException e) {
            LOG.error("Failed to save {}", m_cfgFile, e);
            throw new IllegalStateException("Failed to save " + m_cfgFile, e);
        }
    }

    /**
     * This method loads the configuration data from disk.
     */
    public synchronized void load() {
        if (!m_cfgFile.exists()) {
            m_wallboards = new Wallboards();
        } else {
            m_wallboards = JaxbUtils.unmarshal(Wallboards.class, m_cfgFile);
        }
    }

    /**
     * Checks whether this object contains {@link Wallboard} data concerning the given title.
     *
     * @param title the title to search for
     * @return true, if a {@link Wallboard} with the given title exists, false otherwise
     */
    public synchronized boolean containsWallboard(String title) {
        for (Wallboard wallboard : m_wallboards.getWallboards()) {
            if (wallboard.getTitle().equals(title)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the {@link Wallboard} for the given title.
     *
     * @param title the title to search for
     * @return the {@link Wallboard} instance if found, null otherwise
     */
    public synchronized Wallboard getWallboard(String title) {
        for (Wallboard wallboard : m_wallboards.getWallboards()) {
            if (wallboard.getTitle().equals(title)) {
                return wallboard;
            }
        }

        return null;
    }

    /**
     * Checks whether this object contains a given {@link Wallboard} instance.
     *
     * @param wallboard the {@link Wallboard} instance to search for
     * @return true, if the {@link Wallboard} exists, false otherwise
     */
    public synchronized boolean containsWallboard(Wallboard wallboard) {
        return m_wallboards.getWallboards().contains(wallboard);
    }

    /**
     * This method adds a {@link Wallboard} to this provider.
     *
     * @param wallboard the {@link Wallboard} instance to be added
     */
    public synchronized void addWallboard(Wallboard wallboard) {
        if (m_wallboards == null) {
            load();
        }
        m_wallboards.getWallboards().add(wallboard);
        save();
    }

    /**
     * This method removes a {@link Wallboard} from this provider.
     *
     * @param wallboard the {@link Wallboard} instance to be removed
     */
    public synchronized void removeWallboard(Wallboard wallboard) {
        if (m_wallboards == null) {
            load();
        }
        m_wallboards.getWallboards().remove(wallboard);
        save();
    }
}
