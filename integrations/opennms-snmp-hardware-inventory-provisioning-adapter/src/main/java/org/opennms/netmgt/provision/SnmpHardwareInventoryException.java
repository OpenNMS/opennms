/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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
