/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.opennms.core.utils.TimeKeeper;
import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.utils.NodeLabel;
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
                return NodeLabel.retrieveLabel(getIfInfo().getNodeId()).getLabel();
            } 
            catch (Throwable e) {
                return "nodeid-" + Integer.toString(getIfInfo().getNodeId());
            }
        } else {
        return m_domain;
        }
    }
 
    /** {@inheritDoc} */
    @Override
    public File getResourceDir(final RrdRepository repository) {
        File domainDir = new File(repository.getRrdBaseDir(), getDomain());
        File aliasDir = new File(domainDir, getAliasDir());
        return aliasDir;
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
    public int getType() {
        return getIfInfo().getType();
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
        return "if"; //AliasedResources are implicitly interface type data, at least as far as I (Craig Miskell) understand.  If anyone is sure, please adjust this comment
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
    public String getParent() {
        return null; //For node and interface type resources, use the default parent
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return getDomain() + '/' + getAliasDir();
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }
}
