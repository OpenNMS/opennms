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
