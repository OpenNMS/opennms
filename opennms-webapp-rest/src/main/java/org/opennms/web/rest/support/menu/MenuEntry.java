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
package org.opennms.web.rest.support.menu;

// Similar to org.opennms.web.navigate.MenuEntry
public class MenuEntry {
    public String id;
    public String className;
    public String name;
    public String url;
    public String locationMatch;
    public String icon;
    /** The icon type, "fa" for font-awesome, "feather" for FeatherDS */
    public String iconType;  // "fa" or "feather"
    /** If true, display an icon only, no name/title. */
    public Boolean isIconOnly;
    public Boolean isVueLink;
    /** Comma-separated list of roles. If present, user must have at least one of these roles to display */
    public String roles;
}
