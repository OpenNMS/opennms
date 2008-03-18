//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
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

package org.opennms.poller.remote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public class Main {
    
    String[] m_args;
    ClassPathXmlApplicationContext m_context;
    PollerFrontEnd m_frontEnd;
    String m_url;
    String m_locationName;
    boolean m_shuttingDown = false;;
    
    
    private Main(String[] args) {
        m_args = args;
    }
    
    private void run() {
        
        try {
        
            parseArguments();
        
            createAppContext();
        
            registerShutDownHook();

            if (!m_frontEnd.isRegistered()) {
                m_frontEnd.register(m_locationName);
            }    
            
        } catch(Exception e) {
            // a fatal exception occurred
            ThreadCategory.getInstance(getClass()).fatal("Exception occurred during registration!", e);
            System.exit(27);
        }
        
    }

    private void parseArguments() {
        if (m_args.length < 2) {
            usage();
        }
        
        m_url = m_args[0];
        m_locationName = m_args[1];
        
    }

    private void registerShutDownHook() {
        Thread shutdownHook = new Thread() {
            public void run() {
                m_shuttingDown = true;
                m_context.close();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void createAppContext() {
        
        File homeDir = new File(System.getProperty("user.home"));
        String homeUrl = homeDir.toURI().toString();
        if (homeUrl.endsWith("/")) {
            homeUrl = homeUrl.substring(0, homeUrl.length()-1);
        }

        System.err.println("user.home.url = "+homeUrl);
        System.setProperty("user.home.url", homeUrl);

        System.err.println("opennms.poller.server.url = "+m_url);
        System.setProperty("opennms.poller.server.url", m_url);
        
        String[] configs = {
                "classpath:/META-INF/opennms/applicationContext-remotePollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerFrontEnd.xml"
        };
        
        m_context = new ClassPathXmlApplicationContext(configs);
        m_frontEnd = (PollerFrontEnd) m_context.getBean("pollerFrontEnd");
        
        m_frontEnd.addPropertyChangeListener(new PropertyChangeListener() {
            
            private boolean shouldExit(PropertyChangeEvent e) {
                String propName = e.getPropertyName();
                Object newValue = e.getNewValue();
                
                // if exitNecessary becomes true.. then return true
                if ("exitNecessary".equals(propName) && Boolean.TRUE.equals(newValue)) {
                    return true;
                }
                
                // if started becomes false the we should exit
                if ("started".equals(propName) && Boolean.FALSE.equals(newValue)) {
                    return true;
                }
                
                return false;
                
            }

            public void propertyChange(PropertyChangeEvent e) {
                if (!m_shuttingDown && shouldExit(e)) {
                    System.exit(1);
                }
            }
            
        });
    }
		
    private void usage() {
        System.err.println("The remote poller is not registered with the server.");
        System.err.println("Register it by running this command:");
        System.err.println("\tjava -jar opennms-remote-poller.jar <server URL> <location name>");
        System.err.println("where:");
        System.err.println("\t<server URL>    is URL of the RMI service on the OpenNMS server,");
        System.err.println("\t                usually 'rmi://<server name>'.");
        System.err.println("\t<location name> is name of a configured monitoring location");
        System.err.println("\t                definition on the OpenNMS server.");
        System.exit(1);
    }

    public static void main(String[] args) {
        
        new Main(args).run();
        
	}



}
