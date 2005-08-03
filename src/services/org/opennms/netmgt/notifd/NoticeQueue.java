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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Category;

import org.opennms.core.utils.ThreadCategory;

/**
 * This is a data class designed to hold NotificationTasks in an ordered map
 * that can handle collisions.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 */
public class NoticeQueue extends TreeMap {
    public NoticeQueue() {
        super();
    }

    public NoticeQueue(Comparator c) {
        super(c);
    }

    public NoticeQueue(Map m) {
        super(m);
    }

    public NoticeQueue(SortedMap m) {
        super(m);
    }

    public Object put(Object key, Object task) {
        Category log = ThreadCategory.getInstance(getClass());
        Object result = null;

        // see if there is a collision
        if (super.containsKey(key)) {
            Object o = super.get(key);
            if (o instanceof NotificationTask) {
                List duplicate = new ArrayList();
                duplicate.add(o);
                duplicate.add(task);

                result = super.put(key, duplicate);
            } else if (o instanceof List) {
                ((List) o).add(task);
                result = o;
            }
        } else {
            result = super.put(key, task);
        }
        if (log.isDebugEnabled()) {
            if (task instanceof NotificationTask) {
                NotificationTask notice = (NotificationTask) task;
                if(notice.getNotifyId() == -1) {
                    log.debug("autoNotify task queued");
                } else {
                    log.debug("task queued for notifyID " + notice.getNotifyId());
                }
            } else {
                log.debug("task is not an instance of NotificationTask");
            }
        }
        return result;
    }

    public Object remove(Object task) {
        Object result = null;

        if (task instanceof NotificationTask) {
            NotificationTask notice = (NotificationTask) task;
            Long key = new Long(notice.getSendTime());

            Object o = get(key);

            if (o instanceof NotificationTask) {
                result = super.remove(key);
            } else if (o instanceof List) {
                ((List) o).remove(task);
                result = task;
            }
        } else {
            result = super.remove(task);
        }

        return result;
    }

    public Collection values() {
        Collection originals = super.values();
        Collection expanded = new ArrayList();

        Iterator i = originals.iterator();
        while (i.hasNext()) {
            Object next = i.next();
            if (next instanceof NotificationTask) {
                expanded.add(next);
            } else if (next instanceof List) {
                expanded.addAll((List) next);
            }
        }

        return expanded;
    }

    public String toString() {
        Collection values = values();
        StringBuffer buffer = new StringBuffer();

        Iterator i = values.iterator();
        while (i.hasNext()) {
            buffer.append(i.next().toString() + System.getProperty("line.separator"));
        }

        return buffer.toString();
    }
}
