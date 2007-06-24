package org.opennms.dashboard.client;

import java.util.Iterator;
import java.util.Vector;


public class SurveillanceListenerCollection extends Vector {
    private static final long serialVersionUID = 1L;

    public void fireAllClicked(Dashlet viewer) {
        for (Iterator it = iterator(); it.hasNext();) {
            SurveillanceListener listener = (SurveillanceListener) it.next();
            listener.onAllClicked(viewer);
          }
    }
    
    public void fireSurveillanceGroupClicked(Dashlet viewer, SurveillanceGroup group) {
        for (Iterator it = iterator(); it.hasNext();) {
            SurveillanceListener listener = (SurveillanceListener) it.next();
            listener.onSurveillanceGroupClicked(viewer, group);
          }
    }
    
    public void fireIntersectionClicked(Dashlet viewer, SurveillanceIntersection intersection) {
        for (Iterator it = iterator(); it.hasNext();) {
            SurveillanceListener listener = (SurveillanceListener) it.next();
            listener.onIntersectionClicked(viewer, intersection);
          }
    }


}
