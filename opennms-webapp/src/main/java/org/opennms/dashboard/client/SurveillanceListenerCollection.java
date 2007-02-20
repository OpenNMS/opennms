package org.opennms.dashboard.client;

import java.util.Iterator;
import java.util.Vector;


public class SurveillanceListenerCollection extends Vector {
    
    public void fireAllClicked(SurveillanceDashlet viewer) {
        for (Iterator it = iterator(); it.hasNext();) {
            SurveillanceListener listener = (SurveillanceListener) it.next();
            listener.onAllClicked(viewer);
          }
    }
    
    public void fireSurveillanceGroupClicked(SurveillanceDashlet viewer, SurveillanceGroup group) {
        for (Iterator it = iterator(); it.hasNext();) {
            SurveillanceListener listener = (SurveillanceListener) it.next();
            listener.onSurveillanceGroupClicked(viewer, group);
          }
    }
    
    public void fireIntersectionClicked(SurveillanceDashlet viewer, SurveillanceIntersection intersection) {
        for (Iterator it = iterator(); it.hasNext();) {
            SurveillanceListener listener = (SurveillanceListener) it.next();
            listener.onIntersectionClicked(viewer, intersection);
          }
    }


}
