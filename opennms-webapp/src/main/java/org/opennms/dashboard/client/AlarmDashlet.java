/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * <p>AlarmDashlet class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class AlarmDashlet extends Dashlet {
    
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
    
    private AlarmView m_view = new AlarmView(this);
    private AlarmLoader m_loader = new AlarmLoader();
    
    class AlarmLoader extends DashletLoader implements AsyncCallback<Alarm[]> {
        
        private SurveillanceServiceAsync m_suveillanceService;
        
        public void load(final SurveillanceSet surveillanceSet) {
            loading();
            m_suveillanceService.getAlarmsForSet(surveillanceSet, this);
        }
        
        public void onDataLoaded(Alarm[] alarms) {
            try {
                m_view.setAlarms(alarms);
            } finally {
                complete();
            }
        }

        public void setSurveillanceService(SurveillanceServiceAsync svc) {
            m_suveillanceService = svc;
        }

        public void onFailure(Throwable caught) {
            loadError(caught);
            error(caught);
        }

        public void onSuccess(Alarm[] result) {
            onDataLoaded(result);
        }
        
    }
    
    AlarmDashlet(Dashboard dashboard) {
        super(dashboard, "Alarms");
        setLoader(m_loader);
        setView(m_view);
    }
    
    /** {@inheritDoc} */
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
