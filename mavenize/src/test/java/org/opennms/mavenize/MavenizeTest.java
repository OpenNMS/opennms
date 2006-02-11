package org.opennms.mavenize;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.codehaus.plexus.util.FileUtils;

public class MavenizeTest extends AbstractFileTest {
	
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
		Mavenize mavenize = createMavenizer("/testSpec.xml");
		
		CountingInvocationHandler counter = new CountingInvocationHandler();
		SpecVisitor visitor = getCountingVisitor(counter);
		
		mavenize.visitSpec(visitor);
		
		assertEquals(1, counter.getCount("visitProject"));
		assertEquals(1, counter.getCount("completeProject"));
		assertEquals(1, counter.getCount("visitModule"));
		assertEquals(1, counter.getCount("completeModule"));
		assertEquals(2, counter.getCount("visitSources"));
		assertEquals(2, counter.getCount("completeSources"));
		assertEquals(2, counter.getCount("visitFileSet"));
		assertEquals(2, counter.getCount("completeFileSet"));
		assertEquals(2, counter.getCount("visitInclude"));
		assertEquals(2, counter.getCount("completeInclude"));
		assertEquals(1, counter.getCount("visitExclude"));
		assertEquals(1, counter.getCount("completeExclude"));
		assertEquals(1, counter.getCount("visitDependencies"));
		assertEquals(1, counter.getCount("completeDependencies"));
		assertEquals(1, counter.getCount("visitDependency"));
		assertEquals(1, counter.getCount("completeDependency"));
		assertEquals(0, counter.getCount("visitModuleDependency"));
		assertEquals(0, counter.getCount("completeModuleDependency"));
		
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
		//System.setProperty("opennms.dir", "..");
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

}
