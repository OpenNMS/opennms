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

package org.opennms.core.camel;

import java.io.StringReader;

import javax.xml.bind.Unmarshaller;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Camel {@link Processor} uses {@link JaxbUtils} to unmarshal classes
 * from XML String representations.
 * 
 * @author Seth
 */
public class JaxbUtilsUnmarshalProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(JaxbUtilsUnmarshalProcessor.class);

	private final Class<?> m_class;

	/**
	 * Store a thread-local reference to the {@link Unmarshaller} because 
	 * Unmarshallers are not thread-safe.
	 */
	private final ThreadLocal<Unmarshaller> m_unmarshaller = new ThreadLocal<>();

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public JaxbUtilsUnmarshalProcessor(Class clazz) {
		m_class = clazz;
	}

	public JaxbUtilsUnmarshalProcessor(String className) throws ClassNotFoundException {
		this(Class.forName(className));
	}

	/**
	 * Make sure that this method is fast because it is used to deserialize
	 * JMS messages.
	 */
	@Override
	public void process(final Exchange exchange) throws Exception {
		Unmarshaller unmarshaller = m_unmarshaller.get();
		if (unmarshaller == null) {
			unmarshaller = JaxbUtils.getUnmarshallerFor(m_class, null, false);
			m_unmarshaller.set(unmarshaller);
		}

		// Super slow
		//final String object = exchange.getIn().getBody(String.class);
		//exchange.getIn().setBody(JaxbUtils.unmarshal(m_class, object), m_class);

		// Faster
		//final InputStream object = exchange.getIn().getBody(InputStream.class);
		//exchange.getIn().setBody(unmarshaller.unmarshal(new StreamSource(object)));

		// Somehow, using String is marginally faster than using InputStream ¯\_(ツ)_/¯
		final String object = exchange.getIn().getBody(String.class);
		exchange.getIn().setBody(m_class.cast(unmarshaller.unmarshal(new StringReader(object))));
	}
}
