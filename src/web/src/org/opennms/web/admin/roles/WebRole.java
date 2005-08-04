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
package org.opennms.web.admin.roles;

import java.util.Date;

public class WebRole {
    
    private String m_name;
    private String m_description;
    private String m_defaultUser;
    private String m_membershipGroup;
    
    private static int sm_count = 1;
    
    public WebRole() {
        int count = sm_count++;
        m_name = "onCall"+count;
        m_description = "The "+count+"th on call role";
        m_defaultUser = "defaultUser"+count;
        m_membershipGroup = "membershipGroup"+count;
    }
    
    public String getDefaultUser() {
        return m_defaultUser;
    }
    public void setDefaultUser(String defaultUser) {
        m_defaultUser = defaultUser;
    }
    public String getDescription() {
        return m_description;
    }
    public void setDescription(String description) {
        m_description = description;
    }
    public String getMembershipGroup() {
        return m_membershipGroup;
    }
    public void setMembershipGroup(String memberShipGroup) {
        m_membershipGroup = memberShipGroup;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public String getCurrentUser() {
        return getDefaultUser();
    }
    
    public WebCalendar getWeeklyCalendar() {
        return null;
    }
    
    public WebCalendar getCalendar() {
        return getCalendar(new Date());
    }

    public WebCalendar getCalendar(Date month) {
        return new MonthlyCalendar(month);
    }

}
