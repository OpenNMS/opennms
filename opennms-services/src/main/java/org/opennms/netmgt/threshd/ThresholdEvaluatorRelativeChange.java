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
 * 2007 Jan 29: Implementation for triggering on relative changes between two samples
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.util.Assert;

/**
 * Implements a relative change threshold check.  A 'value' setting of
 * less than 1.0 means that a threshold will fire if the current value
 * is less than or equal to the previous value multiplied by the 'value'
 * setting.  A 'value' setting greater than 1.0 causes the threshold to
 * fire if the current value is greater than or equal to the previous
 * value multiplied by the 'value' setting.  A 'value' setting of 1.0
 * (unity) is not allowed, as it represents no change.  Zero valued
 * samples (0.0) are ignored, as 0.0 multiplied by anything is 0.0 (if
 * they were not ignored, an interface that gets no traffic would always
 * trigger a threshold, for example).
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdEvaluatorRelativeChange implements ThresholdEvaluator {
    
    private static final String TYPE = "relativeChange";

    /** {@inheritDoc} */
    public ThresholdEvaluatorState getThresholdEvaluatorState(BaseThresholdDefConfigWrapper threshold) {
        return new ThresholdEvaluatorStateRelativeChange(threshold);
    }

    /** {@inheritDoc} */
    public boolean supportsType(String type) {
        return TYPE.equals(type);
    }
    
    public static class ThresholdEvaluatorStateRelativeChange implements ThresholdEvaluatorState {
        private BaseThresholdDefConfigWrapper m_thresholdConfig;
        private double m_multiplier;

        private double m_lastSample = 0.0;
        private double m_previousTriggeringSample;

        public ThresholdEvaluatorStateRelativeChange(BaseThresholdDefConfigWrapper threshold) {
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
            Assert.isTrue(thresholdConfig.getValue() != 1.0, "threshold must not be unity (1.0)");

            m_thresholdConfig = thresholdConfig;
            setMultiplier(thresholdConfig.getValue());
        }

        public BaseThresholdDefConfigWrapper getThresholdConfig() {
            return m_thresholdConfig;
        }

        public Status evaluate(double dsValue) {
        	//Fix for Bug 2275 so we handle negative numbers
        	//It will not handle values which cross the 0 boundary (from - to +, or v.v.) properly, but
        	// after some discussion, we can't come up with a sensible scenario when that would actually happen.
        	// If such a scenario eventuates, reconsider
        	dsValue=Math.abs(dsValue);
            if (getLastSample() != 0.0) {
                double threshold = getMultiplier() * getLastSample();

                if (getMultiplier() < 1.0) {
                    if (dsValue <= threshold) {
                        setPreviousTriggeringSample(getLastSample());
                        setLastSample(dsValue);
                        return Status.TRIGGERED;
                    }
                } else {
                    if (dsValue >= threshold) {
                        setPreviousTriggeringSample(getLastSample());
                        setLastSample(dsValue);
                        return Status.TRIGGERED;
                    }
                }

                setLastSample(dsValue);
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

        public Event getEventForState(Status status, Date date, double dsValue, String dsInstance) {
            if (status == Status.TRIGGERED) {
                String uei=getThresholdConfig().getTriggeredUEI();
                if(uei==null || "".equals(uei)) {
                    uei=EventConstants.RELATIVE_CHANGE_THRESHOLD_EVENT_UEI;
                }
                return createBasicEvent(uei, date, dsValue, dsInstance);
            } else {
                return null;
            }
        }
        
        private Event createBasicEvent(String uei, Date date, double dsValue, String dsInstance) {
            // create the event to be sent
            Event event = new Event();
            event.setUei(uei);

            // set the source of the event to the datasource name
            event.setSource("OpenNMS.Threshd." + getThresholdConfig().getDatasourceExpression());

            // Set event host
            try {
                event.setHost(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                event.setHost("unresolved.host");
                log().warn("Failed to resolve local hostname: " + e, e);
            }

            // Set event time
            event.setTime(EventConstants.formatToString(date));

            // Add appropriate parms
            Parms eventParms = new Parms();
            Parm eventParm = null;
            Value parmValue = null;

            // Add datasource name
            eventParm = new Parm();
            eventParm.setParmName("ds");
            parmValue = new Value();
            parmValue.setContent(getThresholdConfig().getDatasourceExpression());
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            // Add last known value of the datasource fetched from its RRD file
            eventParm = new Parm();
            eventParm.setParmName("value");
            parmValue = new Value();
            String pattern = System.getProperty("org.opennms.threshd.value.decimalformat", "###.##");
            DecimalFormat valueFormatter = new DecimalFormat(pattern);
            parmValue.setContent(valueFormatter.format(dsValue));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            eventParm = new Parm();
            eventParm.setParmName("previousValue");
            parmValue = new Value();
            parmValue.setContent(valueFormatter.format(getPreviousTriggeringSample()));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            eventParm = new Parm();
            eventParm.setParmName("multiplier");
            parmValue = new Value();
            parmValue.setContent(Double.toString(getThresholdConfig().getValue()));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            /*
            eventParm = new Parm();
            eventParm.setParmName("trigger");
            parmValue = new Value();
            parmValue.setContent(Integer.toString(getThresholdConfig().getTrigger()));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
            */

            /*
            eventParm = new Parm();
            eventParm.setParmName("rearm");
            parmValue = new Value();
            parmValue.setContent(Double.toString(getThresholdConfig().getRearm()));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
            */
            
            // Add the instance name of the resource in question
            eventParm = new Parm();
            eventParm.setParmName("instance");
            parmValue = new Value();
            parmValue.setContent(dsInstance != null ? dsInstance : "null");
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
            
            // Add Parms to the event
            event.setParms(eventParms);
            
            return event;
        }

        public double getPreviousTriggeringSample() {
            return m_previousTriggeringSample;
        }
        
        public void setPreviousTriggeringSample(double previousTriggeringSample) {
            m_previousTriggeringSample = previousTriggeringSample;
        }

        public double getMultiplier() {
            return m_multiplier;
        }

        public void setMultiplier(double multiplier) {
            m_multiplier = multiplier;
        }

        private final Category log() {
            return ThreadCategory.getInstance(getClass());
        }
        
        public ThresholdEvaluatorState getCleanClone() {
            return new ThresholdEvaluatorStateRelativeChange(m_thresholdConfig);
        }
    }

}
