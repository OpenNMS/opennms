<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Apr 16: Changed the notification box to show outstanding notifications
//              for logged in user.
// 2003 Feb 07: Fixed URLEncoder issues.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%>

<h3 class="o-box">Quick Search</h3>
<div class="boxWrapper">
  <div class="searchHost" style="position:relative; left: 0px;">
    <form action="element/nodeList.htm" method="GET">
      <font style="font-size: 70%; line-height: 1.25em; align=left">Node label like:</font><br />
      <input type="hidden" name="listInterfaces" value="true"/>
      <input type="text" size="20" name="nodename" />
      <input type="submit" value="Search"/>
    </form>
    <form action="element/nodeList.htm" method="GET">
      <font style="font-size: 70%; line-height: 1.25em; align=left">TCP/IP Address like:</font><br />
      <input type="hidden" name="listInterfaces" value="false"/>
      <input type="text" name="iplike" value="*.*.*.*" />
      <input type="submit" value="Search"/>               
    </form>
  </div>
</div>
