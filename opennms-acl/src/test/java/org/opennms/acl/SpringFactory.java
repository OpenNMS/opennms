package org.opennms.acl;

import javax.servlet.ServletContext;

import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class SpringFactory {

    public static void setUpXmlWebApplicationContext() {
        if (xmlWebCtx == null) {
            ServletContext servletContext = new MockServletContext("file:src/test/resources/org/opennms/acl/conf/");
            String[] paths = { "acl-context.xml" };
            xmlWebCtx = new XmlWebApplicationContext();
            xmlWebCtx.setConfigLocations(paths);
            xmlWebCtx.setServletContext(servletContext);
            xmlWebCtx.refresh();
            System.out.println("Start XmlWebApplicationContext");
        }
    }

    public static Object getBean(String name) {
        return xmlWebCtx.getBean(name);
    }

    public static XmlWebApplicationContext getXmlWebApplicationContext() {
        if (xmlWebCtx == null) {
            setUpXmlWebApplicationContext();
        }
        return xmlWebCtx;

    }

    public static void destroyXmlWebApplicationContext() {
        xmlWebCtx = null;
        System.out.println("XmlWebApplicationContext Stop");
    }

    private static XmlWebApplicationContext xmlWebCtx;
}