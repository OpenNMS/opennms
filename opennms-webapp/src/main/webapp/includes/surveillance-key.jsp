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
<style type="text/css">
.notstyle
{
background-color:transparent;
border:none;
padding:5px;
}
.keytable
{
border-collapse:collapse;
width:45px;
height:15px;
margin:0px 
}
.keytable TBODY TR TD {
margin:0px;
padding:0px;
width:12px;
height:12px;
border:1px solid #666666  
}
</style>

<!--
<img src="images/key.png" alt="openNMS status colour key" usemap="#keymap" />
-->
<table style="width:100%">
<tbody><tr>
<td class="notstyle" style="text-align:right;width:100%;vertical-align:middle;height:15px">
Legend
</td>
<td class="notstyle" style="vertical-align:middle">
<table class="keytable">
<tbody>
<tr>
<td style="background: #336600 url(images/keyGradient.png) repeat-x" title="${param.normalCaption == null ? 'NORMAL: No service outages in category.' : param.normalCaption}"></td>
<td style="background: #FFCC00 url(images/keyGradient.png) repeat-x" title="${param.warnCaption == null ? 'WARNING: 1 service outage in category.' : param.warnCaption}"></td>
<td style="background: #FF3300 url(images/keyGradient.png) repeat-x" title="${param.criticalCaption == null ? 'CRITICAL: 2 or more service outages in category' : param.criticalCaption}"></td>
</tr>
</tbody>
</table>
</td>
</tr></tbody>
</table>
