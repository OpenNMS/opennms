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
        private double m_change;

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

            Assert.isTrue(!Double.isNaN(thresholdConfig.getValue()), "threshold must have a 'value' value that is a number");
            Assert.isTrue(thresholdConfig.getValue() != Double.POSITIVE_INFINITY && thresholdConfig.getValue() != Double.NEGATIVE_INFINITY, "threshold must have a 'value' value that is not positive or negative infinity");
            Assert.isTrue(thresholdConfig.getValue() != 0.0, "threshold must not be 0 for absolute change");

            m_thresholdConfig = thresholdConfig;
            setChange(thresholdConfig.getValue());
        }

        @Override
        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        @Override
        public Status evaluateAfterFetch(double dsValue) {
            if(!Double.isNaN(getLastSample())) {
                double threshold = getLastSample()+getChange();

                if (getChange() < 0.0) {
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
        public Event getEventForState(Status status, Date date, double dsValue, CollectionResourceWrapper resource) {
            if (status == Status.TRIGGERED) {
                final String uei=getThresholdConfig().getTriggeredUEI().orElse(EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI);
                return createBasicEvent(uei, date, dsValue, resource);
            } else {
                return null;
            }
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, CollectionResourceWrapper resource) {
            Map<String,String> params = new HashMap<String,String>();
            params.put("previousValue", formatValue(getPreviousTriggeringSample()));
            params.put("changeThreshold", Double.toString(getThresholdConfig().getValue()));
            params.put("trigger", Integer.toString(getThresholdConfig().getTrigger()));
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

        private double getChange() {
            return m_change;
        }

        private void setChange(double change) {
            m_change = change;
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
