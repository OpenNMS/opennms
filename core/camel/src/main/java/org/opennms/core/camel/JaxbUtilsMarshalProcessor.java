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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Camel {@link Processor} uses {@link JaxbUtils} to marshal classes
 * into XML String representations.
 * 
 * @author Seth
 */
public class JaxbUtilsMarshalProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(JaxbUtilsMarshalProcessor.class);

	private final Class<?> m_class;

	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public JaxbUtilsMarshalProcessor(Class clazz) {
		m_class = clazz;
	}

	public JaxbUtilsMarshalProcessor(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object object = exchange.getIn().getBody(m_class);
		exchange.getIn().setBody(JaxbUtils.marshal(object), String.class);
	}
}
