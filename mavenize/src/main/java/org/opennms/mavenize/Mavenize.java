//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.mavenize;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
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

public class Mavenize {
	
	private Project m_module;

	public Mavenize(String specFile) throws Exception {
		System.out.println("Loading spec file: "+specFile);
		FileReader reader = new FileReader(specFile);
		parse(reader);
		reader.close();
	}
	
	Mavenize(Reader reader) throws Exception {
		parse(reader);
	}

	private void parse(Reader reader) throws MarshalException, ValidationException {
		m_module = (Project)Unmarshaller.unmarshal(Project.class, reader);
	}
	
	Project getTopLevelModule() { return m_module; }

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			usage();
			return;
		}
		
		Mavenize mavenize = new Mavenize(args[0]);
		
		PomBuilder builder = PomBuilder.createProjectBuilder();
		mavenize.visitSpec(new ProjectBuildingVisitor(builder));
		builder.save(new File("."));
		

	}
	
	public void visitSpec(SpecVisitor visitor) {
		visitProject(visitor, getTopLevelModule());
	}
	
	private void visitProject(SpecVisitor visitor, Project project) {
		visitor.visitProject(project);
		
		// then visit the dependencies
		if (project.getDependencies() != null)
			visitDependencies(visitor, project.getDependencies());

		// then visit the sub modules
		Enumeration en = project.enumerateModule();
		while (en.hasMoreElements()) {
			Module subModule = (Module) en.nextElement();
			visitModule(visitor, subModule);
		}
		
		// now visit the repositories
		Enumeration repos = project.enumerateRepository();
		while (repos.hasMoreElements()) {
			Repository repo = (Repository) repos.nextElement();
			visitRepository(visitor, repo);
		}
		
		visitor.completeProject(project);
	}

	private void visitRepository(SpecVisitor visitor, Repository repo) {
		visitor.visitRepository(repo);
		visitor.completeRepository(repo);
		
	}

	private void visitModule(SpecVisitor visitor, Module module) {
		visitor.visitModule(module);
		
		// first visit the sources
		Enumeration sources = module.enumerateSources();
		while (sources.hasMoreElements()) {
			Sources source = (Sources) sources.nextElement();
			visitorSources(visitor, source);
		}
		
		// then visit the dependencies
		if (module.getDependencies() != null)
			visitDependencies(visitor, module.getDependencies());

		// then visit the sub modules
		Enumeration en = module.enumerateModule();
		while (en.hasMoreElements()) {
			Module subModule = (Module) en.nextElement();
			visitModule(visitor, subModule);
		}
		
		visitor.completeModule(module);
	}

	private void visitDependencies(SpecVisitor visitor, Dependencies deps) {
		visitor.visitDependencies(deps);
		
		Enumeration modDepends = deps.enumerateModuleDependency();
		while (modDepends.hasMoreElements()) {
			ModuleDependency modDepend = (ModuleDependency) modDepends.nextElement();
			visitModuleDependency(visitor, modDepend);
		}
		
		Enumeration depends = deps.enumerateDependency();
		while (depends.hasMoreElements()) {
			Dependency depend = (Dependency) depends.nextElement();
			visitDependency(visitor, depend);
		}
		
		visitor.completeDependencies(deps);
		
	}

	private void visitDependency(SpecVisitor visitor, Dependency depend) {
		visitor.visitDependency(depend);
		visitor.completeDependency(depend);
	}

	private void visitModuleDependency(SpecVisitor visitor, ModuleDependency modDepend) {
		visitor.visitModuleDependency(modDepend);
		visitor.completeModuleDependency(modDepend);
	}

	private void visitorSources(SpecVisitor visitor, Sources sources) {
		visitor.visitSources(sources);
		
		Enumeration filesets = sources.enumerateFileset();
		while (filesets.hasMoreElements()) {
			Fileset fileset = (Fileset) filesets.nextElement();
			visitFileSet(visitor, fileset);
		}
		
		visitor.completeSources(sources);
	}

	private void visitFileSet(SpecVisitor visitor, Fileset fileset) {
		visitor.visitFileSet(fileset);
		
		Enumeration includes = fileset.enumerateInclude();
		while (includes.hasMoreElements()) {
			Include include = (Include) includes.nextElement();
			visitInclude(visitor, include);
		}
		
		Enumeration excludes = fileset.enumerateExclude();
		while (excludes.hasMoreElements()) {
			Exclude exclude = (Exclude) excludes.nextElement();
			visitExclude(visitor, exclude);
		}
		
		visitor.completeFileSet(fileset);
	}

	private void visitExclude(SpecVisitor visitor, Exclude exclude) {
		visitor.visitExclude(exclude);
		visitor.completeExclude(exclude);
	}

	private void visitInclude(SpecVisitor visitor, Include include) {
		visitor.visitInclude(include);
		visitor.completeInclude(include);
	}

	public static void usage() {
		System.err.println("mavenize <maven-spec-file>");
	}

}
