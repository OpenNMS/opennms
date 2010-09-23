<%

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
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
// 2002 Sep 24: Added a "select" option for SNMP data and a config page.
// 2002 Sep 19: Added a "delete nodes" page to the webUI.
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

%>

<%@page language="java" contentType="text/html" session="true" import=" org.opennms.netmgt.config.discovery.*, org.opennms.web.admin.discovery.ActionDiscoveryServlet,org.opennms.protocols.snmp.SnmpPeer" %>
<% 
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-cache");
	}
%>
<% String breadcrumb1 = "<a href='admin/index.jsp'> Admin </a>"; %>
<% String breadcrumb2 = "<a href='admin/discovery/index.jsp'> Discovery </a>"; %>
<% String breadcrumb3 = "Modify Configuration"; %>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify Discovery Configuration" />
  <jsp:param name="headTitle" value="Discovery" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<script type="text/javascript">




function addSpecific(){
	window.open('<%=org.opennms.web.Util.calculateUrlBase( request )%>admin/discovery/add-specific.jsp', 'AddSpecific', 'toolbar=0,width=700 ,height=150, left=0, top=0, resizable=1, scrollbars=1') 
}

function addIncludeRange(){
	window.open('<%=org.opennms.web.Util.calculateUrlBase( request )%>admin/discovery/add-ir.jsp', 'AddIncludeRange', 'toolbar=0,width=750 ,height=230, left=0, top=0, resizable=1, scrollbars=1') 
}

function addIncludeUrl(){
	window.open('<%=org.opennms.web.Util.calculateUrlBase( request )%>admin/discovery/add-url.jsp', 'AddIncludeUrl', 'toolbar=0,width=750 ,height=150, left=0, top=0, resizable=1, scrollbars=1') 
}

function addExcludeRange(){
	window.open('<%=org.opennms.web.Util.calculateUrlBase( request )%>admin/discovery/add-er.jsp', 'AddExcludeRange', 'toolbar=0,width=600 ,height=200, left=0, top=0, resizable=1, scrollbars=1') 
}


