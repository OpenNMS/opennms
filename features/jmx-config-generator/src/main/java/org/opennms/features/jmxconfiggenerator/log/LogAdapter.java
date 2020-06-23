/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.log;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.InputStream;

/**
 * The command line tool needs to be enabled to only log to STDOUT or STDERR.
 * Due to some limitations within the onejar bootstraping, we have to provide our own LogAdapter
 * to be used within the "library" of the jmxconfiggenerator implementations.
 * Otherwise we would see log output on STDOUT/STDERR when using the cli tool.
 */
public interface LogAdapter {
    void warn(String warnMessage, Object... args);

    void error(String message, Object... args);

    void debug(String message, Object... args);

    void info(String message, Object... args);

    void info(InputStream inputStream);

    boolean isDebugEnabled();

    default String format(String message, Object... args) {
        FormattingTuple tp = MessageFormatter.arrayFormat(message, args);
        String formattedMessage = tp.getMessage();
        return formattedMessage.replaceAll("%t", "\t").replaceAll("%n", "\n");
    }
}
