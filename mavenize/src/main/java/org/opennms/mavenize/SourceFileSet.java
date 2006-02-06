package org.opennms.mavenize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

public class SourceFileSet {
	
	String m_baseDir;
	StringBuffer m_includes = null;
	StringBuffer m_excludes = null;
	
	public SourceFileSet(String baseDir) {
		m_baseDir = baseDir;
		
		// add default excludes
		for (Iterator it = FileUtils.getDefaultExcludesAsList().iterator(); it.hasNext();) {
			String exclude = (String) it.next();
			addExclude(exclude);
		}
	}

	public void addInclude(String name) {
		if (m_includes == null) {
			m_includes = new StringBuffer(name);
		}
		else {
			m_includes.append(',');
			m_includes.append(name);
		}
	}
	
	public String getIncludes() {
		if (m_includes == null) return "**";
		
		return m_includes.toString();
	}

	public void addExclude(String name) {
		if (m_excludes == null) {
			m_excludes = new StringBuffer(name);
		} else {
			m_excludes.append(',');
			m_excludes.append(name);
		}
	}
	
	public String getExcludes() {
		if (m_excludes == null) return null;
		return m_excludes.toString();
	}

	public void save(File targetDir) throws IOException {
		FileUtils.copyDirectory(new File(m_baseDir), targetDir, getIncludes(), getExcludes());
	}

}
