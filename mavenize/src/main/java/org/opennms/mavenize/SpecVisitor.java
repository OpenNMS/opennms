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

public interface SpecVisitor {

	public abstract void visitProject(Project module);
	public abstract void visitModule(Module module);
	public abstract void visitSources(Sources souces);
	public abstract void visitFileSet(Fileset fileset);
	public abstract void visitInclude(Include include);
	public abstract void visitExclude(Exclude exclude);
	public abstract void visitDependencies(Dependencies deps);
	public abstract void visitDependency(Dependency dep);
	public abstract void visitModuleDependency(ModuleDependency modDep);
	public abstract void visitRepository(Repository repo);
	
	public abstract void completeProject(Project project);	
	public abstract void completeModule(Module module);
	public abstract void completeSources(Sources sources);
	public abstract void completeFileSet(Fileset fileset);
	public abstract void completeInclude(Include include);
	public abstract void completeExclude(Exclude exclude);
	public abstract void completeDependencies(Dependencies deps);
	public abstract void completeDependency(Dependency dep);
	public abstract void completeModuleDependency(ModuleDependency modDep);
	public abstract void completeRepository(Repository repo);
	
}
