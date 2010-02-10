/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 27, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.ackd.readers;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.JUnitHttpServerExecutionListener;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.netmgt.ackd.Ackd;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.ackd.Parameter;
import org.opennms.netmgt.config.ackd.Reader;
import org.opennms.netmgt.config.ackd.Readers;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.opennms.netmgt.dao.castor.DefaultAckdConfigurationDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.acknowledgments.AckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    JUnitHttpServerExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-ackd.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})

/**
 * Integration test for the Hyperic Acknowledgement Reader Implementation.
 */
@JUnitTemporaryDatabase(populate=true) @Transactional
public class HypericAckProcessorTest {

    @Autowired
    private Ackd m_daemon;

    @Autowired
    private AckService m_ackService;

    @Autowired
    private HypericAckProcessor m_processor;

    @Test
    public void verifyWiring() {
        Assert.assertNotNull(m_ackService);
        Assert.assertNotNull(m_daemon);
        Assert.assertNotNull(m_processor);
    }

    private AckdConfigurationDao createAckdConfigDao() {

        class AckdConfigDao extends DefaultAckdConfigurationDao {

            public AckdConfiguration getConfig() {
                AckdConfiguration config = new AckdConfiguration();
                config.setAckExpression("~(?i)^AcK$");
                config.setAlarmidMatchExpression("~(?i).*alarmid:([0-9]+).*");
                config.setAlarmSync(true);
                config.setClearExpression("~(?i)^(Resolve|cleaR)$");
                config.setEscalateExpression("~(?i)^esc$");
                config.setNotifyidMatchExpression("~(?i).*RE:.*Notice #([0-9]+).*");
                config.setUnackExpression("~(?i)^unAck$");

                Readers readers = new Readers();
                {
                    Reader reader = new Reader();
                    reader.setEnabled(false);
                    reader.setReaderName("JavaMailReader");

                    Parameter hypericHosts = new Parameter();
                    hypericHosts.setKey("readmail-config");
                    hypericHosts.setValue("localhost");
                    reader.addParameter(hypericHosts);

                    org.opennms.netmgt.config.ackd.ReaderSchedule hypericSchedule = new org.opennms.netmgt.config.ackd.ReaderSchedule();
                    hypericSchedule.setInterval(60);
                    hypericSchedule.setUnit("s");

                    readers.addReader(reader);
                }

                {
                    Reader reader = new Reader();
                    reader.setEnabled(true);
                    reader.setReaderName(HypericAckProcessor.READER_NAME_HYPERIC);

                    Parameter hypericHosts = new Parameter();
                    hypericHosts.setKey(HypericAckProcessor.PARAMETER_PREFIX_HYPERIC_SOURCE + "HQ-Datacenter");
                    hypericHosts.setValue("http://hqadmin:hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu");
                    reader.addParameter(hypericHosts);

                    hypericHosts = new Parameter();
                    hypericHosts.setKey(HypericAckProcessor.PARAMETER_PREFIX_HYPERIC_SOURCE + "HQ-Corporate-IT");
                    hypericHosts.setValue("http://hqadmin:hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu");
                    reader.addParameter(hypericHosts);

                    org.opennms.netmgt.config.ackd.ReaderSchedule hypericSchedule = new org.opennms.netmgt.config.ackd.ReaderSchedule();
                    hypericSchedule.setInterval(3);
                    hypericSchedule.setUnit("s");
                    reader.setReaderSchedule(hypericSchedule);

                    readers.addReader(reader);
                }

                config.setReaders(readers);
                return config;
            }

        }

        return new AckdConfigDao();
    }

    @Test
    @JUnitHttpServer(port=7081)
    public void testStartAckd() throws Exception {
        m_daemon.setConfigDao(createAckdConfigDao());
        m_daemon.start();
        try { Thread.sleep(5000); } catch (InterruptedException e) {}
        m_daemon.destroy();
    }

    @Test
    public void testFetchUnclearedHypericAlarms() throws Exception {
        List<OnmsAlarm> alarms = m_processor.fetchUnclearedHypericAlarms();
        System.out.println(alarms.size());
    }

    @Test
    @JUnitHttpServer(port=7081,basicAuth=true)
    public void testFetchHypericAlerts() throws Exception {
        // Test reading alerts over the HTTP server
        {
            List<HypericAckProcessor.HypericAlertStatus> alerts = HypericAckProcessor.fetchHypericAlerts("http://hqadmin:hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu", Arrays.asList(new String[] { "1", "2", "3" }));
            assertEquals(5, alerts.size());
            for (HypericAckProcessor.HypericAlertStatus alert : alerts) {
                System.out.println(alert.toString());
            }

            alerts = HypericAckProcessor.fetchHypericAlerts("http://uhohcolons:this%3Apassword%3Ahas%3Acolons@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu", Arrays.asList(new String[] { "1", "2", "3" }));
            assertEquals(5, alerts.size());
        }

        // Try with bad credentials to make sure we get a malformed response
        {
            boolean caughtAuthFailure = false;
            for (String url : new String[] {
                    "http://:badcredentials@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://blankpass@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://blankpass:@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://hqadmin@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://hqadmin:@127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu",
                    "http://127.0.0.1:7081/hqu/opennms/alertStatus/list.hqu"
            }) {
                try {
                    List<HypericAckProcessor.HypericAlertStatus> alerts = HypericAckProcessor.fetchHypericAlerts(url, Arrays.asList(new String[] { "1", "2", "3" }));
                } catch (JAXBException e) {
                    // Expected state
                    caughtAuthFailure = true;
                }
                assertTrue("Did not catch expected authorization failure for URL: " + url, caughtAuthFailure);
            }
        }
    }

    @Test
    public void testParseHypericAlerts() throws Exception {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("hqu/opennms/alertStatus/list.hqu")));
        reader.mark(4000);
        try {
            while(true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                } else {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            // End of file
        }
        reader.reset();
        List<HypericAckProcessor.HypericAlertStatus> alerts = HypericAckProcessor.parseHypericAlerts(reader);
        assertEquals(5, alerts.size());
        for (HypericAckProcessor.HypericAlertStatus alert : alerts) {
            System.out.println(alert.toString());
        }
    }
}
