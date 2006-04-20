package org.opennms.netmgt.config;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;

import junit.framework.TestCase;

public class CollectdConfigFactoryTest extends TestCase {
	
	private CollectdConfigFactory m_factory;

	protected void setUp() throws Exception {
		super.setUp();
		
		MockNetwork network = new MockNetwork();
		
		MockDatabase db = new MockDatabase();
		db.populate(network);
		
		DataSourceFactory.setInstance(db);
		
		

		Reader rdr = new InputStreamReader(getClass().getResourceAsStream("/org/opennms/netmgt/config/collectd-testdata.xml"));
		m_factory = new CollectdConfigFactory(rdr, "localhost", false);
		rdr.close();
		
	}

	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	public void testGetName() {
		String pkgName = "example1";
		Package pkg = m_factory.getPackage(pkgName);
		assertNotNull(pkg);
		assertEquals(pkgName, pkg.getName());
	}

}
