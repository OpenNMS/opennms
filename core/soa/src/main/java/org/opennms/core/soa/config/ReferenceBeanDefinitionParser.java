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

import org.opennms.core.soa.support.ReferenceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * <p>ReferenceBeanDefinitionParser class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReferenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
        
    /** Constant <code>INTERFACE_ATTR="interface"</code> */
    public static final String INTERFACE_ATTR = "interface";
    public static final String DEPENDS_ON_ATTR = "depends-on";
    public static final String FILTER_ATTR = "filter";
    
    /** {@inheritDoc} */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return ReferenceFactoryBean.class;
    }
    
    /** {@inheritDoc} */
    @Override
    public void doParse(Element element, ParserContext context, BeanDefinitionBuilder bean){
        
        bean.addPropertyReference("serviceRegistry", Constants.SERVICE_REGISTRY_BEAN_NAME);
        
        String serviceInterface = element.getAttribute(INTERFACE_ATTR);
        if (StringUtils.hasText(serviceInterface)) {
            bean.addPropertyValue("serviceInterface", serviceInterface);
        }
        
        String dependsOn = element.getAttribute(DEPENDS_ON_ATTR);
        if (dependsOn != null && !"".equals(dependsOn.trim())) {
            bean.addDependsOn(dependsOn.trim());
        }
        
        String filter = element.getAttribute(FILTER_ATTR);
        if (StringUtils.hasText(filter)) {
            bean.addPropertyValue("filter", filter);
        }
    }
}

