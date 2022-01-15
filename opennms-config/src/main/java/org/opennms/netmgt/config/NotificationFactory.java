/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.notifications.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NotificationFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NotificationFactory extends NotificationManager {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationFactory.class);

    public static final String CONFIG_NAME = "notifications";

    public static final String DEFAULT_CONFIG_ID = "default";
    /**
     * Singleton instance
     */
    private static NotificationFactory instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * Since there is no lastModified for files, replace with loaded thread
     */
    private boolean loaded = false;

    /**
     *
     */
    private NotificationFactory() {
        super(NotifdConfigFactory.getInstance(), DataSourceFactory.getInstance());
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        reload();
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotificationFactory} object.
     */
    static synchronized public NotificationFactory getInstance() {
        if (!initialized)
            return null;

        return instance;
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.sql.SQLException if any.
     * @throws java.beans.PropertyVetoException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException, ClassNotFoundException, SQLException, PropertyVetoException  {
        if (!initialized) {
            instance = new NotificationFactory();
            initialized = true;
        }
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     */
    public synchronized void reload() {
        m_notifications = this.loadConfig();
        postReload();
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    @Override
    public void update() throws IOException {
        if (loaded) {
            return;
        }
        reload();
        loaded = true;
    }

    @Override
    public void updateConfig(Notifications notifications) throws IOException {
        loaded = false;
        super.updateConfig(notifications);
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return DEFAULT_CONFIG_ID;
    }
}
