/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.protocols.wmi;

/**
 * This object implements the internal exceptions used by the
 * <code>WmiManager</code> system.
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 */
public class WmiException extends Exception {
	/**
	 * Constructor.
	 */
	public WmiException() {
		super();
	}

	/**
	 * Constructor, sets the message pertaining to the exception problem.
	 * 
	 * @param message
	 *            the message pertaining to the exception problem.
	 */
	public WmiException(String message) {
		super(message);
	}

	/**
	 * Constructor, sets the message pertaining to the exception problem and
	 * the root cause exception (if applicable.)
	 * 
	 * @param message
	 *            the message pertaining to the exception problem.
	 * @param cause
	 *            the exception that caused this exception to be generated.
	 */
	public WmiException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor, sets the exception that caused this exception to be
	 * generated.
	 * 
	 * @param cause
	 *            the exception that caused this exception to be generated.
	 */
	public WmiException(Throwable cause) {
		super(cause);
	}
}
