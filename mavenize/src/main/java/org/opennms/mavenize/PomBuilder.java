package org.opennms.mavenize;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class PomBuilder {
    
    private static Map m_moduleLookup = new HashMap();
    
    public static PomBuilder findModule(String moduleId) {
        return (PomBuilder)m_moduleLookup.get(moduleId);
    }
    
    public static PomBuilder createBuilder(PomBuilder parent, String moduleId, String moduleName, String moduleType) {
        PomBuilder module = new PomBuilder(parent, moduleId, moduleName, ModuleType.get(moduleType));
        m_moduleLookup.put(moduleId, module);
        return module;
    }
    
    public static PomBuilder createBuilder(String moduleId, String moduleName, String moduleType) {
        return createBuilder(null, moduleId, moduleName, moduleType);
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

	public void save() throws Exception {
		save(new File("."));
	}
	
	public void save(File targetDir) throws Exception {
		File baseDir = new File(targetDir, getArtifactId());
        
		m_type.beforeSaveSourceSets(this, baseDir);
	
		baseDir.mkdirs();

        saveSourceSets(baseDir);

		m_type.afterSaveSourceSets(this, baseDir);

        saveModules(baseDir);

        File pom = new File(baseDir, "pom.xml");
        Writer writer = new FileWriter(pom);
        MavenXpp3Writer modelWriter = new MavenXpp3Writer();
        modelWriter.write(writer, m_model);
    
		
	}
	
	private void saveSourceSets(File baseDir) throws Exception {
		for (Iterator it = m_sourceSets.iterator(); it.hasNext();) {
			SourceSet sourceSet = (SourceSet) it.next();
			sourceSet.save(baseDir);
		}
	}

	private void saveModules(File baseDir) throws Exception {
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

	public void addDependency(String groupId, String artifactId, String version, String scope, boolean platformSpecific) {
		Dependency dependency = new Dependency();
		dependency.setGroupId(groupId);
		dependency.setArtifactId(platformSpecific ? artifactId+"-${platform}" : artifactId);
		dependency.setVersion(version);
		dependency.setScope(scope);
        if (platformSpecific) {
            PomBuilder modDep = PomBuilder.findModule(artifactId);
            ModuleType modType = modDep.getModuleType();
            dependency.setType("${platform."+modType.getTypeName()+"}");
        }
		m_model.addDependency(dependency);
	}

	private ModuleType getModuleType() {
        return m_type;
    }

    public void addSourceSet(String sourceType, String targetDir) {
		SourceSet sourceSet = SourceSet.create(sourceType, targetDir, this);
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
		
		Plugin plugin = getPlugin(groupId, artifactId);
		if (plugin == null) {
		    plugin = new Plugin();
		    plugin.setGroupId(groupId);
		    plugin.setArtifactId(artifactId);
		    m_model.getBuild().addPlugin(plugin);
		}
		return plugin;
	}
    
    public Plugin addPlugin(String groupId, String artifactId, Xpp3Dom config) {
        Plugin plugin = addPlugin(groupId, artifactId);
        if (config != null)
            plugin.setConfiguration(config);
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

	public void addRepository(String repoId, String repoName, String url, boolean release, boolean snaphot) {
		Repository repo = new Repository();
		repo.setId(repoId);
		repo.setUrl(url);
		repo.setName(repoName);
		
		RepositoryPolicy releasePolicy = new RepositoryPolicy();
		releasePolicy.setEnabled(release);
		
		RepositoryPolicy snapshotPolicy = new RepositoryPolicy();
		snapshotPolicy.setEnabled(snaphot);
		
		repo.setReleases(releasePolicy);
		repo.setSnapshots(snapshotPolicy);
		
		m_model.addRepository(repo);
		m_model.addPluginRepository(repo);
	}
    
    public Plugin getPlugin(String groupId, String artifactId) {
        for (Iterator it = m_model.getBuild().getPlugins().iterator(); it.hasNext();) {
            Plugin plugin = (Plugin) it.next();
            if (groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId())) {
                return plugin;
            }
        }
        return null;
    }
    
    public Resource getResourceByDirectory(String directory) {
        for (Iterator it = m_model.getBuild().getResources().iterator(); it.hasNext();) {
            Resource resource = (Resource) it.next();
            if (directory.equals(resource.getDirectory()))
                return resource;
        }
        return null;
    }

    public void addResource(Resource resource) {
        if (m_model.getBuild() == null) {
            m_model.setBuild(new Build());
        }
        
        m_model.getBuild().addResource(resource);
    }

    public void addFilterFile(String fileName) {
        if (m_model.getBuild() == null) {
            m_model.setBuild(new Build());
        }
        
        if (!hasFilterFile(fileName))
            m_model.getBuild().addFilter(fileName);
    }

    private boolean hasFilterFile(String fileName) {
        for (Iterator it = m_model.getBuild().getFilters().iterator(); it.hasNext();) {
            String filterFile = (String) it.next();
            if (fileName.equals(filterFile))
                return true;
        }
        return false;
    }

	
	

}
