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
package org.opennms.web.rest.support.menu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

// Similar to org.opennms.web.navigate.MenuEntry
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuEntry {
    /** A MenuEntryTypes value */
    public String type;

    public String id;

    public String name;

    public String url;

    /** If present and true, the link in 'url' is an external link. */
    public Boolean isExternalLink;

    /**
     * An optional MenuEntryActions value which informs the UI to do additional processing.
     * 'link' makes a top-level entry a link to its url; 'logout' wires clicking the item to a logout action.
     */
    public String action;

    /**
     * Can use this to provide an HTML link target for the link.
     * Currently may only support a subset, e.g. '_blank' and '_self'.
     * Default: '_self'.
     */
    public String linkTarget;

    /**
     * An icon specifier, most used for top-level menu items.
     * Should correspond to a Feather icon id, e.g.: 'actions/AccountCircle'
     * Currently only certain icons are actually supported.
     */
    public String icon;

    /** If present, user must have at least one of these roles to display */
    public List<String> roles;

    /*
     * If present, the system properties must exist and have the given value.
     */
    public List<RequiredSystemProperty> requiredSystemProperties = new ArrayList<>();

    public List<MenuEntry> items;

    public void addItem(MenuEntry menuEntry) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(menuEntry);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequiredSystemProperty {
        public String name;
        public String value;
    }

    public static class MenuEntryTypes {
        public static final String TYPE_ITEM = "item";
        public static final String TYPE_SEPARATOR = "separator";
    }

    public static class MenuEntryActions {
        public static final String ACTION_LINK = "link";
        public static final String ACTION_LOGOUT = "logout";
    }
}
