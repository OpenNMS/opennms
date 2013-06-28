package org.opennms.netmgt.config.poller;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

public class AnyObjectAdapterTest {
	
	@Test
	public void testAdapter() throws Exception {
		String source = "<person name=\"alejandro\"/>";
		AnyObjectAdapter adapter = new AnyObjectAdapter();
		Element element = adapter.marshal(new XmlContent(source));
		XmlContent xml = adapter.unmarshal(element);
		Assert.assertEquals(source, xml.getContent());
	}

}
