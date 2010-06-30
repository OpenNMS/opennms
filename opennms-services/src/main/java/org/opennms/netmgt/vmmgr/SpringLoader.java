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

import java.io.File;
import java.rmi.ConnectException;
import java.util.Iterator;
import java.util.Map;

import org.opennms.netmgt.Registry;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>SpringLoader class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
		
		Map stati = getDaemonMgr().status();
		for (Iterator it = stati.keySet().iterator(); it.hasNext();) {
			String name = (String) it.next();
			System.err.println(name+": "+stati.get(name));
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
