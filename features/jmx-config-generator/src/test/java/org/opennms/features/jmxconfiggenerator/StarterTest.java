/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("there has to be a running server to test these")
public class StarterTest {

	@Test
	public void testGeronimo() throws Throwable {

		// -jmx -url service:jmx:rmi:///jndi/rmi://localhost/JMXConnector -out
		// JMX-DatacollectionDummy.xml -username system -password manager

		List<String> args = new ArrayList<String>();
		args.add("-jmx");
		args.add("-url");
		args.add("service:jmx:rmi:///jndi/rmi://localhost/JMXConnector");
		args.add("-out");
		args.add("JMX-DatacollectionDummy.xml");
		args.add("-username");
		args.add("system");
		args.add("-password");
		args.add("manager");

		Starter.main(args.toArray(new String[] {}));

	}

	@Test
	public void testJboss7() throws Throwable {

		System.out.print(System.getProperty("java.class.path"));

		// CLASSPATH
		// /usr/lib/jvm/default-java//lib/jconsole.jar:/usr/lib/jvm/default-java//lib/tools.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/remoting3/remoting-jmx/main/remoting-jmx-1.0.2.Final.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/remoting3/main/jboss-remoting-3.2.3.GA.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/logging/main/jboss-logging-3.1.0.GA.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/xnio/main/xnio-api-3.0.3.GA.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/xnio/nio/main/xnio-nio-3.0.3.GA.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/sasl/main/jboss-sasl-1.0.0.Final.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/marshalling/main/jboss-marshalling-1.3.11.GA.jar
		// :/opt/jboss/jboss-as-7.1.1.Final/modules/org/jboss/marshalling/river/main/jboss-marshalling-river-1.3.11.GA.jar

		// /usr/lib/jvm/default-java//lib/jconsole.jar:/usr/lib/jvm/default-java//lib/tools.jar:/opt/jboss/jboss-as-7.1.1.Final/bin/client/jboss-client.jar

		// -jmx -url service:jmx:remoting-jmx://localhost:9999 -out
		// JMX-DatacollectionDummy.xml

		// java -classpath /usr/lib/jvm/default-java//lib/jconsole.jar:/usr/lib/jvm/default-java//lib/tools.jar:/opt/jboss/jboss-as-7.1.1.Final/bin/client/jboss-client.jar  -jar JmxConfigGenerator.jar -jmx -url service:jmx:remoting-jmx://localhost:9999 -out JMX-DatacollectionDummy.xml

		List<String> args = new ArrayList<String>();
		args.add("-jmx");
		args.add("-url");
		args.add("service:jmx:remoting-jmx://localhost:9999");
		args.add("-out");
		args.add("JMX-DatacollectionDummy.xml");
		// args.add("-username");
		// args.add("system");
		// args.add("-password");
		// args.add("manager");

		Starter.main(args.toArray(new String[] {}));
	}

}
