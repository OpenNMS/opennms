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

import org.apache.maven.model.Plugin;

class Platform {
    
    private final ShlibModuleType m_type;
    private String m_platform;
	private PomBuilder m_subModuleBuilder;

    public Platform(ShlibModuleType type, String platform) {
        m_type = type;
        m_platform = platform;
    }
    
    public String getKey() {
        return m_platform;
    }

    String getPlatformString(String platformKey) {
        return m_type.getString(getKey()+'.'+platformKey);
    }

    void addPlatformModule(PomBuilder builder) {
        
        String moduleType = getPlatformString("subModuleType");
		String platformName = getPlatformString("platformName");
		if (moduleType == null) throw new NullPointerException("subModuleType is null for platfrom "+m_platform);
		
		m_subModuleBuilder = createSubModule(builder, moduleType, platformName);
    }

	public void addNativePlugin(File baseDir) {
		Plugin plugin = createPlugin();
        NativePluginConfig conf = m_type.createNativeConfiguration(this, baseDir);
        plugin.setConfiguration(conf.getConfiguration());
        m_type.addPluginExecution(plugin, this);
	}

    private Plugin createPlugin() {
        String groupId = Configuration.get().getString("plugin.native.groupId");
        String artifactId = Configuration.get().getString("plugin.native.artifactId");
        Plugin plugin = m_subModuleBuilder.addPlugin(groupId, artifactId);
        plugin.setExtensions(true);
        return plugin;
    }

    private PomBuilder createSubModule(PomBuilder builder, String moduleType, String platformName) {
		String moduleId = builder.getArtifactId()+"-"+m_platform;
		PomBuilder module = PomBuilder.createBuilder(builder, moduleId, builder.getName()+" - "+platformName, moduleType);
		builder.addModuleDirectory(module);
		return module;
	}
    
}