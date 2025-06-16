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
package org.opennms.web.utils.filter;

import org.opennms.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CheckboxFilterUtils {

    /**  Constant <code>ARRAY_DELIMITER=","</code> */
    public static final String ARRAY_DELIMITER = ",";

    /** Constant <code>POSITIVE_CHECKBOX_VALUE="ON"</code> */
    public static final String POSITIVE_CHECKBOX_VALUE = "on";

    /**
     * Constant <code>MULTI_CHECKBOX_PATTERN="[a-zA-Z]+-\d+=1"</code>
     * Matches filter strings such as the format "severity-4=1" or "service-1=1" or similar
     */
    private static final Pattern MULTI_CHECKBOX_PATTERN = Pattern.compile("[a-zA-Z]+-\\d+=1");

    /**
     * <p>isCheckboxToggled</p>
     *
     * @param tokenizedFilterString a {@link java.lang.String}[] object representing the type and value tokenized.
     * @return a {@link java.lang.Boolean}[] representing if the option is toggled.
     */
    public static boolean isCheckboxToggled(String[] tokenizedFilterString) {
        return tokenizedFilterString != null &&
                StringUtils.equalsTrimmed(tokenizedFilterString[1], POSITIVE_CHECKBOX_VALUE);
    }

    /**
     * <p>handleCheckboxDuplication</p>
     *
     * @param filterStrings array of filter strings
     * @return an array of filter strings which have checkbox deduplication handled. With multi-select there could be
     * multiple IDs selected so we try to consolidate it into a single filter string
     */
    public static String[] handleCheckboxDuplication(String[] filterStrings) {
        if (filterStrings == null) {
            return filterStrings;
        }
        Map<String, List<String>> selectedCheckboxes = dedupCheckboxSelections(filterStrings);

        if (selectedCheckboxes.isEmpty()) {
            return filterStrings;
        }
        return replaceCheckboxValues(filterStrings, selectedCheckboxes);
    }

    /**
     * <p>dedupCheckboxSelections</p>
     *
     * @param filterStrings array of filter strings
     * @return a Map of Filter type to List of IDs associated with the filter.
     */
    private static Map<String, List<String>> dedupCheckboxSelections(String[] filterStrings) {
        Map<String, List<String>> selectedCheckboxes = new HashMap<>();

        for (String filterString : filterStrings) {

            if (MULTI_CHECKBOX_PATTERN.matcher(filterString).matches()) {

                String[] filterStringHyphenSplit = filterString.split("-");
                String type = filterStringHyphenSplit[0];
                String remainder = filterStringHyphenSplit[1];
                String idStr = remainder.split("=")[0];

                if (selectedCheckboxes.containsKey(type)) {
                    selectedCheckboxes.get(type).add(idStr);
                } else {
                    List<String> checkedIds = new ArrayList<>();
                    checkedIds.add(idStr);
                    selectedCheckboxes.put(type, checkedIds);
                }
            }
        }
        return selectedCheckboxes;
    }

    /**
     * <p>replaceCheckboxValues</p>
     *
     * @param filterStrings      array of filter strings
     * @param selectedCheckboxes map of multiselect filter to IDs
     * @return the new filter string array with consolidated multi-select checkbox values
     */
    private static String[] replaceCheckboxValues(String[] filterStrings, Map<String, List<String>> selectedCheckboxes) {
        List<String> collectedFilterList = Arrays.stream(filterStrings).filter(thisFilterString -> {
            for (String selectedKey : selectedCheckboxes.keySet()) {
                if (thisFilterString.startsWith(selectedKey + "-")) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        for (String selectedKey : selectedCheckboxes.keySet()) {
            List<String> selectedIds = selectedCheckboxes.get(selectedKey);
            String joinedIds = String.join(ARRAY_DELIMITER, selectedIds);
            String checkboxFormattedFilter = String.format("%s=%s", selectedKey, joinedIds);

            collectedFilterList.add(checkboxFormattedFilter);
        }

        return collectedFilterList.toArray(new String[0]);
    }
}
