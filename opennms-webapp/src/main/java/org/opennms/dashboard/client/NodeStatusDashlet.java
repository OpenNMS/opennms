package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
