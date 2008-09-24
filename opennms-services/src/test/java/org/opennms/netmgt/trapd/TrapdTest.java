//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 26: Test startup of trapd with Spring. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.trapd;

import java.io.Reader;
import java.net.InetAddress;

import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;

public class TrapdTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private int m_port = 1162;
    private Trapd m_trapd = new Trapd();

    @Override
    protected void setUpConfiguration() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
        
        Reader rdr = ConfigurationTestUtils.getReaderForResourceWithReplacements(this, "trapd-configuration.xml", new String[] { "@snmp-trap-port@", Integer.toString(m_port) });
        TrapdConfigFactory.setInstance(new TrapdConfigFactory(rdr));
    }


    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/applicationContext-trapDaemon.xml"
        };
    }

    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        m_trapd.onStart();
    }

    @Override
    protected void onTearDownInTransactionIfEnabled() throws Exception {
        m_trapd.onStop();
    }
    
    public Trapd getDaemon() {
        return m_trapd;
    }

    public void setDaemon(Trapd daemon) {
        m_trapd = daemon;
    }

    public void testSnmpV1TrapSend() throws Exception {
        String localhost = "127.0.0.1";
        InetAddress localAddr = InetAddress.getByName(localhost);

        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(".1.3.6.1.4.1.5813"));
        pdu.setGeneric(1);
        pdu.setSpecific(0);
        pdu.setTimeStamp(666L);
        pdu.setAgentAddress(localAddr);

        Event e = new Event();
        e.setUei("uei.opennms.org/default/trap");
        e.setSource("trapd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);

        pdu.send(localhost, m_port, "public");
        pdu.send(localhost, m_port, "public");
        pdu.send(localhost, m_port, "public");
        pdu.send(localhost, m_port, "public");

        assertEquals("number of anticipated events", 1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals("number of unanticipated events", 0, ea.unanticipatedEvents().size());
    }
}

