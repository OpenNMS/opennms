<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
<tbody><tr>
<td class="notstyle" style="text-align:right;width:100%;vertical-align:middle;height:15px">
Legend
</td>
<td class="notstyle" style="vertical-align:middle">
<table class="keytable">
<tbody>
<tr>
<td style="background: #999 url(images/keyGradient.png) repeat-x" title="${param.clearedCaption == null ? 'CLEARED' : param.clearedCaption}">	</td>
<td style="background: #336600 url(images/keyGradient.png) repeat-x" title="${param.normalCaption == null ? 'NORMAL :  Informational message. No action required.' : param.normalCaption}">	</td>
<td style="background: #999000 url(images/keyGradient.png) repeat-x" title="${param.indetermCaption == null ? 'INDETERMINATE - No severity could be associated.' : param.indetermCaption}">	</td>
<td style="background: #FFCC00 url(images/keyGradient.png) repeat-x" title="${param.warnCaption == null ? 'WARNING - May require action. Should possibly be logged.' : param.warnCaption}">	</td>
<td style="background: #FF9900 url(images/keyGradient.png) repeat-x" title="${param.minorCaption == null ? 'MINOR - Part of a device (service, interface, power supply etc.) has stopped. Attention required.' : param.minorCaption}">	</td>
<td style="background: #FF3300 url(images/keyGradient.png) repeat-x" title="${param.majorCaption == null ? 'MAJOR - Device completely down or in danger of going down. Immediate attention required.' : param.majorCaption}">	</td>
<td style="background: #CC0000 url(images/keyGradient.png) repeat-x" title="${param.criticalCaption == null ? 'CRITICAL - Numerous devices are affected, fixing the problem is essential.' : param.criticalCaption}">	</td>
</tr>
</tbody>
</table>
</td>
</tr></tbody>
</table>
<!--
<map id="keymap" name="keymap">
	<area shape="rect" coords="0,0,15,15" title="${params.clearedCaption == null ? 'CLEARED' : params.clearedCaption}"/>
	<area shape="rect" coords="16,0,31,15" title="${params.normalCaption == null ? 'NORMAL :  Informational message. No action required.' : params.normalParam}" />
	<area shape="rect" coords="32,0,47,15" title="${params.indetermCaption == null ? 'INDETERMINATE - No severity could be associated.' : params.indetermParam}" />
	<area shape="rect" coords="48,0,63,15" title="${params.warnParam == null ? 'WARNING - May require action. Should possibly be logged.' : params.warnParam}" />
	<area shape="rect" coords="64,0,79,15" title="${params.minorParam == null ? 'MINOR - Part of a device (service, interface, power supply etc.) has stopped. Attention required.' : params.minorParam}" />
	<area shape="rect" coords="80,0,95,15" title="${params.majorParam == null ? 'MAJOR - Device completely down or in danger of going down. Immediate attention required.' : params.majorParam}" />
	<area shape="rect" coords="96,0,111,15" title="${params.criticalParam == null ? 'CRITICAL - Numerous devices are affected, fixing the problem is essential.' : params.criticalParam}" />
</map>
-->

