/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.rrdtool;

import java.io.InputStream;

import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;

/**
 * <p>JniGraphDetails class.</p>
 *
 * @author <a href="mailto:alejandro@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class JniGraphDetails implements RrdGraphDetails {

	private int m_height;
	private int m_width;
	private String[] m_printLines;
	private InputStream m_inputStream;

	/**
	 * <p>Constructor for JniGraphDetails.</p>
	 *
	 * @param height a int.
	 * @param width a int.
	 * @param lines an array of {@link java.lang.String} objects.
	 * @param stream a {@link java.io.InputStream} object.
	 */
	public JniGraphDetails(int height, int width, String[] lines, InputStream stream) {
		m_height = height;
		m_width = width;
		m_printLines = lines;
		m_inputStream = stream;
	}

	/**
	 * <p>getHeight</p>
	 *
	 * @return a int.
	 * @throws org.opennms.netmgt.rrd.RrdException if any.
	 */
        @Override
	public int getHeight() throws RrdException {
		return m_height;
	}

	/**
	 * <p>getInputStream</p>
	 *
	 * @return a {@link java.io.InputStream} object.
	 * @throws org.opennms.netmgt.rrd.RrdException if any.
	 */
        @Override
	public InputStream getInputStream() throws RrdException {
		return m_inputStream;
	}

	/**
	 * <p>getPrintLines</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 * @throws org.opennms.netmgt.rrd.RrdException if any.
	 */
        @Override
	public String[] getPrintLines() throws RrdException {
		return m_printLines;
	}

	/**
	 * <p>getWidth</p>
	 *
	 * @return a int.
	 * @throws org.opennms.netmgt.rrd.RrdException if any.
	 */
        @Override
	public int getWidth() throws RrdException {
		return m_width;
	}

}
