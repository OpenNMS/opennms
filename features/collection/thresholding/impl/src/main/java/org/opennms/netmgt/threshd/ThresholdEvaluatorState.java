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

import java.io.Serializable;
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

    Status evaluate(double dsValue, ThresholdValues thresholdValues, Long sequenceNumber);

    /**
     * @return the value that was evaluated along with the resulting status
     */
    ValueStatus evaluate(ExpressionThresholdValueSupplier valueSupplier, Long sequenceNumber)
            throws ThresholdExpressionException;

    /**
     * @return the value that was evaluated along with the resulting status
     */
    ValueStatus evaluate(ThresholdValuesSupplier thresholdValuesSupplier, Long sequenceNumber)
            throws ThresholdExpressionException;
    
    /**
     * <p>getEventForState</p>
     *  @param status a {@link Status} object.
     * @param date a {@link Date} object.
     * @param dsValue a double.
     * @param thresholdValues
     * @param resource a {@link CollectionResourceWrapper} object.  @return a {@link Event} object.
     */
    public Event getEventForState(Status status, Date date, double dsValue, ThresholdValues thresholdValues, CollectionResourceWrapper resource);
    
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
        public final ThresholdValues thresholdValues;

        public ValueStatus(double value, Status status, ThresholdValues thresholdValues) {
            this.value = value;
            this.status = Objects.requireNonNull(status);
            this.thresholdValues = thresholdValues;
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

    class ThresholdValues implements Serializable {

        private static final long serialVersionUID = -788891453989005407L;
        private final Double threshold;
        private final Double rearm;
        private final Integer trigger;
        private Double dsValue;

        public ThresholdValues(Double threshold, Double rearm, Integer trigger) {
            this.threshold = threshold;
            this.rearm = rearm;
            this.trigger = trigger;
        }

        public Double getThresholdValue() {
            return threshold;
        }

        public Double getRearm() {
            return rearm;
        }

        public Integer getTrigger() {
            return trigger;
        }

        public Double getDsValue() {
            return dsValue;
        }

        public void setDsValue(Double dsValue) {
            this.dsValue = dsValue;
        }
    }
}
