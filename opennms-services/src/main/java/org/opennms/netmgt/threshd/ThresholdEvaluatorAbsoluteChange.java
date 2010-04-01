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
 * 
 *  * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

/**
 * Implements an absolute change threshold check.  If the value changes by more than the specified amount
 * then it will trigger.  As for relative change, re-arm and trigger are unused
 */
public class ThresholdEvaluatorAbsoluteChange implements ThresholdEvaluator {
    
    private static final String TYPE = "absoluteChange";

    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold) {
        return new ThresholdEvaluatorStateAbsoluteChange(threshold);
    }

    public boolean supportsType(String type) {
        return TYPE.equals(type);
    }
    
    public static class ThresholdEvaluatorStateAbsoluteChange extends AbstractThresholdEvaluatorState {
        private BaseThresholdDefConfigWrapper m_thresholdConfig;
        private double m_change;

        private double m_lastSample = Double.NaN;
        
        private double m_previousTriggeringSample;

        public ThresholdEvaluatorStateAbsoluteChange(BaseThresholdDefConfigWrapper threshold) {
            Assert.notNull(threshold, "threshold argument cannot be null");

            setThresholdConfig(threshold);
        }

        public void setThresholdConfig(BaseThresholdDefConfigWrapper thresholdConfig) {
            Assert.notNull(thresholdConfig.getType(), "threshold must have a 'type' value set");
            Assert.notNull(thresholdConfig.getDatasourceExpression(), "threshold must have a 'ds-name' value set");
            Assert.notNull(thresholdConfig.getDsType(), "threshold must have a 'ds-type' value set");
            Assert.isTrue(thresholdConfig.hasValue(), "threshold must have a 'value' value set");
            Assert.isTrue(thresholdConfig.hasRearm(), "threshold must have a 'rearm' value set");
            Assert.isTrue(thresholdConfig.hasTrigger(), "threshold must have a 'trigger' value set");

            Assert.isTrue(TYPE.equals(thresholdConfig.getType()), "threshold for ds-name '" + thresholdConfig.getDatasourceExpression() + "' has type of '" + thresholdConfig.getType() + "', but this evaluator only supports thresholds with a 'type' value of '" + TYPE + "'");

            Assert.isTrue(thresholdConfig.getValue() != Double.NaN, "threshold must have a 'value' value that is a number");
            Assert.isTrue(thresholdConfig.getValue() != Double.POSITIVE_INFINITY && thresholdConfig.getValue() != Double.NEGATIVE_INFINITY, "threshold must have a 'value' value that is not positive or negative infinity");
            Assert.isTrue(thresholdConfig.getValue() != 0.0, "threshold must not be 0 for absolute change");

            m_thresholdConfig = thresholdConfig;
            setChange(thresholdConfig.getValue());
        }

        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        public Status evaluate(double dsValue) {
        	if(getLastSample()!=Double.NaN) {
	            double threshold = getLastSample()+getChange();
	
	            if (getChange() < 0.0) {
	            	//Negative change; care if the value is *below* the threshold
	                if (dsValue <= threshold) {
	                    setPreviousTriggeringSample(getLastSample());
	                    setLastSample(dsValue);
	                    return Status.TRIGGERED;
	                }
	            } else {
	            	//Positive change; care if the current value is *above* the threshold
	                if (dsValue >= threshold) {
	                    setPreviousTriggeringSample(getLastSample());
	                    setLastSample(dsValue);
	                    return Status.TRIGGERED;
	                }
	            }
        	}
            setLastSample(dsValue);
            return Status.NO_CHANGE;
        }

        public Double getLastSample() {
            return m_lastSample;
        }

        public void setLastSample(double lastSample) {
            m_lastSample = lastSample;
        }

        public Event getEventForState(Status status, Date date, double dsValue, CollectionResourceWrapper resource) {
            if (status == Status.TRIGGERED) {
                String uei=getThresholdConfig().getTriggeredUEI();
                if(uei==null || "".equals(uei)) {
                    uei=EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI;
                }
                return createBasicEvent(uei, date, dsValue, resource);
            } else {
                return null;
            }
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, CollectionResourceWrapper resource) {
            Map<String,String> params = new HashMap<String,String>();
            params.put("previousValue", formatValue(getPreviousTriggeringSample()));
            params.put("changeThreshold", Double.toString(getThresholdConfig().getValue()));
            return createBasicEvent(uei, date, dsValue, resource, params);
        }

        public double getPreviousTriggeringSample() {
            return m_previousTriggeringSample;
        }
        
        public void setPreviousTriggeringSample(double previousTriggeringSample) {
            m_previousTriggeringSample = previousTriggeringSample;
        }

        public double getChange() {
            return m_change;
        }

        public void setChange(double change) {
            m_change = change;
        }

        public ThresholdEvaluatorState getCleanClone() {
            return new ThresholdEvaluatorStateAbsoluteChange(m_thresholdConfig);
        }

        // FIXME This must be implemented correctly
        public boolean isTriggered() {
            return false;
        }

        // FIXME This must be implemented correctly
        public void clearState() {
        }
    }

}
