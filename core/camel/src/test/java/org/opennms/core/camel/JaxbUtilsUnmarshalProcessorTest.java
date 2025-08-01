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

import java.util.List;
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
import org.opennms.core.xml.JaxbUtils;

/**
 * @author Seth
 */
public class JaxbUtilsUnmarshalProcessorTest {

	@XmlRootElement(name="test-me")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class TestMe {
		public String lets;
		public String make;
		public String an;
		public String object;
	}

	@Test
	public void testSpeed() throws Exception {
		TestMe test = new TestMe();
		test.lets = "lets";
		test.make = "make";
		test.an = "an";
		test.object = "object";
		String marshalled = JaxbUtils.marshal(test);

		final int numberOfMessages = 10000;
		final int warmUp = 4000;

		CamelContext m_camel = new DefaultCamelContext(new SimpleRegistry());
		JaxbUtilsUnmarshalProcessor processor = new JaxbUtilsUnmarshalProcessor(TestMe.class);
		List<Exchange> exchanges = IntStream.range(0, numberOfMessages).mapToObj(i -> { return new ExchangeBuilder(m_camel).withBody(marshalled).build(); }).collect(Collectors.toList());

		int counter = 0;
		long begin = 0;
		long end = 0;
		for (Exchange exchange : exchanges) {
			processor.process(exchange);
			if (counter++ < warmUp) {
				// Warm up
				continue;
			}

			if (begin == 0) {
				begin = System.currentTimeMillis();
			}
			//System.out.println(end = System.currentTimeMillis());
			end = System.currentTimeMillis();
		}
		System.out.println("BEGIN: " + begin);
		System.out.println("END: " + end);
		System.out.println("TOTAL TIME: " + (end - begin));
		System.out.println("RATE: " + ((double)(numberOfMessages - warmUp)/ (double)(end - begin)) + "/ms");
	}
}
