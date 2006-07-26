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


public class AliasedResource extends CollectionResource {
    
	private IfInfo m_ifInfo;
    private String m_ifAliasComment;
    private String m_domain;

    public AliasedResource(ResourceType resourceType, String domain, IfInfo ifInfo, String ifAliasComment) {
        super(resourceType);
        m_domain = domain;
        m_ifInfo = ifInfo;
        m_ifAliasComment = ifAliasComment;
    }
    
    public IfInfo getIfInfo() {
        return m_ifInfo;
    }

    public Collection getAttributeTypes() {
        return m_ifInfo.getAttributeTypes();
    }

    String getAliasDir() {
        return getIfInfo().getAliasDir(m_ifAliasComment);
    }

    public String getDomain() {
        return m_domain;
    }

    protected File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File domainDir = new File(rrdBaseDir, getDomain());
        File aliasDir = new File(domainDir, getAliasDir());
        return aliasDir;
    }

    public CollectionAgent getCollectionAgent() {
        return getIfInfo().getCollectionAgent();
    }

    public String toString() {
        return getDomain()+'/'+getAliasDir()+" ["+m_ifInfo+']';
    }

    public boolean rescanNeeded() {
        return getIfInfo().currentAliasIsOutOfDate();
    }

    public boolean isScheduledForCollection() {
        return getIfInfo().isScheduledForCollection();
    }

    public boolean shouldPersist(ServiceParameters serviceParameters) {
        return (serviceParameters.aliasesEnabled() && getAliasDir() != null) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getAliasDir()));
    }

    protected int getType() {
        return getIfInfo().getType();
    }

    @Override
	public void visit(CollectionSetVisitor visitor) {
		visitor.visitResource(this);
		
		for (Iterator it = getGroups().iterator(); it.hasNext();) {
		    AttributeGroup group = (AttributeGroup) it.next();
		    AttributeGroup aliased = new AliasedGroup(this, group);
		    group.visit(visitor);
		}
		
		visitor.completeResource(this);
	}

}
