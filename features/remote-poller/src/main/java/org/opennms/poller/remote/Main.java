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
 *******************************************************************************/

package org.opennms.poller.remote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * <p>Main class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class Main {
	
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    
    String[] m_args;
    ClassPathXmlApplicationContext m_context;
    PollerFrontEnd m_frontEnd;
    URI m_uri;
    String m_locationName;
    String m_username = null;
    String m_password = null;
    String m_pollerHome = null;
    boolean m_shuttingDown = false;
    boolean m_gui = false;
    CommandLine m_cl;

    private Main(String[] args) throws Exception {
        m_args = args;
        m_pollerHome = System.getProperty("poller.home");
        if (m_pollerHome == null) {
        	if (System.getProperty("os.name").contains("Windows")) {
        		m_pollerHome = System.getProperty("java.io.tmpdir");
        	} else {
        		m_pollerHome = System.getProperty("user.home") + File.separator + ".opennms";
        	}
        }
        initializeLogging();
        
        final String pingerClass = System.getProperty("org.opennms.netmgt.icmp.pingerClass");
        if (pingerClass == null) {
        	LOG.info("org.opennms.netmgt.icmp.pingerClass not set; using JnaPinger by default");
        	System.setProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.jna.JnaPinger");
        }
    }

    private void initializeLogging() throws Exception {
    }

    private void getAuthenticationInfo() {
    	if (m_uri == null || m_uri.getScheme() == null) {
    		throw new RuntimeException("no URI specified!");
    	}
    	if (m_uri.getScheme().equals("rmi")) {
    		// RMI doesn't have authentication
    		return;
    	}
    	
    	if (m_username == null) {
    		GroovyGui gui = createGui();
            gui.createAndShowGui();
            AuthenticationBean auth = gui.getAuthenticationBean();
            m_username = auth.getUsername();
            m_password = auth.getPassword();
    	}
    	
    	if (m_username != null) {
    		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
    		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(m_username, m_password));
    	}
    }

	private GroovyGui createGui() {
		try {
			return (GroovyGui)Class.forName("org.opennms.groovy.poller.remote.ConfigurationGui").newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Unable to find Configuration GUI!", e);
		}
	}
    
    private void run() {
        
        try {
            parseArguments();
            getAuthenticationInfo();
            createAppContext();
            registerShutDownHook();

            if (!m_gui) {
                if (!m_frontEnd.isRegistered()) {
                    if (m_locationName == null) {
                        LOG.error("No location name provided.  You must pass a location name the first time you start the remote poller!");
                        System.exit(27);
                    } else {
                        m_frontEnd.register(m_locationName);
                    }
                }
            }
        } catch(Throwable e) {
            // a fatal exception occurred
            LOG.error("Exception occurred during registration!", e);
            System.exit(27);
        }
        
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
        formatter.printHelp(HelpFormatter.DEFAULT_SYNTAX_PREFIX, o);
    }
    
    private void registerShutDownHook() {
        Thread shutdownHook = new Thread() {
            @Override
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

        LOG.info("user.home.url = {}", homeUrl);
        System.setProperty("user.home.url", homeUrl);

        String serverURI = m_uri.toString().replaceAll("/*$", "");
        LOG.info("opennms.poller.server.url = {}", serverURI);
        System.setProperty("opennms.poller.server.url", serverURI);

        LOG.info("location name = {}", m_locationName);

        List<String> configs = new ArrayList<String>();
        configs.add("classpath:/META-INF/opennms/applicationContext-remotePollerBackEnd-" + m_uri.getScheme() + ".xml");
        configs.add("classpath:/META-INF/opennms/applicationContext-pollerFrontEnd.xml");

        if (m_gui) {
            configs.add("classpath:/META-INF/opennms/applicationContext-ws-gui.xml");
        }

        m_context = new ClassPathXmlApplicationContext(configs.toArray(new String[0]));
        m_frontEnd = (PollerFrontEnd) m_context.getBean("pollerFrontEnd");

        m_frontEnd.addPropertyChangeListener(new PropertyChangeListener() {
            
            private boolean shouldExit(PropertyChangeEvent e) {
				LOG.info("shouldExit: received property change event: {};oldvalue:{};newvalue:{}", e.getPropertyName(), e.getOldValue(), e.getNewValue());
                String propName = e.getPropertyName();
                Object newValue = e.getNewValue();

                // if exitNecessary becomes true.. then return true
                if ("exitNecessary".equals(propName) && Boolean.TRUE.equals(newValue)) {
                	LOG.info("shouldExit: Exiting because exitNecessary is TRUE");
                    return true;
                }
                
                // if started becomes false the we should exit
                if ("started".equals(propName) && Boolean.FALSE.equals(newValue)) {
                	LOG.info("shouldExit: Exiting because started is now false");
                    return true;
                }
                
            	LOG.info("shouldExit: not exiting");
                return false;
                
            }

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (!m_shuttingDown && shouldExit(e)) {
                    System.exit(10);
                }
            }
            
        });
    }
		
    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
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
