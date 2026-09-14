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
package org.opennms.netmgt.shared.bootstrap;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Minimal stand-in for the Gemini {@code osgi:} namespace. Our Spring-Context
 * bundles only use {@code <osgi:reference id=".." interface=".."/>} (service
 * exports go through {@code onmsgi:}), so only that element is supported;
 * anything else fails with Spring's "cannot locate parser" error.
 */
class OsgiCompatNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());
    }

    private static final class ReferenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

        @Override
        protected Class<?> getBeanClass(Element element) {
            return OsgiServiceReferenceFactoryBean.class;
        }

        @Override
        protected void doParse(Element element, BeanDefinitionBuilder builder) {
            builder.addPropertyValue("interfaceName", element.getAttribute("interface"));
            final String filter = element.getAttribute("filter");
            if (StringUtils.hasText(filter)) {
                builder.addPropertyValue("filter", filter);
            }
            builder.addPropertyReference("bundleContext", SpringContextTracker.BUNDLE_CONTEXT_BEAN_NAME);
        }
    }
}
