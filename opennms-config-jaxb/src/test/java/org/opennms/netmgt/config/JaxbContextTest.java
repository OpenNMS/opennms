package org.opennms.netmgt.config;

import org.junit.Test;
import org.opennms.core.test.xml.JaxbTestUtils;

import javax.xml.bind.JAXBException;

public class JaxbContextTest {
    @Test
    public void testJaxbContext() throws JAXBException {
        JaxbTestUtils.verifyJaxbContext(getClass().getPackage().getName());
    }
}
