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

package org.opennms.netmgt.threshd;

import java.util.Date;

import org.opennms.netmgt.xml.event.Event;

/**
 * Provides a method to evaluate a threshold and do basic population of
 * events.  There is an instance of ThresholdEvaluatorState for each
 * configured thresholding type on each configured data source on each
 * configured node/interface/etc..  The object that implements this
 * interface usually also stores state (hence the name).
 *
 * @author ranger
 * @version $Id: $
 */
public interface ThresholdEvaluatorState {
    public enum Status {
        NO_CHANGE,
        TRIGGERED,
        RE_ARMED
    }

    /**
     * <p>evaluate</p>
     *
     * @param dsValue a double.
     * @return a {@link org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status} object.
     */
    public Status evaluate(double dsValue);

    /**
     * <p>getEventForState</p>
     *
     * @param status a {@link org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status} object.
     * @param date a {@link java.util.Date} object.
     * @param dsValue a double.
     * @param resource a {@link org.opennms.netmgt.threshd.CollectionResourceWrapper} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEventForState(Status status, Date date, double dsValue, CollectionResourceWrapper resource);
    
    /**
     * Return true if current state is TRIGGERED
     *
     * @return a boolean.
     */
    public boolean isTriggered();
    
    /**
     * <p>clearState</p>
     */
    public void clearState();
    
    /**
     * <p>getThresholdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     */
    public BaseThresholdDefConfigWrapper getThresholdConfig();

    /**
     * Returns a "clean" (armed, non-triggered) clone of this object
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdEvaluatorState} object.
     */
    public ThresholdEvaluatorState getCleanClone();
}
