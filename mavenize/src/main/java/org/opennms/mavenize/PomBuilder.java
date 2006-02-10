package org.opennms.mavenize;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

public class PomBuilder {
    
    public static PomBuilder createBuilder(PomBuilder parent, String moduleId, String moduleName, String moduleType) {
        return new PomBuilder(parent, moduleId, moduleName, ModuleType.get(moduleType));
    }
    
    public static PomBuilder createBuilder(String moduleId, String moduleType, String moduleName) {
        return new PomBuilder(null, moduleId, moduleName, ModuleType.get(moduleType));
    }
    
    public static PomBuilder createProjectBuilder() {
        return new PomBuilder(null, null, null, ModuleType.get("pom"));
    }
    
	private PomBuilder m_parent;
    private ModuleType m_type;
	private Model m_model = new Model();
	private List m_modules = new ArrayList();
	private LinkedList m_sourceSets = new LinkedList();
	
	private PomBuilder(PomBuilder parent, String moduleId, String moduleName, ModuleType type) {
        m_type = type;
		m_model.setModelVersion("4.0.0");
		setParent(parent);
        setArtifactId(moduleId);
        setName(moduleName);
        
        m_type.configureModule(this);
	}

	public void save() throws IOException {
		save(new File("."));
	}
	
	public void save(File targetDir) throws IOException {
		File baseDir = new File(targetDir, getArtifactId());
		m_type.beforeSave(this, baseDir);
	
		baseDir.mkdirs();

		File pom = new File(baseDir, "pom.xml");
		
		Writer writer = new FileWriter(pom);
		MavenXpp3Writer modelWriter = new MavenXpp3Writer();
		modelWriter.write(writer, m_model);
	
		saveSourceSets(baseDir);

		m_type.afterSave(this, baseDir);

		
		saveModules(baseDir);
	}
	
	private void saveSourceSets(File baseDir) throws IOException {
		for (Iterator it = m_sourceSets.iterator(); it.hasNext();) {
			SourceSet sourceSet = (SourceSet) it.next();
			sourceSet.save(baseDir);
		}
	}

	private void saveModules(File baseDir) throws IOException {
		for (Iterator it = m_modules.iterator(); it.hasNext();) {
			PomBuilder subModule = (PomBuilder) it.next();
			subModule.save(baseDir);
		}
	}

	public void setParent(PomBuilder parent) {
		m_parent = parent;
		
		if (parent == null) {
			m_model.setParent(null);
		} else {
			Parent modelParent = new Parent();
			modelParent.setGroupId(parent.getGroupId());
			modelParent.setArtifactId(parent.getArtifactId());
			modelParent.setVersion(parent.getVersion());
			m_model.setParent(modelParent);
		}
	}

	public String getGroupId() {
		return (m_model.getGroupId() != null ? m_model.getGroupId() : m_parent.getGroupId());
	}

	public void setGroupId(String groupId) {
		m_model.setGroupId(groupId);
	}

	public String getArtifactId() {
		return m_model.getArtifactId();
	}

	public void setArtifactId(String artifactId) {
		m_model.setArtifactId(artifactId);
	}

	public String getVersion() {
		return (m_model.getVersion() != null ? m_model.getVersion() : m_parent.getVersion());
	}

	public void setVersion(String version) {
		m_model.setVersion(version);
	}

	public String getName() {
		return m_model.getName();
	}

	public void setName(String name) {
		m_model.setName(name);
	}

	public String getPackaging() {
		return m_model.getPackaging();
	}

	public void setPackaging(String packaging) {
		m_model.setPackaging(packaging);
	}
	
	public PomBuilder createModule(String moduleId, String moduleName, String moduleType) {
		PomBuilder module = PomBuilder.createBuilder(this, moduleId, moduleName, moduleType);
		
		addModuleReference(moduleId);
		addModuleDirectory(module);
		
		return module;
	}

	public void addModuleDirectory(PomBuilder module) {
		m_modules.add(module);
	}

	public void addModuleReference(String moduleId) {
		m_model.addModule(moduleId);
	}

	public void addDependency(String groupId, String artifactId, String version, String scope) {
		Dependency dependency = new Dependency();
		dependency.setGroupId(groupId);
		dependency.setArtifactId(artifactId);
		dependency.setVersion(version);
		dependency.setScope(scope);
		m_model.addDependency(dependency);
	}

	public void addSourceSet(String sourceType) {
		SourceSet sourceSet = SourceSet.create(sourceType, this);
		m_sourceSets.add(sourceSet);
	}
	
	public SourceSet getCurrentSourceSet() {
		return (SourceSet)m_sourceSets.getLast();
	}

	public void addFileSet(String dir) {
		getCurrentSourceSet().addFileSet(dir);
	}

	public void addInclude(String name) {
		getCurrentSourceSet().addInclude(name);
	}

	public void addExclude(String name) {
		getCurrentSourceSet().addExclude(name);
	}

	public Plugin addPlugin(String groupId, String artifactId) {
		if (m_model.getBuild() == null) {
			m_model.setBuild(new Build());
		}
		
		Plugin plugin = new Plugin();
		plugin.setGroupId(groupId);
		plugin.setArtifactId(artifactId);
		m_model.getBuild().addPlugin(plugin);
		
		return plugin;
	}

	public void moduleComplete() {
		m_type.moduleComplete(this);
	}

	public List getSourceSetsByType(String sourceType) {
		List sets = new ArrayList();
		for (Iterator it = m_sourceSets.iterator(); it.hasNext();) {
			SourceSet sourceSet = (SourceSet) it.next();
			if (sourceSet.isType(sourceType)) {
				sets.add(sourceSet);
			}
		}
		return sets;
	}
	
	

}
