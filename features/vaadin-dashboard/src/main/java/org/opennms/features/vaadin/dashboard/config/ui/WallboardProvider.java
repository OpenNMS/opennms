/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.opennms.features.vaadin.dashboard.model.Wallboards;

import javax.xml.bind.JAXB;
import java.io.File;

/**
 * This class is used for loading, holding and saving of {@link Wallboard} definitions.
 */
public class WallboardProvider {
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
     * The beancontainer this class uses.
     */
    private BeanItemContainer<Wallboard> m_beanItemContainer = new BeanItemContainer<Wallboard>(Wallboard.class);

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
        return m_beanItemContainer;
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

        JAXB.marshal(m_wallboards, m_cfgFile);
    }

    /**
     * This method loads the configuration data from disk.
     */
    public synchronized void load() {
        if (!m_cfgFile.exists()) {
            m_wallboards = new Wallboards();
        } else {
            m_wallboards = JAXB.unmarshal(m_cfgFile, Wallboards.class);
        }

        updateBeanItemContainer();
    }

    /**
     * This method updates the {@link BeanItemContainer} of this object.
     */
    private void updateBeanItemContainer() {
        m_beanItemContainer.removeAllItems();
        for (Wallboard wallboard : m_wallboards.getWallboards()) {
            m_beanItemContainer.addItem(wallboard);
        }
    }

    /**
     * Checks whether this object contains {@link Wallboard} data concerning the given title.
     *
     * @param title the title to search for
     * @return true, if a {@link Wallboard} with the given title exists, false otherwise
     */
    synchronized public boolean containsWallboard(String title) {
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
    synchronized public Wallboard getWallboard(String title) {
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
    synchronized public boolean containsWallboard(Wallboard wallboard) {
        return m_wallboards.getWallboards().contains(wallboard);
    }

    /**
     * This method adds a {@link Wallboard} to this provider.
     *
     * @param wallboard the {@link Wallboard} instance to be added
     */
    synchronized public void addWallboard(Wallboard wallboard) {
        if (m_wallboards == null) {
            load();
        }
        m_wallboards.getWallboards().add(wallboard);
        save();
        updateBeanItemContainer();
    }

    /**
     * This method removes a {@link Wallboard} from this provider.
     *
     * @param wallboard the {@link Wallboard} instance to be removed
     */
    synchronized public void removeWallboard(Wallboard wallboard) {
        if (m_wallboards == null) {
            load();
        }
        m_wallboards.getWallboards().remove(wallboard);
        save();
        updateBeanItemContainer();
    }
}
