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
package org.opennms.secret.web;

import java.util.Set;

import org.opennms.secret.service.MemberService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;

/**
 * <p>CompleterController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class CompleterController implements ThrowawayController {
    
    private String m_searchKey = "";
    private MemberService m_memberService;
    
    /**
     * <p>setSearchKey</p>
     *
     * @param key a {@link java.lang.String} object.
     */
    public void setSearchKey(String key) {
        m_searchKey = key;
    }
    
    /**
     * <p>setMemberService</p>
     *
     * @param memberService a {@link org.opennms.secret.service.MemberService} object.
     */
    public void setMemberService(MemberService memberService) {
        m_memberService = memberService;
    }

    /**
     * <p>execute</p>
     *
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     * @throws java.lang.Exception if any.
     */
    public ModelAndView execute() throws Exception {
        Set matches = m_memberService.findMatching(m_searchKey);
        return new ModelAndView("completions", "matches", matches);
    }
    

}
