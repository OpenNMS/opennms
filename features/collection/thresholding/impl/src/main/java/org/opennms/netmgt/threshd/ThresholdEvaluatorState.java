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

package org.opennms.netmgt.threshd;

import java.util.Date;
import java.util.Objects;

import org.opennms.netmgt.threshd.api.ReinitializableState;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
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
public interface ThresholdEvaluatorState extends ReinitializableState {
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
    default Status evaluate(double dsValue) {
        return evaluate(dsValue, null);
    }

    Status evaluate(double dsValue, Long sequenceNumber);

    /**
     * @return the value that was evaluated along with the resulting status
     */
    ValueStatus evaluate(ExpressionThresholdValue valueSupplier, Long sequenceNumber)
            throws ThresholdExpressionException;
    
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

    ThresholdingSession getThresholdingSession();
    
    void setInstance(String instance);

    class ValueStatus {
        public final double value;
        public final Status status;

        public ValueStatus(double value, Status status) {
            this.value = value;
            this.status = Objects.requireNonNull(status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValueStatus that = (ValueStatus) o;
            return Double.compare(that.value, value) == 0 &&
                    status == that.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, status);
        }

        @Override
        public String toString() {
            return "ValueStatus{" +
                    "value=" + value +
                    ", status=" + status +
                    '}';
        }
    }
}
