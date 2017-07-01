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
