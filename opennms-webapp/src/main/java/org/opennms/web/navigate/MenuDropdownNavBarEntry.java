/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.navigate;

public class MenuDropdownNavBarEntry extends LocationBasedNavBarEntry {
    private String m_contents = null;

    /**
     * <p>getDisplayString</p>
     *
     * @return The text containing the menu entry/entries.
     */
    public String getDisplayString() {
        if (getName() == null || m_contents == null) return "";
        final StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"nav-dropdown\">");
        sb.append("<a href=\"");
        if (getUrl() == null) {
            sb.append("#");
        } else {
            sb.append(getUrl());
        }
        sb.append("\" class=\"nav-dropdown\">");
        sb.append(getName());
        sb.append(" ");
        sb.append("<span class=\"nav-item\">\u25BC</span>");
        sb.append("</a>");
        sb.append("<ul>");
        sb.append(m_contents);
        sb.append("</ul>");
        sb.append("</div>");

        return sb.toString();
    }

    /**
     * If there are any {@link NavBarEntry} objects in this
     * dropdown object, return DISPLAY_NO_LINK (since the
     * individual entries will handle their own)
     */
    public DisplayStatus evaluate(final MenuContext context) {
        boolean display = false;
        if (hasEntries()) {
            final StringBuilder sb = new StringBuilder();
            for (final NavBarEntry entry : getEntries()) {
                final DisplayStatus status = entry.evaluate(context);
                switch (status) {
                case DISPLAY_LINK:
                    sb.append("<li><a href=\"" + entry.getUrl() + "\">" + entry.getName() + "</a></li>");
                    display = true;
                    break;
                case DISPLAY_NO_LINK:
                    sb.append("<li>" + entry.getName() + "</li>");
                    display = true;
                    break;
                default:
                    break;
                }
            }
            m_contents = sb.toString();
        }
        return display? DisplayStatus.DISPLAY_NO_LINK : DisplayStatus.NO_DISPLAY;
    }
}
