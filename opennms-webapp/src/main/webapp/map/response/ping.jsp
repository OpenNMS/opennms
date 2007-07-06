<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 28: Corrected issue with day/week/month/year reports and some browsers.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 01: Added day/week/month/year reports.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 12: Added response time reports to webUI. Based on original
//              performance reports.
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

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.*,org.opennms.web.element.*,java.util.*,java.io.*,org.opennms.web.element.NetworkElementFactory" %>

<%

        
    //required parameter node
    String nodeIdString = request.getParameter( "node" );
    if(nodeIdString == null) {
        throw new MissingParameterException( "node", new String[] {"report", "node", "intf"} );
    }
    int nodeId = Integer.parseInt(nodeIdString);
    
    //required parameter intf
    String intf = request.getParameter( "intf" );


%>

<html>
<head>
    <title>Ping | OpenNMS Web Console</title>
    <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
    <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="javascript">

function checkIpAddress(ip){
	var ipArr = ip.split(".");
	if(ipArr.length!=4)
		return false;
	if(ipArr[0]=="" || ipArr[1]=="" || ipArr[2]=="" || ipArr[3]=="")
		return false;
	if(isNaN(ipArr[0]) || isNaN(ipArr[1]) || isNaN(ipArr[2]) || isNaN(ipArr[3]) || 
		ipArr[0]<0 || ipArr[0]>255 || ipArr[1]<0 || ipArr[1]>255 || ipArr[2]<0 || ipArr[2]>255 || ipArr[3]<0 || ipArr[3]>255)
		return false;
	return true;
}


function doCommand(){
     var command = document.getElementById("command").value;
     var address = document.getElementById("address").value;
     
     if(!checkIpAddress(document.getElementById("address").value)){
             	alert("Invalid IP address");
             	document.getElementById("address").focus();
             	return;
        }
     
     var timeOut = document.getElementById("timeOut").value;
     if(isNaN(timeOut)){
     	alert("Invalid timeout");
     	document.getElementById("timeOut").focus();
     	return;
     }
     if(timeOut==""){
     	timeOut="1";
     }
     var numberOfRequest = document.getElementById("numberOfRequest").value;
     if(numberOfRequest=="" || isNaN(numberOfRequest)){
     	alert("Invalid request number");
     	document.getElementById("numberOfRequest").focus();
     	return;
     }     
     if(numberOfRequest==""){
     	numberOfRequest="10";
     } 
     
     var packetSize = document.getElementById("packetSize").value;
     if(isNaN(packetSize)){
     	alert("Invalid packet size");
     	document.getElementById("packetSize").focus();
     	return;
     }else{
     	packetSize=packetSize-8;
     }
     if(packetSize==""){
     	packetSize="56";
     }
     
     
     if(document.getElementById("numericOutput").checked){
     	command=command+ " -n ";
     }
     
     command=command+" -c "+numberOfRequest+ " -i "+timeOut+ " -s "+packetSize;
     
     window.close();
     window.open('<%=org.opennms.web.Util.calculateUrlBase( request )%>ExecCommand.map?command='+command+'&address='+address, 'AddSpecific', 'toolbar,width='+self.screen.width-150+' ,height=300, left=0, top=0, scrollbars=1') ;
}
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<br/>

<table width="100%" cellpadding="0" cellspacing="0" border="0">
  <tr>
    <td align="left">
      <table>
        <tr>
          <td>&nbsp;</td>
          <td align="left">
            <h3>
              Node: <%=NetworkElementFactory.getNodeLabel(nodeId)%><br/>
              <% if(intf != null ) { %>
                Interface: <%=intf%>
              <% } %>
            </h3>
          </td>
          <td>&nbsp;</td>
        </tr>
      </table>
    </td>
  </tr>

  <tr>
    <td height="20">&nbsp;</td>
  </tr>



    <input type="hidden" id="command" name="command" value="ping" />

    <tr>
      <td align="left">
        <table >
          <tr>
            <td>&nbsp;</td>
	    <td>Ip Address: </td>
            <td>
              <%
	      String ipAddress = null;              
              if( intf != null ){
              	    ipAddress = intf;
              }else{
              %>
                    <% 
                    Interface[] intfs = NetworkElementFactory.getActiveInterfacesOnNode( nodeId );
                    for( int i=0; i < intfs.length; i++ ) { 
                    	if(intfs[i]!=null){
                    	      ipAddress = intfs[i].getIpAddress();
                    	      if(ipAddress.equals("0.0.0.0"))
                    	      	continue;
                    	      else
			      	break;
			}
		    }
		    if(ipAddress==null){
		    	ipAddress="";
		    }
              }%>
              <input type="text" size="10" id="address" name="address" value="<%=ipAddress%>" />
            </td>  
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left">
		Number of request: 
	    </td>
            <td><input id="numberOfRequest" type="text" size="2" value="10" />
            </td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left">
		Time-out (sec.):
 	    </td>
            <td>
            	<input id="timeOut" type="text" size="2" value="1" />
            </td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left">
		Packet size (byte):
 	    </td>
            <td>
            	<input id="packetSize" type="text" size="2" value="64" />
            </td>
            <td>&nbsp;</td>
          </tr>  
          <tr>
            <td>&nbsp;</td>
            <td align="left">
		Numeric output:
 	    </td>
            <td>
            	<input id="numericOutput" type="checkbox" />
            </td>
            <td>&nbsp;</td>
          </tr>          
          <tr>
            <td colspan="4">&nbsp;</td>            
          </tr>          
          <tr>
            <td colspan="2" >&nbsp;</td>          
            <td align="right">
            	<input type="button" value="Ping" onclick="doCommand()" />
            </td>
            <td>&nbsp;</td>
          </tr>
        </table>
      </td>
  </tr>

</table>


</body>
</html>

