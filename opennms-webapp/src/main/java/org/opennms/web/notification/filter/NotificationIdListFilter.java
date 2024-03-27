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
