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
package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/mockSinkConsumerManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class EventUtilHibernateIT {

    @Autowired
    private EventUtilDaoImpl eventUtilDaoImpl;

    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private AssetRecordDao m_assetRecordDao;

    @Autowired
    private HwEntityDao m_hwEntityDao;

    @Before
    public void setUp() throws Exception {
    	m_populator.populateDatabase();
    }

    @Test
    @JUnitTemporaryDatabase
    public void testExpandParms() {
        String testString = "%uei%:%nodeid%:%nodelabel%:%nodelocation%";

        /*
         * Checking default location
         */
        Event event1 = new EventBuilder("testUei", "testSource").setNodeid(1).getEvent();
        String string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("testUei:1:node1:Default", string1);

        /*
         * Checking custom location
         */
        OnmsNode onmsNode = m_populator.getNodeDao().get(2);
        onmsNode.setLocation(m_populator.getMonitoringLocationDao().get("RDU"));
        m_populator.getNodeDao().update(onmsNode);

        Event event2 = new EventBuilder("testUei", "testSource").setNodeid(2).getEvent();
        String string2 = eventUtilDaoImpl.expandParms(testString, event2);
        assertEquals("testUei:2:node2:RDU", string2);
    }

    /**
     * <p>Requirements:</p>
     * <ul>
     * <li>'%%' will expand to a single percent sign in output.</li>
     * <li>If whitespace is found before the expansion parameter is closed
     *   (ie. %event uei%) then it is an invalid expansion parameter. The
     *   leading percent sign is reproduced in the output and parsing
     *   continues after its position.</li>
     * <li>Percent signs that are unpaired by the time the end of input
     *   is reached will be reproduced in the output.</li>
     * </ul>
     */
    @Test
    @JUnitTemporaryDatabase
    public void testExpansionStringWithPercentSign() {
        // Escaped percent sign '%%'
        String testString = "This string for %uei% has a %% sign in it.";
        Event event1 = new EventBuilder("testUei", "testSource").getEvent();
        String string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("This string for testUei has a % sign in it.", string1);

        // Escaped percent sign '%%' with trailing percent sign
        testString = "This string for %uei% has a %%uei% sign in it.";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("This string for testUei has a %uei% sign in it.", string1);

        // No whitespace with several escaped percent signs and trailing percent
        testString = "%%uei%%%uei%%%%%uei%%%";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("%uei%testUei%%uei%%", string1);

        // Single percent
        testString = "%";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("%", string1);

        // Parameter expansion with leading and trailing single percents
        testString = "%This string for %uei% is 100% awesome.";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("%This string for testUei is 100% awesome.", string1);

        // Parameter expansion with trailing single percent
        testString = "This string for %uei% is 100% awesome.";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("This string for testUei is 100% awesome.", string1);

        // No parameter expansion, 1 percent
        testString = "This string is 100% awesome.";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("This string is 100% awesome.", string1);

        // No parameter expansion, 2 percents with whitespace in between
        testString = "This string is 100% awesome and 100% lame.";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("This string is 100% awesome and 100% lame.", string1);

        // Parameter expansion with interstitial percent
        testString = "This string for %uei% is 100% awesome even though it is %uei%.";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("This string for testUei is 100% awesome even though it is testUei.", string1);

        // Percent sign at end of string without trailing whitespace
        testString = "This string for %uei% is 100%awesomeAndCool";
        event1 = new EventBuilder("testUei", "testSource").getEvent();
        string1 = eventUtilDaoImpl.expandParms(testString, event1);
        assertEquals("This string for testUei is 100%awesomeAndCool", string1);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetNodeLabel() {
    	String label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode3().getId());
		assertEquals("node3",label);
		label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode1().getId());
		assertEquals("node1",label);
		label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode2().getId());
		assertEquals("node2",label);
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testGetForeignSource() {
        String label = eventUtilDaoImpl.getForeignSource(m_populator.getNode3().getId());
        assertEquals("imported:", label);
        label = eventUtilDaoImpl.getForeignSource(m_populator.getNode1().getId());
        assertEquals("imported:", label);
        label = eventUtilDaoImpl.getForeignSource(m_populator.getNode6().getId());
        assertNull(label);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetForeignId() {
        String label = eventUtilDaoImpl.getForeignId(m_populator.getNode3().getId());
        assertEquals("3", label);
        label = eventUtilDaoImpl.getForeignId(m_populator.getNode1().getId());
        assertEquals("1", label);
        label = eventUtilDaoImpl.getForeignId(m_populator.getNode6().getId());
        assertNull(label);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetIfAlias() {
    	String alias = eventUtilDaoImpl.getIfAlias(m_populator.getNode1().getId(), "192.168.1.1");
    	assertEquals("Initial ifAlias value", alias);
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testGetAssetFieldValue() {
        OnmsNode node1 = m_populator.getNode1();
        OnmsAssetRecord asset1 = node1.getAssetRecord();
        asset1.setAdmin("some-adm1n-label");
        asset1.setSerialNumber("42");
        m_assetRecordDao.saveOrUpdate(asset1);

        String asset = eventUtilDaoImpl.getAssetFieldValue("asset[admin]", node1.getId());
        assertEquals("some-adm1n-label", asset);

        asset = eventUtilDaoImpl.getAssetFieldValue("asset[serialNumber]", node1.getId());
        assertEquals("42", asset);

        // Checking case sensitivity
        asset = eventUtilDaoImpl.getAssetFieldValue("asset[serialnumber]", node1.getId());
        assertEquals("42", asset);
    }

    @Test
    @JUnitTemporaryDatabase
    public void getHardwareFieldValue() {
        OnmsNode node1 = m_populator.getNode1();
        OnmsHwEntity hwEntity = new OnmsHwEntity();
        hwEntity.setNode(node1);
        hwEntity.setEntPhysicalIndex(0);
        hwEntity.setEntPhysicalName("Chassis");
        hwEntity.setEntPhysicalDescr("some-physical-d3scr");
        m_hwEntityDao.save(hwEntity);

        // Access the field by index
        String hwfield = eventUtilDaoImpl.getHardwareFieldValue("hardware[0:entPhysicalDescr]", node1.getId());
        assertEquals("some-physical-d3scr", hwfield);

        // Access the field by name
        hwfield = eventUtilDaoImpl.getHardwareFieldValue("hardware[Chassis:entPhysicalDescr]", node1.getId());
        assertEquals("some-physical-d3scr", hwfield);

        // Access the field by regex
        hwfield = eventUtilDaoImpl.getHardwareFieldValue("hardware[~%Cha%:entPhysicalDescr]", node1.getId());
        assertEquals("some-physical-d3scr", hwfield);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testPrimaryInterface() {
        final String testString = "This string has a primary interface set to %primaryinterface% and the interface set to %interface%.";
        final Event event = new EventBuilder("testUei", "testSource").setNodeid(1).setInterface(InetAddressUtils.addr("10.10.10.10")).getEvent();
        final String string = eventUtilDaoImpl.expandParms(testString, event);
        assertEquals("This string has a primary interface set to 192.168.1.1 and the interface set to 10.10.10.10.", string);
    }

    protected DatabasePopulator getDatabasePopulator() {
        return m_populator;
    }
}
