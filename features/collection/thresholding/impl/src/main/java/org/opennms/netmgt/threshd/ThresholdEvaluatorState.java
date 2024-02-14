/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
