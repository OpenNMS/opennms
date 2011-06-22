/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class
})
@JUnitConfigurationEnvironment
public class MonitoringLocationsFactoryTest {

    private MonitoringLocationsFactory m_locationFactory;

    private PollerConfigManager m_pollerConfigManager;

    @Before
    public void setUp() throws Exception {
        
        MockNetwork network = new MockNetwork();

        MockDatabase db = new MockDatabase();
        db.populate(network);

        DataSourceFactory.setInstance(db);

        InputStream stream = getClass().getResourceAsStream("/org/opennms/netmgt/config/monitoring-locations.testdata.xml");
        m_locationFactory = new MonitoringLocationsFactory(stream);
        stream.close();
        
        SnmpPeerFactory.init();
        
        stream = getClass().getResourceAsStream("/org/opennms/netmgt/config/poller-configuration.testdata.xml");
        m_pollerConfigManager = new TestPollerConfigManager(stream, "localhost", false);
        stream.close();
        
    }
    
    @Test
    public void testGetName() throws MarshalException, ValidationException,
            IOException {
        final String locationName = "RDU";
        LocationDef def = m_locationFactory.getDef(locationName);
        assertNotNull(def);
        assertEquals(locationName, def.getLocationName());
        assertEquals("raleigh", def.getMonitoringArea());

        assertNotNull(m_pollerConfigManager.getPackage(def.getPollingPackageName()));

    }

    static class TestPollerConfigManager extends PollerConfigManager {
        String m_xml;

        @Deprecated
        public TestPollerConfigManager(Reader rdr, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
            super(rdr, localServer, verifyServer);
            save();
        }

        public TestPollerConfigManager(InputStream stream, String localServer, boolean verifyServer) throws MarshalException, ValidationException {
            super(stream, localServer, verifyServer);
        }

        public void update() throws IOException, MarshalException, ValidationException {
            m_config = CastorUtils.unmarshal(PollerConfiguration.class, new ByteArrayInputStream(m_xml.getBytes("UTF-8")));
            setUpInternalData();
        }

        protected void saveXml(String xml) throws IOException {
            m_xml = xml;
        }

        public String getXml() {
            return m_xml;
        }

    }
}
