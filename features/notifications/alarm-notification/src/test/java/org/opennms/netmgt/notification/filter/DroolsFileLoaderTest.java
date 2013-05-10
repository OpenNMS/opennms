package org.opennms.netmgt.notification.filter;

import static org.junit.Assert.*;

import org.aspectj.lang.annotation.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DroolsFileLoaderTest {
	@Before(value = "")
	public void testDroolsFileLoader() {
		new DroolsFileLoader();
	}

	//
	// @Test
	// public void testSetBuilderHash() {
	// fail("Not yet implemented");
	// }

	@Test
	public void testGetKnowledgeBaseForDrl() {
		String drlName = "Ip_10.212.96.214.drl";
		new DroolsFileLoader();
		assertNull(DroolsFileLoader.getKnowledgeBaseForDrl(drlName));
	}

}
