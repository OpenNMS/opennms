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

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class NewManager {
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Manager";

    public static void main(String[] args) throws Exception {
        NewManager mgr = new NewManager();
        mgr.doMain(args);
    }

    void doMain(String[] args) throws Exception {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);


        if (args.length == 0 || "start".equals(args[0])) {
            startServer();
        } else if (args.length != 0 && "stop".equals(args[0])) {
            stopServer();
        } else if (args.length != 0 && "status".equals(args[0])) {
            statusOfServer();
        }
       
    }

    private void startServer() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        // set up the JMX logging
        //
        mx4j.log.Log.redirectTo(new mx4j.log.Log4JLogger());
        
        String appContext = System.getProperty("opennms.appcontext", "opennms-appContext.xml");
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(appContext);
    }

    private void statusOfServer() {
        invokeCmd("status");
    }

    private void stopServer() {
        invokeCmd("stop");
    }

    private void invokeCmd(String cmd) {
        Category log = ThreadCategory.getInstance(Manager.class);
        try {
            URL invoke = new URL("http://127.0.0.1:8181/invoke?objectname=OpenNMS%3AName=FastExit&operation="+cmd);
            InputStream in = invoke.openStream();
            int ch;
            while ((ch = in.read()) != -1)
                System.out.write((char) ch);
            in.close();
            System.out.println("");
            System.out.flush();
        } catch (Throwable t) {
            log.error("error invoking "+cmd+" command", t);
        }
    }

    public List status() {
        System.err.println("Status Called");
        return Collections.singletonList("We are here. We are here! WE ARE HERE!");
    }

    public void stop() {
        System.err.println("Stop Called!");
    }

    public void doSystemExit() {
        // TODO Auto-generated method stub
        
    }


}
