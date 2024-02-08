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
package org.opennms.netmgt.provision;

/**
 * The Class EntityPluginException.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpHardwareInventoryException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 915744339081242021L;

    /**
     * The Constructor.
     */
    public SnmpHardwareInventoryException() {
        super();
    }

    /**
     * The Constructor.
     *
     * @param message the message
     * @param cause the cause
     * @param enableSuppression the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public SnmpHardwareInventoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * The Constructor.
     *
     * @param message the message
     * @param cause the cause
     */
    public SnmpHardwareInventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * The Constructor.
     *
     * @param message the message
     */
    public SnmpHardwareInventoryException(String message) {
        super(message);
    }

    /**
     * The Constructor.
     *
     * @param cause the cause
     */
    public SnmpHardwareInventoryException(Throwable cause) {
        super(cause);
    }

}
