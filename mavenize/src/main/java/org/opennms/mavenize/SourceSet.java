package org.opennms.mavenize;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class SourceSet {
	
	public static SourceSet create(String sourceType, PomBuilder pomBuilder) {
		return new SourceSet(SourceType.get(sourceType), pomBuilder);
	}
	
	private LinkedList m_fileSets = new LinkedList();
	private PomBuilder m_pomBuilder;
	private SourceType m_sourceType;

	private SourceSet(SourceType sourceType, PomBuilder pomBuilder) {
		m_sourceType = sourceType;
		m_pomBuilder = pomBuilder;
		
		m_sourceType.addPlugins(m_pomBuilder);
	}

	public void save(File baseDir) throws IOException {
		File target = new File(baseDir, getTargetDir());
		target.mkdirs();
		
		saveFileSets(target);
	}
	

	private void saveFileSets(File target) throws IOException {
		for (Iterator it = m_fileSets.iterator(); it.hasNext();) {
			SourceFileSet fileSet = (SourceFileSet) it.next();
			fileSet.save(target);
		}
	}

	public void addFileSet(String dir) {
		m_fileSets.add(new SourceFileSet(dir));
	}
	
	public SourceFileSet getCurrentFileSet() {
		return (SourceFileSet)m_fileSets.getLast();
	}

	public void addInclude(String name) {
		getCurrentFileSet().addInclude(name);
	}

	public void addExclude(String name) {
		getCurrentFileSet().addExclude(name);
	}

	private String getTargetDir() {
		return m_sourceType.getStandardDir();
	}

	public boolean isType(String sourceType) {
		return m_sourceType.isType(sourceType);
	}

}
