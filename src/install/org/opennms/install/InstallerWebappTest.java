//
//  $Id$
//

package org.opennms.install;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ListIterator;
import java.util.LinkedList;

import junit.framework.TestCase;

public class InstallerWebappTest extends TestCase {
	private Installer m_installer;

	private LinkedList m_deleteMe = new LinkedList();
	private File m_tmpDir;
	private File m_tomcat_webapps;
	private File m_tomcat_conf_dir;
	private File m_opennmsXml;
	
	private LinkedList m_expecting = new LinkedList();
	
	public void setUp() throws IOException {
		String path = System.getProperty("java.io.tmpdir") + File.separator +
			"opennms_installer_webapp_test_" + System.currentTimeMillis();
		m_tmpDir = tempDir(null, path);

		File dist = tempDir("dist");
		File dist_webapps = tempDir(dist, "webapps");
		File opennms_webapp = tempDir(dist_webapps, "opennms");
		File opennms_xml = tempFile(dist_webapps, "opennms.xml");
		
		File web_inf = tempDir(opennms_webapp, "WEB-INF");
		File lib = tempDir(web_inf, "lib");
	    tempFile(lib, "log4j.jar");
	    tempFile(lib, "castor-0.9.3.9.jar");
	    tempFile(lib, "castor-0.9.3.9-xml.jar");
	    tempFile(lib, "opennms_core.jar");
	    tempFile(lib, "opennms_services.jar");
	    tempFile(lib, "opennms_web.jar");

	    File tomcat = tempDir("tomcat");
	    m_tomcat_webapps = tempDir(tomcat, "webapps");
	    m_tomcat_conf_dir = tempDir(tomcat, "conf");
	    File tomcat_server = tempDir(tomcat, "server");
	    File tomcat_lib = tempDir(tomcat_server, "lib");
	    
	    expecting(m_tomcat_webapps, "opennms.xml");
	    expecting(tomcat_lib, "log4j.jar");
	    expecting(tomcat_lib, "castor-0.9.3.9.jar");
	    expecting(tomcat_lib, "castor-0.9.3.9-xml.jar");
	    expecting(tomcat_lib, "opennms_core.jar");
	    expecting(tomcat_lib, "opennms_services.jar");
	    expecting(tomcat_lib, "opennms_web.jar");
	    
		m_installer = new Installer();
		m_installer.m_out = new PrintStream(new ByteArrayOutputStream());
//		m_installer.m_out = System.out;
		m_installer.m_tomcat_serverlibs = "log4j.jar:castor-0.9.3.9.jar:castor-0.9.3.9-xml.jar:opennms_core.jar:opennms_services.jar:opennms_web.jar";
		m_installer.m_install_webappsdir = dist_webapps.getAbsolutePath();
		m_installer.m_webappdir = m_tomcat_webapps.getAbsolutePath();
		m_installer.m_tomcatserverlibdir = tomcat_lib.getAbsolutePath();
	}
	

	public void tearDown() throws Exception {
		try {
			for (ListIterator i = m_deleteMe.listIterator(m_deleteMe.size()); i.hasPrevious(); ) {
				File f = (File) i.previous();
				if (f.exists()) {
					if (!f.delete()) {
						fail("Could not delete " + f.getAbsolutePath() + ": is it a non-empty directory?");
					}
				}
			}
			if (m_tmpDir != null) {
				assertFalse(m_tmpDir.exists());
			}
		} catch (Exception e) {
			if (m_tmpDir != null && m_tmpDir.exists()) {
				ProcessExec ex = new ProcessExec(System.out, System.err);
				String[] cmd = new String[3];
				cmd[0] = "rm";
				cmd[1] = "-r";
				cmd[2] = m_tmpDir.getAbsolutePath();
				ex.exec(cmd);
			}
			throw e;
		}
	}

	public void testWebappInstall() throws Exception {
		m_installer.installWebApp();
		deleteExpected();
	}
	
	public void testWebappExistingOpennms() throws Exception {
	    tempDir(m_tomcat_webapps, "opennms");
	    
		final String expecting = "Old OpenNMS web application exists";
		
		try {
			m_installer.checkWebappOldOpennmsDir();
		} catch (Exception e) {
			if (!e.getMessage().startsWith(expecting)) {
				fail("Unexpected exception received while waiting for \"" + expecting + "\": " + e);
			}
			return;
		}
		fail("Did not receive expected exception: \"" + expecting + "\"");
	}
	
	public void testWebappNoOpennms() throws Exception {
		m_installer.checkWebappOldOpennmsDir();
	}
	
	public void testServerXmlContext() throws Exception {
		final String expecting = "Old OpenNMS context found";
		final String context = 
"		<Context \n" +
"            path=\"/opennms\" docBase=\"opennms\" debug=\"0\" reloadable=\"true\">\n" +
"            <Logger className=\"org.opennms.web.log.Log4JLogger\"\n" + 
"                    homeDir=\"${OPENNMS_HOME}\"/>\n" +
"            <Realm className=\"org.opennms.web.authenticate.OpenNMSTomcatRealm\"\n" + 
"                    homeDir=\"${OPENNMS_HOME}\"/>\n" +
"        </Context>\n";

		File f = tempFile(m_tomcat_conf_dir, "server.xml");
		
		PrintWriter w = new PrintWriter(new FileOutputStream(f));

		w.print(context);
		w.close();

		
		try {
			m_installer.checkServerXmlOldOpennmsContext();
		} catch (Exception e) {
			if (!e.getMessage().startsWith(expecting)) {
				fail("Unexpected exception received while waiting for \"" + expecting + "\": " + e);
			}
			return;
		}
		fail("Did not receive expected exception: \"" + expecting + "\"");
	}
	
	public void testServerXmlNoContext() throws Exception {
		m_installer.checkServerXmlOldOpennmsContext();
	}
	
	public void testServerXmlNoFile() throws Exception {
		m_installer.checkServerXmlOldOpennmsContext();
	}
	
	public File tempFile(String name) throws IOException {
		return tempFile(m_tmpDir, name);
	}
	
	public File tempFile(File parent, String name) throws IOException {
		String path;
		if (parent != null) {
			path = parent.getAbsolutePath() + File.separator + name;
		} else {
			path = name;
		}
		
		File f = new File(path);
		assertTrue(f.createNewFile());
		m_deleteMe.add(f);
		return f;
	}
	
	public File tempDir(String name) throws IOException {
		return tempDir(m_tmpDir, name);
	}
	
	public File tempDir(File parent, String name) throws IOException {
		String path;
		if (parent != null) {
			path = parent.getAbsolutePath() + File.separator + name;
		} else {
			path = name;
		}
		
		File f = new File(path);
		assertTrue(f.mkdir());
		m_deleteMe.add(f);
		return f;
	}
	
	public File expecting(File parent, String name) throws IOException {
		String path;
		if (parent != null) {
			path = parent.getAbsolutePath() + File.separator + name;
		} else {
			path = name;
		}
		
		File f = new File(path);
		m_expecting.add(f);
		return f;
	}
	
	public void deleteExpected() {
		for (ListIterator i = m_expecting.listIterator(m_expecting.size()); i.hasPrevious(); ) {
			File f = (File) i.previous();
			assertTrue(f.exists());
			assertTrue(f.delete());
			i.remove();
		}
		assertEquals(m_expecting.size(), 0);
	}

}

