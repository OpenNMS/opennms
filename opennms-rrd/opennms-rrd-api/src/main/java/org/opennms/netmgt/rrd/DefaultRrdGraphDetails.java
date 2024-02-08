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
package org.opennms.netmgt.rrd;

import java.io.InputStream;
import java.util.Arrays;

/**
 * Simple RrdGraphDetails implementation.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DefaultRrdGraphDetails implements RrdGraphDetails {
    private InputStream m_inputStream;
    private String[] m_printLines;
    private int m_width;
    private int m_height;
    
    /**
     * <p>getHeight</p>
     *
     * @return a int.
     */
    @Override
    public int getHeight() {
        return m_height;
    }
    
    /**
     * <p>setHeight</p>
     *
     * @param height a int.
     */
    public void setHeight(int height) {
        m_height = height;
    }
    
    /**
     * <p>getInputStream</p>
     *
     * @return a {@link java.io.InputStream} object.
     */
    @Override
    public InputStream getInputStream() {
        return m_inputStream;
    }
    
    /**
     * <p>setInputStream</p>
     *
     * @param inputStream a {@link java.io.InputStream} object.
     */
    public void setInputStream(InputStream inputStream) {
        m_inputStream = inputStream;
    }
    
    /**
     * <p>getPrintLines</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    @Override
    public String[] getPrintLines() {
        return m_printLines;
    }
    
    /**
     * <p>setPrintLines</p>
     *
     * @param printLines an array of {@link java.lang.String} objects.
     */
    public void setPrintLines(String[] printLines) {
        m_printLines = Arrays.copyOf(printLines, printLines.length);
    }
    
    /**
     * <p>getWidth</p>
     *
     * @return a int.
     */
    @Override
    public int getWidth() {
        return m_width;
    }
    
    /**
     * <p>setWidth</p>
     *
     * @param width a int.
     */
    public void setWidth(int width) {
        m_width = width;
    }
}
