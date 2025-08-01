/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.rrd.rrdtool;

import java.io.InputStream;
import java.util.Arrays;

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
		m_printLines = Arrays.copyOf(lines, lines.length);
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
