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
package org.opennms.features.namecutter;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
public class NameCutter {

    private final static Logger logger = LoggerFactory.getLogger(NameCutter.class);
    private Map<String, String> dictionary = new HashMap<String, String>();

    public String trimByCamelCase(String name, Integer maxLength) {
        String result = "";
        String[] nameParts = StringUtils.splitByCharacterTypeCamelCase(name);
        Integer charsOver = name.length() - maxLength;
        for (int i = 0; i < charsOver; i++) {
            Integer largest = 0;
            Integer index = 0;
            for (int j = 0; j < nameParts.length; j++) {
                if (nameParts[j].length() > largest) {
                    largest = nameParts[j].length();
                    index = j;
                }
            }
            nameParts[index] = StringUtils.chop(nameParts[index]);
        }
        for (String namePart : nameParts) {
            result = result + namePart;
        }
        return result;
    }

    public String trimByDictionary(String name) {
        String result = "";

        String[] nameParts = StringUtils.splitByCharacterTypeCamelCase(name);
        for (int i = 0;
                i < nameParts.length;
                i++) {
            String namePart = nameParts[i];

            for (String word : dictionary.keySet()) {
                if (namePart.equalsIgnoreCase(word)) {
                    logger.debug("dictionary Hit at '{}' result '{}'", name, name.replaceAll(word, dictionary.get(word)));
                    nameParts[i] = dictionary.get(word);
                }
            }
        }
        for (String namePart : nameParts) {
            result = result + namePart;
        }
        return result;
    }

    public Map<String, String> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<String, String> dictionary) {
        this.dictionary = dictionary;
    }
}
