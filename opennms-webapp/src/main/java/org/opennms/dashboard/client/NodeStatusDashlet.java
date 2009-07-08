/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class NodeStatusDashlet extends Dashlet {
    

    private NodeStatusView m_view = new NodeStatusView(this);
    private NodeStatusLoader m_loader = new NodeStatusLoader();
    
    

    class NodeStatusLoader extends DashletLoader implements AsyncCallback {
        
        private SurveillanceServiceAsync m_suveillanceService;
        
        public void load(final SurveillanceSet surveillanceSet) {
            loading();
            m_suveillanceService.getRtcForSet(surveillanceSet, this);
        }
        
        public void onDataLoaded(NodeRtc[] rtcs) {
            try {
                m_view.setNodeRtc(rtcs);
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

        public void onSuccess(Object result) {
            onDataLoaded((NodeRtc[])result);
        }
        
    }
    
    public NodeStatusDashlet(Dashboard dashboard) {
        super(dashboard, "Node Status");
        setLoader(m_loader);
        setView(m_view);
    }

    public void setSurveillanceSet(SurveillanceSet set) {
        m_loader.load(set);
    }

    
    public void setSurveillanceService(SurveillanceServiceAsync svc) {
        m_loader.setSurveillanceService(svc);
    }


}
