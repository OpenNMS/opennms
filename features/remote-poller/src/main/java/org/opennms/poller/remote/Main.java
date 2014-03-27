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
import java.util.Arrays;
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
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * <p>Main class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class Main implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	protected final String[] m_args;
	protected URI m_uri = null;
	protected String m_locationName;
	protected String m_username = null;
	protected String m_password = null;
	protected static boolean m_shuttingDown = false;
	protected boolean m_gui = false;

	private Main(String[] args) {
		// Give us some time to attach a debugger if necessary
		//try { Thread.sleep(20000); } catch (InterruptedException e) {}        

		m_args = Arrays.copyOf(args, args.length);

		final String pingerClass = System.getProperty("org.opennms.netmgt.icmp.pingerClass");
		if (pingerClass == null) {
			LOG.info("System property org.opennms.netmgt.icmp.pingerClass is not set; using JnaPinger by default");
			System.setProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.jna.JnaPinger");
		}
		LOG.info("Pinger class: {}", System.getProperty("org.opennms.netmgt.icmp.pingerClass"));
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

	private static GroovyGui createGui() {
		try {
			return (GroovyGui)Class.forName("org.opennms.groovy.poller.remote.ConfigurationGui").newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Unable to find Configuration GUI!", e);
		}
	}

	@Override
	public void run() {

		try {
			parseArguments(m_args);
			getAuthenticationInfo();
			AbstractApplicationContext context = createAppContext();
			PollerFrontEnd frontEnd = getPollerFrontEnd(context);

			if (!m_gui) {
				if (!frontEnd.isRegistered()) {
					if (m_locationName == null) {
						LOG.error("No location name provided.  You must pass a location name the first time you start the remote poller!");
						System.exit(27);
					} else {
						frontEnd.register(m_locationName);
					}
				}
			}
		} catch(Throwable e) {
			// a fatal exception occurred
			LOG.error("Exception occurred during registration!", e);
			System.exit(27);
		}

	}

	private void parseArguments(String[] args) throws ParseException {
		Options options = new Options();

		options.addOption("h", "help", false, "this help");

		options.addOption("d", "debug", false, "write debug messages to the log");
		options.addOption("g", "gui", false, "start a GUI (default: false)");
		options.addOption("l", "location", true, "the location name of this remote poller");
		options.addOption("u", "url", true, "the URL for OpenNMS (example: https://server-name/opennms-remoting)");
		options.addOption("n", "name", true, "the name of the user to connect as");
		options.addOption("p", "password", true, "the password to use when connecting");

		CommandLineParser parser = new PosixParser();
		CommandLine cl = parser.parse(options, args);

		if (cl.hasOption("h")) {
			usage(options);
			System.exit(1);
		}

		if (cl.hasOption("d")) {
		}

		if (cl.hasOption("l")) {
			m_locationName = cl.getOptionValue("l");
		}
		if (cl.hasOption("u")) {
			String arg = cl.getOptionValue("u").toLowerCase();
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

		if (cl.hasOption("g")) {
			m_gui = true;
		}

		if (cl.hasOption("n")) {
			m_username = cl.getOptionValue("n");
			m_password = cl.getOptionValue("p");
			if (m_password == null) {
				m_password = "";
			}
		}
	}

	private static void usage(Options o) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(Main.class.getName() + " -u [URL] [options]", o);
	}

	private AbstractApplicationContext createAppContext() {

		/*
		 * Set a system property called user.home.url so that the
		 * Spring contexts can reference resources that are stored
		 * in the user's home directory.
		 */
		File homeDir = new File(System.getProperty("user.home"));
		String homeUrl = homeDir.toURI().toString();
		// Trim the trailing file separator off of the end of the URI
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

		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configs.toArray(new String[0]));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				m_shuttingDown = true;
				context.close();
			}
		});
		return context;
	}

	private static PollerFrontEnd getPollerFrontEnd(AbstractApplicationContext context) {
		PollerFrontEnd frontEnd = (PollerFrontEnd) context.getBean("pollerFrontEnd");

		frontEnd.addPropertyChangeListener(new PropertyChangeListener() {

			private boolean shouldExit(PropertyChangeEvent e) {
				LOG.info("shouldExit: received property change event for property: {}; oldvalue: {}; newvalue: {}", e.getPropertyName(), e.getOldValue(), e.getNewValue());
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

		return frontEnd;
	}

	/**
	 * <p>main</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) {
		try {
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
