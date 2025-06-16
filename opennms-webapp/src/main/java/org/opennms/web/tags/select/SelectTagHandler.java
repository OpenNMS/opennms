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
package org.opennms.web.tags.select;

/**
 * Is used by the {@link org.opennms.web.tags.SelectTag} to determine how to render its options elements.
 * @param <T> The type of the elements which will be rendered by the SelectTag (e.g. String)
 */
public interface SelectTagHandler<T> {
    /**
     * Returns the String which should be put in the value attribute of the option.
     *
     * @param input The object to get the value from.
     * @return the value for the value attribute of the option-tag.
     */
    String getValue(T input);

    /**
     * Returns the String which should be put in the element section of the option tag.
     * @param input
     * @return
     */
    String getDescription(T input);

    /**
     * Determines if the currentElement is the selectedElement
     * @return if the currentElement is the selectedElement.
     */
    boolean isSelected(T currentElement, T selectedElement);

}
