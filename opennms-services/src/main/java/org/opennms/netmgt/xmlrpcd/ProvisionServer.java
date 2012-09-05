/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
 *******************************************************************************/

package org.opennms.netmgt.xmlrpcd;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>ProvisionServer class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class ProvisionServer {
    
    private ClassPathXmlApplicationContext m_context;

    /**
     * <p>run</p>
     */
    public void run() {
        m_context = new ClassPathXmlApplicationContext("**/*-context.xml");
    }

    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
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

    /**
     * <p>getContext</p>
     *
     * @return a {@link org.springframework.context.ApplicationContext} object.
     */
    public ApplicationContext getContext() {
        return m_context;
    }

}
