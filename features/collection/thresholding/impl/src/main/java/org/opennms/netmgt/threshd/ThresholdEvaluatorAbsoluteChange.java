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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Implements an absolute change threshold check.  If the value changes by more than the specified amount
 * then it will trigger.  As for relative change, re-arm and trigger are unused
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdEvaluatorAbsoluteChange implements ThresholdEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdEvaluatorAbsoluteChange.class);
    private static final ThresholdType TYPE = ThresholdType.ABSOLUTE_CHANGE;

    /** {@inheritDoc} */
    @Override
    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
        return new ThresholdEvaluatorStateAbsoluteChange(threshold, thresholdingSession);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsType(ThresholdType type) {
        return TYPE.equals(type);
    }
    
    public static class ThresholdEvaluatorStateAbsoluteChange extends AbstractThresholdEvaluatorState<ThresholdEvaluatorStateAbsoluteChange.State> {
        private BaseThresholdDefConfigWrapper m_thresholdConfig;

        static class State extends AbstractThresholdEvaluatorState.AbstractState {
            private static final long serialVersionUID = 1L;
            private double m_lastSample = Double.NaN;
            private double m_previousTriggeringSample;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("lastSample=").append(m_lastSample);
                sb.append("\npreviousTriggeringSample=").append(m_previousTriggeringSample);
                String superString = super.toString();

                if (superString != null) {
                    sb.append("\n").append(superString);
                }

                return sb.toString();
            }
        }

        public ThresholdEvaluatorStateAbsoluteChange(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
            super(threshold, thresholdingSession, ThresholdEvaluatorStateAbsoluteChange.State.class);
            setThresholdConfig(threshold);
        }

        @Override
        protected void initializeState() {
            state = new State();
        }

        private void setThresholdConfig(BaseThresholdDefConfigWrapper thresholdConfig) {
            Assert.notNull(thresholdConfig.getType(), "threshold must have a 'type' value set");
            Assert.notNull(thresholdConfig.getDatasourceExpression(), "threshold must have a 'ds-name' value set");
            Assert.notNull(thresholdConfig.getDsType(), "threshold must have a 'ds-type' value set");
            Assert.isTrue(thresholdConfig.hasValue(), "threshold must have a 'value' value set");
            Assert.isTrue(thresholdConfig.hasRearm(), "threshold must have a 'rearm' value set");
            Assert.isTrue(thresholdConfig.hasTrigger(), "threshold must have a 'trigger' value set");

            Assert.isTrue(TYPE.equals(thresholdConfig.getType()), "threshold for ds-name '" + thresholdConfig.getDatasourceExpression() + "' has type of '" + thresholdConfig.getType() + "', but this evaluator only supports thresholds with a 'type' value of '" + TYPE + "'");

            m_thresholdConfig = thresholdConfig;
        }

        @Override
        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        @Override
        public Status evaluateAfterFetch(double dsValue, ThresholdValues thresholdValues) {
            if(!Double.isNaN(getLastSample())) {
                Double change = thresholdValues != null && thresholdValues.getThresholdValue() != null ?
                        thresholdValues.getThresholdValue() : m_thresholdConfig.getValue();
                if(change == null) {
                    return Status.NO_CHANGE;
                }
                double threshold = getLastSample()+ change;

                if (change < 0.0) {
                    //Negative change; care if the value is *below* the threshold
                    if (dsValue <= threshold) {
                        setPreviousTriggeringSample(getLastSample());
                        setLastSample(dsValue);
                        LOG.debug("evaluate: absolute negative change threshold triggered");
                        return Status.TRIGGERED;
                    }
                } else {
                    //Positive change; care if the current value is *above* the threshold
                    if (dsValue >= threshold) {
                        setPreviousTriggeringSample(getLastSample());
                        setLastSample(dsValue);
                        LOG.debug("evaluate: absolute positive change threshold triggered");
                        return Status.TRIGGERED;
                    }
                }
            }
            setLastSample(dsValue);
            return Status.NO_CHANGE;
        }

        private Double getLastSample() {
            return state.m_lastSample;
        }

        private void setLastSample(double lastSample) {
            if (lastSample != state.m_lastSample) {
                state.m_lastSample = lastSample;
                markDirty();
            }
        }

        @Override
        public Event getEventForState(Status status, Date date, double dsValue, ThresholdValues thresholdValues, CollectionResourceWrapper resource) {
            if (status == Status.TRIGGERED) {
                final String uei=getThresholdConfig().getTriggeredUEI().orElse(EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI);
                return createBasicEvent(uei, date, dsValue, thresholdValues, resource);
            } else {
                return null;
            }
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, ThresholdValues thresholdValues, CollectionResourceWrapper resource) {
            if(thresholdValues == null) {
                thresholdValues = state.getThresholdValues();
            }
            Map<String,String> params = new HashMap<String,String>();
            params.put("previousValue", formatValue(getPreviousTriggeringSample()));
            params.put("changeThreshold", Double.toString(thresholdValues.getThresholdValue()));
            params.put("trigger", Integer.toString(thresholdValues.getTrigger()));
            return createBasicEvent(uei, date, dsValue, resource, params);
        }

        private double getPreviousTriggeringSample() {
            return state.m_previousTriggeringSample;
        }

        private void setPreviousTriggeringSample(double previousTriggeringSample) {
            if (state.m_previousTriggeringSample != previousTriggeringSample) {
                state.m_previousTriggeringSample = previousTriggeringSample;
                markDirty();
            }
        }

        @Override
        public ThresholdEvaluatorState getCleanClone() {
            return new ThresholdEvaluatorStateAbsoluteChange(m_thresholdConfig, getThresholdingSession());
        }

        // FIXME This must be implemented correctly
        @Override
        public boolean isTriggered() {
            return false;
        }

        @Override
        public void clearStateBeforePersist() {
            initializeState();
        }
    }

}
