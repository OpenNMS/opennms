package org.opennms.mavenize;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.FileUtils;


public class ShlibModuleType extends ModuleType {
	
	public class CFileFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			return FileUtils.getExtension(name).equals("c");
		}

	}

	List m_platforms = new ArrayList();
	
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
            m_platforms.add(platform);
		}
		
	}

    protected Platform createPlatform(String platfrm) {
        return new Platform(this, platfrm);
    }

    NativePluginConfig createNativeConfiguration(Platform platform, File baseDir) {
        NativePluginConfig conf = new NativePluginConfig();
        conf.setCompilerProvider(platform.getPlatformString("compilerProvider"));
        conf.setCompilerOptions(platform.getPlatformString("compilerOptions"));
        conf.setLinkerOptions(platform.getPlatformString("linkerOptions"));
        
        
        File nativeDir = new File(baseDir, "src/main/native");
        conf.addFileNames("../src/main/native", nativeDir.list(new CFileFilter()));
        
    
        return conf;
    }

    public void addPluginExecution(Plugin plugin, Platform platform) {
    }

	public void afterSave(PomBuilder builder, File baseDir) {
		super.afterSave(builder, baseDir);
		
		for (Iterator it = m_platforms.iterator(); it.hasNext();) {
			Platform platform = (Platform) it.next();
			platform.addNativePlugin(baseDir);
		}
		
	}

	public void beforeSave(PomBuilder builder, File baseDir) {
		super.beforeSave(builder, baseDir);
		
		
	}

}
