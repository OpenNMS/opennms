/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.wmi;

/**
 * This object implements the internal exceptions used by the
 * <code>WmiManager</code> system.
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @version $Id: $
 */
public class WmiException extends Exception {
   /**
     * 
     */
    private static final long serialVersionUID = -2373078958094279134L;

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
	public WmiException(final String message) {
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
	public WmiException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor, sets the exception that caused this exception to be
	 * generated.
	 *
	 * @param cause
	 *            the exception that caused this exception to be generated.
	 */
	public WmiException(final Throwable cause) {
		super(cause);
	}
}
