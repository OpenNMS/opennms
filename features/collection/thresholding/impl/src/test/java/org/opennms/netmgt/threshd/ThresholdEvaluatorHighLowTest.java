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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.threshd.ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow;
import org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ThrowableAnticipator;

public class ThresholdEvaluatorHighLowTest extends AbstractThresholdEvaluatorTestCase {

    @Test
    public void testConstructorThresholdNull() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold argument cannot be null"));
        
        try {
            new ThresholdEvaluatorStateHighLow(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testConstructorThresholdNoType() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'type' value set"));
        Threshold threshold = new Threshold();
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(1.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        
        try {
            new ThresholdEvaluatorStateHighLow(wrapper);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testConstructorThresholdNoDsName() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'ds-name' value set"));
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsType("node");
        threshold.setValue(1.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        try {
            new ThresholdEvaluatorStateHighLow(wrapper);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testConstructorThresholdNoDsType() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'ds-type' value set"));
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setValue(1.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       
        try {
            new ThresholdEvaluatorStateHighLow(wrapper);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testConstructorThresholdNoValue() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'value' value set"));
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setRearm(0.5);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       
        try {
            new ThresholdEvaluatorStateHighLow(wrapper);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testConstructorThresholdNoRearm() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'rearm' value set"));
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(1.0);
        threshold.setTrigger(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
      
        try {
            new ThresholdEvaluatorStateHighLow(wrapper);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testConstructorThresholdNoTrigger() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'trigger' value set"));
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(1.0);
        threshold.setRearm(0.5);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        
        try {
            new ThresholdEvaluatorStateHighLow(wrapper);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testEvaluateHighNoTrigger() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(101.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
      
        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    @Test
    public void testEvaluateHighTriggerOnce() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       
        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.TRIGGERED, status);
    }
    
    @Test
    public void testEvaluateHighNoTriggerTwice() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(2);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    @Test
    public void testEvaluateHighTriggerTwice() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(2);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", Status.NO_CHANGE, status);

        status = item.evaluate(100.0);
        assertEquals("second threshold evaluation status", Status.TRIGGERED, status);
    }
    
    @Test
    public void testEvaluateHighTriggerTwiceNoRetrigger() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(2);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", Status.NO_CHANGE, status);

        status = item.evaluate(100.0);
        assertEquals("second threshold evaluation status", Status.TRIGGERED, status);
        
        status = item.evaluate(100.0);
        assertEquals("third threshold evaluation status", Status.NO_CHANGE, status);

        status = item.evaluate(100.0);
        assertEquals("fourth threshold evaluation status", Status.NO_CHANGE, status);

    }
    
    @Test
    public void testEvaluateHighTriggerRearm() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", Status.TRIGGERED, status);

        status = item.evaluate(0.0);
        assertEquals("second threshold evaluation status", Status.RE_ARMED, status);
    }

    @Test
    public void testEvaluateHighTriggerRearmTriggerAgain() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", Status.TRIGGERED, status);

        status = item.evaluate(0.0);
        assertEquals("second threshold evaluation status", Status.RE_ARMED, status);

        status = item.evaluate(100.0);
        assertEquals("third threshold evaluation status", Status.TRIGGERED, status);
    }

    @Test
    public void testEvaluateLowTriggerOnce() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    @Test
    public void testIsThresholdExceededHighTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(100.0));
    }
    
    @Test
    public void testIsThresholdExceededHighTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(99.0));
    }
    
    @Test
    public void testIsThresholdExceededHighNotTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("threshold should not be exceeded", item.isThresholdExceeded(98.0));
    }

    @Test
    public void testIsThresholdExceededLowTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(98.0));
    }
    
