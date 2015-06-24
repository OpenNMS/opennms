/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
<!-- Tooltip  Stuff -->
ttContent = null;
document.onmousemove = updateTT;

<!-- shows the tool tip -->
function showTT(id) {
    ttContent = document.getElementById(id);
    if (ttContent != null) ttContent.style.display = "block";
}

<!-- hides the tool tip -->
function hideTT() {
    if (ttContent != null) ttContent.style.display = "none";
}

<!-- ensures that the tool tip moves with the mouse, but only if the tool tip is visible -->
function updateTT() {
    if (ttContent != null && ttContent.style.display == 'block') {
        x = (event.pageX ? event.pageX : window.event.x) + ttContent.offsetParent.scrollLeft - ttContent.offsetParent.offsetLeft;
        y = (event.pageY ? event.pageY : window.event.y) + ttContent.offsetParent.scrollTop - ttContent.offsetParent.offsetTop;
        ttContent.style.left = (x + 20) + "px";
        ttContent.style.top  = (y + 20) + "px";
    }
}
