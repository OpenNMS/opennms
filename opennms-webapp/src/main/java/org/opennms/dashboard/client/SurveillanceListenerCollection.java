/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.dashboard.client;

import java.util.Iterator;
import java.util.Vector;

/**
 * <p>SurveillanceListenerCollection class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceListenerCollection extends Vector<SurveillanceListener> {
    private static final long serialVersionUID = 5693264759623736384L;

    /**
     * <p>fireAllClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     */
    public void fireAllClicked(Dashlet viewer) {
        for (Iterator<SurveillanceListener> it = iterator(); it.hasNext();) {
            SurveillanceListener listener = it.next();
            listener.onAllClicked(viewer);
          }
    }
    
    /**
     * <p>fireSurveillanceGroupClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     * @param group a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public void fireSurveillanceGroupClicked(Dashlet viewer, SurveillanceGroup group) {
        for (Iterator<SurveillanceListener> it = iterator(); it.hasNext();) {
            SurveillanceListener listener = it.next();
            listener.onSurveillanceGroupClicked(viewer, group);
          }
    }
    
    /**
     * <p>fireIntersectionClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     * @param intersection a {@link org.opennms.dashboard.client.SurveillanceIntersection} object.
     */
    public void fireIntersectionClicked(Dashlet viewer, SurveillanceIntersection intersection) {
        for (Iterator<SurveillanceListener> it = iterator(); it.hasNext();) {
            SurveillanceListener listener = it.next();
            listener.onIntersectionClicked(viewer, intersection);
          }
    }


}
