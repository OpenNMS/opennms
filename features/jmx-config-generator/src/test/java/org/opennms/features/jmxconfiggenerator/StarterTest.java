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
