package org.opennms.netmgt.model.ncs;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.Test;
import org.opennms.netmgt.model.ncs.NCSBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.xml.sax.SAXException;

public class JAXBTest {
	


	@Test
	public void testMarshall() throws JAXBException, UnsupportedEncodingException, SAXException {
		
		final String expectedXML = "" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
				"<component xmlns=\"http://xmlns.opennms.org/xsd/model/ncs\" type=\"Service\" foreignId=\"123\" foreignSource=\"NA-Service\">\n" + 
				"    <name>CokeP2P</name>\n" + 
				"    <component type=\"ServiceElement\" foreignId=\"8765\" foreignSource=\"NA-ServiceElement\">\n" + 
				"        <name>PE1,SE1</name>\n" + 
				"        <node foreignId=\"1111-PE1\" foreignSource=\"space\"/>\n" + 
				"        <component type=\"ServiceElementComponent\" foreignId=\"8765,jnxVpnIf\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"            <name>jnxVpnIf</name>\n" + 
				"            <upEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp</upEventUei>\n" + 
				"            <downEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown</downEventUei>\n" + 
				"            <attributes>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnIfVpnType</key>\n" + 
				"                    <value>5</value>\n" + 
				"                </attribute>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnIfVpnName</key>\n" + 
				"                    <value>ge-1/0/2.50</value>\n" + 
				"                </attribute>\n" + 
				"            </attributes>\n" + 
				"            <component type=\"ServiceElementComponent\" foreignId=\"8765,link\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"                <name>link</name>\n" + 
				"                <upEventUei>uei.opennms.org/vendor/Juniper/traps/linkUp</upEventUei>\n" + 
				"                <downEventUei>uei.opennms.org/vendor/Juniper/traps/linkDown</downEventUei>\n" + 
				"                <attributes>\n" + 
				"                    <attribute>\n" + 
				"                        <key>linkName</key>\n" + 
				"                        <value>ge-1/0/2</value>\n" + 
				"                    </attribute>\n" + 
				"                </attributes>\n" + 
				"            </component>\n" + 
				"        </component>\n" + 
				"        <component type=\"ServiceElementComponent\" foreignId=\"8765,jnxVpnPw-vcid(50)\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"            <name>jnxVpnPw-vcid(50)</name>\n" + 
				"            <upEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp</upEventUei>\n" + 
				"            <downEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown</downEventUei>\n" + 
				"            <dependenciesRequired>ANY</dependenciesRequired>\n" + 
				"            <attributes>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnPwVpnType</key>\n" + 
				"                    <value>5</value>\n" + 
				"                </attribute>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnPwVpnName</key>\n" + 
				"                    <value>ge-1/0/2.50</value>\n" + 
				"                </attribute>\n" + 
				"            </attributes>\n" + 
				"            <component type=\"ServiceElementComponent\" foreignId=\"8765,lspA-PE1-PE2\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"                <name>lspA-PE1-PE2</name>\n" + 
				"                <upEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp</upEventUei>\n" + 
				"                <downEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown</downEventUei>\n" + 
				"                <attributes>\n" + 
				"                    <attribute>\n" + 
				"                        <key>mplsLspName</key>\n" + 
				"                        <value>lspA-PE1-PE2</value>\n" + 
				"                    </attribute>\n" + 
				"                </attributes>\n" + 
				"            </component>\n" + 
				"            <component type=\"ServiceElementComponent\" foreignId=\"8765,lspB-PE1-PE2\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"                <name>lspB-PE1-PE2</name>\n" + 
				"                <upEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp</upEventUei>\n" + 
				"                <downEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown</downEventUei>\n" + 
				"                <attributes>\n" + 
				"                    <attribute>\n" + 
				"                        <key>mplsLspName</key>\n" + 
				"                        <value>lspB-PE1-PE2</value>\n" + 
				"                    </attribute>\n" + 
				"                </attributes>\n" + 
				"            </component>\n" + 
				"        </component>\n" + 
				"    </component>\n" + 
				"    <component type=\"ServiceElement\" foreignId=\"9876\" foreignSource=\"NA-ServiceElement\">\n" + 
				"        <name>PE2,SE1</name>\n" + 
				"        <node foreignId=\"2222-PE2\" foreignSource=\"space\"/>\n" + 
				"        <component type=\"ServiceElementComponent\" foreignId=\"9876,jnxVpnIf\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"            <name>jnxVpnIf</name>\n" + 
				"            <upEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp</upEventUei>\n" + 
				"            <downEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown</downEventUei>\n" + 
				"            <attributes>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnIfVpnType</key>\n" + 
				"                    <value>5</value>\n" + 
				"                </attribute>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnIfVpnName</key>\n" + 
				"                    <value>ge-3/1/4.50</value>\n" + 
				"                </attribute>\n" + 
				"            </attributes>\n" + 
				"            <component type=\"ServiceElementComponent\" foreignId=\"9876,link\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"                <name>link</name>\n" + 
				"                <upEventUei>uei.opennms.org/vendor/Juniper/traps/linkUp</upEventUei>\n" + 
				"                <downEventUei>uei.opennms.org/vendor/Juniper/traps/linkDown</downEventUei>\n" + 
				"                <attributes>\n" + 
				"                    <attribute>\n" + 
				"                        <key>linkName</key>\n" + 
				"                        <value>ge-3/1/4</value>\n" + 
				"                    </attribute>\n" + 
				"                </attributes>\n" + 
				"            </component>\n" + 
				"        </component>\n" + 
				"        <component type=\"ServiceElementComponent\" foreignId=\"9876,jnxVpnPw-vcid(50)\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"            <name>jnxVpnPw-vcid(50)</name>\n" + 
				"            <upEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp</upEventUei>\n" + 
				"            <downEventUei>uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown</downEventUei>\n" + 
				"            <dependenciesRequired>ANY</dependenciesRequired>\n" + 
				"            <attributes>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnPwVpnType</key>\n" + 
				"                    <value>5</value>\n" + 
				"                </attribute>\n" + 
				"                <attribute>\n" + 
				"                    <key>jnxVpnPwVpnName</key>\n" + 
				"                    <value>ge-3/1/4.50</value>\n" + 
				"                </attribute>\n" + 
				"            </attributes>\n" + 
				"            <component type=\"ServiceElementComponent\" foreignId=\"9876,lspA-PE2-PE1\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"                <name>lspA-PE2-PE1</name>\n" + 
				"                <upEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp</upEventUei>\n" + 
				"                <downEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown</downEventUei>\n" + 
				"                <attributes>\n" + 
				"                    <attribute>\n" + 
				"                        <key>mplsLspName</key>\n" + 
				"                        <value>lspA-PE2-PE1</value>\n" + 
				"                    </attribute>\n" + 
				"                </attributes>\n" + 
				"            </component>\n" + 
				"            <component type=\"ServiceElementComponent\" foreignId=\"9876,lspB-PE2-PE1\" foreignSource=\"NA-SvcElemComp\">\n" + 
				"                <name>lspB-PE2-PE1</name>\n" + 
				"                <upEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp</upEventUei>\n" + 
				"                <downEventUei>uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown</downEventUei>\n" + 
				"                <attributes>\n" + 
				"                    <attribute>\n" + 
				"                        <key>mplsLspName</key>\n" + 
				"                        <value>lspB-PE2-PE1</value>\n" + 
				"                    </attribute>\n" + 
				"                </attributes>\n" + 
				"            </component>\n" + 
				"        </component>\n" + 
				"    </component>\n" + 
				"</component>\n" + 
				"";

		
		NCSComponent svc = new NCSBuilder("Service", "NA-Service", "123")
		.setName("CokeP2P")
		.pushComponent("ServiceElement", "NA-ServiceElement", "8765")
			.setName("PE1,SE1")
			.setNodeIdentity("space", "1111-PE1")
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,jnxVpnIf")
				.setName("jnxVpnIf")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
				.setAttribute("jnxVpnIfVpnType", "5")
				.setAttribute("jnxVpnIfVpnName", "ge-1/0/2.50")
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,link")
					.setName("link")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
					.setAttribute("linkName", "ge-1/0/2")
				.popComponent()
			.popComponent()
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,jnxVpnPw-vcid(50)")
				.setName("jnxVpnPw-vcid(50)")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
				.setAttribute("jnxVpnPwVpnType", "5")
				.setAttribute("jnxVpnPwVpnName", "ge-1/0/2.50")
				.setDependenciesRequired(DependencyRequirements.ANY)
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,lspA-PE1-PE2")
					.setName("lspA-PE1-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspA-PE1-PE2")
				.popComponent()
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765,lspB-PE1-PE2")
					.setName("lspB-PE1-PE2")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspB-PE1-PE2")
				.popComponent()
			.popComponent()
		.popComponent()
		.pushComponent("ServiceElement", "NA-ServiceElement", "9876")
			.setName("PE2,SE1")
			.setNodeIdentity("space", "2222-PE2")
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnIf")
				.setName("jnxVpnIf")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
				.setAttribute("jnxVpnIfVpnType", "5")
				.setAttribute("jnxVpnIfVpnName", "ge-3/1/4.50")
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,link")
					.setName("link")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
					.setAttribute("linkName", "ge-3/1/4")
				.popComponent()
			.popComponent()
			.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,jnxVpnPw-vcid(50)")
				.setName("jnxVpnPw-vcid(50)")
				.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
				.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
				.setAttribute("jnxVpnPwVpnType", "5")
				.setAttribute("jnxVpnPwVpnName", "ge-3/1/4.50")
				.setDependenciesRequired(DependencyRequirements.ANY)
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,lspA-PE2-PE1")
					.setName("lspA-PE2-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspA-PE2-PE1")
				.popComponent()
				.pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876,lspB-PE2-PE1")
					.setName("lspB-PE2-PE1")
					.setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
					.setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
					.setAttribute("mplsLspName", "lspB-PE2-PE1")
				.popComponent()
			.popComponent()
		.popComponent()
		.get();
		
		// Create a Marshaller
		JAXBContext context = JAXBContext.newInstance(NCSComponent.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		// save the output in a byte array
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// marshall the output
		marshaller.marshal(svc, out);

		// verify its matches the expected results
		byte[] utf8 = out.toByteArray();

		String result = new String(utf8, "UTF-8");
		assertEquals(expectedXML, result);
		
		System.err.println(result);
		
		// unmarshall the generated XML
		
		URL xsd = getClass().getResource("/ncs-model.xsd");
		
		assertNotNull(xsd);
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema schema = schemaFactory.newSchema(xsd);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
		unmarshaller.setSchema(schema);
		Source source = new StreamSource(new ByteArrayInputStream(utf8));
		NCSComponent read = unmarshaller.unmarshal(source, NCSComponent.class).getValue();
		
		assertNotNull(read);
		
		// round trip back to XML and make sure we get the same thing
		ByteArrayOutputStream reout = new ByteArrayOutputStream();
		marshaller.marshal(read, reout);
		
		String roundTrip = new String(reout.toByteArray(), "UTF-8");
		
		assertEquals(expectedXML, roundTrip);
	}

}
