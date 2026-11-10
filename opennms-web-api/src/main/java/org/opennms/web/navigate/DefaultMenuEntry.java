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

import java.util.ArrayList;
import java.util.List;

public class DefaultMenuEntry implements MenuEntry {

    private final String name;
    private final String url;
    private final DisplayStatus displayStatus;
    private final List<MenuEntry> entries = new ArrayList<>();

    public DefaultMenuEntry(String name, String url, DisplayStatus displayStatus) {
        this.name = name;
        this.url = url;
        this.displayStatus = displayStatus;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public List<MenuEntry> getEntries() {
        return entries;
    }

    @Override
    public DisplayStatus getDisplayStatus() {
        return displayStatus;
    }

    public void addEntries(List<MenuEntry> entries) {
        this.entries.addAll(entries);
    }
}
