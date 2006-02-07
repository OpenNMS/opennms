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
			return getTypeConfiguration().getString(getPrefix()+".groupId");
		}
		String getArtifactId() {
			return getTypeConfiguration().getString(getPrefix()+".artifactId");
		}
		List getGoals() {
			return getTypeConfiguration().getList(getPrefix()+".goals");
		}
	}
	
	private String m_typeKey;
	
	public static SourceType get(String typeName) {
		return new SourceType(typeName);
	}

	private SourceType(String typeKey) {
		m_typeKey = typeKey;
	}
	
	public String getTypeKey() {
		return m_typeKey;
	}
	
	public String getStandardDir() {
		return getTypeConfiguration().getString(getTypeKey()+".standardDir");
	}

	 public void addPlugins(PomBuilder builder) {
		 List plugins = getTypeConfiguration().getList(getTypeKey()+".plugins");

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

	private static Configuration s_configuration;
	private static Configuration getTypeConfiguration() {
	    if (s_configuration == null) {
	        Configuration config = new Configuration("/sourceTypes.properties");
	        s_configuration = config;
	    }
	    return s_configuration;
	}


}
