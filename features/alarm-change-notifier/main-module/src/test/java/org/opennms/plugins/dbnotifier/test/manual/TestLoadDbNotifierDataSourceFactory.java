package org.opennms.plugins.dbnotifier.test.manual;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.opennms.plugins.dbnotifier.DbNotifierDataSourceFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.impossibl.postgres.jdbc.PGDataSource;

public class TestLoadDbNotifierDataSourceFactory {

	
	@Test
	public void testLoadFromProperties() {
		
		System.out.println("start of testLoadFromProperties");

		DbNotifierDataSourceFactory dsConfig = new DbNotifierDataSourceFactory();
		
		dsConfig.setDataBaseName("testdataBaseName");
		dsConfig.setUserName("testuserName");
		dsConfig.setPassWord("testpassWord");
		dsConfig.setHostname("localhost");
		dsConfig.setPort("5432");

		dsConfig.init();
		
		assertEquals("testdataBaseName",dsConfig.getDataBaseName());
		assertEquals("testuserName",dsConfig.getUserName());
		assertEquals("testpassWord",dsConfig.getPassWord());
		assertEquals("localhost",dsConfig.getHostname());
		assertEquals("5432",dsConfig.getPort());
		
		PGDataSource pgds = dsConfig.getPGDataSource();
		assertNotNull(pgds);
		
		System.out.println("end of testLoadFromProperties");
		
	}
	
	@Test
	public void testLoadFromXML() {
		
		System.out.println("start of testLoadFromXML");

		DbNotifierDataSourceFactory dsConfig = new DbNotifierDataSourceFactory();
		
		String fileUri=null;
		fileUri="./src/test/resources/opennms-datasources.xml";
		dsConfig.setDataSourceFileUri(fileUri);
		dsConfig.init();
		
		assertEquals("opennms",dsConfig.getDataBaseName());
		assertEquals("opennms",dsConfig.getUserName());
		assertEquals("opennms",dsConfig.getPassWord());
		assertEquals("localhost",dsConfig.getHostname());
		assertEquals("5432",dsConfig.getPort());
		
		PGDataSource pgds = dsConfig.getPGDataSource();
		assertNotNull(pgds);
		
		System.out.println("end of testLoadFromXML");
		
	}

}
