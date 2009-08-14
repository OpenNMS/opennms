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
// 2008 Mar 10: Use MockEventIpcManager in synchronous mode to speed
//              up tests and do the right verifications on events. - dj@opennms.org
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

import java.io.InputStream;
import java.net.InetAddress;

import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;

public class TrapdTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private int m_port = 1162;
    private Trapd m_trapd = new Trapd();
    private MockEventIpcManager m_mockEventIpcManager;

    @Override
    protected void setUpConfiguration() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
        
        InputStream stream = ConfigurationTestUtils.getInputStreamForResourceWithReplacements(this, "trapd-configuration.xml", new String[] { "@snmp-trap-port@", Integer.toString(m_port) });
        TrapdConfigFactory.setInstance(new TrapdConfigFactory(stream));
    }


    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/mockEventIpcManager.xml",
                "classpath:META-INF/opennms/applicationContext-commonConfigs.xml",
                "classpath:META-INF/opennms/applicationContext-trapDaemon.xml",
                "classpath:META-INF/opennms/smallEventConfDao.xml"
        };
    }

    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        getMockEventIpcManager().setSynchronous(true);
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

        EventBuilder defaultTrapBuilder = new EventBuilder("uei.opennms.org/default/trap", "trapd");
        defaultTrapBuilder.setInterface(localhost);
        getMockEventIpcManager().getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());
        
        EventBuilder newSuspectBuilder = new EventBuilder("uei.opennms.org/internal/discovery/newSuspect", "trapd");
        newSuspectBuilder.setInterface(localhost);
        getMockEventIpcManager().getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());

        pdu.send(localhost, m_port, "public");
        
        getMockEventIpcManager().getEventAnticipator().verifyAnticipated(100, 0, 0, 0, 0);
    }

    public MockEventIpcManager getMockEventIpcManager() {
        return m_mockEventIpcManager;
    }

    public void setMockEventIpcManager(MockEventIpcManager mockEventIpcManager) {
        m_mockEventIpcManager = mockEventIpcManager;
    }
}

