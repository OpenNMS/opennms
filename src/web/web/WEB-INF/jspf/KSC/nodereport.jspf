<!--
 
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
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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

-->

<%!
    public Report buildNodeReport(int node_id)
    {
        Report node_report = new Report();
        String report_title = "Node Report for Node Number " + node_id;
        node_report.setTitle(report_title);
        node_report.setShow_timespan_button(true);
        node_report.setShow_graphtype_button(true);
 
        String query_interface[] = this.model.getQueryableInterfacesForNode(node_id);
        Arrays.sort (query_interface);
        for (int i=0; i < query_interface.length;  i++) {
            Graph graph = new Graph(); 
            graph.setTitle("");
            graph.setNodeId(String.valueOf(node_id));
            graph.setInterfaceId(query_interface[i]);  
            graph.setTimespan("7_day");
            graph.setGraphtype("mib2.bits");
            node_report.addGraph(graph);
        }
        return node_report; 
    }   
%>
