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
package org.opennms.netmgt.collectd.wmi;

/**
 * <P>
 * Encapsulates WmiExceptions and any other exceptions that occur during WMI
 * WPM collection and storage.
 * </P>
 *
 * @author <a href="mailto:matt.raykowski@gmail.com">Matt Raykowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class WmiCollectorException extends RuntimeException {
        /**
     * 
     */
    private static final long serialVersionUID = 7123162374663054884L;

        /**
         * <p>Constructor for WmiCollectorException.</p>
         *
         * @param message a {@link java.lang.String} object.
         * @param cause a {@link java.lang.Throwable} object.
         */
        public WmiCollectorException(final String message, final Throwable cause) {
            super(message, cause);
        }
}
