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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event Severity Legend" />
  <jsp:param name="headTitle" value="Severity Legend" />
  <jsp:param name="headTitle" value="Events" />
  <jsp:param name="quiet" value="true" />
</jsp:include>


<table>
  <tr class="Critical">
    <td class="bright">&nbsp;</td>
    <td class="divider"><b>Critical</b></td>
    <td class="divider">This event means numerous devices on the network are affected by the event. Everyone who can should stop what they are doing and focus on fixing the problem.</td>
  </tr>
  <tr class="Major">
    <td class="bright">&nbsp;</td>
    <td class="divider"><b>Major</b></td>
    <td class="divider">A device is completely down or in danger of going down. Attention needs to be paid to this problem immediately.</td>
  </tr>
  <tr class="Minor">
    <td class="bright">&nbsp;</td>
    <td class="divider"><b>Minor</b></td>
    <td class="divider">A part of a device (a service, and interface, a power supply, etc.) has stopped functioning. The device needs attention.</td>
  </tr>
  <tr class="Warning">
    <td class="bright">&nbsp;</td>
    <td class="divider"><b>Warning</b></td>
    <td class="divider">An event has occurred that may require action. This severity can also be used to indicate a condition that should be noted (logged) but does not require direct action.</td>
  </tr>
  <tr class="Indeterminate">
    <td class="bright">&nbsp;</td>
    <td class="divider"><b>Indeterminate</b></td>
    <td class="divider">No Severity could be associated with this event.</td>
  </tr>
  <tr class="Normal">
    <td class="bright">&nbsp;</td>
    <td class="divider"><b>Normal</b></td>
    <td class="divider">Informational message. No action required.</td>
  </tr>
  <tr class="Cleared">
    <td class="bright">&nbsp;</td>
    <td class="divider"><b>Cleared</b></td><td class="divider">This event indicates that a prior error condition has been corrected and service is restored</td>
  </tr>
</table>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="quiet" value="true" />
</jsp:include>

