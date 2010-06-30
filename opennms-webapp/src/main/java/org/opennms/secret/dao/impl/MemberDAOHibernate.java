//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.secret.dao.impl;

import java.util.List;

import org.opennms.secret.dao.MemberDAO;
import org.opennms.secret.model.OGPMember;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * <p>MemberDAOHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class MemberDAOHibernate extends HibernateDaoSupport implements MemberDAO {
    
    /** {@inheritDoc} */
    public void initialize(Object obj) {
        getHibernateTemplate().initialize(obj);
    }

    /** {@inheritDoc} */
    public OGPMember getMember(final Long id) {
        return (OGPMember)getHibernateTemplate().load(OGPMember.class, id);
    }
    
    /** {@inheritDoc} */
    public List findMembersByLastName(String lastName) {
        return getHibernateTemplate().find("from OGPMember member where member.lastName = ?", lastName);
    }
    
    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List findAll() {
        return getHibernateTemplate().loadAll(OGPMember.class);
    }

    /** {@inheritDoc} */
    public void createMember(OGPMember member) {
        getHibernateTemplate().save(member);
    }

}
