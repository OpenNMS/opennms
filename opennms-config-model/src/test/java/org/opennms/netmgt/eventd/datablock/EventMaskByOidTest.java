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
package org.opennms.netmgt.eventd.datablock;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbind;

public class EventMaskByOidTest {
    final private EventConfData eventConfData = new EventConfData();

    @Test
    public void matchNormalCase() {
        final Event eventConfEvent = new Event();
        eventConfEvent.setMask(new Mask());
        final Maskelement maskelement = new Maskelement();
        maskelement.setMename("id");
        maskelement.setMevalues(Lists.newArrayList(".1.2.3.4.%"));
        eventConfEvent.getMask().addMaskelement(maskelement);
        final Varbind v1 = new Varbind();
        v1.setVbnumber(3);
        v1.setVbvalues(Lists.newArrayList("2"));
        eventConfEvent.getMask().addVarbind(v1);
        final EventKey eventKey = new EventKey(eventConfEvent);
        org.opennms.netmgt.xml.event.Event xmlEvent = getEvent();
        Assert.assertTrue(eventConfData.eventMatchesKey(eventKey, xmlEvent));
    }

    @Test
    public void failNormalCase() {
        final Event eventConfEvent = new Event();
        eventConfEvent.setMask(new Mask());
        final Maskelement maskelement = new Maskelement();
        maskelement.setMename("id");
        maskelement.setMevalues(Lists.newArrayList(".1.2.3.4.%"));
        eventConfEvent.getMask().addMaskelement(maskelement);
        final Varbind v1 = new Varbind();
        v1.setVbnumber(3);
        v1.setVbvalues(Lists.newArrayList("0"));
        eventConfEvent.getMask().addVarbind(v1);
        final EventKey eventKey = new EventKey(eventConfEvent);
        org.opennms.netmgt.xml.event.Event xmlEvent = getEvent();
        Assert.assertFalse(eventConfData.eventMatchesKey(eventKey, xmlEvent));
    }

    @Test
    public void matchByOid() {
        final Event eventConfEvent = new Event();
        eventConfEvent.setMask(new Mask());
        final Maskelement maskelement = new Maskelement();
        maskelement.setMename("id");
        maskelement.setMevalues(Lists.newArrayList(".1.2.3.4.%"));
        eventConfEvent.getMask().addMaskelement(maskelement);
        final Varbind v1 = new Varbind();
        v1.setVbnumber(3);
        v1.setVbvalues(Lists.newArrayList("2"));
        eventConfEvent.getMask().addVarbind(v1);
        final Varbind v2 = new Varbind();
        v2.setVboid(".1.2.3.4.4");
        v2.setVbvalues(Lists.newArrayList("4","5"));
        eventConfEvent.getMask().addVarbind(v2);
        final EventKey eventKey = new EventKey(eventConfEvent);
        final EventConfData eventConfData = new EventConfData();
        var xmlEvent = getEvent();
        Assert.assertTrue(eventConfData.eventMatchesKey(eventKey, xmlEvent));
    }

    @Test
    public void failByOid() {
        final Event eventConfEvent = new Event();
        eventConfEvent.setMask(new Mask());
        final Maskelement maskelement = new Maskelement();
        maskelement.setMename("id");
        maskelement.setMevalues(Lists.newArrayList(".1.2.3.4.%"));
        eventConfEvent.getMask().addMaskelement(maskelement);
        final Varbind v1 = new Varbind();
        v1.setVbnumber(3);
        v1.setVbvalues(Lists.newArrayList("2"));
        eventConfEvent.getMask().addVarbind(v1);
        final Varbind v2 = new Varbind();
        v2.setVboid(".1.2.3.4.4");
        v2.setVbvalues(Lists.newArrayList("0"));
        eventConfEvent.getMask().addVarbind(v2);
        final EventKey eventKey = new EventKey(eventConfEvent);
        final EventConfData eventConfData = new EventConfData();
        var xmlEvent = getEvent();
        Assert.assertFalse(eventConfData.eventMatchesKey(eventKey, xmlEvent));
    }

    private org.opennms.netmgt.xml.event.Event getEvent() {
        final org.opennms.netmgt.xml.event.Event xmlEvent = new org.opennms.netmgt.xml.event.Event();
        final org.opennms.netmgt.xml.event.Snmp snmp = new org.opennms.netmgt.xml.event.Snmp();
        snmp.setId(".1.2.3.4.0");
        snmp.setVersion("2c");
        snmp.setCommunity("public");
        xmlEvent.setSnmp(snmp);
        xmlEvent.addParm(new Parm("1", "1"));
        xmlEvent.addParm(new Parm("2", "1"));
        xmlEvent.addParm(new Parm("3", "2"));
        xmlEvent.addParm(new Parm("4", "3"));
        xmlEvent.addParm(new Parm(".1.2.3.4.1", "1"));
        xmlEvent.addParm(new Parm(".1.2.3.4.2", "2"));
        xmlEvent.addParm(new Parm(".1.2.3.4.3", "3"));
        xmlEvent.addParm(new Parm(".1.2.3.4.4", "4"));
        xmlEvent.addParm(new Parm(".1.2.3.4.5", "5"));
        return xmlEvent;
    }
}
