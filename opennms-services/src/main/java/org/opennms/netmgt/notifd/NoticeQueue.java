/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
