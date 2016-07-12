package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import kafka.utils.Json;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.persistence.sessions.coordination.broadcast.BroadcastTransportManager;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager.EmptyEventConfDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-trapDaemon.xml",
        }
)
@JUnitTemporaryDatabase
@JUnitConfigurationEnvironment
public class json implements InitializingBean {

	@Autowired
	TrapdIpMgr m_trapdIpManager;
	
	  @Autowired
	    private IpInterfaceDao m_ipInterfaceDao;

	@Test
	public void test() {
		try {
			m_trapdIpManager.dataSourceSync();
			System.out.println(m_trapdIpManager);
			
			String json = "{\"partionKey\":7822,\"trapProcessor\":{\"agentAddress\":\"127.0.0.1\",\"community\":\"%0ix5fuy\",\"varBinds\":{\".1.3.6.1.2.1.2.2.1.3.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":6,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"Bg==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.9.2.2.1.1.20.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"TG9zdCBDYXJyaWVy\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":12,\"berlength\":14,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"TG9zdCBDYXJyaWVy\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.5.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.1.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":343,\"syntax\":2,\"berlength\":4,\"berpayloadLength\":4,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AVc=\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.6.0\":{\"displayable\":false,\"endOfMib\":false,\"variable\":{\"value\":\"BwMA6A==\",\"printable\":false,\"syntax\":4,\"berpayloadLength\":4,\"berlength\":6,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"BwMA6A==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.2.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"R2lnYWJpdEV0aGVybmV0OC8zOQ==\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":19,\"berlength\":21,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"R2lnYWJpdEV0aGVybmV0OC8zOQ==\",\"type\":4,\"null\":false,\"error\":false}},\"trapAddress\":\"7.3.0.232\",\"trapIdentity\":{\"generic\":2,\"specific\":0,\"enterpriseId\":\".1.3.6.1.6.3.1.1.5\"},\"version\":\"v1\",\"timeStamp\":2478956802}}";

			// json="{\"partionKey\":2891,\"trapAddress\":\"159.140.176.92\",\"trapProcessor\":{\"varBinds\":{\".1.3.6.1.6.3.1.1.4.3.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":[1,3,6,1,6,3,1,1,5,4],\"syntax\":6,\"berlength\":11,\"valid\":true,\"berpayloadLength\":11,\"syntaxString\":\"OBJECT IDENTIFIER\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"LjEuMy42LjEuNi4zLjEuMS41LjQ=\",\"type\":6,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.8.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.6.3.18.1.3.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"inetAddress\":\"7.192.48.102\",\"syntax\":64,\"berlength\":6,\"valid\":true,\"berpayloadLength\":6,\"syntaxString\":\"IpAddress\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"B8AwZg==\",\"type\":64,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.1.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":527106048,\"syntax\":2,\"berlength\":6,\"berpayloadLength\":6,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"H2sAAA==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.5.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.7.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.6.0\":{\"displayable\":false,\"endOfMib\":false,\"variable\":{\"value\":\"B8AwZg==\",\"printable\":false,\"syntax\":4,\"berpayloadLength\":4,\"berlength\":6,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"B8AwZg==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.2.1.31.1.1.1.18.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"VUhTTllDVFgxMA==\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":10,\"berlength\":12,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"VUhTTllDVFgxMA==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.2.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"RXRoZXJuZXQxMDgvMS8x\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":15,\"berlength\":17,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"RXRoZXJuZXQxMDgvMS8x\",\"type\":4,\"null\":false,\"error\":false}},\"agentAddress\":\"159.140.176.92\",\"community\":\"%0ix5fuy\",\"trapIdentity\":{\"generic\":3,\"specific\":0,\"enterpriseId\":\".1.3.6.1.6.3.1.1.5.4\"},\"trapAddress\":\"159.140.176.92\",\"version\":\"v2\",\"timeStamp\":2841522205}}";

			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(json);

			TrapdKafkaDecoder trapd = new TrapdKafkaDecoder();
			// System.out.println(trapd.parseV1Information(actualObj));
			TrapNotification not = trapd.parseV1Information(actualObj);


			TrapQueueProcessor trapQueu = new TrapQueueProcessor();
			// System.out.println(((EventCreator)not.getTrapProcessor()).getEvent());
			BasicTrapProcessor process = (BasicTrapProcessor) not
					.getTrapProcessor();
			EventCreator event = new EventCreator(m_trapdIpManager);
			event.setAgentAddress(process.getAgentAddress());
			event.setCommunity(process.getCommunity());
			event.setTimeStamp(process.getTimeStamp());
			event.setTrapAddress(process.getTrapAddress());
			event.setTrapIdentity(process.getTrapIdentity());
			event.setVersion(process.getVersion());
			System.out.println(event.getEvent());

			EventConfDao event1 = new EmptyEventConfDao();
			EventForwarder event2 = new EventForwarder() {

				@Override
				public void sendNow(Log eventLog) {
					// TODO Auto-generated method stub

				}

				@Override
				public void sendNow(org.opennms.netmgt.xml.event.Event event) {
					// TODO Auto-generated method stub

				}
			};
			trapQueu.setEventForwarder(event2);
			trapQueu.setEventConfDao(event1);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test1() {
		Connection c = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager
					.getConnection(
							"jdbc:postgresql://localhost:5432/opennms_test_10354379211671_1551268204",
							"postgres", "123");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		BeanUtils.assertAutowiring(this);
	}

}
