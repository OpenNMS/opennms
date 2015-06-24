/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.notification.filter;

import org.opennms.web.filter.InFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>NotificationIdListFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationIdListFilter extends InFilter<Integer> {
    /** Constant <code>TYPE="notificationIdList"</code> */
    public static final String TYPE = "notificationIdList";
    //private int[] m_notificationIds;
    
    private static Integer[] box(int[] values) {
        if (values == null) {
            return null;
        }
        
        Integer[] boxed = new Integer[values.length];
        for(int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        
        return boxed;
    }
    
    /**
     * <p>Constructor for NotificationIdListFilter.</p>
     *
     * @param notificationIds an array of {@link java.lang.Integer} objects.
     */
    public NotificationIdListFilter(Integer[] notificationIds) {
        super(TYPE, SQLType.INT, "NOTIFICATIONS.NOTIFYID", "notifyId", notificationIds);
    }

    /**
     * <p>Constructor for NotificationIdListFilter.</p>
     *
     * @param notificationIds an array of int.
     */
    public NotificationIdListFilter(int[] notificationIds){
        super(TYPE, SQLType.INT, "NOTIFICATIONS.NOTIFYID", "notifyId", box(notificationIds));
    }

    /** {@inheritDoc} */
    @Override
    public String getTextDescription() {
        return String.format("notifyId in (%s)", getValueString());
    }

}
