package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.test.ConfigurationTestUtils;

public class C3P0ConnectionFactoryTest extends TestCase {
    public void testMarshalDataSourceFromConfig() throws Exception {
        makeFactory("opennms");
        C3P0ConnectionFactory factory2 = makeFactory("opennms2");
        
        Connection conn = null;
        Statement s = null;
        try {
            conn = factory2.getConnection();
            s = conn.createStatement();
            s.execute("select * from service");
        } finally {
            if (s != null) {
                s.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

    }

    private C3P0ConnectionFactory makeFactory(String database) throws MarshalException, ValidationException, PropertyVetoException, SQLException, IOException {
        Reader rdr = ConfigurationTestUtils.getReaderForResource(this, "opennms-datasources.xml");
        try {
            return new C3P0ConnectionFactory(rdr, database);
        } finally {
            rdr.close();
        }
    }
}
