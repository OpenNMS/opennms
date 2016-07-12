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
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager.EmptyEventConfDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

public class json {
	
	 @Autowired
	    TrapdIpMgr m_trapdIpManager;
	
	@Test
	public void test() {
try
{
		String json="{\"partionKey\":7822,\"trapProcessor\":{\"agentAddress\":\"127.0.0.1\",\"community\":\"%0ix5fuy\",\"varBinds\":{\".1.3.6.1.2.1.2.2.1.3.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":6,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"Bg==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.9.2.2.1.1.20.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"TG9zdCBDYXJyaWVy\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":12,\"berlength\":14,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"TG9zdCBDYXJyaWVy\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.5.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.1.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":343,\"syntax\":2,\"berlength\":4,\"berpayloadLength\":4,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AVc=\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.6.0\":{\"displayable\":false,\"endOfMib\":false,\"variable\":{\"value\":\"BwMA6A==\",\"printable\":false,\"syntax\":4,\"berpayloadLength\":4,\"berlength\":6,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"BwMA6A==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.2.343\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"R2lnYWJpdEV0aGVybmV0OC8zOQ==\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":19,\"berlength\":21,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"R2lnYWJpdEV0aGVybmV0OC8zOQ==\",\"type\":4,\"null\":false,\"error\":false}},\"trapAddress\":\"7.3.0.232\",\"trapIdentity\":{\"generic\":2,\"specific\":0,\"enterpriseId\":\".1.3.6.1.6.3.1.1.5\"},\"version\":\"v1\",\"timeStamp\":2478956802}}";
		
		json="{\"partionKey\":2891,\"trapAddress\":\"159.140.176.92\",\"trapProcessor\":{\"varBinds\":{\".1.3.6.1.6.3.1.1.4.3.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":[1,3,6,1,6,3,1,1,5,4],\"syntax\":6,\"berlength\":11,\"valid\":true,\"berpayloadLength\":11,\"syntaxString\":\"OBJECT IDENTIFIER\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"LjEuMy42LjEuNi4zLjEuMS41LjQ=\",\"type\":6,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.8.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.6.3.18.1.3.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"inetAddress\":\"7.192.48.102\",\"syntax\":64,\"berlength\":6,\"valid\":true,\"berpayloadLength\":6,\"syntaxString\":\"IpAddress\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"B8AwZg==\",\"type\":64,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.1.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":527106048,\"syntax\":2,\"berlength\":6,\"berpayloadLength\":6,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"H2sAAA==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.5.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.7.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.6.0\":{\"displayable\":false,\"endOfMib\":false,\"variable\":{\"value\":\"B8AwZg==\",\"printable\":false,\"syntax\":4,\"berpayloadLength\":4,\"berlength\":6,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"B8AwZg==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.2.1.31.1.1.1.18.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"VUhTTllDVFgxMA==\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":10,\"berlength\":12,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"VUhTTllDVFgxMA==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.2.527106048\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"RXRoZXJuZXQxMDgvMS8x\",\"printable\":true,\"syntax\":4,\"berpayloadLength\":15,\"berlength\":17,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"RXRoZXJuZXQxMDgvMS8x\",\"type\":4,\"null\":false,\"error\":false}},\"agentAddress\":\"159.140.176.92\",\"community\":\"%0ix5fuy\",\"trapIdentity\":{\"generic\":3,\"specific\":0,\"enterpriseId\":\".1.3.6.1.6.3.1.1.5.4\"},\"trapAddress\":\"159.140.176.92\",\"version\":\"v2\",\"timeStamp\":2841522205}}";
		//json="{\"partionKey\":7463,\"trapProcessor\":{\"trapIdentity\":{\"generic\":2,\"specific\":0,\"enterpriseId\":\".1.3.6.1.6.3.1.1.5\"},\"trapAddress\":\"10.101.253.168\",\"varBinds\":{\".1.3.6.1.2.1.2.2.1.2.10003\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"RmFzdEV0aGVybmV0MC8z\",\"syntax\":4,\"printable\":true,\"berlength\":17,\"berpayloadLength\":15,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"RmFzdEV0aGVybmV0MC8z\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.5.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"syntaxString\":\"Integer32\",\"berpayloadLength\":3,\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.1.10003\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":10003,\"syntax\":2,\"berlength\":4,\"syntaxString\":\"Integer32\",\"berpayloadLength\":4,\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"JxM=\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.9.2.2.1.1.20.10003\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"ZG93bg==\",\"syntax\":4,\"printable\":true,\"berlength\":6,\"berpayloadLength\":4,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"ZG93bg==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.2.1.2.2.1.3.10003\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":6,\"syntax\":2,\"berlength\":3,\"syntaxString\":\"Integer32\",\"berpayloadLength\":3,\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"Bg==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.6.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"CmX9qA==\",\"syntax\":4,\"printable\":false,\"berlength\":6,\"berpayloadLength\":4,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"CmX9qA==\",\"type\":4,\"null\":false,\"error\":false}},\"community\":\"%0ix5fuy\",\"agentAddress\":\"159.140.176.92\",\"version\":\"v1\",\"timeStamp\":445331335}}";
		json="{\"partionKey\":9239,\"trapProcessor\":{\"trapAddress\":\"159.140.177.234\",\"trapIdentity\":{\"generic\":0,\"specific\":4,\"enterpriseId\":\".1.3.6.1.4.1.1991.1.13\"},\"varBinds\":{\".1.3.6.1.4.1.1991.1.13.2.1.18.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"W0Nlcm5lciBNQVBTIFdhcm5pbmdzIGFuZCBBbGVydHNdICgxIHRpbWVzIGluIDAgc2Vjb25kcykgUG9ydCAxMC8xLCBDb25kaXRpb249Tk9OX0VfRl9QT1JUUyhMRi9taW4+MyksIEN1cnJlbnQgVmFsdWU6W0xGLDZdLCBSdWxlTmFtZT1kZWZOT05fRV9GX1BPUlRTTEZfMywgRGFzaGJvYXJkIENhdGVnb3J5PVBvcnQgSGVhbHRoLg==\",\"syntax\":4,\"berpayloadLength\":187,\"berlength\":190,\"printable\":true,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"W0Nlcm5lciBNQVBTIFdhcm5pbmdzIGFuZCBBbGVydHNdICgxIHRpbWVzIGluIDAgc2Vjb25kcykgUG9ydCAxMC8xLCBDb25kaXRpb249Tk9OX0VfRl9QT1JUUyhMRi9taW4+MyksIEN1cnJlbnQgVmFsdWU6W0xGLDZdLCBSdWxlTmFtZT1kZWZOT05fRV9GX1BPUlRTTEZfMywgRGFzaGJvYXJkIENhdGVnb3J5PVBvcnQgSGVhbHRoLg==\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.6.3.1.1.4.3.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":[1,3,6,1,4,1,1991,1,13],\"syntax\":6,\"berlength\":11,\"valid\":true,\"berpayloadLength\":11,\"syntaxString\":\"OBJECT IDENTIFIER\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"LjEuMy42LjEuNC4xLjE5OTEuMS4xMw==\",\"type\":6,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.5.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":1,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AQ==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.6.3.1.1.4.1.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":[1,3,6,1,4,1,1991,1,13,2,0,4],\"syntax\":6,\"berlength\":14,\"valid\":true,\"berpayloadLength\":14,\"syntaxString\":\"OBJECT IDENTIFIER\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"LjEuMy42LjEuNC4xLjE5OTEuMS4xMy4yLjAuNA==\",\"type\":6,\"null\":false,\"error\":false},\".1.3.6.1.2.1.1.3.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":0,\"syntax\":67,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"TimeTicks\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"AA==\",\"type\":67,\"null\":false,\"error\":false},\".1.3.6.1.4.1.11.2.17.2.2.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":\"MTU5LjE0MC4xNzcuMjM0\",\"syntax\":4,\"berpayloadLength\":15,\"berlength\":17,\"printable\":true,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"MTU5LjE0MC4xNzcuMjM0\",\"type\":4,\"null\":false,\"error\":false},\".1.3.6.1.4.1.1991.1.13.2.1.4.0\":{\"displayable\":true,\"endOfMib\":false,\"variable\":{\"value\":4,\"syntax\":2,\"berlength\":3,\"berpayloadLength\":3,\"syntaxString\":\"Integer32\",\"exception\":false,\"dynamic\":false},\"numeric\":true,\"bytes\":\"BA==\",\"type\":2,\"null\":false,\"error\":false},\".1.3.6.1.4.1.733.6.3.18.1.6.0\":{\"displayable\":false,\"endOfMib\":false,\"variable\":{\"value\":\"n4yx6g==\",\"syntax\":4,\"berpayloadLength\":4,\"berlength\":6,\"printable\":false,\"syntaxString\":\"OCTET STRING\",\"exception\":false,\"dynamic\":false},\"numeric\":false,\"bytes\":\"n4yx6g==\",\"type\":4,\"null\":false,\"error\":false}},\"community\":\"Public\",\"agentAddress\":\"159.140.176.92\",\"version\":\"v1\",\"timeStamp\":0}}";
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(json);
		
		TrapdKafkaDecoder trapd=new TrapdKafkaDecoder();
		//System.out.println(trapd.parseV1Information(actualObj));
		TrapNotification not=trapd.parseV1Information(actualObj);
		m_trapdIpManager=new TrapdIpManagerDaoImpl();
		TrapQueueProcessor trapQueu=new TrapQueueProcessor();
		//System.out.println(((EventCreator)not.getTrapProcessor()).getEvent());
		BasicTrapProcessor process=(BasicTrapProcessor) not.getTrapProcessor();
		EventCreator event=new EventCreator(m_trapdIpManager);
		event.setAgentAddress(process.getAgentAddress());
		event.setCommunity(process.getCommunity());
		event.setTimeStamp(process.getTimeStamp());
		event.setTrapAddress(process.getTrapAddress());
		event.setTrapIdentity(process.getTrapIdentity());
		event.setVersion(process.getVersion());
		System.out.println(event.getEvent());
		
		EventConfDao event1=new EmptyEventConfDao();
		EventForwarder event2=new EventForwarder() {
			
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
		
		
}
catch(Exception e)
{
	e.printStackTrace();
}
		
		

	}
	
	@Test
	public void test1() {
		  Connection c = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/opennms_test_10354379211671_1551268204",
	            "postgres", "123");
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");
	   }


}
