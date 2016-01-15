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

package org.opennms.netmgt.alarmd.northbounder.email;

import java.io.FileWriter;
import java.io.IOException;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EmailNorthbounderConfigDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EmailNorthbounderConfigDao extends AbstractJaxbConfigDao<EmailNorthbounderConfig, EmailNorthbounderConfig> {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(EmailNorthbounderConfigDao.class);

    /**
     * Instantiates a new Email northbounder configuration DAO.
     */
    public EmailNorthbounderConfigDao() {
        super(EmailNorthbounderConfig.class, "Config for E-mail Northbounder");
    }

    /* (non-Javadoc)
     * @see org.opennms.core.xml.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    protected EmailNorthbounderConfig translateConfig(EmailNorthbounderConfig config) {
        return config;
    }

    /**
     * Gets the Email northbounder configuration.
     *
     * @return the configuration object
     */
    public EmailNorthbounderConfig getConfig() {
        return getContainer().getObject();
    }

    /**
     * Reload.
     */
    public void reload() {
        getContainer().reload();
    }

    /**
     * Save.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void save() throws IOException {
        JaxbUtils.marshal(getConfig(), new FileWriter(getConfigResource().getFile()));
    }

}
