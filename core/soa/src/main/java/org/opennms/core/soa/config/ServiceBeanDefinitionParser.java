/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    /** Constant <code>DEPENDS_ON_ATTR="depends-on"</code> */
    public static final String DEPENDS_ON_ATTR = "depends-on";
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
        
        String dependsOn = element.getAttribute(DEPENDS_ON_ATTR);
        if (dependsOn != null && !"".equals(dependsOn.trim())) {
            bean.addDependsOn(dependsOn.trim());
        }
        
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
                        context.getReaderContext().error("either 'interface' attribute or <interfaces> sub-element has to be specified", element);
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
