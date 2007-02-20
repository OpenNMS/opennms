package org.opennms.dashboard.client;

public interface SurveillanceListener {
    
    public void onAllClicked(SurveillanceDashlet viewer);
    
    public void onSurveillanceGroupClicked(SurveillanceDashlet viewer, SurveillanceGroup group);
    
    public void onIntersectionClicked(SurveillanceDashlet viewer, SurveillanceIntersection intersection);

}
