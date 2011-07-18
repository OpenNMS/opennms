package org.opennms.netmgt.config.dhcpd;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class DhcpdConfigFactoryTest {

	@Test
	public void testRead() throws Exception {
		/*
		 * <DhcpdConfiguration
		 *    port="5818"
	     *    macAddress="00:06:0D:BE:9C:B2"
         *    myIpAddress="127.0.0.1"
         *    extendedMode="false"
         *    requestIpAddress="127.0.0.1"> 
         * </DhcpdConfiguration>
         *
		 */
		DhcpdConfigFactory factory = new DhcpdConfigFactory(new File("src/main/etc/dhcpd-configuration.xml"));
		
		assertEquals(5818, factory.getPort());
		assertEquals("00:06:0D:BE:9C:B2", factory.getMacAddress());
		assertEquals("127.0.0.1", factory.getMyIpAddress());
		assertEquals("false", factory.getExtendedMode());
		assertEquals("127.0.0.1", factory.getRequestIpAddress());
	}

}
