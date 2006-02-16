package org.opennms.mavenize;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

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
		String getPhase() {
			return Configuration.get().getString(getPrefix()+".phase");
		}
        
        boolean hasConfiguration() {
            return getConfiguration() != null;
        }
        
        String getConfiguration() {
            return Configuration.get().getString(getPrefix()+".configuration");
        }
        
        String getConfigurationElement(String suffix) {
            return Configuration.get().getString(getPrefix()+".configuration"+suffix);
        }

        public String getConfigIncludes() {
            return Configuration.get().getString(getPrefix()+".configIncludes");
        }

        public String getConfigExcludes() {
            return Configuration.get().getString(getPrefix()+".configExcludes");
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

	 private void addPlugins(File baseDir, String target, PomBuilder builder) throws Exception {
		 List plugins = Configuration.get().getList(getTypePrefix()+".plugins");

		 for (Iterator it = plugins.iterator(); it.hasNext();) {
			String pluginName = (String) it.next();
			addPlugin(new SpecPlugin(pluginName), builder, baseDir, target);
		}
	}

	private void addPlugin(SpecPlugin specPlugin, PomBuilder builder, File baseDir, String target) throws Exception {
		
		Plugin plugin = builder.addPlugin(specPlugin.getGroupId(), specPlugin.getArtifactId());
        
		
		PluginExecution execution = new PluginExecution();
		execution.setPhase(specPlugin.getPhase());
		for (Iterator it = specPlugin.getGoals().iterator(); it.hasNext();) {
			String goal = (String) it.next();
			execution.addGoal(goal);
		}
		plugin.addExecution(execution);
        
        if (specPlugin.hasConfiguration()) {
            Xpp3Dom configuration = new Xpp3Dom("configuration");
            Xpp3Dom parent = configuration;
            String suffix = "";
            String element = specPlugin.getConfiguration();
            System.err.println("element = "+element);
            while(element.indexOf("${file}") < 0) {
                Xpp3Dom child = new Xpp3Dom(element);
                parent.addChild(child);
                parent = child;
                suffix = suffix+'.'+element;
                element = specPlugin.getConfigurationElement(suffix);
                System.err.println("element = "+element);
            }
            
            System.err.println("Creating per file configuration!");
            Properties props = new Properties(System.getProperties());
            List fileNames = FileUtils.getFileNames(baseDir, specPlugin.getConfigIncludes(), specPlugin.getConfigExcludes(), false);
            for (Iterator it = fileNames.iterator(); it.hasNext();) {
                String fileName = (String) it.next();
                System.err.println("Configuration File: "+fileName);
                props.put("file", fileName);
                String configResult = PropertiesUtils.substitute(element, props);
                Xpp3Dom child = Xpp3DomBuilder.build(new StringReader(configResult));
                parent.addChild(child);
            }
            
            plugin.setConfiguration(configuration);
            
        }
		
	}

	public boolean isType(String sourceType) {
		return m_typeName.equals(sourceType);
	}

    public void beforeSaveFileSets(File baseDir, String target, PomBuilder builder) throws Exception {
        
    }

    public void afterSaveFileSets(File baseDir, String target, PomBuilder builder) throws Exception {
        addPlugins(baseDir, target, builder);
    }


}
