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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.threshd.ThresholdEvaluatorAbsoluteChange.ThresholdEvaluatorStateAbsoluteChange;
import org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ThrowableAnticipator;

public class ThresholdEvaluatorAbsoluteChangeTest extends AbstractThresholdEvaluatorTestCase {

    @Test
    public void testConstructor() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("interface");
        threshold.setValue("0.9");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
    }
    
    @Test
    public void testConstructorThresholdNull() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new NullPointerException());
        
        try {
            new ThresholdEvaluatorStateAbsoluteChange(null, MockSession.getSession());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @Test
    public void testEvaluateOnce() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("0.9");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
       ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
    }
    
    @Test
    public void testEvaluateTwiceNoTrigger() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("0.9");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
    }
    
    @Test
    public void testEvaluateTwiceTriggerLowBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("-1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(8.0));
    }
    
    @Test
    public void testEvaluateTwiceTriggerLowEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("-1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(9.0));
    }
    
    @Test
    public void testEvaluateTwiceNoTriggerLowAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("-1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(9.5));
    }
    
    @Test
    public void testEvaluateTwiceTriggerHighAbove() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(12.0));
    }
    
    @Test
    public void testEvaluateTwiceTriggerHighEqual() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(11.0));
    }
    
    @Test
    public void testEvaluateTwiceNoTriggerHighBelow() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.0));
        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(10.5));
    }
    
    @Test
    public void testGetEventForStateNoChange() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("1.1");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());

        assertNull("should not have created an event", evaluator.getEventForState(Status.NO_CHANGE, new Date(), 10.0, null, null));
    }
    
    @Test
    public void testGetEventForStateTriggered() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("1.0");
        threshold.setRearm("0.5");
        threshold.setTrigger("3");
        ThresholdEvaluatorState.ThresholdValues thresholdValues = mock(ThresholdEvaluatorState.ThresholdValues.class);
        when(thresholdValues.getThresholdValue()).thenReturn(1.0);
        when(thresholdValues.getRearm()).thenReturn(0.5);
        when(thresholdValues.getTrigger()).thenReturn(3);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);
        ThresholdEvaluatorStateAbsoluteChange evaluator = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());

        assertEquals("should not trigger", Status.NO_CHANGE, evaluator.evaluate(8.0));
        assertEquals("should trigger", Status.TRIGGERED, evaluator.evaluate(10.0));
        
        // Do it once with a null instance
        Event event = evaluator.getEventForState(Status.TRIGGERED, new Date(), 10.0, thresholdValues, null);
        assertNotNull("should have created an event", event);
        assertEquals("UEIs should be the same", EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI, event.getUei());
        
        assertNotNull("event should have parms", event.getParmCollection());
        parmPresentAndValueNonNull(event, "instance");
        parmPresentWithValue(event, "value", "10.0");
        parmPresentWithValue(event, "previousValue", "8.0");
        parmPresentWithValue(event, "changeThreshold", "1.0");
        
        // And again with a non-null instance
        event = evaluator.getEventForState(Status.TRIGGERED, new Date(), 10.0, thresholdValues, new MockCollectionResourceWrapper("testInstance"));
        assertNotNull("should have created an event", event);
        assertEquals("UEIs should be the same", EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI, event.getUei());
        
        assertNotNull("event should have parms", event.getParmCollection());
        
        parmPresentWithValue(event, "instance", "testInstance");
        parmPresentWithValue(event, "value", "10.0");
        parmPresentWithValue(event, "previousValue", "8.0");
        parmPresentWithValue(event, "changeThreshold", "1.0");
    }
    
    @Test
    public void testGetEventForStateDefaultUEIS() {
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("95.0");
        threshold.setTrigger("1");
       ThresholdEvaluatorState.ThresholdValues thresholdValues = mock(ThresholdEvaluatorState.ThresholdValues.class);
        when(thresholdValues.getThresholdValue()).thenReturn(99.0);
        when(thresholdValues.getRearm()).thenReturn(95.0);
        when(thresholdValues.getTrigger()).thenReturn(1);
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateAbsoluteChange item = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, thresholdValues, null);
        assertEquals("UEI should be the absoluteChangeThresholdTriggered", EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI, event.getUei());
    }

    @Test
    public void testGetEventForStateCustomUEIS() {
        String triggeredUEI="uei.opennms.org/custom/absoluteChangeThresholdTriggered";
        Threshold threshold = new Threshold();
        threshold.setType(ThresholdType.ABSOLUTE_CHANGE);
        threshold.setDsName("ds-name");
        threshold.setDsType("node");
        threshold.setValue("99.0");
        threshold.setRearm("95.0");
        threshold.setTrigger("1");
        threshold.setTriggeredUEI(triggeredUEI);
        ThresholdEvaluatorState.ThresholdValues thresholdValues = mock(ThresholdEvaluatorState.ThresholdValues.class);
        when(thresholdValues.getThresholdValue()).thenReturn(99.0);
        when(thresholdValues.getRearm()).thenReturn(95.0);
        when(thresholdValues.getTrigger()).thenReturn(1);
        
        ThresholdConfigWrapper wrapper=new ThresholdConfigWrapper(threshold);

        ThresholdEvaluatorStateAbsoluteChange item = new ThresholdEvaluatorStateAbsoluteChange(wrapper, MockSession.getSession());
        Event event=item.getEventForState(Status.TRIGGERED, new Date(), 100.0, thresholdValues, null);
        assertEquals("UEI should be the "+triggeredUEI, triggeredUEI, event.getUei());
       
    }
}
