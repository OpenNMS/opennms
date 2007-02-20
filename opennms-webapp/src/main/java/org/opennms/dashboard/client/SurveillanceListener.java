package org.opennms.dashboard.client;

public interface SurveillanceListener {
    
    public void onAllClicked(Dashlet viewer);
    
    public void onSurveillanceGroupClicked(Dashlet viewer, SurveillanceGroup group);
    
    public void onIntersectionClicked(Dashlet viewer, SurveillanceIntersection intersection);

}
