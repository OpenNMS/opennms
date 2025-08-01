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
package org.opennms.features.jest.client.index;

import java.util.ArrayList;
import java.util.List;

public class StringCollapser {

    private List<String> list;
    private int collapseAfter;
    private boolean doCollapsingAtBeginning = true;
    private boolean doCollapsingAtEnd = true;
    private String wildcard = "*";

    private StringCollapser(List<String> list) {
        this.list = list;
    }

    public static StringCollapser forList(List<String> list) {
        return new StringCollapser(list);
    }

    public StringCollapser doCollapsingAtBeginning(boolean doCollapsingAtBeginning) {
        this.doCollapsingAtBeginning = doCollapsingAtBeginning;
        return this;
    }

    public StringCollapser doCollapsingAtEnd(boolean doCollapsingAtEnd) {
        this.doCollapsingAtEnd = doCollapsingAtEnd;
        return this;
    }

    public StringCollapser collapseAfterChars(int collapseAfter) {
        this.collapseAfter = collapseAfter;
        return this;
    }

    public StringCollapser replaceCollapsedCharsWith(String wildcard) {
        this.wildcard = wildcard;
        return this;
    }

    public List<String> collapse() {

        if(this.list == null || this.list.size() == 0){
            // shortcut
            return this.list;
        }

        String currentStem = null;
        String firstStem = list.get(0).substring(0, this.collapseAfter);
        String lastStem = list.get(list.size()-1).substring(0, this.collapseAfter);
        List<String> result = new ArrayList<>();
        boolean doCollapsing;
        boolean currentStemAdded = false;

        for (String element : this.list) {

            // 1.) determine current stem
            if(!element.substring(0, this.collapseAfter).equals(currentStem)) {
                currentStem = element.substring(0, this.collapseAfter);
                currentStemAdded = false;
            }

            // 2.) determine if we need to collapse
            doCollapsing = true;
            if(firstStem.equals(currentStem) && !this.doCollapsingAtBeginning){
                doCollapsing = false;
            }
            if(!lastStem.equals(firstStem) && lastStem.equals(currentStem) && !this.doCollapsingAtEnd){
                doCollapsing = false;
            }

            // 3.) collapse or add
            if (doCollapsing && !currentStemAdded) {
                result.add(currentStem + this.wildcard);
                currentStemAdded = true;
            } else if (doCollapsing) {
                // ignore element we have added wildcard already
            } else {
                result.add(element);
                currentStemAdded = false;
            }

        }
        return result;
    }
}

