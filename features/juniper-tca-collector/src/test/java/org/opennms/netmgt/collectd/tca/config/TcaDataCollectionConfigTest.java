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
package org.opennms.netmgt.collectd.tca.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.test.FileAnticipator;
import org.xml.sax.SAXException;

/**
 * The Class TcaDataCollectionConfigTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class TcaDataCollectionConfigTest {

	/** The marshaller. */
	private Marshaller marshaller;

	/** The unmarshaller. */
	private Unmarshaller unmarshaller;

	/** The file anticipator. */
	private FileAnticipator fileAnticipator;

	/** The JAXB context. */
	private JAXBContext context;

	/** The TCA data collection configuration. */
	private TcaDataCollectionConfig tcadcc;

	/**
	 * The Class TestOutputResolver.
	 */
	static private class TestOutputResolver extends SchemaOutputResolver {

		/** The schema file. */
		private final File m_schemaFile;

		/**
		 * Instantiates a new test output resolver.
		 *
		 * @param schemaFile the schema file
		 */
		public TestOutputResolver(File schemaFile) {
			m_schemaFile = schemaFile;
		}

		/* (non-Javadoc)
		 * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String, java.lang.String)
		 */
                @Override
		public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
			return new StreamResult(m_schemaFile);
		}
	}

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		fileAnticipator = new FileAnticipator();
		context = JAXBContext.newInstance(TcaDataCollectionConfig.class);
		marshaller = context.createMarshaller();
		unmarshaller = context.createUnmarshaller();

		TcaRrd rrd = new TcaRrd();
		rrd.addRra("RRA:AVERAGE:0.5:1:3600");
		rrd.addRra("RRA:AVERAGE:0.5:300:288");
		rrd.addRra("RRA:MIN:0.5:300:288");
		rrd.addRra("RRA:MAX:0.5:300:288");
		rrd.addRra("RRA:AVERAGE:0.5:900:2880");
		rrd.addRra("RRA:MIN:0.5:900:2880");
		rrd.addRra("RRA:MAX:0.5:900:2880");
		rrd.addRra("RRA:AVERAGE:0.5:3600:4300");
		rrd.addRra("RRA:MIN:0.5:3600:4300");
		rrd.addRra("RRA:MAX:0.5:3600:4300");

		TcaDataCollection tcadc = new TcaDataCollection();
		tcadc.setName("default");
		tcadc.setRrd(rrd);
		tcadcc = new TcaDataCollectionConfig();
		tcadcc.addDataCollection(tcadc);
		tcadcc.setRrdRepository("target/snmp/");

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setNormalize(true);
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	/**
	 * Generate schema.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void generateSchema() throws Exception {
		File schemaFile = fileAnticipator.expecting("tca-datacollection-config.xsd");
		context.generateSchema(new TestOutputResolver(schemaFile));
		if (fileAnticipator.isInitialized()) {
			fileAnticipator.deleteExpected();
		}
	}

	/**
	 * Generate XML.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void generateXML() throws Exception {
		// Marshal the test object to an XML string
		StringWriter objectXML = new StringWriter();
		marshaller.marshal(tcadcc, objectXML);

		// Read the example XML from src/test/resources
		StringBuffer exampleXML = new StringBuffer();
		File tcaCollectionConfig = getSourceFile();
		assertTrue(TcaDataCollectionConfig.TCA_DATACOLLECTION_CONFIG_FILE + " is readable", tcaCollectionConfig.canRead());
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tcaCollectionConfig), StandardCharsets.UTF_8));
		String line;
		while (true) {
			line = reader.readLine();
			if (line == null) {
				reader.close();
				break;
			}
			exampleXML.append(line).append("\n");
		}
		System.err.println("========================================================================");
		System.err.println("Object XML:");
		System.err.println("========================================================================");
		System.err.print(objectXML.toString());
		System.err.println("========================================================================");
		System.err.println("Example XML:");
		System.err.println("========================================================================");
		System.err.print(exampleXML.toString());
		DetailedDiff myDiff = getDiff(objectXML, exampleXML);
		assertEquals("number of XMLUnit differences between the example XML and the mock object XML is 0", 0, myDiff.getAllDifferences().size());
	}

	/**
	 * Read XML.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void readXML() throws Exception {
		File xmlCollectionConfig = getSourceFile();
		assertTrue(TcaDataCollectionConfig.TCA_DATACOLLECTION_CONFIG_FILE + " is readable", xmlCollectionConfig.canRead());

		InputStream reader = new FileInputStream(xmlCollectionConfig);

		unmarshaller.setSchema(null);
		TcaDataCollectionConfig exampleXmldcc = (TcaDataCollectionConfig)unmarshaller.unmarshal(reader);

		assertTrue("Compare TCA Data Collection Config objects.", tcadcc.equals(exampleXmldcc));

		reader.close();
	}

	/**
	 * Gets the source file.
	 *
	 * @return the source file
	 */
	private File getSourceFile() {
		File tcaCollectionConfig = new File("src/test/resources/etc/", TcaDataCollectionConfig.TCA_DATACOLLECTION_CONFIG_FILE);
		System.err.println("Source File: " + tcaCollectionConfig.getAbsolutePath());
		return tcaCollectionConfig;
	}

	/**
	 * Gets the diff.
	 *
	 * @param objectXML the object XML
	 * @param exampleXML the example XML
	 * @return the detailed diff
	 * @throws SAXException the sAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private DetailedDiff getDiff(StringWriter objectXML, StringBuffer exampleXML) throws SAXException, IOException {
		DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(exampleXML.toString(), objectXML.toString()));
		List<Difference> allDifferences = myDiff.getAllDifferences();
		if (allDifferences.size() > 0) {
			for (Difference d : allDifferences) {
				System.err.println(d);
			}
		}
		return myDiff;
	}
}
