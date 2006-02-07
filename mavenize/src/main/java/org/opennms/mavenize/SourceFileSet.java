package org.opennms.mavenize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SystemConfiguration;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

public class SourceFileSet {
	
	String m_baseDir;
	
	List m_includes = new ArrayList();
	List m_excludes = new ArrayList();
	
	public SourceFileSet(String baseDir) {
		SystemConfiguration config = new SystemConfiguration();
		config.addProperty("fileSet.basedir", baseDir);
		m_baseDir = config.getString("fileSet.basedir");
		config.clearProperty("fileSet.basedir");
	}

	public void addInclude(String name) {
		m_includes.add(name);
	}
	
	public String[] getIncludes() {
		return (String[]) m_includes.toArray(new String[m_includes.size()]);
	}
	
	public void addExclude(String name) {
		m_excludes.add(name);
	}
	
	public String[] getExcludes() {
		return (String[]) m_excludes.toArray(new String[m_excludes.size()]);
	}
	
	public void save(File targetDir) throws IOException {
		
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.addDefaultExcludes();
		scanner.setBasedir(getBaseDir());
		scanner.setIncludes(getIncludes());
		scanner.setExcludes(getExcludes());
		scanner.scan();

		System.out.println("Results using scanner");
		System.out.println("From: "+new File(getBaseDir()).getAbsolutePath());
		System.out.println("To: "+targetDir.getPath());
		String[] included = scanner.getIncludedFiles();
		for (int i = 0; i < included.length; i++) {
			String includedFile = included[i];
			System.out.println("\tFile: "+includedFile);
			FileUtils.copyFile(new File(getBaseDir(), includedFile), new File(targetDir, includedFile));
		}
		
	}

	private String getBaseDir() {
		return m_baseDir;
	}
	

}
