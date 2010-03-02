
<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.*,org.opennms.web.element.*,java.util.*,java.io.*,org.opennms.web.element.NetworkElementFactory" %>
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
    <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
    <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="javascript">

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
    var url ='<%=org.opennms.web.Util.calculateUrlBase( request )%>ExecCommand.map?command='+document.getElementById("command").value;
    var address = document.getElementById("address").value;
    
    url = url+'&address='+address;

    if(document.getElementById("numericOutput").checked){
 	     url = url+'&numericOutput=true';
    }
    url=url+"&ipmiCommand="+document.getElementById("ipmiCommand").value;
    url=url+"&ipmiProtocol="+document.getElementById("ipmiProtocol").value;
    url=url+"&ipmiUser="+document.getElementById("ipmiUser").value;
    url=url+"&ipmiPassword="+document.getElementById("ipmiPassword").value;
    
    window.close();
    window.open(url, 'IPMITool', 'toolbar,width='+self.screen.width-150+' ,height=300, left=0, top=0, scrollbars=1') ;
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

    <input type="hidden" id="command" name="command" value="ipmitool" />

    <tr>
      <td align="left">
        <table >
          <tr>
            <td>&nbsp;</td>
            <td>Ip Address: </td>
	    <td><select id="address" name="address">
	<%
    String ipAddress = null;              
        Interface[] intfs = NetworkElementFactory.getActiveInterfacesOnNode( nodeId );
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
            <td>IPMI User</td>
	    <td><input id="ipmiUser" type="text"  size="10" />
            </td>
            <td align="left"><a style="color: #0000cc; text-decoration: underline; cursor:pointer;" onclick="if(document.getElementById('info').style.display=='none') document.getElementById('info').style.display='block'; else document.getElementById('info').style.display='none'; ">?</a>
            </td>
            <td>&nbsp;</td>            
          </tr>
        <tr>
            <td>&nbsp;</td>
            <td>IPMI Password</td>
	    <td><input id="ipmiPassword" type="password"  size="10" />
            </td>
            <td align="left"><a style="color: #0000cc; text-decoration: underline; cursor:pointer;" onclick="if(document.getElementById('info').style.display=='none') document.getElementById('info').style.display='block'; else document.getElementById('info').style.display='none'; ">?</a>
            </td>
            <td>&nbsp;</td>            
          </tr>
        <tr>
            <td>&nbsp;</td>
            <td>IPMI Command</td>
		<td><select id=ipmiCommand name=ipmiCommand>
			<option value="chassis status">chassis status</option>
			<option value="chassis power status">chassis power status</option>
			<option value="chassis power on">chassis power on</option>
			<option value="chassis power off">chassis power off</option>
			<option value="chassis power cycle">chassis power cycle</option>
			<option value="chassis power reset">chassis power reset</option>
			<option value="chassi power soft">chassis power soft</option>
		</select>
            </td>
            <td align="left"><a style="color: #0000cc; text-decoration: underline; cursor:pointer;" onclick="if(document.getElementById('info').style.display=='none') document.getElementById('info').style.display='block'; else document.getElementById('info').style.display='none'; ">?</a>
            </td>
            <td>&nbsp;</td>            
          </tr>
        <tr>
            <td>&nbsp;</td>
            <td>IPMI Protocol</td>
	    <td>
		<select id=ipmiProtocol name=ipmiProtocol>
			<option value="lanplus">lanplus</option>
			<option value="lan">lan</option>
		</select>
            </td>
            <td align="left"><a style="color: #0000cc; text-decoration: underline; cursor:pointer;" onclick="if(document.getElementById('info').style.display=='none') document.getElementById('info').style.display='block'; else document.getElementById('info').style.display='none'; ">?</a>
            </td>
            <td>&nbsp;</td>            
          </tr>



          <tr id="info" style="display:none">
             <td>&nbsp;</td>
	    <td colspan="3" align="right">Insert an Ip address to force <br> a route passing through it.
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
            	<input type="button" value="Run Command" onclick="doCommand()" />
            </td>
            <td colspan="2">&nbsp;</td>
          </tr>
        </table>
      </td>
  </tr>

</table>


</body>
</html>

