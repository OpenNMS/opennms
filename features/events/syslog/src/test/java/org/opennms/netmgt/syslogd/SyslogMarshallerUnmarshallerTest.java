/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.Test;
import org.opennms.core.camel.JaxbUtilsUnmarshalProcessor;
import org.opennms.core.camel.MinionDTO;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;

/**
 * @author Deepak
 */
public class SyslogMarshallerUnmarshallerTest {

	@Test
	public void testSyslogMarshalierAndUnmarshaller() throws Exception {

		// Create a mock SyslogdConfig
		SyslogConfigBean config = new SyslogConfigBean();
		config.setSyslogPort(10514);
		config.setNewSuspectOnMessage(false);

		byte[] messageBytes = "<34>main: 2010-08-19 localhost foo0: load test 0 on tty1\0".getBytes("US-ASCII");

		UUID systemId = UUID.randomUUID();
		SyslogConnection syslogConn = new SyslogConnection(InetAddressUtils.ONE_TWENTY_SEVEN, 2000, ByteBuffer.wrap(messageBytes), config, systemId.toString(), MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
		
		// Map from SyslogConnection to SyslogDTO object
		MinionDTOMapper minionDtoMapper = new MinionDTOMapper();
		SyslogDTO syslogDto = minionDtoMapper.object2dto(syslogConn);
		
		// Marshall SyslogDTO
		String marshalled = JaxbUtils.marshal(syslogDto);
		
		System.out.println("marshalled is : "+marshalled);
		
		
		// Unmarshall SyslogDTO
		CamelContext m_camel = new DefaultCamelContext(new SimpleRegistry());
		JaxbUtilsUnmarshalProcessor processor = new JaxbUtilsUnmarshalProcessor(SyslogDTO.class);
		List<Exchange> exchanges = IntStream.range(0,1).mapToObj(i -> { return new ExchangeBuilder(m_camel).withBody(marshalled).build(); }).collect(Collectors.toList());

		for(Exchange exchange : exchanges){
			//Exchange exchange = exchanges.get(0);
			processor.process(exchange);
			System.out.println("exchange is : "+exchange);
		}
	}
}
