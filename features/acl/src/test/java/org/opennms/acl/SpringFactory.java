/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

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