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
<style>
.notstyle
{
background-color:transparent;
border:none;
padding:5px;
}
.keytable
{
border-collapse:collapse;
width:105px;
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
<tr>
<td class="notstyle" style="text-align:right;width:100%;vertical-align:middle;height:15px">

<a href="#" onClick="MyWindow=window.open('includes/legendInfo-box.jsp','MyWindow','toolbar=no,location=yes,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,width=700,height=250,left=550'); return false;">Legend</a>

</td>
<!--
<table style="width:100%">
<td class="notstyle" style="text-align:right;width:100%;vertical-align:middle;height:15px" title="This mouseover window works">
Legend
</td>
-->
<td class="notstyle" style="vertical-align:middle">
<table class="keytable">
<tbody>
<tr>

<td style="background: #336600 url(images/keyGradient.png) repeat-x" title="${param.normalCaption == null ? 'NORMAL :  Informational message. No action required.' : param.normalCaption}">	</td>
<td style="background: #999000 url(images/keyGradient.png) repeat-x" title="${param.indetermCaption == null ? 'INDETERMINATE - No severity could be associated.' : param.indetermCaption}">	</td>
<td style="background: #FFCC00 url(images/keyGradient.png) repeat-x" title="${param.warnCaption == null ? 'WARNING - May require action. Should possibly be logged.' : param.warnCaption}">	</td>
<td style="background: #CC0000 url(images/keyGradient.png) repeat-x" title="${param.criticalCaption == null ? 'CRITICAL - Numerous devices are affected, fixing the problem is essential.' : param.criticalCaption}">	</td>
</tr>
</tbody>
</table>
</td>

</tr>

</table>

<!--
<map id="keymap" name="keymap">
	<area shape="rect" coords="16,0,31,15" title="${params.normalCaption == null ? 'NORMAL :  Informational message. No action required.' : params.normalParam}" />
	<area shape="rect" coords="32,0,47,15" title="${params.indetermCaption == null ? 'INDETERMINATE - No severity could be associated.' : params.indetermParam}" />
	<area shape="rect" coords="48,0,63,15" title="${params.warnParam == null ? 'WARNING - May require action. Should possibly be logged.' : params.warnParam}" />
	<area shape="rect" coords="96,0,111,15" title="${params.criticalParam == null ? 'CRITICAL - Numerous devices are affected, fixing the problem is essential.' : params.criticalParam}" />
</map>
-->

