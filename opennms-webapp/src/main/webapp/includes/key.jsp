<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

--%>

<p class="key"><img src="images/key.png" alt="openNMS status colour key" usemap="#keymap" />Legend</p>
<map id="keymap" name="keymap">
	<area shape="rect" coords="0,0,15,15" title="CLEARED" />
	<area shape="rect" coords="16,0,31,15" title="NORMAL - Informational message. No action required." />
	<area shape="rect" coords="32,0,47,15" title="INDETERMINATE - No severity could be associated." />
	<area shape="rect" coords="48,0,63,15" title="WARNING - May require action. Should possibly be logged." />
	<area shape="rect" coords="64,0,79,15" title="MINOR - Part of a device (service, interface, power supply etc.) has stopped. Attention required." />
	<area shape="rect" coords="80,0,95,15" title="MAJOR - Device completely down or in danger of going down. Immediate attention required." />
	<area shape="rect" coords="96,0,111,15" title="CRITICAL - Numerous devices are affected, fixing the problem is essential." />
</map>
