/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: April 22, 2007
 * 
 * 
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.vacuumd;

/**
 * <p>AutomationException class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class AutomationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for AutomationException.</p>
	 *
	 * @param arg0 a {@link java.lang.String} object.
	 */
	public AutomationException(String arg0) {
		super(arg0);
	}

	/**
	 * <p>Constructor for AutomationException.</p>
	 *
	 * @param arg0 a {@link java.lang.Throwable} object.
	 */
	public AutomationException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * <p>Constructor for AutomationException.</p>
	 *
	 * @param arg0 a {@link java.lang.String} object.
	 * @param arg1 a {@link java.lang.Throwable} object.
	 */
	public AutomationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
