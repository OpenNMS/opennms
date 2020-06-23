/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

/**
 * <p>RuntimePersistenceException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RuntimePersistenceException extends RuntimeException {

    private static final long serialVersionUID = 8462261466853731107L;

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public RuntimePersistenceException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public RuntimePersistenceException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public RuntimePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
