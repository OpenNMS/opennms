/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007 The OpenNMS Group.  All rights reserved.
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

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status;
import org.opennms.netmgt.threshd.ThresholdEvaluatorHighLow.ThresholdEvaluatorStateHighLow;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

public class ThresholdEvaluatorHighLowTest extends TestCase {
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
    
    public void testConstructorThresholdNoType() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'type' value set"));
        Threshold threshold = new Threshold();
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testConstructorThresholdNoDsName() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'ds-name' value set"));
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsType("ds-type");
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
    
    public void testConstructorThresholdNoDsType() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'ds-type' value set"));
        Threshold threshold = new Threshold();
        threshold.setType("high");
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
    
    public void testConstructorThresholdNoValue() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'value' value set"));
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testConstructorThresholdNoRearm() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'rearm' value set"));
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testConstructorThresholdNoTrigger() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("threshold must have a 'trigger' value set"));
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testEvaluateHighNoTrigger() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(101.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
      
        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    public void testEvaluateHighTriggerOnce() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       
        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.TRIGGERED, status);
    }
    
    public void testEvaluateHighNoTriggerTwice() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(2);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    public void testEvaluateHighTriggerTwice() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testEvaluateHighTriggerTwiceNoRetrigger() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testEvaluateHighTriggerRearm() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testEvaluateHighTriggerRearmTriggerAgain() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testEvaluateLowTriggerOnce() {
        Threshold threshold = new Threshold();
        threshold.setType("low");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper);

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    public void testIsThresholdExceededHighTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(100.0));
    }
    
    public void testIsThresholdExceededHighTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(99.0));
    }
    
    public void testIsThresholdExceededHighNotTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("threshold should not be exceeded", item.isThresholdExceeded(98.0));
    }

    public void testIsThresholdExceededLowTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType("low");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(98.0));
    }
    
    public void testIsThresholdExceededLowTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType("low");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(99.0));
    }
    
    public void testIsThresholdExceededLowNotTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType("low");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("threshold should not be exceeded", item.isThresholdExceeded(100.0));
    }
    
    // FIXME: This doesn't work because all allow ThresholdTypeType values are currently checked for in the if statement
    public void testIsThresholdExceededBogusType() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(98.0));
    }
    
    public void testIsRearmExceededHighTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.0));
    }
    
    public void testIsRearmExceededHighTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.5));
    }
    
    public void testIsRearmExceededHighNoTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("rearm should not be exceeded", item.isRearmExceeded(1.0));
    }
    

    public void testIsRearmExceededLowTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType("low");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(1.0));
    }
    
    public void testIsRearmExceededLowTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType("low");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.5));
    }
    
    public void testIsRearmExceededLowNoTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType("low");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertFalse("rearm should not be exceeded", item.isRearmExceeded(0.0));
    }

    // FIXME: This doesn't work because all allow ThresholdTypeType values are currently checked for in the if statement
    public void testIsRearmExceededBogusType() {
        Threshold threshold = new Threshold();
        threshold.setType("relativeChange");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(0.5);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        assertTrue("rearm should be exceeded", item.isThresholdExceeded(0.0));
    }
    
    public void testIsTriggerCountExceededAtTriggerValueOne() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testIsTriggerCountExceededNotAtTriggerValueTwo() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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

    public void testIsTriggerCountExceededNotAfterReArm() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
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
    
    public void testGetEventForStateDefaultUEIS() {
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(95.0);
        threshold.setTrigger(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0);
        assertEquals("UEI should be the highThresholdExceededUEI", EventConstants.HIGH_THRESHOLD_EVENT_UEI, event.getUei());
        
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0);
        assertEquals("UEI should be the highThresholdRearmedUEI", EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        
        threshold.setType("low");
        threshold.setValue(95.0);
        threshold.setRearm(99.0);
        event=item.getEventForState(Status.TRIGGERED, new Date(), 94.0);
        assertEquals("UEI should be the lowThresholdExceededUEI", EventConstants.LOW_THRESHOLD_EVENT_UEI, event.getUei());
        
        event=item.getEventForState(Status.RE_ARMED, new Date(), 100.0);
        assertEquals("UEI should be the lowThresholdRearmedUEI", EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, event.getUei());
    }

    public void testGetEventForStateCustomUEIS() {
        String triggeredUEI="uei.opennms.org/custom/thresholdTriggered";
        String rearmedUEI="uei.opennms.org/custom/thresholdRearmed";
        Threshold threshold = new Threshold();
        threshold.setType("high");
        threshold.setDsName("ds-name");
        threshold.setDsType("ds-type");
        threshold.setValue(99.0);
        threshold.setRearm(95.0);
        threshold.setTrigger(1);
        threshold.setTriggeredUEI(triggeredUEI);
        threshold.setRearmedUEI(rearmedUEI);
        
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper);
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0);
        assertEquals("UEI should be the uei.opennms.org/custom/thresholdTriggered", triggeredUEI, event.getUei());
        
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0);
        assertEquals("UEI should be the uei.opennms.org/custom/thresholdRearmed", rearmedUEI, event.getUei());
        
    }
}
  