<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

<table class="table table-condensed severity" style="margin-bottom: 0px;">
  <tr class="severity-critical">
    <td class="bright"><strong>Critical</strong></td>
    <td>This alarm means numerous devices on the network are affected by the alarm. Everyone who can should stop what they are doing and focus on fixing the problem.</td>
  </tr>
  <tr class="severity-major">
    <td class="bright"><strong>Major</strong></td>
    <td>A device is completely down or in danger of going down. Attention needs to be paid to this problem immediately.</td>
  </tr>
  <tr class="severity-minor">
    <td class="bright"><strong>Minor</strong></td>
    <td>A part of a device (a service, and interface, a power supply, etc.) has stopped functioning. The device needs attention.</td>
  </tr>
  <tr class="severity-warning">
    <td class="bright"><strong>Warning</strong></td>
    <td>An alarm has occurred that may require action. This severity can also be used to indicate a condition that should be noted (logged) but does not require direct action.</td>
  </tr>
  <tr class="severity-indeterminate">
    <td class="bright"><strong>Indeterminate</strong></td>
    <td>No Severity could be associated with this alarm.</td>
  </tr>
  <tr class="severity-normal">
    <td class="bright"><strong>Normal</strong></td>
    <td>Informational message. No action required.</td>
  </tr>
  <tr class="severity-cleared">
    <td class="bright"><strong>Cleared</strong></td>
    <td>This alarm indicates that a prior error condition has been corrected and service is restored</td>
  </tr>
</table>
