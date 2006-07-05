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
import java.util.Iterator;
import java.util.Map;

import org.opennms.netmgt.Registry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringLoader {
	
	private ApplicationContext m_appContext;
	
	public SpringLoader() {
		String startupUrl = getStartupResource();
		String[] paths = new String[] {startupUrl, "classpath:/META-INF/opennms/manager.xml", "classpath*:/META-INF/opennms/context.xml"};
		m_appContext = new ClassPathXmlApplicationContext(paths);
		
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

	public void start() {
		Registry.getBean("start");
	}

	private void stop() {
		Registry.getBean("stop");
	}
	
	private void pause() {
		Registry.getBean("pause");
	}
	
	private void resume() {
		Registry.getBean("resume");
	}
	
	private void status() {
		Map stati = (Map)Registry.getBean("status");
		for (Iterator it = stati.keySet().iterator(); it.hasNext();) {
			String name = (String) it.next();
			System.err.println(name+": "+stati.get(name));
		}
	}
	
	
	public static void main(String[] args) {
		SpringLoader loader = new SpringLoader();
		if ("start".equals(args[0]))
			loader.start();
		else if ("stop".equals(args[0]))
			loader.stop();
		else if ("pause".equals(args[0]))
			loader.pause();
		else if ("resume".equals(args[0]))
			loader.resume();
		else if ("status".equals(args[0]))
			loader.status();
		
		
	}


}
