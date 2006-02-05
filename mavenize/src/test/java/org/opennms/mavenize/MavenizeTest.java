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
		assertEquals(27, counter.getCount("visitSources"));
		assertEquals(27, counter.getCount("completeSources"));
		assertEquals(31, counter.getCount("visitFileSet"));
		assertEquals(31, counter.getCount("completeFileSet"));
		assertEquals(37, counter.getCount("visitInclude"));
		assertEquals(37, counter.getCount("completeInclude"));
		assertEquals(21, counter.getCount("visitExclude"));
		assertEquals(21, counter.getCount("completeExclude"));
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

	public void testMavenize() throws Exception {
		FileUtils.deleteDirectory("target/test/work");
		
		Mavenize mavenize = createMavenizer("/opennmsMavenizeSpec.xml");
		
		PomBuilder builder = new PomBuilder();
		mavenize.visitSpec(new ProjectBuildingVisitor(builder));
		builder.save(new File("target/test/work"));
		
		assertDirectoryExists("target/test/work");
		assertDirectoryExists("target/test/work/opennms");
		assertFileExists("target/test/work/opennms/pom.xml");
		assertDirectoryExists("target/test/work/opennms/opennms-rrd/opennms-rrd-rrdtool/opennms-rrdtool-jni");
		
		
	}

	private void assertDirectoryExists(String dirName) {
		File dir = new File(dirName);
		assertTrue("Directory "+dirName+" does not exist.", dir.exists());
		assertTrue(dirName+" is not a directory", dir.isDirectory());
	}
	
	private void assertFileExists(String fileName) {
		assertTrue("File "+fileName+" does not exist.", new File(fileName).exists());
	}
}
