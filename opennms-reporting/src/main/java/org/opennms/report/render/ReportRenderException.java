/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.report.render;

/**
 * <p>ReportRenderException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportRenderException extends Exception {

    private static final long serialVersionUID = 3345608823051705228L;

    /**
     * <p>Constructor for ReportRenderException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public ReportRenderException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>Constructor for ReportRenderException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public ReportRenderException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>Constructor for ReportRenderException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public ReportRenderException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
