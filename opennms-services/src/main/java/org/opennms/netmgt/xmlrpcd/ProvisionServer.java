/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.xmlrpcd;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class ProvisionServer {
    
    private ClassPathXmlApplicationContext m_context;

    public void run() {
        m_context = new ClassPathXmlApplicationContext("**/*-context.xml");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Properties logConfig = new Properties();
        logConfig.put("log4j.rootCategory", "DEBUG, CONSOLE");
        logConfig.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        logConfig.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        logConfig.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");
        PropertyConfigurator.configure(logConfig);

        ProvisionServer svr = new ProvisionServer();
        svr.run();
    }

    public ApplicationContext getContext() {
        return m_context;
    }

}
