package org.opennms.mavenize;

import java.io.FileWriter;
import java.io.Writer;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import junit.framework.TestCase;

public class ModelTest extends TestCase {
	
	public void testModelWrite() throws Exception {
		Model model = new Model();
		model.setGroupId("org.opennms");
		model.setArtifactId("opennms-test");
		model.setVersion("1.0");
		Dependency dependency = new Dependency();
		dependency.setGroupId("junit");
		dependency.setArtifactId("junit");
		dependency.setVersion("3.8.1");
		model.addDependency(dependency);
		
		Writer writer = new FileWriter("test-pom.xml");
		MavenXpp3Writer modelWriter = new MavenXpp3Writer();
		modelWriter.write(writer, model);
		
	}

}
