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
package org.opennms.features.vaadin.config;

import org.slf4j.LoggerFactory;
import org.opennms.features.vaadin.api.Logger;

/**
 * The Class Simple Logger.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SimpleLogger implements Logger {

    /** The Constant LOG. */
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
