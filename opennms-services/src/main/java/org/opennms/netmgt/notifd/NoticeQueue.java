//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.notifd;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;

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
    /**
     * 
     */
    private static final long serialVersionUID = 7463770974135218140L;

    /** {@inheritDoc} */
    @Override
    public NotificationTask putItem(Long key, NotificationTask value) {
        NotificationTask ret = super.putItem(key, value);

        Category log = ThreadCategory.getInstance(getClass());
        
        if (log.isDebugEnabled()) {
            if (value.getNotifyId() == -1) {
                log.debug("autoNotify task queued");
            } else {
                log.debug("task queued for notifyID " + value.getNotifyId());
            }
        }
        
        return ret;
    }
}
