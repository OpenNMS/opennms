/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.smoketest.utils.jsch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from http://logogin.blogspot.com/2013/04/slf4j-bridge-for-jsch.html on March 12th 2016
 */
public class SLF4JLogger implements com.jcraft.jsch.Logger {

    private static final Logger slf4jLogger = LoggerFactory.getLogger("com.jcraft.jsch");

    private static final int DEBUG_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.DEBUG;
    private static final int INFO_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.INFO;
    private static final int WARN_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.WARN;

    @Override
    public boolean isEnabled(int level) {
        if (level <= DEBUG_LEVEL_THRESHOLD) {
            return slf4jLogger.isDebugEnabled();
        }
        if (level <= INFO_LEVEL_THRESHOLD) {
            return slf4jLogger.isInfoEnabled();
        }
        if (level <= WARN_LEVEL_THRESHOLD) {
            return slf4jLogger.isWarnEnabled();
        }

        return slf4jLogger.isErrorEnabled();
    }

    @Override
    public void log(int level, String message) {
        if (level <= DEBUG_LEVEL_THRESHOLD) {
            slf4jLogger.debug(message);
        } else if (level <= INFO_LEVEL_THRESHOLD) {
            slf4jLogger.info(message);
        } else if (level <= WARN_LEVEL_THRESHOLD) {
            slf4jLogger.warn(message);
        } else {
            slf4jLogger.error(message);
        }
    }
}