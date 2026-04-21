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

<div class="modal-header">
    <h5 class="modal-title">Help</h5>
    <button type="button" class="close" data-dismiss="modal">&times;</button>
</div>
<div class="modal-body">
    <div>
        <h6>Outstanding and acknowledged alarms</h6>
    </div>
    <div>
      <p>
        Alarms can be <em>acknowledged</em>, or removed from the default
        view of all users, by selecting the alarms' <em>Ack</em> check box
        and clicking the <em>Acknowledge Selected Alarms</em> at the bottom
        of the page. Acknowledging an alarm gives users the ability to take
        personal responsibility for addressing a network or systems-related
        issue. Any alarm that has not been acknowledged is active in the
        default alarms view of all users' browsers and is considered
        outstanding.
      </p>

      <p>
        To view acknowledged alarms, go to the <em>All Alarms</em> (<em>summary</em>
        or <em>details</em>) list and click the minus sign next to the <em>alarm
          is outstanding</em> search constraint.
      </p>

      <p>
        If an alarm has been acknowledged in error, find the alarm and <em>unacknowledge</em>
        it, making it available again to all users' default alarm views.
      </p>

      <p>
        If you have a specific alarm identifier for which you want a
        detailed alarm description, type the identifier into the <em>Get
          details for Alarm ID</em> box and hit <b>[Enter]</b>. You will then go
        to the appropriate details page.
      </p>
    </div>
</div>
