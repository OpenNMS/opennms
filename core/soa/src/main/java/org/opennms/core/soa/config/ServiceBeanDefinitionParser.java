/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.soa.config;

import static org.opennms.core.soa.config.Constants.SERVICE_REGISTRY_BEAN_NAME;

import java.util.Map;
import java.util.Set;

import org.opennms.core.soa.support.ServiceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ServiceRegistrationBeanDefinitionParser
 *
 * Simplest
 * <onmsgi:service ref="beanToRegister" interface="com.example.Interface" />
 *
 * ID also allowed and becomes the ID of the Registration object
 * <onmsgi:service id="registrationId" ref="beanToRegister" interface="interfaceToPublish" />
 *
 * More than interface supported with nested interfaces element
 *
 * <onmsgi:service ref="beanToRegister">
 *   <onmsgi:interfaces>
 *     <value>com.example.Interface1</value>
 *     <value>com.example.Interface2</value>
 *   </onmsgi:interfaces>
 * </onmsgi:service>
 *
 * @author brozow
 * @version $Id: $
 */
public class ServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    
    /** Constant <code>REF_ATTR="ref"</code> */
    public static final String REF_ATTR = "ref";
    /** Constant <code>INTERFACE_ATTR="interface"</code> */
    public static final String INTERFACE_ATTR = "interface";
    /** Constant <code>INTERFACES_ELEM="interfaces"</code> */
    public static final String INTERFACES_ELEM = "interfaces";
    public static final String PROPS_ELEM = "service-properties";
    
    
    /** {@inheritDoc} */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return ServiceFactoryBean.class;
    }

    /** {@inheritDoc} */
    @Override
    public void doParse(Element element, ParserContext context, BeanDefinitionBuilder bean) {
        
        String ref = element.getAttribute(REF_ATTR);
        bean.addPropertyReference("target", ref);
        bean.addPropertyReference("serviceRegistry", SERVICE_REGISTRY_BEAN_NAME);
        
        String serviceInterface = element.getAttribute(INTERFACE_ATTR);
        if (StringUtils.hasText(serviceInterface)) {
            bean.addPropertyValue("interfaces", serviceInterface);
        }
        
        NodeList nodeList = element.getChildNodes();
        
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            
            if (n instanceof Element) {
                Element child = (Element) n;
                
                if (INTERFACES_ELEM.equals(child.getLocalName())) {
                    
                    if (element.hasAttribute(INTERFACE_ATTR)) {
                        context.getReaderContext().error("either 'interface' attribute or <intefaces> sub-element has be specified", element);
                    }                
                
                    Set<?> interfaces = context.getDelegate().parseSetElement(child, bean.getBeanDefinition());
                    bean.addPropertyValue("interfaces", interfaces);
                
                } else if (PROPS_ELEM.equals(child.getLocalName())) {
                    Map<?,?> svcProps = context.getDelegate().parseMapElement(child, bean.getBeanDefinition());
                    bean.addPropertyValue("serviceProperties", svcProps);
                }
                
            }

            
        }
        

    }

    /** {@inheritDoc} */
    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }
    
    

}