function deleteSpecific(i){
      if(confirm("Are you sure you want to delete the 'Specific'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeSpecificAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteIR(i){
      if(confirm("Are you sure you want to delete the 'Include Range'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeIncludeRangeAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteIncludeUrl(i){
    if(confirm("Are you sure you want to delete the 'Include URL'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeIncludeUrlAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function deleteER(i){
      if(confirm("Are you sure you want to delete the 'Exclude Range'?")){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.removeExcludeRangeAction%>&index="+i;
	document.modifyDiscoveryConfig.submit();
	}
}

function restartDiscovery(){
	document.modifyDiscoveryConfig.action=document.modifyDiscoveryConfig.action+"?action=<%=ActionDiscoveryServlet.saveAndRestartAction%>";
	return true;
}


</script>

<%
HttpSession sess = request.getSession(false);
DiscoveryConfiguration currConfig  = (DiscoveryConfiguration) sess.getAttribute("discoveryConfiguration");
%>
<!-- Body -->


<br/>

<form method="post" id="modifyDiscoveryConfig" name="modifyDiscoveryConfig" action="<%=org.opennms.web.Util.calculateUrlBase( request )%>admin/discovery/actionDiscovery" onsubmit="return restartDiscovery();">
<input type="hidden" id="specificipaddress" name="specificipaddress" value=""/>
<input type="hidden" id="specifictimeout" name="specifictimeout" value=""/>
<input type="hidden" id="specificretries" name="specificretries" value=""/>

<input type="hidden" id="iuurl" name="iuurl" value=""/>
<input type="hidden" id="iutimeout" name="iutimeout" value=""/>
<input type="hidden" id="iuretries" name="iuretries" value=""/>

<input type="hidden" id="irbase" name="irbase" value=""/>
<input type="hidden" id="irend" name="irend" value=""/>
<input type="hidden" id="irtimeout" name="irtimeout" value=""/>
<input type="hidden" id="irretries" name="irretries" value=""/>

<input type="hidden" id="specificipaddress" name="specificipaddress" value=""/>
<input type="hidden" id="specifictimeout" name="specifictimeout" value=""/>
<input type="hidden" id="specificretries" name="specificretries" value=""/>

<input type="hidden" id="erbegin" name="erbegin" value=""/>
<input type="hidden" id="erend" name="erend" value=""/>


      <input type="submit" value="Save and Restart Discovery"/>

<h3>General settings</h3>
			    
			    <table class="standard" width="100%">
				 <tr align="left">
			     
					  <td class="standard" align="center" width="25%">Initial sleep time (sec.): 
					  	<select id="initialsleeptime" name="initialsleeptime">
					  		<option value="30000" <%if(currConfig.getInitialSleepTime()==30000) out.print("selected");%>>30</option>
					  		<option value="60000" <%if(currConfig.getInitialSleepTime()==60000) out.print("selected");%>>60</option>
					  		<option value="90000" <%if(currConfig.getInitialSleepTime()==90000) out.print("selected");%>>90</option>
					  		<option value="120000" <%if(currConfig.getInitialSleepTime()==120000) out.print("selected");%>>120</option>
					  		<option value="150000" <%if(currConfig.getInitialSleepTime()==150000) out.print("selected");%>>150</option>
					  		<option value="300000" <%if(currConfig.getInitialSleepTime()==300000) out.print("selected");%>>300</option>
					  		<option value="600000" <%if(currConfig.getInitialSleepTime()==600000) out.print("selected");%>>600</option>
					  	</select>
					  </td>
					  <td class="standard" align="center" width="25%">Restart sleep time (hours): 
					  	<select id="restartsleeptime" name="restartsleeptime">
					  		<option value="3600000" <%if(currConfig.getRestartSleepTime()==3600000) out.print("selected");%>>1</option>
					  		<option value="7200000" <%if(currConfig.getRestartSleepTime()==7200000) out.print("selected");%>>2</option>
					  		<option value="10800000" <%if(currConfig.getRestartSleepTime()==10800000) out.print("selected");%>>3</option>
					  		<option value="14400000" <%if(currConfig.getRestartSleepTime()==14400000) out.print("selected");%>>4</option>
					  		<option value="18000000" <%if(currConfig.getRestartSleepTime()==18000000) out.print("selected");%>>5</option>
					  		<option value="21600000" <%if(currConfig.getRestartSleepTime()==21600000) out.print("selected");%>>6</option>
					  		<option value="43200000" <%if(currConfig.getRestartSleepTime()==43200000) out.print("selected");%>>12</option>
					  		<option value="86400000" <%if(currConfig.getRestartSleepTime()==86400000) out.print("selected");%>>24</option>
					  		<option value="129600000" <%if(currConfig.getRestartSleepTime()==129600000) out.print("selected");%>>36</option>
					  		<option value="259200000" <%if(currConfig.getRestartSleepTime()==259200000) out.print("selected");%>>72</option>
					  	</select>
					  </td>
					  <td  class="standard" align="center" width="17%">Threads: 
					  	<select id="threads" name="threads">
					  		<option value="1" <%if(currConfig.getThreads()==1) out.print("selected");%>>1</option>
					  		<option value="2" <%if(currConfig.getThreads()==2) out.print("selected");%>>2</option>
					  		<option value="3" <%if(currConfig.getThreads()==3) out.print("selected");%>>3</option>
					  		<option value="4" <%if(currConfig.getThreads()==4) out.print("selected");%>>4</option>
					  		<option value="5" <%if(currConfig.getThreads()==5) out.print("selected");%>>5</option>
					  		<option value="6" <%if(currConfig.getThreads()==6) out.print("selected");%>>6</option>
					  		<option value="7" <%if(currConfig.getThreads()==7) out.print("selected");%>>7</option>
					  		<option value="8" <%if(currConfig.getThreads()==8) out.print("selected");%>>8</option>
					  		<option value="9" <%if(currConfig.getThreads()==9) out.print("selected");%>>9</option>
					  		<option value="10" <%if(currConfig.getThreads()==10) out.print("selected");%>>10</option>
					  		<option value="15" <%if(currConfig.getThreads()==15) out.print("selected");%>>15</option>
					  	</select>
					  </td>
					  <td class="standard" align="center" width="17%">Retries: <input type="text" id="retries" name="retries" size="2" value="<%=((currConfig.getRetries()==0)?"3":""+currConfig.getRetries())%>"/></td>
					  <td class="standard" align="center" width="17%">Timeout (ms.): <input type="text" id="timeout" name="timeout" size="4" value="<%=((currConfig.getTimeout()==0)?"800":""+currConfig.getTimeout())%>"/></td>
				</tr>
				
			    </table>
			    
			<h3>Specifics</h3>
		    <table class="standard">
	    	<tr>
	    	  <td class="standard" valign="top" width="1%">
			    <input type="button" value="Add New" onclick="addSpecific();"/>
			  </td>
			  <td class="standard">
			    <%if(currConfig.getSpecificCount()>0){
				    Specific[] specs = currConfig.getSpecific();
			    %>
				    <table class="standard">
				      <tr>
					<td class="standardheaderplain">
					    <b>IP Address</b>
					</td> 
					<td class="standardheaderplain">
					    <b>Timeout (ms.)</b>
					</td>	
					<td class="standardheaderplain">
					    <b>Retries</b>
					</td>			
					<td class="standardheaderplain">
					    <b>Action</b>
					</td>
				      </tr>
				      <%for(int i=0; i<specs.length; i++){%>
					 <tr>
					  <td class="standard"  align="center"><%=specs[i].getContent()%></td>
					  <td class="standard" align="center"><%=(specs[i].getTimeout()!=0)?""+specs[i].getTimeout():""+currConfig.getTimeout() %></td>
					  <td class="standard" align="center"><%=(specs[i].getRetries()!=0)?""+specs[i].getRetries():""+currConfig.getRetries() %></td>
					  <td class="standard" width="1%" align="center"><input type="button" value="Delete" onclick="deleteSpecific(<%=i%>);"/></td> 
					</tr>		      	
				      <%} // end for%>

				     </table>
			  <%}else{ // end if currConfig.getSpecificsCount()>0  
			  	 out.print("No Specifics found.");
			  	}
			  %>
			     </td>
		  	   </tr>
		       </table>			  

			<h3>Include URLs</h3>
		    <table class="standard">
	    	<tr>
	    	  <td class="standard" valign="top" width="1%">
			    <input type="button" value="Add New" onclick="addIncludeUrl();"/>
			  </td>
			  <td class="standard">
			    <%if(currConfig.getIncludeUrlCount()>0){
			        IncludeUrl[] urls = currConfig.getIncludeUrl();
			    %>
				    <table class="standard">
				      <tr>
					<td class="standardheaderplain">
					    <b>URL</b>
					</td> 
					<td class="standardheaderplain">
					    <b>Timeout (ms.)</b>
					</td>	
					<td class="standardheaderplain">
					    <b>Retries</b>
					</td>			
					<td class="standardheaderplain">
					    <b>Action</b>
					</td>
				      </tr>
				      <%for(int i=0; i<urls.length; i++){%>
					 <tr>
					  <td class="standard"  align="center"><%=urls[i].getContent()%></td>
					  <td class="standard" align="center"><%=(urls[i].getTimeout()!=0)?""+urls[i].getTimeout():""+currConfig.getTimeout() %></td>
					  <td class="standard" align="center"><%=(urls[i].getRetries()!=0)?""+urls[i].getRetries():""+currConfig.getRetries() %></td>
					  <td class="standard" width="1%" align="center"><input type="button" value="Delete" onclick="deleteIncludeUrl(<%=i%>);"/></td> 
					</tr>		      	
				      <%} // end for%>

				     </table>
			  <%}else{ // end if currConfig.getIncludeUrlCount()>0  
			  	 out.print("No Include URLs found.");
			  	}
			  %>
			     </td>
		  	   </tr>
		       </table>			  

		<h3>Include Ranges</h3>
		    <table class="standard">
		    	<tr>
		    	  <td width="1%" class="standard" valign="top">		    
				    <input type="button" value="Add New" onclick="addIncludeRange();"/>
			  </td>
			  <td>
				    <%if(currConfig.getIncludeRangeCount()>0){
					    IncludeRange[] irange = currConfig.getIncludeRange();
				    %>
					    <table  class="standard">
					      <tr>
						<td class="standardheaderplan">
						    <b>Begin Address</b>
						</td> 
						<td class="standardheaderplan">
						    <b>End Address</b>
						</td> 			
						<td class="standardheaderplan">
						    <b>Timeout (ms.)</b>
						</td>	
						<td class="standardheaderplan">
						    <b>Retries</b>
						</td>			
						<td class="standardheaderplan">
						    <b>Action</b>
						</td>						

					      </tr>
					      <%for(int i=0; i<irange.length; i++){

					      %>
						 <tr>
						  <td class="standard" align="center"><%=irange[i].getBegin()%></td>
						  <td class="standard" align="center"><%=irange[i].getEnd()%></td>
						  <td class="standard" align="center"><%=(irange[i].getTimeout()!=0)?""+irange[i].getTimeout():""+currConfig.getTimeout() %></td>
						  <td class="standard" align="center"><%=(irange[i].getRetries()!=0)?""+irange[i].getRetries():""+currConfig.getRetries() %></td>
						  <td class="standard" width="1%" align="center"><input type="button" value="Delete" onclick="deleteIR(<%=i%>);"/></td> 						  
						</tr>
					      <%} // end for%>

					     </table>
				  <%}else{ // end if currConfig.getIncludeRangeCount()>0  
			  		  out.print("No include range defined.");
			    	    }%>
			     </td>
		  	   </tr>
		       </table>		          
     
		<h3>Exclude Ranges</h3>
		    <table class="standard">
		    	<tr>
		    	  <td width="1%" class="standard" valign="top">
				    <input type="button" value="Add New" onclick="addExcludeRange();"/>
			  </td>
			  <td>		    
			    <%if(currConfig.getExcludeRangeCount()>0){
				    ExcludeRange[] irange = currConfig.getExcludeRange();
			    %>
				    <table class="standard">
				      <tr bgcolor="#999999">
					<td class="standardheaderplan">
					    <b>Begin</b>
					</td> 
					<td class="standardheaderplan">
					    <b>End</b>
					</td> 			
					<td class="standardheaderplan">
					    <b>Action</b>
					</td>
				      </tr>
				      <%for(int i=0; i<irange.length; i++){

				      %>
					 <tr>
					  <td class="standard" align="center"><%=irange[i].getBegin()%></td>
					  <td class="standard" align="center"><%=irange[i].getEnd()%></td>
					  <td class="standard" width="1%" align="center"><input type="button" value="Delete" onclick="deleteER(<%=i%>);"/></td> 
					</tr>		      	
				      <%} // end for%>

				     </table>
			  <%}else{ // end if currConfig.getExcludeRangeCount()>0  
			  	  out.print("No exclude range defined.");
			    }%>
			  </td>
		  	</tr>
		    </table>
			
			<input type="submit" value="Save and Restart Discovery"/>
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />
