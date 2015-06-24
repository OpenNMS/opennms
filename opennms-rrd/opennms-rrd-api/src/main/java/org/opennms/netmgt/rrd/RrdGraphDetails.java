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

package org.opennms.netmgt.rrd;

import java.io.InputStream;

/**
 * Container for details from an RRD graph.  Stores the graph image (if any)
 * and details relating to the graph.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface RrdGraphDetails {
    /**
     * Gets the PNG image representing the graph.  If a graph wasn't created,
     * an RrdException will be thrown.
     *
     * @return InputStream containg a PNG image representing the graph
     * @throws org.opennms.netmgt.rrd.RrdException if there is an error getting an input stream for
     *      the graph, such as if no graph image was created
     */
    public InputStream getInputStream() throws RrdException;
    
    /**
     * Gets the PRINT lines associated with the graph command.
     *
     * @return PRINT lines associated with the graph command.  If there were
     *      no PRINT lines, an empty array is returned.
     * @throws org.opennms.netmgt.rrd.RrdException if there is an error getting the PRINT lines
     */
    public String[] getPrintLines() throws RrdException;
    
    /**
     * Gets the height of the PNG image.
     *
     * @return height of the PNG image in pixels.  This is the height of the
     *      entire PNG image, not just the height of the graph box within
     *      the image.
     * @throws org.opennms.netmgt.rrd.RrdException if no graph image was produced or if there is an
     *      error getting the height
     */
    public int getHeight() throws RrdException;

    /**
     * Gets the width of the PNG image.
     *
     * @return width of the PNG image in pixels.  This is the width of the
     *      entire PNG image, not just the width of the graph box within
     *      the image.
     * @throws org.opennms.netmgt.rrd.RrdException if no graph image was produced or if there is an
     *      error getting the height
     */
    public int getWidth() throws RrdException;
}
