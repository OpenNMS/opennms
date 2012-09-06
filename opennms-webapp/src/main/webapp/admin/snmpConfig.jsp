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
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/ipv6.js'></script>" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn.js'></script>" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn2.js'></script>" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/sprintf.js'></script>" />
</jsp:include>

<script type="text/javascript">
        function verifySnmpConfig()
        {
                var errorMsg = new String("");
                var ipValue = new String("");

                ipValue = new String(document.snmpConfigForm.firstIPAddress.value);

                if (!isValidIPAddress(ipValue)) {
                    errorMsg = ipValue + " is not a valid IP address!";
                }
                if (errorMsg == ""){
                        ipValue = new String(document.snmpConfigForm.lastIPAddress.value);
                        if (ipValue != ""){
                            if (!isValidIPAddress(ipValue)) {
                                errorMsg = ipValue + " is not a valid IP address!";
                            }
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
                        return false;
                }
                else{
                        document.snmpConfigForm.action="admin/snmpConfig";
                        return true;
                }
        }
    
        function cancel()
        {
                document.snmpConfigForm.action="admin/index.jsp";
                document.snmpConfigForm.submit();
        }
</script>

<form method="post" name="snmpConfigForm" onsubmit="return verifySnmpConfig();">

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
               <option selected="true">v2c</option>
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
                <input type="submit" value="Submit">
             </td>
             <td>
                <input type="button" value="Cancel" onclick="cancel()">
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

      <p>For devices that have already been provisioned and that have an event stating that 
         data collection has failed because the community name changed, it may be necessary to 
         update the SNMP information on the interface page for that device (by selecting the "Update SNMP"
         link) for these changes to take effect.
      </p>
  </div>

</form>

<jsp:include page="/includes/footer.jsp" flush="false" />
