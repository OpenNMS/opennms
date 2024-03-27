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
package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Iterator;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.model.ResourcePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AliasedResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AliasedResource extends SnmpCollectionResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(AliasedResource.class);
    
    private final IfInfo m_ifInfo;
    private final String m_ifAliasComment;
    private final String m_domain;
    private final String m_ifAlias;

    /**
     * <p>Constructor for AliasedResource.</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param domain a {@link java.lang.String} object.
     * @param ifInfo a {@link org.opennms.netmgt.collectd.IfInfo} object.
     * @param ifAliasComment a {@link java.lang.String} object.
     * @param ifAlias a {@link java.lang.String} object.
     */
    public AliasedResource(final ResourceType resourceType, final String domain, final IfInfo ifInfo, final String ifAliasComment, final String ifAlias) {
        super(resourceType);
        m_domain = domain;
        m_ifInfo = ifInfo;
        m_ifAliasComment = ifAliasComment;
        m_ifAlias = ifAlias;
    }
    
    /**
     * <p>getIfInfo</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.IfInfo} object.
     */
    public IfInfo getIfInfo() {
        return m_ifInfo;
    }

    String getAliasDir() {
        return getIfInfo().getAliasDir(m_ifAlias, m_ifAliasComment);
    }

    /**
     * <p>getDomain</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDomain() {
        if ("nodeid".equalsIgnoreCase(m_domain)) {
            return Integer.toString(getIfInfo().getNodeId());
        } else if ("nodelabel".equalsIgnoreCase(m_domain)) {
            try {
                return new NodeLabelJDBCImpl().retrieveLabel(getIfInfo().getNodeId()).getLabel();
            } catch (Throwable e) {
                return "nodeid-" + Integer.toString(getIfInfo().getNodeId());
            }
        } else {
            return m_domain;
        }
    }

    @Override
    public ResourcePath getPath() {
        return ResourcePath.get(getDomain(), getAliasDir());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getDomain()+'/'+getAliasDir()+" ["+m_ifInfo+']';
    }

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean rescanNeeded() {
        boolean outOfDate = getIfInfo().currentAliasIsOutOfDate(m_ifAlias);
        if(outOfDate) {
            getIfInfo().setIfAlias(m_ifAlias);
        }
        return outOfDate;
    }

    /**
     * <p>isScheduledForCollection</p>
     *
     * @return a boolean.
     */
    public boolean isScheduledForCollection() {
        return getIfInfo().isScheduledForCollection();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(final ServiceParameters serviceParameters) {
        boolean shdPrsist = (serviceParameters.aliasesEnabled() && getAliasDir() != null && !getAliasDir().equals("")) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getAliasDir()));
        LOG.debug("shouldPersist = {}", shdPrsist);
        return shdPrsist;
    }

    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    @Override
    public int getSnmpIfType() {
        return getIfInfo().getSnmpIfType();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitResource(this);
	
        for (Iterator<AttributeGroup> it = getGroups().iterator(); it.hasNext();) {
            AttributeGroup aliased = new AliasedGroup(this, it.next());
            aliased.visit(visitor);
        }
	
        visitor.completeResource(this);
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<AttributeGroup> getGroups() {
    	return getIfInfo().getGroups();
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_IF; //AliasedResources are implicitly interface type data, at least as far as I (Craig Miskell) understand.  If anyone is sure, please adjust this comment
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInstance() {
        return null; //For node and interface type resources, use the default instance
    }

    @Override
    public String getUnmodifiedInstance() {
        return getInstance();
    }

    @Override
    public ResourcePath getParent() {
        return getCollectionAgent().getStorageResourcePath();
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInterfaceLabel() {
        return getDomain() + '/' + getAliasDir();
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
