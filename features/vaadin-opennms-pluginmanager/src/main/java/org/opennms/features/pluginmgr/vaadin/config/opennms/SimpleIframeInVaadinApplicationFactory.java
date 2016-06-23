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

import org.opennms.vaadin.extender.AbstractApplicationFactory;
import org.opennms.web.api.OnmsHeaderProvider;

import com.vaadin.ui.UI;

/**
 * A factory for creating Plugin Manager Administration Application objects.
 *  

 * SimpleIframeInVaadinApplicationFactory allows iframe pages to be defined in the blueprint
 * which are served using the SimpleIframeInVaadinApplication
 */
public class SimpleIframeInVaadinApplicationFactory extends AbstractApplicationFactory {
	
	private String iframePageUrl;
	
	private Map<String, String> headerLinks;
	
    private OnmsHeaderProvider m_headerProvider;

	public String getIframePageUrl() {
		return iframePageUrl;
	}

	public void setIframePageUrl(String iframePageUrl) {
		this.iframePageUrl = iframePageUrl;
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

	public OnmsHeaderProvider getHeaderProvider() {
		return m_headerProvider;
	}

	public void setHeaderProvider(OnmsHeaderProvider headerProvider) {
		this.m_headerProvider = headerProvider;
	}

    /* (non-Javadoc)
     * @see org.opennms.vaadin.extender.AbstractApplicationFactory#getUI()
     */
    @Override
    public UI createUI() {
        SimpleIframeInVaadinApplication simpleIframeInVaadinApplication = new SimpleIframeInVaadinApplication();
        simpleIframeInVaadinApplication.setHeaderProvider(m_headerProvider);
        simpleIframeInVaadinApplication.setIframePageUrl(iframePageUrl);
        simpleIframeInVaadinApplication.setHeaderLinks(headerLinks);;
        return simpleIframeInVaadinApplication;
    }

    /* (non-Javadoc)
     * @see org.opennms.vaadin.extender.AbstractApplicationFactory#getUIClass()
     */
    @Override
    public Class<? extends UI> getUIClass() {
        return SimpleIframeInVaadinApplication.class;
    }

}
