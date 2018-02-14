/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.pluginmgr.vaadin.config.opennms;


import java.util.Map;

import org.opennms.features.pluginmgr.PluginManager;
import org.opennms.features.pluginmgr.SessionPluginManager;
import org.opennms.vaadin.extender.AbstractApplicationFactory;
import org.opennms.web.api.OnmsHeaderProvider;
import org.osgi.service.blueprint.container.BlueprintContainer;

import com.vaadin.ui.UI;

/**
 * A factory for creating Plugin Manager Administration Application objects.
 */
public class PluginManagerAdminApplicationFactory extends AbstractApplicationFactory {
	

	private OnmsHeaderProvider m_headerProvider;
    
    private PluginManager pluginManager;
    
    private BlueprintContainer blueprintContainer;
    
    // headerLinks map of key= name and value=url for links to be placed in header of page
    private Map<String, String> headerLinks;

	
	public OnmsHeaderProvider getHeaderProvider() {
		return m_headerProvider;
	}

	public void setHeaderProvider(OnmsHeaderProvider headerProvider) {
		this.m_headerProvider = headerProvider;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
	
	/**
	 * headerLinks map of key= name and value=url for links to be placed in header of page
	 * @return
	 */
	public Map<String,String> getHeaderLinks() {
		return headerLinks;
	}


	/**
	 * @param headerLinks map of key= name and value=url for links to be placed in header of page
	 */
	public void setHeaderLinks(Map<String,String> headerLinks) {
		this.headerLinks = headerLinks;
	}

    /**
	 * @return the blueprintContainer
	 */
	public BlueprintContainer getBlueprintContainer() {
		return blueprintContainer;
	}

	/**
	 * @param blueprintContainer the blueprintContainer to set
	 */
	public void setBlueprintContainer(BlueprintContainer blueprintContainer) {
		this.blueprintContainer = blueprintContainer;
	}

	
    /* (non-Javadoc)
     * @see org.opennms.vaadin.extender.AbstractApplicationFactory#getUI()
     */
    @Override
    public UI createUI() {
        PluginManagerAdminApplication pluginManagerAdminApplication = new PluginManagerAdminApplication();
        pluginManagerAdminApplication.setHeaderProvider(m_headerProvider);
        pluginManagerAdminApplication.setHeaderLinks(headerLinks);
        
        //local plugin model persists data for session instance
        SessionPluginManager sessionPluginManager=new SessionPluginManager();
        sessionPluginManager.setPluginManager(pluginManager);
        sessionPluginManager.setBlueprintContainer(blueprintContainer);
        pluginManagerAdminApplication.setSessionPluginManager(sessionPluginManager);
        return pluginManagerAdminApplication;
    }

    /* (non-Javadoc)
     * @see org.opennms.vaadin.extender.AbstractApplicationFactory#getUIClass()
     */
    @Override
    public Class<? extends UI> getUIClass() {
        return PluginManagerAdminApplication.class;
    }

}
