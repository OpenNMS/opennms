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

package org.opennms.netmgt.rrd;

import java.io.InputStream;

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
        m_printLines = printLines;
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
