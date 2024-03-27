<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<table class="table table-sm severity" style="margin-bottom: 0px;">
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
