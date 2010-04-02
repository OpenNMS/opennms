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
// 2003 Oct 27: created
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

<%@page language="java"
	contentType="text/html"
	session="true"
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure SNMP Parameters per polled IP" />
  <jsp:param name="headTitle" value="SNMP Configuration" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Configure SNMP by IP" />
</jsp:include>

<script type="text/javascript">
        function verifyIpAddress (prompt, ipValue) {
                var errorMsg = new String("");

                var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
                var ipArray = ipValue.match(ipPattern); 

                if (ipValue == "0.0.0.0")
                        errorMsg = prompt + ': ' + ipValue + ' is a special IP address and cannot be used here.';
                else if (ipValue == "255.255.255.255")
                        errorMsg = prompt + ': ' + ipValue + ' is a special IP address and cannot be used here.';
                if (ipArray == null)
                        errorMsg = prompt + ': ' + ipValue + ' is not a valid IP address.';
                else {
                        for (i = 0; i < 4; i++) {
                                thisSegment = ipArray[i];
                                if (thisSegment > 255) {
                                        errorMsg = prompt + ': ' + ipValue + ' is not a valid IP address.';
                                        break;
                                }
                        }
                }
                
                return errorMsg;
        }
    
        function verifyAndSubmit()
        {
                var errorMsg = new String("");
                var ipValue = new String("");

                ipValue = new String(document.snmpConfigForm.firstIPAddress.value);
                errorMsg = verifyIpAddress("First IP Address", ipValue);
                if (errorMsg == ""){
                        ipValue = new String(document.snmpConfigForm.lastIPAddress.value);
                        if (ipValue != ""){
                                errorMsg = verifyIpAddress("Last IP Address", ipValue);
                        }
                }

                if (errorMsg == ""){
                        var communityStringValue = new String(document.snmpConfigForm.communityString.value);
                        if (communityStringValue == "") {
                                errorMsg = "Community String is required";
                        }
                }

                if (errorMsg != ""){
                        alert (errorMsg);
                }
                else{
                        document.snmpConfigForm.action="admin/snmpConfig";
                        document.snmpConfigForm.submit();
                }
        }
    
        function cancel()
        {
                document.snmpConfigForm.action="admin/index.jsp";
                document.snmpConfigForm.submit();
        }
</script>

<form method="post" name="snmpConfigForm">

  <div class="TwoColLAdmin">

      <h3>Please enter an IP or a range of IPs and the read community string below</h3>
  	
      <table>
         <tr>
            <td width="25%">
               First IP Address:
            </td>
            <td width="50%">
               <input size=15 name="firstIPAddress">
            </td>
         </tr>

         <tr>
            <td width="25%">
               Last IP Address:
            </td>
            <td width="50%">
               <input size=15 name="lastIPAddress"> (Optional)
            </td>
          </tr>

          <tr>
             <td width="25%">
                Community String:
             </td>
             <td width="50%">
                <input size=30 name="communityString">
             </td>
          </tr>

         <tr>
            <td width="25%">
               Timeout:
            </td>
            <td width="50%">
               <input size=15 name="timeout"> (Optional)
            </td>
          </tr>

         <tr>
            <td width="25%">
               Version:
            </td>
            <td width="50%">
               <select name="version">
               <option>v1</option>
               <option>v2c</option>
               <option>v3</option>
               </select>
                (Optional)
            </td>
          </tr>

         <tr>
            <td width="25%">
               Retries:
            </td>
            <td width="50%">
               <input size=15 name="retryCount"> (Optional)
            </td>
          </tr>

         <tr>
            <td width="25%">
               Port:
            </td>
            <td width="50%">
               <input size=15 name="port"> (Optional)
            </td>
          </tr>

          <tr>
             <td>
                <input type="submit" value="Submit" onClick="verifyAndSubmit()">
             </td>
             <td>
                <input type="button" value="Cancel" onClick="cancel()">
             </td>
          </tr>
       </table>
  </div>

  <div class="TwoColRAdmin">
      <h3>Updating SNMP Community Names</h3>

      <p>In the boxes on the left, enter in a specific IP address and community string, 
         or a range of IP addresses and a community string, and other SNMP parameters.
      </p>

      <p>OpenNMS will optimize this list, so enter the most generic first (i.e. the largest range) 
         and the specific IP addresses last, because if a range is added that includes a specific IP address, 
         the community name for the specific address will be changed to be that of the range.
      </p>

      <p>For devices that have already been discovered and that have an event stating that 
         data collection has failed because the community name changed, it may be necessary to 
         update the SNMP information on the interface page for that device (by selecting the "Update SNMP"
         link) for these changes to take effect.
      </p>
  </div>

</form>

<jsp:include page="/includes/footer.jsp" flush="false" />
