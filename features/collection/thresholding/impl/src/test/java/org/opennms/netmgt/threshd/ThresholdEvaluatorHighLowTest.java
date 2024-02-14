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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        ta.anticipate(new NullPointerException());
        
        try {
            new ThresholdEvaluatorStateHighLow(null, MockSession.getSession());
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
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        
        try {
            new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        try {
            new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       
        try {
            new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       
        try {
            new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("1.0");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
      
        try {
            new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        
        try {
            new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("101.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
      
        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    @Test
    public void testEvaluateHighTriggerOnce() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       
        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.TRIGGERED, status);
    }
    
    @Test
    public void testEvaluateHighNoTriggerTwice() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("2");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    @Test
    public void testEvaluateHighTriggerTwice() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("2");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("2");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorState item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

        Status status = item.evaluate(100.0);
        assertEquals("threshold evaluation status", Status.NO_CHANGE, status);
    }
    
    @Test
    public void testIsThresholdExceededHighTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(100.0 ));
    }
    
    @Test
    public void testIsThresholdExceededHighTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(99.0));
    }
    
    @Test
    public void testIsThresholdExceededHighNotTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertFalse("threshold should not be exceeded", item.isThresholdExceeded(98.0));
    }

    @Test
    public void testIsThresholdExceededLowTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(98.0));
    }
    
    @Test
    public void testIsThresholdExceededLowTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("threshold should be exceeded", item.isThresholdExceeded(99.0));
    }
    
    @Test
    public void testIsThresholdExceededLowNotTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertFalse("threshold should not be exceeded", item.isThresholdExceeded(100.0));
    }
    
    @Test
    public void testIsThresholdExceededBogusType() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.RELATIVE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'."));

        try {
            ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.0));
    }
    
    @Test
    public void testIsRearmExceededHighTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.5));
    }
    
    @Test
    public void testIsRearmExceededHighNoTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.HIGH);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertFalse("rearm should not be exceeded", item.isRearmExceeded(1.0));
    }
    
    @Test
    public void testIsRearmExceededLowTriggeredAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("rearm should be exceeded", item.isRearmExceeded(1.0));
    }
    
    @Test
    public void testIsRearmExceededLowTriggeredEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertTrue("rearm should be exceeded", item.isRearmExceeded(0.5));
    }
    
    @Test
    public void testIsRearmExceededLowNoTriggeredBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.LOW);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        assertFalse("rearm should not be exceeded", item.isRearmExceeded(0.0));
    }

    @Test
    public void testIsRearmExceededBogusType() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.RELATIVE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("This thresholding strategy can only be used for thresholding types of 'high' and 'low'."));

        try {
            ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("2");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("99.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
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
        threshold.setValue("99.0");
        threshold.setRearm("95.0");
        threshold.setTrigger("1");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorState.ThresholdValues thresholdValues = mock(ThresholdEvaluatorState.ThresholdValues.class);
        when(thresholdValues.getThresholdValue()).thenReturn(99.0);
        when(thresholdValues.getRearm()).thenReturn(95.0);
        when(thresholdValues.getTrigger()).thenReturn(1);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());

        // High exceed, with null instance
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, thresholdValues, null);
        assertEquals("UEI should be the highThresholdExceededUEI", EventConstants.HIGH_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // High rearm, with null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0, thresholdValues, null);
        assertEquals("UEI should be the highThresholdRearmedUEI", EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // High exceed, with non-null instance
        event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, thresholdValues, new MockCollectionResourceWrapper("testInstance"));
        assertEquals("UEI should be the highThresholdExceededUEI", EventConstants.HIGH_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentWithValue(event, "instance", "testInstance");
        
        // High rearm, with non-null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0, thresholdValues, new MockCollectionResourceWrapper("testInstance"));
        assertEquals("UEI should be the highThresholdRearmedUEI", EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        parmPresentWithValue(event, "instance", "testInstance");
        
        // Set it up again for low tests
        threshold.setType(ThresholdType.LOW);
        threshold.setValue("95.0");
        threshold.setRearm("99.0");
        
        // Low exceed, with null instance
        event=item.getEventForState(Status.TRIGGERED, new Date(), 94.0, thresholdValues, null);
        assertEquals("UEI should be the lowThresholdExceededUEI", EventConstants.LOW_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // Low rearm, with null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 100.0, thresholdValues, null);
        assertEquals("UEI should be the lowThresholdRearmedUEI", EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        // Low exceed, with non-null instance
        event=item.getEventForState(Status.TRIGGERED, new Date(), 94.0, thresholdValues, new MockCollectionResourceWrapper("testInstance"));
        assertEquals("UEI should be the lowThresholdExceededUEI", EventConstants.LOW_THRESHOLD_EVENT_UEI, event.getUei());
        parmPresentWithValue(event, "instance", "testInstance");
        
        // Low rearm, with non-null instance
        event=item.getEventForState(Status.RE_ARMED, new Date(), 100.0, thresholdValues, new MockCollectionResourceWrapper("testInstance"));
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
        threshold.setValue("99.0");
        threshold.setRearm("95.0");
        threshold.setTrigger("1");
        threshold.setTriggeredUEI(triggeredUEI);
        threshold.setRearmedUEI(rearmedUEI);
        ThresholdEvaluatorState.ThresholdValues thresholdValues = mock(ThresholdEvaluatorState.ThresholdValues.class);
        when(thresholdValues.getThresholdValue()).thenReturn(99.0);
        when(thresholdValues.getRearm()).thenReturn(95.0);
        when(thresholdValues.getTrigger()).thenReturn(1);
        
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateHighLow item = new ThresholdEvaluatorStateHighLow(wrapper, MockSession.getSession());
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, thresholdValues, null);
        assertEquals("UEI should be the uei.opennms.org/custom/thresholdTriggered", triggeredUEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");
        
        event=item.getEventForState(Status.RE_ARMED, new Date(), 94.0, thresholdValues, null);
        assertEquals("UEI should be the uei.opennms.org/custom/thresholdRearmed", rearmedUEI, event.getUei());
        parmPresentAndValueNonNull(event, "instance");        
    }
}
  
