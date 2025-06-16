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
package org.opennms.netmgt.flows.classification.internal;

import java.util.Objects;

import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.exception.InvalidFilterException;

public class DefaultFilterService implements FilterService {

    private final FilterDao filterDao;

    public DefaultFilterService(FilterDao filterDao) {
        this.filterDao = Objects.requireNonNull(filterDao);
    }

    @Override
    public void validate(final String filterExpression) throws InvalidFilterException {
        try {
            this.filterDao.validateRule(filterExpression);
        } catch (FilterParseException ex) {
            throw new InvalidFilterException(filterExpression, ex);
        }
    }

    @Override
    public boolean matches(final String address, final String filterExpression) {
        return filterDao.isValid(address, filterExpression);
    }
}
