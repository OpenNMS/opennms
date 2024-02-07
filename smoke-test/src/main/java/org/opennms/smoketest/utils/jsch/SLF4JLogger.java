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