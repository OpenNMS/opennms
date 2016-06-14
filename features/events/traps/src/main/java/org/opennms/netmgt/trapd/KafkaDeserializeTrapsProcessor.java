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

package org.opennms.netmgt.trapd;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.snmp.TrapNotification;

import com.sun.mail.iap.ByteArray;

/**
 * This Camel {@link Processor} uses {@link JaxbUtils} to marshal classes
 * into XML String representations.
 * 
 * @author Deepak
 */
public class KafkaDeserializeTrapsProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(KafkaDeserializeTrapsProcessor.class);

	private final Class<?> m_class;

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public KafkaDeserializeTrapsProcessor(Class clazz) {
		m_class = clazz;
	}

	public KafkaDeserializeTrapsProcessor(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		byte[] bytes = exchange.getIn().getBody(byte[].class);
		System.out.println("######################################################");
		System.out.println(bytes);
		System.out.println("######################################################");
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
		TrapNotification notification = (TrapNotification)in.readObject();
		
		exchange.getIn().setBody(notification, TrapNotification.class);
	}
}