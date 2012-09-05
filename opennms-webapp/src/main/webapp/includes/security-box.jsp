<%--
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

--%>

<%-- 
  This page is included by other JSPs to create a box containing a
  security notices.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true"  %>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
  <tr> 
    <td colspan="2" BGCOLOR="#999999"><b>Servers&nbsp;&amp;&nbsp;Services</b></td>
  </tr>
  <tr>
    <td>
      <a HREF="somewhere3">HTTP Servers</a>
    </td>
    <td BGCOLOR="#ff3333" align="right" WIDTH="30%">
      <b>92.50000%</b>
    </td>
  </tr>
  <tr>
    <td>
      <a HREF="somewhere3">SMTP Servers</a>
    </td>
    <td BGCOLOR="green" align="right" WIDTH="30%"> 
      <b>100.00000%</b>
    </td>
  </tr>
  <tr>
    <td>
      <a HREF="somewhere3">All Servers</a>
    </td>
    <td BGCOLOR="#ffff33" align="right" WIDTH="30%">
      <b>99.99976%</b>          
    </td>
  </tr>
</table>    

<br/>
<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc" >
  <tr> 
    <td colspan="2" bgcolor="#999999" ><b><a href="somewhere3">Security</a></b></td>
  </tr>
  <tr> 
    <td>
      <a HREF="somewhere3">mp3.mycompany.com</a>
    </td>
    <td BGCOLOR="#ffff33">
      port scan
    </td>
  </tr>
  <tr> 
    <td>
      <a HREF="somewhere3">192.168.42.173</a>
    </td>
    <td BGCOLOR="#ffff33">
      port scan
    </td>
  </tr>
  <tr> 
    <td>
      <a HREF="somewhere3">192.168.42.2</a>
    </td>
    <td BGCOLOR="#ff3333">
      denial of service
    </td>
  </tr>
  <tr>
    <td>
      <a HREF="somewhere3">1.1.1.1</a>
    </td>
    <td BGCOLOR="#ff3333">
      possible intruder
    </td>
  </tr>
  <tr> 
    <td>
      <a HREF="somewhere3">255.255.255.255</a>
    </td>          
    <td BGCOLOR="#ff3333">
      denial of service
    </td>
  </tr>
  <tr>
    <td COLSPAN=2>
      <a HREF="somewhere3">3 more</a>
    </td>
  </tr>
</table>
