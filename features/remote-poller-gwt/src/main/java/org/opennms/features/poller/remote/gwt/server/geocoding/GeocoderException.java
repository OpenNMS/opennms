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

package org.opennms.features.poller.remote.gwt.server.geocoding;

/**
 * <p>GeocoderException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GeocoderException extends Exception {

	private static final long serialVersionUID = 4375009213100201730L;

	/**
	 * <p>Constructor for GeocoderException.</p>
	 */
	public GeocoderException() {
		super();
	}

	/**
	 * <p>Constructor for GeocoderException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public GeocoderException(String message) {
		super(message);
	}

	/**
	 * <p>Constructor for GeocoderException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public GeocoderException(Throwable cause) {
		super(cause);
	}

	/**
	 * <p>Constructor for GeocoderException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public GeocoderException(String message, Throwable cause) {
		super(message, cause);
	}

}
