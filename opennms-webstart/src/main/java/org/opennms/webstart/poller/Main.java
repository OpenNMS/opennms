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

package org.opennms.webstart.poller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
    boolean m_shuttingDown = false;
    
    
    private Main(String[] args) {
        m_args = args;
    }
    
    private void run() {
        
        try {
        
            parseArguments();
        
            createAppContext();
        
            registerShutDownHook();
        
        } catch(Exception e) {
            System.err.println("Error occured starting!" + e);
            System.exit(27);
        }

    }

    private void parseArguments() {
        if (m_args.length < 1) {
            usage();
        }
        
        String arg = m_args[0];
        arg = arg.toLowerCase();
        
        if (arg.startsWith("http")) {
            try {
                URL url = new URL(arg);
                m_url = "rmi://"+url.getHost();
                
            } catch (MalformedURLException e) {
                usage();
            }
            
        } else {
            m_url = arg;
        }
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
        if (homeUrl.endsWith(File.separator)) {
            homeUrl = homeUrl.substring(0, homeUrl.length()-1);
        }
        System.err.println("user.home.url = "+homeUrl);
        System.setProperty("user.home.url", homeUrl);
        
        System.err.println("opennms.poller.server.url = "+m_url);
        System.setProperty("opennms.poller.server.url", m_url);
        
        String[] configs = {
                "classpath:/META-INF/opennms/applicationContext-remotePollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerFrontEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-ws-gui.xml"
        };
        
        m_context = new ClassPathXmlApplicationContext(configs);
        m_frontEnd = (PollerFrontEnd) m_context.getBean("pollerFrontEnd");
        
        m_frontEnd.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("exitNecessary".equals(evt.getPropertyName()) && Boolean.TRUE.equals(evt.getNewValue())) {
                    if (!m_shuttingDown) {
                        System.exit(27);
                    }
                }
            }
            
        });
    }
        
    private void usage() {
        System.err.println("\tjava -jar opennms-webstart.jar <serverUrl>");
        System.exit(1);
    }

    public static void main(String[] args) {
        
        new Main(args).run();
        
    }


}
