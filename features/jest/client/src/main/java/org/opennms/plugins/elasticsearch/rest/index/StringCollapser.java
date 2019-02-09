/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.index;

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

