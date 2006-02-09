package org.opennms.mavenize;

import java.util.Collections;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class JniModuleType extends ShlibModuleType {

    public void addPluginExecution(Plugin plugin, Platform platform) {
        PluginExecution exec = new PluginExecution();
        
        String javahImplementation = platform.getPlatformString("javahImplementation");

        if (javahImplementation != null) {
            Xpp3Dom impl = new Xpp3Dom("implementation");
            impl.setValue(javahImplementation);
            
            Xpp3Dom pluginConfig = new Xpp3Dom("configuration");
            pluginConfig.addChild(impl);
            
            exec.setConfiguration(pluginConfig);
        }
        
        exec.setPhase("generate-sources");
        exec.setGoals(Collections.singletonList("javah"));
        plugin.addExecution(exec);
        
    }

    public JniModuleType(String moduleType) {
		super(moduleType);
	}

    NativePluginConfig createNativeConfiguration(Platform platform) {
        NativePluginConfig config =  super.createNativeConfiguration(platform);
        
        config.setJavahOS(platform.getPlatformString("javahOS"));
        config.setJdkIncludePath(platform.getPlatformString("jdkIncludePath"));
        config.setJavahImplementaion(platform.getPlatformString("javahImplementation"));
        return config;
    }
    

}
