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
package org.opennms.web.category;

/**
 * <p>CategoryNotFoundException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CategoryNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 5700012759168177139L;

	protected String category;

    /**
     * <p>Constructor for CategoryNotFoundException.</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public CategoryNotFoundException(String category) {
        super("Could not find the " + category + " category");

        if (category == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.category = category;
    }

    /**
     * <p>Getter for the field <code>category</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCategory() {
        return (this.category);
    }

}
