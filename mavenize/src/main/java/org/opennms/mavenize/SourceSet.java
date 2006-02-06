package org.opennms.mavenize;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

public class SourceSet {
	
	private static Properties s_sourceTypeMap = null; 
	static {
		try {
			s_sourceTypeMap = loadSourceMap();
		} catch (IOException e) {
			throw new RuntimeException("Unable to load sourceTypeMap", e);
		}
	}
	
	public static SourceSet create(String sourceType) {
		return new SourceSet(sourceType, getDirectoryToSourceType(sourceType));
	}
	
	private static String getDirectoryToSourceType(String sourceType) {
		return s_sourceTypeMap.getProperty(sourceType+".standardDir");
	}

	private static Properties loadSourceMap() throws IOException {
			Properties sourceProperties = new Properties();
			sourceProperties.load(SourceSet.class.getResourceAsStream("/sourceTypes.properties"));
			return sourceProperties;
	}

	private String m_targetDir;
	private LinkedList m_fileSets = new LinkedList();

	private SourceSet(String type, String targetDir) {
		if (targetDir == null) throw new NullPointerException("targetDir cannot be null for type "+type);
		m_targetDir = targetDir;
	}

	public void save(File baseDir) throws IOException {
		File target = new File(baseDir, m_targetDir);
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

}
