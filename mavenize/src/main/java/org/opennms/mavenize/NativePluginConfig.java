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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.xml.Xpp3Dom;

class NativePluginConfig {
    
    String m_compilerProvider;
    String m_compilerOptions;
    String m_linkerOptions;
    List m_sourceDirs = new LinkedList();
    Map m_filesNames = new HashMap();
    private String m_javahOs;
    private String m_jdkIncludePath;

    public void setCompilerProvider(String compilerProvider) {
        m_compilerProvider = compilerProvider;
    }

    public void addSourceDirectory(String dir) {
        m_sourceDirs.add(dir);
    }

    public void setLinkerOptions(String linkerOptions) {
        m_linkerOptions = linkerOptions;
    }

    public void setCompilerOptions(String compilerOptions) {
        m_compilerOptions = compilerOptions;
    }
    
    public Xpp3Dom getConfiguration() {
        Xpp3Dom config = new Xpp3Dom("configuration");
        addCompilerProvider(config);
        addCompilerOptions(config);
        addSourceDirectories(config);
        addLinkerOptions(config);
        addJavahOs(config);
        addJdkIncludePath(config);
        
        return config;
    }

    private void addJdkIncludePath(Xpp3Dom config) {
        if (m_jdkIncludePath == null) return;
        
        Xpp3Dom dom = new Xpp3Dom("jdkIncludePath");
        dom.setValue(m_jdkIncludePath);
        
        config.addChild(dom);
    }

    private void addJavahOs(Xpp3Dom config) {
        if (m_javahOs == null) return;
        
        Xpp3Dom dom = new Xpp3Dom("javahOS");
        dom.setValue(m_javahOs);
        
        config.addChild(dom);
    }

    private void addLinkerOptions(Xpp3Dom config) {
        if (m_linkerOptions == null) return;
        
        Xpp3Dom option = new Xpp3Dom("linkerStartOption");
        option.setValue(m_linkerOptions);
        Xpp3Dom options = new Xpp3Dom("linkerStartOptions");
        options.addChild(option);
        
        config.addChild(options);
    }

    private void addCompilerOptions(Xpp3Dom config) {
        if (m_compilerOptions == null) return;
        
        Xpp3Dom option = new Xpp3Dom("compilerStartOption");
        option.setValue(m_compilerOptions);
        Xpp3Dom options = new Xpp3Dom("compilerStartOptions");
        options.addChild(option);
        
        config.addChild(options);
    }

    private void addCompilerProvider(Xpp3Dom config) {
        if (m_compilerProvider == null) return;
        
        Xpp3Dom dom = new Xpp3Dom("compilerProvider");
        dom.setValue(m_compilerProvider);
        
        config.addChild(dom);
    }

    public void addSourceDirectories(Xpp3Dom config) {
    	if (m_sourceDirs.isEmpty()) return;
    	
    	
        Xpp3Dom sources = new Xpp3Dom("sources");
        for (Iterator it = m_sourceDirs.iterator(); it.hasNext();) {
            String dir = (String) it.next();
            addSourceDirectory(sources, dir);
        }
        
        config.addChild(sources);
    }

    private void addSourceDirectory(Xpp3Dom sources, String dir) {
    	String[] fileNames = (String[])m_filesNames.get(dir);
    	
        Xpp3Dom dirDom = new Xpp3Dom("directory");
        dirDom.setValue(dir);
        
        Xpp3Dom source = new Xpp3Dom("source");
        source.addChild(dirDom);

    	if (fileNames != null) {
    		Xpp3Dom files = new Xpp3Dom("fileNames");
    		for (int i = 0; i < fileNames.length; i++) {
				Xpp3Dom file = new Xpp3Dom("fileName");
				file.setValue(fileNames[i]);
				files.addChild(file);
			}
    		
    		source.addChild(files);
    	}
        
        sources.addChild(source);
    }

    public void setJavahOS(String javahOs) {
        m_javahOs = javahOs;
    }

    public void setJdkIncludePath(String jdkIncludePath) {
        m_jdkIncludePath = jdkIncludePath;
    }

	public void addFileNames(String directory, String[] fileNames) {
		addSourceDirectory(directory);
		m_filesNames.put(directory, fileNames);
	}
}