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


public class AliasedResource extends SnmpCollectionResource {
    
    private IfInfo m_ifInfo;
    private String m_ifAliasComment;
    private String m_domain;
    private String m_ifAlias;

    public AliasedResource(ResourceType resourceType, String domain, IfInfo ifInfo, String ifAliasComment, String ifAlias) {
        super(resourceType);
        m_domain = domain;
        m_ifInfo = ifInfo;
        m_ifAliasComment = ifAliasComment;
        m_ifAlias = ifAlias;
    }
    
    public IfInfo getIfInfo() {
        return m_ifInfo;
    }

    String getAliasDir() {
        return getIfInfo().getAliasDir(m_ifAlias, m_ifAliasComment);
    }

    public String getDomain() {
        return m_domain;
    }

    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File domainDir = new File(rrdBaseDir, getDomain());
        File aliasDir = new File(domainDir, getAliasDir());
        return aliasDir;
    }

    public String toString() {
        return getDomain()+'/'+getAliasDir()+" ["+m_ifInfo+']';
    }

    public boolean rescanNeeded() {
        boolean outOfDate = getIfInfo().currentAliasIsOutOfDate(m_ifAlias);
        if(outOfDate) {
            getIfInfo().setIfAlias(m_ifAlias);
        }
        return outOfDate;
    }

    public boolean isScheduledForCollection() {
        return getIfInfo().isScheduledForCollection();
    }

    public boolean shouldPersist(ServiceParameters serviceParameters) {
        boolean shdPrsist = (serviceParameters.aliasesEnabled() && getAliasDir() != null && !getAliasDir().equals("")) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getAliasDir()));
        log().debug("shouldPersist = " + shdPrsist);
        return shdPrsist;
    }

    public int getType() {
        return getIfInfo().getType();
    }

    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitResource(this);
	
	for (Iterator it = getGroups().iterator(); it.hasNext();) {
	    AttributeGroup group = (AttributeGroup) it.next();
	    AttributeGroup aliased = new AliasedGroup(this, group);
	    aliased.visit(visitor);
	}
	
	visitor.completeResource(this);
    }

    public Collection getGroups() {
    	return getIfInfo().getGroups();
    }

    public String getResourceTypeName() {
        return "if"; //AliasedResources are implicitly interface type data, at least as far as I (Craig Miskell) understand.  If anyone is sure, please adjust this comment
    }

    public String getInstance() {
        return null; //For node and interface type resources, use the default instance
    }
}
