/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Apr 09: Expose data source indexes in threshold-derived events  - jeffg@opennms.org
 * 2009 Jan 19: Fix a comment. - dj@opennms.org
 * 2007 Jan 29: Extract evaluation and state related interface out of ThresholdEntity and other copies of the same code. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.threshd;

import java.util.Date;

import org.opennms.netmgt.xml.event.Event;

/**
 * Provides a method to evaluate a threshold and do basic population of
 * events.  There is an instance of ThresholdEvaluatorState for each
 * configured thresholding type on each configured data source on each
 * configured node/interface/etc..  The object that implements this
 * interface usually also stores state (hence the name).
 */
public interface ThresholdEvaluatorState {
    public enum Status {
        NO_CHANGE,
        TRIGGERED,
        RE_ARMED
    }

    public Status evaluate(double dsValue);

    //public Event getEventForState(Status status, Date date, double dsValue);
    public Event getEventForState(Status status, Date date, double dsValue, String instance);
    
    public BaseThresholdDefConfigWrapper getThresholdConfig();

    /**
     * Returns a "clean" (armed, non-triggered) clone of this object
     * @return
     */
    public ThresholdEvaluatorState getCleanClone();
}
