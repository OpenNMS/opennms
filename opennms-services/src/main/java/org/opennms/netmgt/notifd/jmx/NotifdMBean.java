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
package org.opennms.netmgt.notifd.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>NotifdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface NotifdMBean extends BaseOnmsMBean {

    /**
     * @return The total number of notification tasks enqueued since Notifd
     *         was last started
     */
    public long getNotificationTasksQueued();

    /**
     * @return The total number of binary notifications attempted since Notifd
     *         was last started
     */
    public long getBinaryNoticesAttempted();

    /**
     * @return The total number of Java notifications attempted since Notifd
     *         was last started
     */
    public long getJavaNoticesAttempted();

    /**
     * @return The total number of attempted binary notifications that
     *         succeeded since Notifd was last started
     */
    public long getBinaryNoticesSucceeded();

    /**
     * @return The total number of attempted Java notifications that succeeded
     *         since Notifd was last started
     */
    public long getJavaNoticesSucceeded();

    /**
     * @return The total number of attempted binary notifications that failed
     *         (returned a non-zero exit code) since Notifd was last started
     */
    public long getBinaryNoticesFailed();

    /**
     * @return The total number of attempted Java notifications that failed
     *         (returned a non-zero value) since Notifd was last started
     */
    public long getJavaNoticesFailed();

    /**
     * @return The total number of attempted binary notifications that were
     *         interrupted (threw an exception) since Notifd was last started.
     */
    public long getBinaryNoticesInterrupted();

    /**
     * @return The total number of attempted Java notifications that were
     *         interrupted (threw an exception) since Notifd was last started
     */
    public long getJavaNoticesInterrupted();

    /**
     * @return The total number of unknown notifications that were interrupted
     *         (threw an exception) since Notifd was last started.
     */
    public long getUnknownNoticesInterrupted();
}
