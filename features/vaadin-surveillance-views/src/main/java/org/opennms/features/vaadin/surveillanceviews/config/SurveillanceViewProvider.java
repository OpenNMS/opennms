/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.config;

import java.io.File;
import java.io.IOException;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for loading, holding and saving of {@link SurveillanceViewConfiguration} definitions.
 *
 * @author Christian Pape
 */
public class SurveillanceViewProvider {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewProvider.class);
    /**
     * Instance variable for this singleton object
     */
    private static SurveillanceViewProvider m_surveillanceViewProvider;
    /**
     * A variable fro holding a {@link SurveillanceViewConfiguration} instance
     */
    private SurveillanceViewConfiguration m_surveillanceViewConfiguration = null;
    /**
     * The configuration {@link java.io.File} to be used.
     */
    private File m_cfgFile;

    /**
     * Private default constructor used to instantiate this class.
     */
    private SurveillanceViewProvider() {
        load();
    }

    /**
     * Method to return the instance of this singleton.
     *
     * @return the singleton instance
     */
    public static SurveillanceViewProvider getInstance() {
        if (m_surveillanceViewProvider == null) {
            m_surveillanceViewProvider = new SurveillanceViewProvider();
        }
        return m_surveillanceViewProvider;
    }

    /**
     * This method saves the data represented by this object to disk.
     */
    public synchronized void save() {
        if (m_surveillanceViewConfiguration == null) {
            load();
        }

        try {
            JaxbUtils.marshal(m_surveillanceViewConfiguration, m_cfgFile);
        } catch (final IOException e) {
            final String filename = ConfigFileConstants.getFileName(ConfigFileConstants.SURVEILLANCE_VIEWS_FILE_NAME);
            LOG.error("Unable to save {}", filename, e);
            throw new IllegalStateException("Unable to save " + filename, e);
        }
    }

    /**
     * This method loads the configuration data from disk.
     */
    public synchronized void load() {
        LOG.debug("Loading surveillance view configuration.");
        if (m_cfgFile == null) {
            try {
                m_cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SURVEILLANCE_VIEWS_FILE_NAME);
            } catch (final IOException e) {
                LOG.error("Unable to load {}", ConfigFileConstants.getFileName(ConfigFileConstants.SURVEILLANCE_VIEWS_FILE_NAME));
                m_cfgFile = new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "surveillance-views.xml");
            }
        }
        LOG.debug("Using surveillance view file: {}", m_cfgFile);
        if (!m_cfgFile.exists()) {
            LOG.warn("Surveillance view configuration {} does not exist!", m_cfgFile);
            m_surveillanceViewConfiguration = new SurveillanceViewConfiguration();
        } else {
            m_surveillanceViewConfiguration = JaxbUtils.unmarshal(SurveillanceViewConfiguration.class, m_cfgFile);
        }
        LOG.debug("Surveillance view configuration loaded: {}", m_surveillanceViewConfiguration);
    }

    /**
     * Checks whether this object contains {@link View} data concerning the given title.
     *
     * @param name the title to search for
     * @return true, if a {@link View} with the given name exists, false otherwise
     */
    public synchronized boolean containsView(String name) {
        for (View view : m_surveillanceViewConfiguration.getViews()) {
            if (view.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the {@link View} for the given title.
     *
     * @param name the title to search for
     * @return the {@link View} instance if found, null otherwise
     */
    public synchronized View getView(String name) {
        for (final View view : m_surveillanceViewConfiguration.getViews()) {
            if (view.getName().equals(name)) {
                return view;
            }
        }

        return null;
    }

    /**
     * Replaces a {@link View} with a new one.
     *
     * @param oldView the old view to be replaced
     * @param newView the new view
     */
    public synchronized void replaceView(View oldView, View newView) {
        View viewFound = null;

        for (View view : m_surveillanceViewConfiguration.getViews()) {
            if (view.getName().equals(oldView.getName())) {
                viewFound = view;

                break;
            }
        }

        if (viewFound != null) {
            int index = getSurveillanceViewConfiguration().getViews().indexOf(viewFound);
            getSurveillanceViewConfiguration().getViews().set(index, newView);
        }
    }

    /**
     * Returns the default {@link View}.
     *
     * @return the {@link View} instance if found, null otherwise
     */
    public synchronized View getDefaultView() {
        final String defaultView = m_surveillanceViewConfiguration.getDefaultView();
		return getView(defaultView);
    }

    /**
     * Returns the loaded {@link org.opennms.features.vaadin.surveillanceviews.model.SurveillanceViewConfiguration} instance
     *
     * @return the loaded config instance
     */
    public SurveillanceViewConfiguration getSurveillanceViewConfiguration() {
        return m_surveillanceViewConfiguration;
    }

    /**
     * Checks whether this object contains a given {@link View} instance.
     *
     * @param view the {@link View} instance to search for
     * @return true, if the {@link View} exists, false otherwise
     */
    public synchronized boolean containsView(View view) {
        return m_surveillanceViewConfiguration.getViews().contains(view);
    }

    /**
     * This method adds a {@link View} to this provider.
     *
     * @param view the {@link View} instance to be added
     */
    public synchronized void addView(View view) {
        if (m_surveillanceViewConfiguration == null) {
            load();
        }
        m_surveillanceViewConfiguration.getViews().add(view);
        save();
    }

    /**
     * This method removes a {@link View} from this provider.
     *
     * @param view the {@link View} instance to be removed
     */
    public synchronized void removeView(View view) {
        if (m_surveillanceViewConfiguration == null) {
            load();
        }
        m_surveillanceViewConfiguration.getViews().remove(view);
        save();
    }
}
