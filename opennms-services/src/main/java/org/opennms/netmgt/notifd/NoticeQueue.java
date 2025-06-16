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
package org.opennms.netmgt.notifd;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a data class designed to hold NotificationTasks in an ordered map
 * that can handle collisions.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 */
public class NoticeQueue extends DuplicateTreeMap<Long, NotificationTask> {
    private static final Logger LOG = LoggerFactory.getLogger(NoticeQueue.class);
    /**
     * 
     */
    private static final long serialVersionUID = 7463770974135218140L;

    /** {@inheritDoc} */
    @Override
    public NotificationTask putItem(Long key, NotificationTask value) {
        NotificationTask ret = super.putItem(key, value);

        
        if (LOG.isDebugEnabled()) {
            if (value.getNotifyId() == -1) {
                LOG.debug("autoNotify task queued");
            } else {
                LOG.debug("task queued for notifyID {}", value.getNotifyId());
            }
        }
        
        return ret;
    }
}
