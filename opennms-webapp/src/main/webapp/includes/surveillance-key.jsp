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
