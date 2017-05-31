package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.enlinkd.snmp.Dot1qVlanCurrentTableTracker.Dot1QVLanCurrent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.enlinkd.snmp.Dot1qVlanCurrentTableTracker;
import org.opennms.netmgt.enlinkd.snmp.Dot1qVlanStaticTableTracker;
import org.opennms.netmgt.nb.NmsNetworkBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
public class NMS7758En extends NmsNetworkBuilder implements InitializingBean {
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.mock.snmp", "ERROR");
        p.setProperty("log4j.logger.org.opennms.core.test.snmp", "ERROR");
        p.setProperty("log4j.logger.org.opennms.netmgt", "ERROR");
        p.setProperty("log4j.logger.org.springframework","ERROR");
        p.setProperty("log4j.logger.com.mchange.v2.resourcepool", "ERROR");
        p.setProperty("log4j.logger.org.opennms.netmgt.enlinkd.snmp.Dot1qVlanCurrentTableTracker", "DEBUG");
        MockLogAppender.setupLogging(p);
    }
	@Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SAMASW01_IP, port=161, resource=SAMASW01_SNMP_RESOURCE)
    })
	public void testVlanCurrentUsable() throws InterruptedException, UnknownHostException {
        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SAMASW01_IP));
		Dot1qVlanCurrentTableTracker tracker = new Dot1qVlanCurrentTableTracker();
		
		List<Dot1QVLanCurrent>instances = new LinkedList<>();
		tracker.setEntryCreationCallback(entry->instances.add(entry));
		
		SnmpWalker walker = SnmpUtils.createWalker(config, "no name", tracker);
		walker.start();
		walker.waitFor();
		assertTrue(tracker.isFdgId2vlanIdMapUsable());
		System.out.println(tracker.getFdgId2VlanIdMap());
		assertEquals("Too many Dot1QVLanCurrent entries were created",tracker.getVlanMap().size(),instances.size());
		
	}
	
	
	
	
	@Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=STCASW01_IP, port=161, resource=STCASW01_SNMP_RESOURCE)
    })
	public void testVlanCurrentUnusable() throws InterruptedException, UnknownHostException {
        SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(STCASW01_IP));
		Dot1qVlanCurrentTableTracker tracker = new Dot1qVlanCurrentTableTracker();
		
		List<Dot1QVLanCurrent>instances = new LinkedList<>();
		tracker.setEntryCreationCallback(entry->instances.add(entry));
		
		SnmpWalker walker = SnmpUtils.createWalker(config, "no name", tracker);
		walker.start();
		walker.waitFor();
		assertFalse(tracker.isFdgId2vlanIdMapUsable());
		assertEquals(tracker.getVlanMap().size(),instances.size());
		
	}
	
	int [] vlanIds = {
			1, 400, 800, 801, 802, 803, 804, 805, 806, 808, 809, 810, 811, 
			816, 817, 819, 820, 821, 1200, 1201, 1202, 1203, 1204, 1205, 
			1206, 1208, 1209, 1210, 1211, 1216, 1217, 1219, 1220, 1221, 
			2200, 2201, 2202, 2203, 2204, 2205, 2206, 2207, 2208, 2209, 
			2210, 2211, 2212, 2213, 2214, 2400, 2600, 2601, 2602, 2603, 
			2604, 2605
	};

	String [] vlanNames = {"VLAN 1", "Vigili-Arco", "Apss", "Aglav", "Biblio", 
			"Comuni", "Infotn", "Medici", "Mitt", "Pat", "Regione", "Scuole", 
			"Capwap", "Tivoli", "Unitn", "Operauni", "Geologico", "Iasma", 
			"ApssVoce", "Aglavvoce", "Bibliovoce", "Comunivoce", "Infotnvoce", 
			"Medicivoce", "Mittvoce", "Patvoce", "Regionevoce", "Scuolevoce", 
			"Alfavoce", "1216", "Unitnvoce", "Operaunivoce", "Geologicovoce", 
			"Iasmavoce", "Alpikom", "Bcomwinet", "Btwinet", "Fwinet", "Tecnodata", 
			"Tiwinet", "Winnet", "E4A", "Asdasd", "Operatore9", "Operatore10", 
			"Operatore11", "Operatore12", "Operatore13", "Operatore14", "Winet24", 
			"Akwinetvoce", "Bcomwinetvoce", "Btwinetvoce", "Fwwinetvoce", "Tdatawinetvoce", 
			"Tiwinetvoce",
	};
	
	@Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=STCASW01_IP, port=161, resource=STCASW01_SNMP_RESOURCE)
    })
	public void testVlanStaticTable() throws InterruptedException, UnknownHostException {
        assertEquals(56,vlanIds.length);
        assertEquals(56,vlanNames.length);
		
		SnmpAgentConfig  config = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(STCASW01_IP));
        Map<Integer,String> vlanNameMap = new HashMap<>();
        
        Dot1qVlanStaticTableTracker vlanStaticTableTracker = new Dot1qVlanStaticTableTracker(){

			@Override
			public void processDot1qVlanStaticRow(Dot1qVlanStaticRow row) {
				vlanNameMap.put(row.getVlanId(), row.getVlanName());
			}
        	
        };
		SnmpWalker walker = SnmpUtils.createWalker(config, "no name", vlanStaticTableTracker);
		walker.start();
		walker.waitFor();
		assertEquals(56,vlanNameMap.size());
		int cpt = 0;
		for (int vid : vlanIds){
			assertEquals(vlanNameMap.get(vid),vlanNames[cpt++]);
		}
		
	}
	

}
