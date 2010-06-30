/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
