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
package org.opennms.secret.service.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opennms.secret.dao.MemberDAO;
import org.opennms.secret.model.OGPMember;
import org.opennms.secret.service.MemberService;

/**
 * <p>MemberServiceImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class MemberServiceImpl implements MemberService {
    
    MemberDAO memberDAO;
    
    /**
     * <p>Setter for the field <code>memberDAO</code>.</p>
     *
     * @param memberDAO a {@link org.opennms.secret.dao.MemberDAO} object.
     */
    public void setMemberDAO(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }

    /** {@inheritDoc} */
    public OGPMember getMemberById(Long id) {
        OGPMember member = memberDAO.getMember(id);
        memberDAO.initialize(member);
        return member;
    }

    /** {@inheritDoc} */
    public void createMember(OGPMember member) {
        memberDAO.createMember(member);
    }

    /** {@inheritDoc} */
    public Set findMatching(String searchKey) {
        List members = memberDAO.findAll();
        Set<OGPMember> matches = new HashSet<OGPMember>();
        for (Iterator it = members.iterator(); it.hasNext();) {
            OGPMember member = (OGPMember) it.next();
            if (member.getFirstName().startsWith(searchKey) || member.getLastName().startsWith(searchKey)) {
                matches.add(member);
            }
        }
        return matches;
    }

}
