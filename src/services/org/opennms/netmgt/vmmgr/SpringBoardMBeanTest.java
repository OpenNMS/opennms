//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.vmmgr;

import java.io.StringReader;

import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.mock.MockLogAppender;

import junit.framework.TestCase;

public class SpringBoardMBeanTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SpringBoardMBeanTest.class);
    }

    public static final String SERVICE_CONFIG = "" +
    "<service-configuration>\n" + 
    "\n" + 
    "   <service>\n" + 
    "       <name>:Name=XSLTProcessor</name>\n" + 
    "       <class-name>mx4j.tools.adaptor.http.XSLTProcessor</class-name>\n" + 
    "   </service>\n" + 
    "\n" + 
    "   <service>\n" + 
    "       <name>:Name=HttpAdaptor</name>\n" + 
    "       <class-name>mx4j.tools.adaptor.http.HttpAdaptor</class-name>\n" + 
    "       <attribute>\n" + 
    "           <name>Port</name>\n" + 
    "           <value type=\"java.lang.Integer\">8180</value>\n" + 
    "       </attribute>\n" + 
    "       <attribute>\n" + 
    "           <name>Host</name>\n" + 
    "           <value type=\"java.lang.String\">127.0.0.1</value>\n" + 
    "       </attribute>\n" + 
    "       <attribute>\n" + 
    "           <name>ProcessorName</name>\n" + 
    "           <value type=\"javax.management.ObjectName\">:Name=XSLTProcessor</value>\n" + 
    "       </attribute>\n" + 
    "       <invoke pass=\"1\" method=\"start\"/>\n" + 
    "   </service>\n" + 
    "\n" + 
    "   <service>\n" + 
    "       <name>:Name=HttpAdaptorMgmt</name>\n" + 
    "       <class-name>mx4j.tools.adaptor.http.HttpAdaptor</class-name>\n" + 
    "       <attribute>\n" + 
    "           <name>Port</name>\n" + 
    "           <value type=\"java.lang.Integer\">8181</value>\n" + 
    "       </attribute>\n" + 
    "       <attribute>\n" + 
    "           <name>Host</name>\n" + 
    "           <value type=\"java.lang.String\">127.0.0.1</value>\n" + 
    "       </attribute>\n" + 
    "       <invoke pass=\"1\" method=\"start\"/>\n" + 
    "   </service>\n" + 
    "\n" + 
    "   <service>\n" + 
    "       <name>OpenNMS:Name=FastExit</name>\n" + 
    "       <class-name>org.opennms.netmgt.vmmgr.Manager</class-name>\n" + 
//    "       <invoke at=\"stop\" pass=\"1\" method=\"doSystemExit\"/>\n" + 
    "   </service>\n" + 
    "\n" + 
    "   <service>\n" + 
    "       <name>OpenNMS:Name=SpringBoard</name>\n" + 
    "       <class-name>org.opennms.netmgt.vmmgr.SpringBoard</class-name>\n" + 
//    "       <invoke pass=\"0\" method=\"init\"/>\n" + 
    "       <invoke pass=\"0\" method=\"start\"/>\n" + 
    "       <invoke at=\"status\" pass=\"0\" method=\"status\"/>\n" + 
    "       <invoke at=\"stop\" pass=\"0\" method=\"stop\"/>\n" + 
    "   </service>\n" + 
    "</service-configuration>\n" + 
    "\n" + 
    "";

    protected void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testMBean() throws Exception {
        StringReader r = new StringReader(SERVICE_CONFIG);
        ServiceConfigFactory f = new ServiceConfigFactory(r);
        r.close();
        ServiceConfigFactory.setInstance(f);
        
        Manager.main(new String[] { "start" });
        
        Manager.main(new String[] { "stop" });
        
        
    }

}
