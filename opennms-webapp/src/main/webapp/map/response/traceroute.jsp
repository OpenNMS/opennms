<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.api.Util, org.opennms.core.utils.WebSecurityUtils, org.opennms.web.servlet.*,
org.opennms.web.element.*,
org.opennms.web.element.NetworkElementFactory" %>
<%

        
    //required parameter node
    String nodeIdString = request.getParameter( "node" );
    if(nodeIdString == null) {
        throw new MissingParameterException( "node", new String[] {"report", "node", "intf"} );
    }
    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);

%>

<html>
<head>
    <title>Trace Route | OpenNMS Web Console</title>
    <base HREF="<%=org.opennms.web.api.Util.calculateUrlBase( request )%>" />
    <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script type="text/javascript">

function checkIpAddress(ip){
	var ipArr = ip.split(".");
	if(ipArr.length!=4)
		return false;
	if(isNaN(ipArr[0]) || isNaN(ipArr[1]) || isNaN(ipArr[2]) || isNaN(ipArr[3]) || 
		ipArr[0]<0 || ipArr[0]>255 || ipArr[1]<0 || ipArr[1]>255 || ipArr[2]<0 || ipArr[2]>255 || ipArr[3]<0 || ipArr[3]>255)
		return false;
	return true;
}


function doCommand(){
    var url ='<%= Util.calculateUrlBase( request, "ExecCommand.map?command=" ) %>'+document.getElementById("command").value;
    var address = document.getElementById("address").value;
    
    url = url+'&address='+address;

    if(document.getElementById("numericOutput").checked){
 	     url = url+'&numericOutput=true';
    }
    if(document.getElementById("hopAddress").value!=""){
     if(!checkIpAddress(document.getElementById("hopAddress").value)){
		alert("Invalid Hop IP address");
		document.getElementById("hopAddress").focus();
		return;
	}
     url=url+"&hopAddress="+document.getElementById("hopAddress").value;
    }
    
    window.close();
    window.open(url, 'TraceRoute', 'toolbar,width='+self.screen.width-150+' ,height=300, left=0, top=0, scrollbars=1') ;
}
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<br/>

<table width="100%">
  <tr>
    <td align="left">
      <table>
        <tr>
          <td>&nbsp;</td>
          <td align="left">
            <h3>
              Node: <%= NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(nodeId) %><br/>
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

    <input type="hidden" id="command" name="command" value="traceroute" />

    <tr>
      <td align="left">
        <table >
          <tr>
            <td>&nbsp;</td>
            <td>IP Address: </td>
	    <td><select id="address" name="address">
	<%
    String ipAddress = null;              
        Interface[] intfs = NetworkElementFactory.getInstance(getServletContext()).getActiveInterfacesOnNode( nodeId );
        for( int i=0; i < intfs.length; i++ ) { 
          	if(intfs[i]!=null){
			    ipAddress = intfs[i].getIpAddress();
				if(ipAddress.equals("0.0.0.0") || !intfs[i].isManaged())
					continue;
	      		else
	%>
	 	<option value="<%=ipAddress%>"><%=ipAddress%></option>
    <%
 			}                     	
 		}
 		    
    %>
            </select>
        </td>  
        <td colspan="2">&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>Forced hop IP:</td>
	    <td><input id="hopAddress" type="text"  size="10" />
            </td>
            <td align="left"><a style="color: #0000cc; text-decoration: underline; cursor:pointer;" onclick="if(document.getElementById('info').style.display=='none') document.getElementById('info').style.display='block'; else document.getElementById('info').style.display='none'; ">?</a>
            </td>
            <td>&nbsp;</td>            
          </tr>
          <tr id="info" style="display:none">
             <td>&nbsp;</td>
	    <td colspan="3" align="right">Insert an IP address to force <br/> a route passing through it.
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
            <td colspan="2">&nbsp;</td>
          </tr>	  
          <tr>
            <td colspan="5">&nbsp;</td>            
          </tr>
          <tr>
            <td colspan="2">&nbsp;</td>
            <td>
            	<input type="button" value="Traceroute" onclick="doCommand()" />
            </td>
            <td colspan="2">&nbsp;</td>
          </tr>
        </table>
      </td>
  </tr>

</table>


</body>
</html>

