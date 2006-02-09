package org.opennms.mavenize;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

public class ShlibModuleType extends ModuleType {
	
	public ShlibModuleType(String moduleType) {
		super(moduleType);
	}

	public void configureModule(PomBuilder builder) {
		builder.setPackaging("pom");
		
		String suffix = "platforms";
		List platforms = getList(suffix);
		for (Iterator it = platforms.iterator(); it.hasNext();) {
			String platform = (String) it.next();
			String moduleType = getString(platform+".subModuleType");
			if (moduleType == null) throw new NullPointerException("subModuleType is null for platfrom "+platform);
			PomBuilder subModuleBuilder = builder.createModule(builder.getArtifactId()+"-"+platform, moduleType);
			addSubModulePlugins(subModuleBuilder);
			
		}
		
	}

	private void addSubModulePlugins(PomBuilder builder) {
		String groupId = Configuration.get().getString("plugin.native.groupId");
		String artifactId = Configuration.get().getString("plugin.native.artifactId");
		Plugin plugin = builder.addPlugin(groupId, artifactId);
		plugin.setExtensions(true);
		
		Xpp3Dom compilerProvider = new Xpp3Dom("compilerProvider");
		compilerProvider.setValue("generic");
		
		Xpp3Dom compilerStartOption = new Xpp3Dom("compilerStartOption");
		compilerStartOption.setValue("-fPIC -O");
		
		Xpp3Dom compilerStartOptions = new Xpp3Dom("compilerStartOptions");
		compilerStartOptions.addChild(compilerStartOption);
		
		Xpp3Dom config = new Xpp3Dom("configuration");
		config.addChild(compilerProvider);
		config.addChild(compilerStartOptions);
		
		plugin.setConfiguration(config);
	}

}
