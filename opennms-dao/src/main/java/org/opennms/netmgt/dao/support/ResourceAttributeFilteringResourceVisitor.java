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
package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceVisitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>ResourceAttributeFilteringResourceVisitor class.</p>
 *
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class ResourceAttributeFilteringResourceVisitor implements ResourceVisitor, InitializingBean {
    private ResourceVisitor m_delegatedVisitor;
    private String m_resourceAttributeKey;
    private String m_resourceAttributeValueMatch;

    /** {@inheritDoc} */
    @Override
    public void visit(OnmsResource resource) {
        if (m_resourceAttributeValueMatch.equals(resource.getExternalValueAttributes().get(m_resourceAttributeKey))
                || m_resourceAttributeValueMatch.equals(resource.getStringPropertyAttributes().get(m_resourceAttributeKey))) {
            m_delegatedVisitor.visit(resource);
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_delegatedVisitor != null, "property delegatedVisitor must be set to a non-null value");
        Assert.state(m_resourceAttributeKey != null, "property resourceAttributeKey must be set to a non-null value");
        Assert.state(m_resourceAttributeValueMatch != null, "property resourceAttributeValueMatch must be set to a non-null value");
    }

    /**
     * <p>getDelegatedVisitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public ResourceVisitor getDelegatedVisitor() {
        return m_delegatedVisitor;
    }

    /**
     * <p>setDelegatedVisitor</p>
     *
     * @param delegatedVisitor a {@link org.opennms.netmgt.model.ResourceVisitor} object.
     */
    public void setDelegatedVisitor(ResourceVisitor delegatedVisitor) {
        m_delegatedVisitor = delegatedVisitor;
    }

    /**
     * <p>getResourceAttributeKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeKey() {
        return m_resourceAttributeKey;
    }

    /**
     * <p>setResourceAttributeKey</p>
     *
     * @param resourceAttributeKey a {@link java.lang.String} object.
     */
    public void setResourceAttributeKey(String resourceAttributeKey) {
        m_resourceAttributeKey = resourceAttributeKey;
    }

    /**
     * <p>getResourceAttributeValueMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeValueMatch() {
        return m_resourceAttributeValueMatch;
    }

    /**
     * <p>setResourceAttributeValueMatch</p>
     *
     * @param resourceAttributeValueMatch a {@link java.lang.String} object.
     */
    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch) {
        m_resourceAttributeValueMatch = resourceAttributeValueMatch;
    }

}
