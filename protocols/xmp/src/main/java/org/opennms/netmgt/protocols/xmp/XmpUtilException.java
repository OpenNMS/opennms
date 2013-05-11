/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.protocols.xmp;

/**
 * <p>XmpUtilException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class XmpUtilException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7653583871376609217L;

    String m_message;
    
    /**
     * <p>Constructor for XmpUtilException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public XmpUtilException(String msg) {
        m_message = msg;
    }
    
    /**
     * <p>getMessage</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getMessage() {
        return m_message;
    }
}
