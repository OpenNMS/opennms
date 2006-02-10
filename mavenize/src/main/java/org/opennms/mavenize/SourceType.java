package org.opennms.mavenize;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

public class SourceType {
	
	class SpecPlugin {
		private String m_plugin;
		SpecPlugin(String name) {
			m_plugin = name;
		}
		
		String getPrefix() {
			return "plugin."+m_plugin;
		}
		
		String getGroupId() {
			return Configuration.get().getString(getPrefix()+".groupId");
		}
		String getArtifactId() {
			return Configuration.get().getString(getPrefix()+".artifactId");
		}
		List getGoals() {
			return Configuration.get().getList(getPrefix()+".goals");
		}
	}
	
	private String m_typeName;
	
	public static SourceType get(String typeName) {
		return new SourceType(typeName);
	}

	private SourceType(String typeName) {
		m_typeName = typeName;
	}
	
	private String getTypePrefix() {
		return "sourceType."+m_typeName;
	}
	
	public String getStandardDir() {
		return Configuration.get().getString(getTypePrefix()+".standardDir");
	}

	 public void addPlugins(PomBuilder builder) {
		 List plugins = Configuration.get().getList(getTypePrefix()+".plugins");

		 for (Iterator it = plugins.iterator(); it.hasNext();) {
			String pluginName = (String) it.next();
			addPlugin(new SpecPlugin(pluginName), builder);
		}
	}

	private void addPlugin(SpecPlugin specPlugin, PomBuilder builder) {
		
		Plugin plugin = builder.addPlugin(specPlugin.getGroupId(), specPlugin.getArtifactId());
		
		PluginExecution execution = new PluginExecution();
		for (Iterator it = specPlugin.getGoals().iterator(); it.hasNext();) {
			String goal = (String) it.next();
			execution.addGoal(goal);
		}
		plugin.addExecution(execution);
		
	}

	public boolean isType(String sourceType) {
		return m_typeName.equals(sourceType);
	}


}
