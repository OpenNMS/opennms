<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Configure Asset Location | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Configure Asset Location"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Notifications" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td valign="top">
      <h3>Configure Asset Location</h3>

      <p>
        <a href="admin/assetLocation/AssetWizard/building.jsp">Configure Buildings and Rooms</a>
      </p>

      <p>
        <a href="admin/assetLocation/AssetWizard/chooseNode.jsp">Configure Nodes</a>
      </p>
    </td>

    <td> &nbsp; </td>

    <td valign="top" width="60%">
      <h3>Configure Buildings and Rooms</h3>

      <p>
      <!--  Pagina di definizione degli Edifici. Permette di aggiungere, modificare ed eliminare delle
        entitàogiche chiamate .Edifici.. Permette anche di associare/de-associare eventuali
        stanze giàefinite. Contiene le seguenti informazioni che sono da associare all.edificio:
        Region, City, Address1, Address2, State, Zip.-->
        Buildings definition page. It allows to add, modify and eliminate logical entity calls Building.<br>
	It contains information about Building: Region, City, Address1, Address2, Been, Zip.<br>
	It allow also to associate/de-associate predefined rooms.
      </p>

      <p>
        <!-- Contiene per ogni edificio la pagina di definizione delle stanze. Permette di aggiungere, modificare ed eliminare
        delle entitàologiche chiamate .Stanze. Permette anche di associare/de-associare
        eventuali postazioni giàedefinite. Consente di definire il piano di appartenenza della
        stanza. -->
	Page contains for every building the page of definition of the rooms.<br> 
	It allows add, modify and eliminate logical entity calls room.<br>
	It allows also associate/de-associate eventually predefined emplacements and concurs to define the plan of belongings of the room.
      </p>

      <h3>Configure Nodes </h3>

      <p>
        <!-- Pagina di associazione dei nodi postazioni. Permette di aggiungere, modificare ed
        eliminare l.associazione dei nodi alla stanze. Una volta effettuata l.associazione
        vengono automaticamente associate le informazioni di asset relative alla categoria
        edificio e stanza al nodo relativo. -->
	Page of association of the nodes emplacements.<br>  
	It allows to add, modify and eliminate node association to the rooms.<br>
	Once associate node to room, node's asset information about category building and room
	are automatically filled.
      </p>
    </td>
    
    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />
</body>
</html>
