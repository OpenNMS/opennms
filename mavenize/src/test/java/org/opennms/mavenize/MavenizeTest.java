package org.opennms.mavenize;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.codehaus.plexus.util.FileUtils;

import junit.framework.TestCase;

public class MavenizeTest extends TestCase {
	
	String m_baseDir = "target/test/work";
	
	
	private final class CountingInvocationHandler implements InvocationHandler {
		HashMap counts = new HashMap();
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			int currentCount = 0;
			if (counts.get(method.getName()) != null)
				currentCount = ((Integer)counts.get(method.getName())).intValue();
			counts.put(method.getName(), new Integer(currentCount+1));
			
			return null;
		}
		
		public int getCount(String methodName) {
			if (counts.get(methodName) == null) return 0;
			return ((Integer)counts.get(methodName)).intValue();
		}
	}

	public void testSpecFile() throws Exception {
		Mavenize mavenize = createMavenizer("/opennmsMavenizeSpec.xml");
		
		CountingInvocationHandler counter = new CountingInvocationHandler();
		SpecVisitor visitor = getCountingVisitor(counter);
		
		mavenize.visitSpec(visitor);
		
		assertEquals(1, counter.getCount("visitProject"));
		assertEquals(1, counter.getCount("completeProject"));
		assertEquals(23, counter.getCount("visitModule"));
		assertEquals(23, counter.getCount("completeModule"));
		assertEquals(30, counter.getCount("visitSources"));
		assertEquals(30, counter.getCount("completeSources"));
		assertEquals(34, counter.getCount("visitFileSet"));
		assertEquals(34, counter.getCount("completeFileSet"));
		assertEquals(43, counter.getCount("visitInclude"));
		assertEquals(43, counter.getCount("completeInclude"));
		assertEquals(26, counter.getCount("visitExclude"));
		assertEquals(26, counter.getCount("completeExclude"));
		assertEquals(21, counter.getCount("visitDependencies"));
		assertEquals(21, counter.getCount("completeDependencies"));
		assertEquals(46, counter.getCount("visitDependency"));
		assertEquals(46, counter.getCount("completeDependency"));
		assertEquals(31, counter.getCount("visitModuleDependency"));
		assertEquals(31, counter.getCount("completeModuleDependency"));
		
	}

	private SpecVisitor getCountingVisitor(InvocationHandler handler) {
		SpecVisitor visitor = (SpecVisitor) Proxy.newProxyInstance(SpecVisitor.class.getClassLoader(),
                new Class[] { SpecVisitor.class },
                handler);
		return visitor;
	}

	private Mavenize createMavenizer(String resourceName) throws Exception, IOException {
		InputStreamReader rdr = new InputStreamReader(getClass().getResourceAsStream(resourceName));
		Mavenize mavenize = new Mavenize(rdr);
		rdr.close();
		return mavenize;
	}
	
	public void testMavenizeTest() throws Exception {
		FileUtils.deleteDirectory(m_baseDir+"/mavenize-test");
		System.setProperty("opennms.dir", "src/test/test-data");
		
		Mavenize mavenize = createMavenizer("/testSpec.xml");
		
		PomBuilder builder = PomBuilder.createProjectBuilder();
		mavenize.visitSpec(new ProjectBuildingVisitor(builder));
		builder.save(new File(m_baseDir));
		
		assertDirectoryExists(m_baseDir);
		assertDirectoryExists(m_baseDir, "mavenize-test");
		assertFileExists     (m_baseDir, "mavenize-test/pom.xml");
		assertDirectoryExists(m_baseDir, "mavenize-test/test-submodule");
		assertFileExists     (m_baseDir, "mavenize-test/test-submodule/pom.xml");
		// java code in main
		assertDirectoryExists(m_baseDir, "mavenize-test/test-submodule/src/main/java");
		assertFileExists     (m_baseDir, "mavenize-test/test-submodule/src/main/java/org/opennms/netmgt/poller/pollables/PollableServiceConfig.java");
		assertFileNotExists  (m_baseDir, "mavenize-test/test-submodule/src/main/java/org/opennms/netmgt/poller/pollables/PollablesTest.java");
		assertFileExists     (m_baseDir, "mavenize-test/test-submodule/src/main/java/org/opennms/netmgt/poller/pollables/PollableVisitor.java");
		assertFileExists     (m_baseDir, "mavenize-test/test-submodule/src/main/java/org/opennms/netmgt/poller/pollables/PollStatus.java");
		assertFileNotExists  (m_baseDir, "mavenize-test/test-submodule/src/main/java/org/opennms/netmgt/poller/pollables/PollStatusTest.java");
		// test code
		assertDirectoryExists(m_baseDir, "mavenize-test/test-submodule/src/test/java");
		assertFileNotExists  (m_baseDir, "mavenize-test/test-submodule/src/test/java/org/opennms/netmgt/poller/pollables/PollableServiceConfig.java");
		assertFileExists     (m_baseDir, "mavenize-test/test-submodule/src/test/java/org/opennms/netmgt/poller/pollables/PollablesTest.java");
		assertFileNotExists  (m_baseDir, "mavenize-test/test-submodule/src/test/java/org/opennms/netmgt/poller/pollables/PollableVisitor.java");
		assertFileNotExists  (m_baseDir, "mavenize-test/test-submodule/src/test/java/org/opennms/netmgt/poller/pollables/PollStatus.java");
		assertFileExists     (m_baseDir, "mavenize-test/test-submodule/src/test/java/org/opennms/netmgt/poller/pollables/PollStatusTest.java");

	}

	public void testMavenizeOpenNMS() throws Exception {
		FileUtils.deleteDirectory(m_baseDir+"/opennms");
		System.setProperty("opennms.dir", "${user.home}/workspaces/HEAD/opennms");
		
		Mavenize mavenize = createMavenizer("/opennmsMavenizeSpec.xml");
		
		PomBuilder builder = PomBuilder.createProjectBuilder();
		mavenize.visitSpec(new ProjectBuildingVisitor(builder));
		builder.save(new File(m_baseDir));
		
		assertDirectoryExists(m_baseDir);
		assertDirectoryExists(m_baseDir, "opennms");
		assertFileExists     (m_baseDir, "opennms/pom.xml");
		assertDirectoryExists(m_baseDir, "opennms/opennms-rrd/opennms-rrd-rrdtool/opennms-rrdtool-jni");
		assertDirectoryExists(m_baseDir, "opennms/opennms-core/src/main/java");
		
		
		
	}

	private void assertDirectoryExists(String dirName) {
		assertDirectoryExists(new File(dirName));
	}
	
	private void assertDirectoryExists(String baseDir, String path) {
		assertDirectoryExists(new File(baseDir, path));
	}

	private void assertDirectoryExists(File dir) {
		assertTrue("Directory "+dir.getPath()+" does not exist.", dir.exists());
		assertTrue(dir.getPath()+" is not a directory", dir.isDirectory());
	}
	
	private void assertFileExists(String baseDir, String path) {
		assertFileExists(new File(baseDir, path));
	}

	private void assertFileExists(File file) {
		assertTrue("File "+file.getPath()+" does not exist.", file.exists());
	}
	
	private void assertFileNotExists(String baseDir, String path) {
		assertFileNotExists(new File(baseDir, path));
	}

	private void assertFileNotExists(File file) {
		assertFalse("File "+file.getPath()+" shoult NOT exist.", file.exists());
	}

}
