package org.opennms.mavenize;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class SourceSet {
	
	public static SourceSet create(String sourceType, String targetDir, PomBuilder pomBuilder) {
		return new SourceSet(SourceType.get(sourceType), targetDir, pomBuilder);
	}
	
	private LinkedList m_fileSets = new LinkedList();
	private PomBuilder m_pomBuilder;
	private SourceType m_sourceType;
	private String m_targetDir;

	private SourceSet(SourceType sourceType, String targetDir, PomBuilder pomBuilder) {
		m_sourceType = sourceType;
		m_targetDir = targetDir;
		m_pomBuilder = pomBuilder;
		
		//m_sourceType.addPlugins(m_pomBuilder);
	}

	public void save(File baseDir) throws Exception {
		File target = new File(baseDir, getTargetDir());
		target.mkdirs();
		
        m_sourceType.beforeSaveFileSets(baseDir, getTargetDir(), m_pomBuilder);
		saveFileSets(target);
        m_sourceType.afterSaveFileSets(baseDir, getTargetDir(), m_pomBuilder);
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
		return m_targetDir != null
			? PropertiesUtils.substitute(m_targetDir, System.getProperties())
			: m_sourceType.getStandardDir();
	}

	public boolean isType(String sourceType) {
		return m_sourceType.isType(sourceType);
	}

}
