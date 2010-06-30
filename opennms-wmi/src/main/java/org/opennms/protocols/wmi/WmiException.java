//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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
