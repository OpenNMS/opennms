//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.opennms.netmgt.model.RrdRepository;


/**
 * <p>AliasedResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AliasedResource extends SnmpCollectionResource {
    
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
        return m_domain;
    }

    /** {@inheritDoc} */
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
    public String toString() {
        return getDomain()+'/'+getAliasDir()+" ["+m_ifInfo+']';
    }

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
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
    public boolean shouldPersist(final ServiceParameters serviceParameters) {
        boolean shdPrsist = (serviceParameters.aliasesEnabled() && getAliasDir() != null && !getAliasDir().equals("")) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getAliasDir()));
        if (log().isDebugEnabled()) {
            log().debug("shouldPersist = " + shdPrsist);
        }
        return shdPrsist;
    }

    /**
     * <p>getType</p>
     *
     * @return a int.
     */
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
    public Collection<AttributeGroup> getGroups() {
    	return getIfInfo().getGroups();
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName() {
        return "if"; //AliasedResources are implicitly interface type data, at least as far as I (Craig Miskell) understand.  If anyone is sure, please adjust this comment
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance() {
        return null; //For node and interface type resources, use the default instance
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return getDomain() + '/' + getAliasDir();
    }
}
