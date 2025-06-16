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
