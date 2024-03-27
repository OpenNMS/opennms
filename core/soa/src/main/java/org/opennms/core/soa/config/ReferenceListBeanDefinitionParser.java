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

/**
 * <p>ReferenceListBeanDefinitionParser class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReferenceListBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String INTERFACE_ATTR = "interface";
    private static final String FILTER_ATTR = "filter";
	private String m_serviceInterface = null;
	
    /** {@inheritDoc} */
    @Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext context) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ReferenceListFactoryBean.class);
		factory.addPropertyReference("serviceRegistry", SERVICE_REGISTRY_BEAN_NAME);
		
		String serviceInterface = element.getAttribute(INTERFACE_ATTR);
        if (StringUtils.hasText(serviceInterface)) {
        	m_serviceInterface = serviceInterface;
            factory.addPropertyValue("serviceInterface", serviceInterface);
        }
        
        String filter = element.getAttribute(FILTER_ATTR);
        if (StringUtils.hasText(filter)) {
            factory.addPropertyValue("filter", filter);
        }
		
        List<Element> childElements = DomUtils.getChildElementsByTagName(element, "listener");
        
        if (childElements != null && childElements.size() > 0) {
        	parseList(childElements, factory);
        }
        
		return factory.getBeanDefinition();
	}

	private void parseList(List<Element> childElements, BeanDefinitionBuilder factory) {
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
