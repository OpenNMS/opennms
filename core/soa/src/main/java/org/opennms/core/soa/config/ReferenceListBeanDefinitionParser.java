/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.soa.config;

import static org.opennms.core.soa.config.Constants.SERVICE_REGISTRY_BEAN_NAME;

import java.util.List;

import org.opennms.core.soa.support.ReferenceListFactoryBean;
import org.opennms.core.soa.support.RegistrationListenerBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class ReferenceListBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String INTERFACE_ATTR = "interface";
	private String m_serviceInterface = null;
	
    @Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext context) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ReferenceListFactoryBean.class);
		factory.addPropertyReference("serviceRegistry", SERVICE_REGISTRY_BEAN_NAME);
		
		String serviceInterface = element.getAttribute(INTERFACE_ATTR);
        if (StringUtils.hasText(serviceInterface)) {
        	m_serviceInterface = serviceInterface;
            factory.addPropertyValue("serviceInterface", serviceInterface);
        }
		
        @SuppressWarnings("unchecked")
        List childElements = DomUtils.getChildElementsByTagName(element, "listener");
        
        if (childElements != null && childElements.size() > 0) {
        	parseList(childElements, factory);
        }
        
		return factory.getBeanDefinition();
	}

    @SuppressWarnings("unchecked")
	private void parseList(List childElements, BeanDefinitionBuilder factory) {
		BeanDefinitionBuilder listener = parseListener((Element)childElements.get(0));
		factory.addPropertyValue("listener", listener.getBeanDefinition());
	}

	private BeanDefinitionBuilder parseListener(Element element) {
		BeanDefinitionBuilder listener = BeanDefinitionBuilder.rootBeanDefinition(RegistrationListenerBean.class);
		listener.addPropertyReference("target", element.getAttribute("ref"));
		listener.addPropertyValue("serviceInterface", m_serviceInterface);
		listener.addPropertyValue("bindMethod", element.getAttribute("bind-method"));
		listener.addPropertyValue("unbindMethod", element.getAttribute("unbind-method"));
		
		return listener;
	}

}
