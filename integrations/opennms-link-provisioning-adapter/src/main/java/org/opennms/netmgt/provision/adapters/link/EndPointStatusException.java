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

package org.opennms.netmgt.provision.adapters.link;

/**
 * <p>EndPointStatusException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EndPointStatusException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2737843487100746888L;

    /**
     * <p>Constructor for EndPointStatusException.</p>
     */
    public EndPointStatusException() {
        super();
    }
    
    /**
     * <p>Constructor for EndPointStatusException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public EndPointStatusException(String message) {
        super(message);
    }
    
    /**
     * <p>Constructor for EndPointStatusException.</p>
     *
     * @param t a {@link java.lang.Throwable} object.
     */
    public EndPointStatusException(Throwable t) {
        super(t);
    }
    
    /**
     * <p>Constructor for EndPointStatusException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public EndPointStatusException(String message, Throwable t) {
        super(message, t);
    }
}
