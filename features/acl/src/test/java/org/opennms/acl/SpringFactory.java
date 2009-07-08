/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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