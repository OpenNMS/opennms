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
package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.v7.data.util.BeanItemContainer;

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
