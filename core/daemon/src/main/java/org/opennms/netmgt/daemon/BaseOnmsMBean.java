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
package org.opennms.netmgt.daemon;

/**
 * <p>BaseOnmsMBean interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface BaseOnmsMBean {

    /**
     * Initialization invoked prior to start.
     */
    void init();

    /**
     * Starts the current managed bean.
     */
    void start();

    /**
     * Starts the current managed bean.
     */
    void stop();

    /**
     * The current status of the managed bean. This is a representation of the
     * managed bean's run state as defined by the Fiber
     * interface.
     *
     * @return a int.
     */
    int getStatus();

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String status();

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getStatusText();

    long getStartTimeMilliseconds();

}
