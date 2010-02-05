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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public class Main {
    
    String[] m_args;
    ClassPathXmlApplicationContext m_context;
    PollerFrontEnd m_frontEnd;
    URI m_uri;
    String m_locationName;
    String m_username = null;
    String m_password = null;
    boolean m_shuttingDown = false;
    boolean m_gui = false;
    CommandLine m_cl;

    private Main(String[] args) throws Exception {
        m_args = args;
        initializeLogging();
    }
    
    private void initializeLogging() throws Exception {
        String logFile;
        if (System.getProperty("os.name").contains("Windows")) {
            logFile = System.getProperty("java.io.tmpdir") + File.separator + "opennms-remote-poller.log";
        } else {
            logFile = System.getProperty("user.home") + File.separator + ".opennms" + File.separator + "remote-poller.log";
        }
        File logDirectory = new File(logFile).getParentFile();
        if (!logDirectory.exists()) {
            if (!logDirectory.mkdirs()) {
                throw new IllegalStateException("Could not create parent directory for log file '" + logFile + "'");
            }
        }
        Handler fh = new FileHandler(logFile);
        Logger.getLogger("").addHandler(fh);
        Logger.getLogger("").setLevel(Level.WARNING);
		org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
    }

    private void run() {
        
        try {
            parseArguments();

            createAppContext();
            registerShutDownHook();

            if (!m_gui) {
            	if (m_username != null) {
            		m_frontEnd.authenticate(m_username, m_password);
            	}
                if (!m_frontEnd.isRegistered()) {
                    if (m_locationName == null) {
                        log().severe("No location name provided.  You must pass a location name the first time you start the remote poller!");
                        System.exit(27);
                    } else {
                        m_frontEnd.register(m_locationName);
                    }
                }
            }
        } catch(Exception e) {
            // a fatal exception occurred
            log().log(Level.SEVERE, "Exception occurred during registration!", e);
            System.exit(27);
        }
        
    }

    private Logger log() {
        return Logger.getLogger("");
    }

    private void parseArguments() throws ParseException {
        Options options = new Options();
        
        options.addOption("h", "help", false, "this help");
        
        options.addOption("d", "debug", false, "write debug messages to the log");
        options.addOption("g", "gui", false, "start a GUI (default: false)");
        options.addOption("l", "location", true, "the location name of this remote poller");
        options.addOption("u", "url", true, "the URL for OpenNMS (default: rmi://server-name/)");
        options.addOption("n", "name", true, "the name of the user to connect as");
        options.addOption("p", "password", true, "the password to use when connecting");

        CommandLineParser parser = new PosixParser();
        m_cl = parser.parse(options, m_args);

        if (m_cl.hasOption("h")) {
            usage(options);
            System.exit(1);
        }

        if (m_cl.hasOption("d")) {
            Logger.getLogger("").setLevel(Level.ALL);
        }
        
        if (m_cl.hasOption("l")) {
            m_locationName = m_cl.getOptionValue("l");
        }
        if (m_cl.hasOption("u")) {
            String arg = m_cl.getOptionValue("u").toLowerCase();
        	try {
				m_uri = new URI(arg);
			} catch (URISyntaxException e) {
                usage(options);
                e.printStackTrace();
                System.exit(2);
			}
        } else {
            usage(options);
            System.exit(3);
        }
        
        if (m_cl.hasOption("g")) {
            m_gui = true;
        }
        
        if (m_cl.hasOption("n")) {
        	m_username = m_cl.getOptionValue("n");
        	m_password = m_cl.getOptionValue("p");
        	if (m_password == null) {
        		m_password = "";
        	}
        }
    }

    private void usage(Options o) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("usage: ", o);
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

        log().fine("user.home.url = "+homeUrl);
        System.setProperty("user.home.url", homeUrl);

        String serverURI = m_uri.toString().replaceAll("/*$", "");
        log().fine("opennms.poller.server.url = " + serverURI);
        System.setProperty("opennms.poller.server.url", serverURI);

        log().fine("location name = " + m_locationName);

        List<String> configs = new ArrayList<String>();
        configs.add("classpath:/META-INF/opennms/applicationContext-remotePollerBackEnd-" + m_uri.getScheme() + ".xml");
        configs.add("classpath:/META-INF/opennms/applicationContext-pollerFrontEnd.xml");

        if (m_gui) {
            configs.add("classpath:/META-INF/opennms/applicationContext-ws-gui.xml");
        }

        m_context = new ClassPathXmlApplicationContext(configs.toArray(new String[0]));
        m_frontEnd = (PollerFrontEnd) m_context.getBean("pollerFrontEnd");
        if (m_username != null) {
        	SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(m_username, m_password));
        }
        
        m_frontEnd.addPropertyChangeListener(new PropertyChangeListener() {
            
            private boolean shouldExit(PropertyChangeEvent e) {
            	log().fine("shouldExit: received property change event: "+e.getPropertyName()+";oldvalue:"+e.getOldValue()+";newvalue:"+e.getNewValue());
                String propName = e.getPropertyName();
                Object newValue = e.getNewValue();
                
                // if exitNecessary becomes true.. then return true
                if ("exitNecessary".equals(propName) && Boolean.TRUE.equals(newValue)) {
                	log().info("shouldExit: Exiting because exitNecessary is TRUE");
                    return true;
                }
                
                // if started becomes false the we should exit
                if ("started".equals(propName) && Boolean.FALSE.equals(newValue)) {
                	log().info("shouldExit: Exiting because started is now false");
                    return true;
                }
                
            	log().fine("shouldExit: not exiting");
                return false;
                
            }

            public void propertyChange(PropertyChangeEvent e) {
                if (!m_shuttingDown && shouldExit(e)) {
                    System.exit(10);
                }
            }
            
        });
    }
		
    public static void main(String[] args) throws Exception {
        String killSwitchFileName = System.getProperty("opennms.poller.killSwitch.resource");
        File killSwitch = null;
        
        if (! "".equals(killSwitchFileName) && killSwitchFileName != null) {
            killSwitch = new File(System.getProperty("opennms.poller.killSwitch.resource"));
            if (!killSwitch.exists()) {
                try {
                    killSwitch.createNewFile();
                } catch (IOException ioe) {
                    // We'll just do without one
                }
            }
        }
        new Main(args).run();
        
    }



}
