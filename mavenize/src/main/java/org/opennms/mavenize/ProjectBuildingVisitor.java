package org.opennms.mavenize;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.opennms.mavenize.config.Configuration;
import org.opennms.mavenize.config.Dependencies;
import org.opennms.mavenize.config.Dependency;
import org.opennms.mavenize.config.Exclude;
import org.opennms.mavenize.config.Fileset;
import org.opennms.mavenize.config.Include;
import org.opennms.mavenize.config.Module;
import org.opennms.mavenize.config.ModuleDependency;
import org.opennms.mavenize.config.Plugin;
import org.opennms.mavenize.config.Project;
import org.opennms.mavenize.config.Repository;
import org.opennms.mavenize.config.Sources;

public class ProjectBuildingVisitor extends AbstractSpecVisitor {
	
	LinkedList m_builders = new LinkedList();

	public ProjectBuildingVisitor(PomBuilder builder) {
		m_builders.addFirst(builder);
	}
	
	public void visitProject(Project project) {
		getBuilder().setGroupId(project.getGroupId());
		getBuilder().setArtifactId(project.getArtifactId());
		getBuilder().setVersion(project.getVersion());
		getBuilder().setName(project.getName());
		getBuilder().setPackaging(project.getPackaging());
	}
	

	public void visitModule(Module module) {
		PomBuilder modBuilder = getBuilder().createModule(module.getModuleId(), module.getModuleName(), module.getModuleType());
		pushBuilder(modBuilder);
	}
	
	public void completeModule(Module module) {
		getBuilder().moduleComplete();
		popBuilder();
	}

	public void visitSources(Sources sources) {
		getBuilder().addSourceSet(sources.getSourceType(), sources.getTargetDir());
	}

	public void visitFileSet(Fileset fileset) {
		getBuilder().addFileSet(fileset.getDir());
	}

	public void visitInclude(Include include) {
		getBuilder().addInclude(include.getName());

	}

	public void visitExclude(Exclude exclude) {
		getBuilder().addExclude(exclude.getName());
	}

	public void visitDependencies(Dependencies deps) {
		// TODO Auto-generated method stub

	}

	public void visitDependency(Dependency dep) {
		getBuilder().addDependency(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getScope(), false);
	}

	public void visitModuleDependency(ModuleDependency modDep) {
		getBuilder().addDependency(getBuilder().getGroupId(), modDep.getModuleId(), getBuilder().getVersion(), modDep.getScope(), modDep.getPlatformSpecific());
	}
	
	public void visitRepository(Repository repo) {
		getBuilder().addRepository(repo.getRepoId(), repo.getRepoName(), repo.getUrl(), repo.getRelease(), repo.getSnaphot());
	}
    
	public void visitPlugin(Plugin plugin) {
        Xpp3Dom config = processConfiguration(plugin.getConfiguration());
        getBuilder().addPlugin(plugin.getGroupId(), plugin.getArtifactId(), config);
    }

    private Xpp3Dom processConfiguration(Configuration configuration) {
        try {

            if (configuration == null) return null;
            if (configuration.getAnyObject() == null) return null;

            Xpp3Dom configDom = new Xpp3Dom("configuration");
            Xpp3Dom configContents = Xpp3DomBuilder.build(new StringReader(configuration.getAnyObject().toString()));
            configDom.addChild(configContents);
            return configDom;
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to process plugin config. ", e);
        }
        
        
    }

    void pushBuilder(PomBuilder builder) {
		m_builders.addFirst(builder);
	}
	
	PomBuilder popBuilder() {
		return (PomBuilder)m_builders.removeFirst();
	}
	

	PomBuilder getBuilder() {
		return (PomBuilder)m_builders.getFirst();
	}

}
