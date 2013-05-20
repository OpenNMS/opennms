/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * <p>NotificationDashlet class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationDashlet extends Dashlet {
    
    /*
    - Transient
    - Don't need to be able to acknowledge them
    - Items to show:
        - Show if they have been acknowledged
        - Description
        - Severity (color-coded)
        - Count
        - May need to show the node label when filtering on the node label
        - Show node/interface/service (maybe as a label)
    - Sort by severity (highest first)
    - Be able to page through results (first page, previous page, next page, last page)
    - Show "Outages x - y of z"
    - Ideally be able to sort by any column

     */
    
    private NotificationView m_view = new NotificationView(this);
    private NotificationLoader m_loader = new NotificationLoader();
    
    class NotificationLoader extends DashletLoader implements AsyncCallback<Notification[]> {
        
        private SurveillanceServiceAsync m_suveillanceService;
        
        public void load(final SurveillanceSet surveillanceSet) {
            try {
                loading();
                m_suveillanceService.getNotificationsForSet(surveillanceSet, this);
            } catch (Throwable e) {
                onFailure(e);
            }
        }
        
        public void onDataLoaded(Notification[] notifications) {
            try {
                m_view.setNotifications(notifications);
            } finally {
                complete();
            }
        }

        public void setSurveillanceService(SurveillanceServiceAsync svc) {
            m_suveillanceService = svc;
        }

        @Override
        public void onFailure(Throwable caught) {
            loadError(caught);
            error(caught);
        }

        @Override
        public void onSuccess(Notification[] result) {
            onDataLoaded(result);
        }
        
    }
    
    NotificationDashlet(Dashboard dashboard) {
        super(dashboard, "Notifications");
        setLoader(m_loader);
        setView(m_view);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setSurveillanceSet(SurveillanceSet set) {
        m_loader.load(set);
    }

    
    /**
     * <p>setSurveillanceService</p>
     *
     * @param svc a {@link org.opennms.dashboard.client.SurveillanceServiceAsync} object.
     */
    public void setSurveillanceService(SurveillanceServiceAsync svc) {
        m_loader.setSurveillanceService(svc);
    }

}
