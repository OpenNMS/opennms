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
package org.opennms.features.vaadin.config;

import org.slf4j.LoggerFactory;
import org.opennms.features.vaadin.api.Logger;

/**
 * The Class Simple Logger.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SimpleLogger implements Logger {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SimpleLogger.class);

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#error(java.lang.String)
     */
    @Override
    public void error(String message) {
        LOG.error(message);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#warn(java.lang.String)
     */
    @Override
    public void warn(String message) {
        LOG.warn(message);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#info(java.lang.String)
     */
    @Override
    public void info(String message) {
        LOG.info(message);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
        LOG.debug(message);
    }

}
