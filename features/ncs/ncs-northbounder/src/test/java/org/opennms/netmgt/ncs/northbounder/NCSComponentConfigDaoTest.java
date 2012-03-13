package org.opennms.netmgt.ncs.northbounder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import org.junit.Test;
import org.opennms.netmgt.ncs.northbounder.NCSNorthbounderConfig.HttpMethod;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class NCSComponentConfigDaoTest {
	
	String xml = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<ncs-northbounder-config enabled=\"true\" nagles-delay=\"10000\"" +
			"		scheme=\"https\" " +
			"		port=\"10321\"" +
			"		path=\"/fmpm/restful/NotificationMessageRelay\"" +
			"		query=\"queryString\"" +
			"		fragment=\"17\"" +
			">\n" +
			"	<uei>uei.opennms.org/internal/ncs/componentImpacted</uei>\n" +
			"	<uei>uei.opennms.org/internal/ncs/componentResolved</uei>\n" +
			"</ncs-northbounder-config>\n" +
			"";

	/*
    
    @XmlAttribute(name="query", required=false)
    private String m_query;
    
    @XmlAttribute(name="fragment", required=false)
    private String m_fragment;

	 */
	@Test
	public void testLoad() {
		
		System.err.println(xml);
		
		Resource resource = new ByteArrayResource(xml.getBytes());
				
		NCSNorthbounderConfigDao dao = new NCSNorthbounderConfigDao();
		dao.setConfigResource(resource);
		dao.afterPropertiesSet();
		
		NCSNorthbounderConfig config = dao.getConfig();
		
		assertNotNull(config);
		assertEquals(HttpMethod.POST, config.getMethod());
		assertEquals("1.1", config.getHttpVersion());
		assertEquals("https", config.getScheme());
		assertEquals(Integer.valueOf(10321), config.getPort());
		assertEquals("/fmpm/restful/NotificationMessageRelay", config.getPath());
		assertEquals("queryString", config.getQuery());
		assertEquals("17", config.getFragment());
		
		assertNotNull(config.getAcceptableUeis());
		assertEquals(2, config.getAcceptableUeis().size());
		assertEquals("uei.opennms.org/internal/ncs/componentImpacted", config.getAcceptableUeis().get(0));
		assertEquals("uei.opennms.org/internal/ncs/componentResolved", config.getAcceptableUeis().get(1));
		
	}

}
