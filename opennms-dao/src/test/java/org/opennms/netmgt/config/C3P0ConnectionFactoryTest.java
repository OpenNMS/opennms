package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

public class C3P0ConnectionFactoryTest extends TestCase {

	private static String m_config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<datasource-configuration xmlns:this=\"http://xmlns.opennms.org/xsd/config/opennms-datasources\" \n" + 
			"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
			"  xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/config/opennms-datasources \n" + 
			"                      http://www.opennms.org/xsd/config/opennms-datasources.xsd \">\n" + 
			"  <jdbc-data-source name=\"opennms\" \n" + 
			"                    class-name=\"org.postgresql.Driver\" \n" + 
			"                    url=\"jdbc:postgresql://localhost:5432/opennms\"\n" + 
			"                    user-name=\"opennms\"\n" + 
			"                    password=\"opennms\" />\n" + 
			"  <jdbc-data-source name=\"opennms2\" " + 
			"                    class-name=\"org.postgresql.Driver\" " + 
			"                    url=\"jdbc:postgresql://localhost:5432/opennms\"> " + 
			"                    <param name=\"user\" value=\"opennms\"/> " + 
			"                    <param name=\"password\" value=\"opennms\"/> " +
			"  </jdbc-data-source> " + 
			"</datasource-configuration>";
	
	public void testMarshalDataSourceFromConfig() throws MarshalException, ValidationException, PropertyVetoException, SQLException, IOException {
		
		Connection conn = null;
		Statement s = null;
		try {
		Reader rdr = new StringReader(m_config);
		C3P0ConnectionFactory factory = new C3P0ConnectionFactory(rdr, "opennms");
		rdr.close();
		rdr = new StringReader(m_config);
		C3P0ConnectionFactory factory2 = new C3P0ConnectionFactory(rdr, "opennms2");
		rdr.close();
		conn = factory2.getConnection();
		s = conn.createStatement();
		s.execute("select * from service");
		} finally {
			if (s != null ) s.close();
			if (conn != null) conn.close();
		}
		
	}

}
