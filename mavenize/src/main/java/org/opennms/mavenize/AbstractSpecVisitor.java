package org.opennms.mavenize;

import org.opennms.mavenize.config.Dependencies;
import org.opennms.mavenize.config.Dependency;
import org.opennms.mavenize.config.Exclude;
import org.opennms.mavenize.config.Fileset;
import org.opennms.mavenize.config.Include;
import org.opennms.mavenize.config.Module;
import org.opennms.mavenize.config.ModuleDependency;
import org.opennms.mavenize.config.Project;
import org.opennms.mavenize.config.Repository;
import org.opennms.mavenize.config.Sources;

public class AbstractSpecVisitor implements SpecVisitor {

	public void visitProject(Project project) {
	}
	
	public void completeProject(Project project) {	
	}

	public void visitModule(Module module) {
	}
	
	public void completeModule(Module module) {
	}

	public void visitSources(Sources sources) {
	}
	
	public void completeSources(Sources sources) {
	}

	public void visitFileSet(Fileset fileset) {
	}
	
	public void completeFileSet(Fileset fileset) {
	}

	public void visitInclude(Include include) {
	}
	
	public void completeInclude(Include include) {
	}

	public void visitExclude(Exclude exclude) {
	}
	
	public void completeExclude(Exclude exclude) {
	}

	public void visitDependencies(Dependencies deps) {
	}
	
	public void completeDependencies(Dependencies deps) {
	}

	public void visitDependency(Dependency dep) {
	}
	
	public void completeDependency(Dependency dep) {
	}

	public void visitModuleDependency(ModuleDependency modDep) {
	}
	
	public void completeModuleDependency(ModuleDependency modDep) {
	}
	
	public void visitRepository(Repository repo) {
	}
	
	public void completeRepository(Repository repo) {
	}

}
