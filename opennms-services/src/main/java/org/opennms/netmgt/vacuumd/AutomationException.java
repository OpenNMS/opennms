/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vacuumd;

/**
 * <p>AutomationException class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class AutomationException extends RuntimeException {

    private static final long serialVersionUID = -8873671974245928627L;

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