    @Test
    public void testIsThresholdExceededLowTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(99.0));
    }
    
    @Test
    public void testIsThresholdExceededLowNotTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("threshold should not be exceeded", item.isThresholdExceeded(100.0));
    }
    
    @Test
    public void testIsThresholdExceededBogusType() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.RELATIVE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'."));

        try {
            ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
            item.isThresholdExceeded(98.0);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testIsRearmExceededHighTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.0));
    }
    
    @Test
    public void testIsRearmExceededHighTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.5));
    }
    
    @Test
    public void testIsRearmExceededHighNoTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("rearm should not be exceeded", item.isRearmExceeded(1.0));
    }
    
    @Test
    public void testIsRearmExceededLowTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(1.0));
    }
    
    @Test
    public void testIsRearmExceededLowTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.5));
    }
    
    @Test
    public void testIsRearmExceededLowNoTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("rearm should not be exceeded", item.isRearmExceeded(0.0));
    }

    @Test
    public void testIsRearmExceededBogusType() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.RELATIVE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'."));

        try {
            ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
            item.isThresholdExceeded(0.0);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testIsTriggerCountExceededAtTriggerValueOne() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("trigger count should not be exeeded before exceeding value", item.isTriggerCountExceeded());

        Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", Status.TRIGGERED, status);

        assertTrue("trigger count should be exeeded after exceeding value (and being triggered)", item.isTriggerCountExceeded());
    }
    
    @Test
    public void testIsTriggerCountExceededNotAtTriggerValueTwo() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(2);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("trigger count should not be exeeded before exceeding value", item.isTriggerCountExceeded());

        Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", Status.NO_CHANGE, status);

        assertFalse("trigger count should not be exeeded after only exceeding value once", item.isTriggerCountExceeded());
    }

    @Test
    public void testIsTriggerCountExceededNotAfterReArm() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("trigger count should not be exeeded before exceeding value", item.isTriggerCountExceeded());

        Status status = item.evaluate(100.0);
        assertEquals("first threshold evaluation status", Status.TRIGGERED, status);

        assertTrue("trigger count should  be exeeded after exceeding value", item.isTriggerCountExceeded());

        status = item.evaluate(0.0);
        assertEquals("first threshold evaluation status", Status.RE_ARMED, status);

        assertFalse("trigger count should be reset after being rearmed", item.isTriggerCountExceeded());
    }

    @Test
    public void testGetEventForStateDefaultUEIS() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(95.0);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);

        // High exceed, with null instance
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, null);
        assertEquals("UEI should be the highThresholdExceededUEI", EventConstants.HIGH_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // High rearm, with null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0, null);
        assertEquals("UEI should be the highThresholdRearmedUEI", EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // High exceed, with non-null instance
        event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, new MockCollectionResourceWrapper("testInstance"));
        assertEquals("UEI should be the highThresholdExceededUEI", EventConstants.HIGH_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentWithValue(event, "instance", "testInstance");
        
        // High rearm, with non-null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0, new MockCollectionResourceWrapper("testInstance"));
        assertEquals("UEI should be the highThresholdRearmedUEI", EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        parmPresentWithValue(event, "instance", "testInstance");
        
        // Set it up again for low tests
        threshold.setType(ThresholdType.LOW);
        threshold.setValue(95.0);
        threshold.setRearm(99.0);
        
        // Low exceed, with null instance
        event=item.getEventForState(Status.TRIGGERED, new Date(), 94.0, null);
        assertEquals("UEI should be the lowThresholdExceededUEI", EventConstants.LOW_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // Low rearm, with null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 100.0, null);
        assertEquals("UEI should be the lowThresholdRearmedUEI", EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // Low exceed, with non-null instance
        event=item.getEventForState(Status.TRIGGERED, new Date(), 94.0, new MockCollectionResourceWrapper("testInstance"));
        assertEquals("UEI should be the lowThresholdExceededUEI", EventConstants.LOW_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentWithValue(event, "instance", "testInstance");
        
        // Low rearm, with non-null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 100.0, new MockCollectionResourceWrapper("testInstance"));
        assertEquals("UEI should be the lowThresholdRearmedUEI", EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        parmPresentWithValue(event, "instance", "testInstance");
    }

    @Test
    public void testGetEventForStateCustomUEIS() {
        String triggeredUEI="uei.opennms.org/custom/thresholdTriggered";
        String rearmedUEI="uei.opennms.org/custom/thresholdRearmed";
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue(99.0);
        threshold.setRearm(95.0);
        threshold.setTrigger(1);
        threshold.setTriggeredUEI(triggeredUEI);
        threshold.setRearmedUEI(rearmedUEI);
        
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, null);
        assertEquals("UEI should be the uei.opennms.org/custom/thresholdTriggered", triggeredUEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0, null);
        assertEquals("UEI should be the uei.opennms.org/custom/thresholdRearmed", rearmedUEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");        
    }
}
  
