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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Correlation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;

/**
 * Guards the fidelity of {@link EventTranslatorConfigFactory#cloneEvent}. Marshalled XML is the
 * assertion because it covers every persisted field at once.
 */
public class EventTranslatorCloneEventTest {

    @Test
    public void testClonePreservesEveryFieldOfAPopulatedEvent() {
        final Event original = populatedEvent();

        final Event clone = EventTranslatorConfigFactory.cloneEvent(original);

        assertNotNull(clone);
        assertNotSame(original, clone);
        assertEquals(JaxbUtils.marshal(original), JaxbUtils.marshal(clone));
    }

    /**
     * EventTranslator.onEvent runs Event.copyFrom before calling translateEvent, so a parm-less
     * event reaches cloneEvent with its parm collection already normalized to empty rather than
     * null. Cloning that shape must not perturb it either way.
     */
    @Test
    public void testCloneOfAParmlessEventMatchesWhatTheDaemonDelivers() {
        final Event parmless = new Event();
        parmless.setUei("uei.opennms.org/test/noParms");
        parmless.setSource("test");
        parmless.setTime(new Date(1700000000000L));

        final Event asDeliveredByOnEvent = Event.copyFrom(ImmutableMapper.fromMutableEvent(parmless));

        final Event clone = EventTranslatorConfigFactory.cloneEvent(asDeliveredByOnEvent);

        assertNotNull(clone);
        assertEquals(JaxbUtils.marshal(asDeliveredByOnEvent), JaxbUtils.marshal(clone));
    }

    /** Mutating the clone must not reach back into the source event. */
    @Test
    public void testCloneIsDeepEnoughToMutateIndependently() {
        final Event original = populatedEvent();
        final String originalXml = JaxbUtils.marshal(original);

        final Event clone = EventTranslatorConfigFactory.cloneEvent(original);
        clone.setUei("uei.opennms.org/translated/somethingElse");
        clone.setSeverity("Critical");
        clone.getParmCollection().get(0).getValue().setContent("mutated");
        clone.getLogmsg().setContent("mutated");
        clone.getSnmp().setCommunity("mutated");
        clone.addParm(parm("extraParm", "extraValue"));

        assertEquals(originalXml, JaxbUtils.marshal(original));
    }

    /**
     * translate() relies on these being clearable on the clone so eventd recomputes them from
     * eventconf after translation (NMS-4038).
     */
    @Test
    public void testClonedFieldsThatTranslationClearsAreIndependent() {
        final Event original = populatedEvent();

        final Event clone = EventTranslatorConfigFactory.cloneEvent(original);
        clone.setAlarmData(null);
        clone.setSeverity(null);
        clone.setDescr(null);
        clone.setSnmp(null);

        assertNotNull(original.getAlarmData());
        assertNotNull(original.getSeverity());
        assertNotNull(original.getDescr());
        assertNotNull(original.getSnmp());
    }

    @Test
    public void testCloneOfNullIsNull() {
        assertNull(EventTranslatorConfigFactory.cloneEvent(null));
    }

    private static Event populatedEvent() {
        final Event event = new Event();
        event.setUuid("6a1b0e5c-0000-0000-0000-000000000001");
        event.setDbid(1234L);
        event.setDistPoller("00000000-0000-0000-0000-000000000000");
        event.setCreationTime(new Date(1700000000000L));
        event.setMasterStation("master");
        event.setUei("uei.opennms.org/generic/traps/SNMP_Link_Down");
        event.setSource("trapd");
        event.setNodeid(42L);
        event.setTime(new Date(1700000000000L));
        event.setHost("router-01.example.com");
        event.setInterface("192.168.1.1");
        event.setSnmphost("192.168.1.1");
        event.setService("SNMP");
        event.setDescr("A linkDown trap was received.");
        event.setSeverity("Minor");
        event.setPathoutage("192.168.1.254");
        event.setOperinstruct("Check the interface.");
        event.setIfIndex(2);
        event.setIfAlias("uplink-to-core");
        event.setMouseovertext("linkDown");

        final Snmp snmp = new Snmp();
        snmp.setId(".1.3.6.1.4.1.9");
        snmp.setVersion("v2c");
        snmp.setCommunity("public");
        snmp.setGeneric(2);
        snmp.setSpecific(0);
        snmp.setTimeStamp(1700000000000L);
        event.setSnmp(snmp);

        final Logmsg logmsg = new Logmsg();
        logmsg.setContent("A linkDown trap was received from interface 2 on node 42.");
        logmsg.setDest("logndisplay");
        logmsg.setNotify(Boolean.TRUE);
        event.setLogmsg(logmsg);

        final Correlation correlation = new Correlation();
        correlation.setState("on");
        correlation.setPath("pathOutage");
        event.setCorrelation(correlation);

        final Autoaction autoaction = new Autoaction();
        autoaction.setContent("echo linkDown");
        autoaction.setState("on");
        event.addAutoaction(autoaction);

        event.addLoggroup("linkEvents");

        final AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey("%uei%:%nodeid%:%parm[ifIndex]%");
        alarmData.setAlarmType(1);
        alarmData.setAutoClean(false);
        event.setAlarmData(alarmData);

        event.addParm(parm(".1.3.6.1.2.1.2.2.1.1.2", "2"));
        event.addParm(parm(".1.3.6.1.2.1.2.2.1.7.2", "1"));
        event.addParm(parm(".1.3.6.1.2.1.2.2.1.8.2", "2"));

        return event;
    }

    private static Parm parm(final String name, final String content) {
        final Value value = new Value();
        value.setContent(content);

        final Parm parm = new Parm();
        parm.setParmName(name);
        parm.setValue(value);
        return parm;
    }
}
