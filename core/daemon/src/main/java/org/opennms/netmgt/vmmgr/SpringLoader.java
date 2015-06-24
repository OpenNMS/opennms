/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.vmmgr;

import java.io.File;
import java.rmi.ConnectException;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringLoader {
	
	private ApplicationContext m_appContext;
	
	/**
	 * <p>Constructor for SpringLoader.</p>
	 *
	 * @param operation a {@link java.lang.String} object.
	 * @throws java.lang.Throwable if any.
	 */
	public SpringLoader(String operation) throws Throwable {
		String startupUrl = getStartupResource();
		
		String[] paths = { "classpath:/org/opennms/netmgt/vmmgr/remote-access.xml" };
		if ("start".equals(operation)) {
			paths = new String[] { startupUrl, "classpath:/org/opennms/netmgt/vmmgr/local-access.xml", "classpath*:/META-INF/opennms/context.xml" } ;
		}
		
		try {
		
			ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(paths);
			classPathXmlApplicationContext.registerShutdownHook();
			m_appContext = classPathXmlApplicationContext;
			
			
		} catch (BeanCreationException e) {
			e.printStackTrace();
			Throwable rc = e.getRootCause();
			System.err.println("ROOT CAUSE is "+rc);
			if (rc == null)
				throw e;
			else
				throw rc;
		}
		
		Registry.setAppContext(m_appContext);
		
	}

	private String getStartupResource() {
		String startupUrl = System.getProperty("opennms.startup.context");
		if (startupUrl != null) return startupUrl;
		
		String etcDir = getEtcDir();
		if (etcDir != null) {
			File startupFile = new File(etcDir, "startup.xml");
			if (startupFile.exists())
				return startupFile.toURI().toString();
		}
		
		return "classpath:/META-INF/opennms/default-startup.xml";
	}

	private String getEtcDir() {
		String etcDir = System.getProperty("opennms.etc");
		if (etcDir != null) return etcDir;

		String homeDir = System.getProperty("opennms.home");
		if (homeDir != null) return homeDir + File.separator + "etc";
		
		return null;
	}

	/**
	 * <p>start</p>
	 *
	 * @throws java.lang.Throwable if any.
	 */
	public void start() throws Throwable {
		getDaemonMgr().start();
	}

	private DaemonManager getDaemonMgr() throws Throwable {
		try {
			return (DaemonManager)m_appContext.getBean("daemonMgr");
		} catch (BeanCreationException e) {
			
			Throwable rc = e.getRootCause();
			System.err.println("ROOT CAUSE is "+rc);
			throw rc;
		}
	}

	private void stop() throws Throwable {
		getDaemonMgr().stop();
	}
	
	private void pause() throws Throwable {
		getDaemonMgr().pause();
	}
	
	private void resume() throws Throwable {
		getDaemonMgr().resume();
	}
	
	private void status() throws Throwable {
		
		Map<String, String> stati = getDaemonMgr().status();
		for(Entry<String, String> entry : stati.entrySet()) {
			System.err.println(entry.getKey()+": "+entry.getValue());
		}
	}
	
	
	/**
	 * <p>main</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		try {
			String cmd = args[0];
			if ("-u".equals(cmd)) {
				cmd = args[2];
			}
			SpringLoader loader = new SpringLoader(cmd);
			if ("start".equals(cmd))
				loader.start();
			else if ("stop".equals(cmd))
				loader.stop();
			else if ("pause".equals(cmd))
				loader.pause();
			else if ("resume".equals(cmd))
				loader.resume();
			else if ("status".equals(cmd))
				loader.status();
			else
				usage();
		} catch (ConnectException e) {
			System.err.println("opennms is not running.");
			System.exit(3);
		} catch (Throwable e) {
			System.err.println("Exception occurred: "+e);
			e.printStackTrace();
			System.exit(2);
		}
		
	}

	private static void usage() {
		System.err.println("opennms.sh [start|pause|resume|stop|status]");
		System.exit(1);
	}


}
