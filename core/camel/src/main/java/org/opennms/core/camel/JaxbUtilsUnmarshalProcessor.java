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
