package org.opennms.mavenize;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;


public class ShlibModuleType extends ModuleType {
	
	public ShlibModuleType(String moduleType) {
		super(moduleType);
	}
    
	public void configureModule(PomBuilder builder) {
		builder.setPackaging("pom");
		builder.addModuleReference(builder.getArtifactId()+"-${platform}");
		
		String suffix = "platforms";
		List platforms = getList(suffix);
		for (Iterator it = platforms.iterator(); it.hasNext();) {
            String platfrm = (String) it.next();
            Platform platform = createPlatform(platfrm);
            platform.addPlatformModule(builder);
		}
		
	}

    protected Platform createPlatform(String platfrm) {
        return new Platform(this, platfrm);
    }

    NativePluginConfig createNativeConfiguration(Platform platform) {
        NativePluginConfig conf = new NativePluginConfig();
        conf.setCompilerProvider(platform.getPlatformString("compilerProvider"));
        conf.setCompilerOptions(platform.getPlatformString("compilerOptions"));
        conf.setLinkerOptions(platform.getPlatformString("linkerOptions"));
        
        conf.addSourceDirectory("../src/main/native");
    
        return conf;
    }

    public void addPluginExecution(Plugin plugin, Platform platform) {
    }
}
