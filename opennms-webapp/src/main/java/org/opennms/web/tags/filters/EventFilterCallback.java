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
package org.opennms.web.tags.filters;

import org.opennms.web.event.EventUtil;
import org.opennms.web.filter.Filter;

import javax.servlet.ServletContext;
import java.util.List;

public class EventFilterCallback extends AbstractFilterCallback {

    public EventFilterCallback(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected String getIndividualFilterString(Filter filter) {
        return EventUtil.getFilterString(filter);
    }

    @Override
    protected List<Filter> getIndividualFilterList(String[] filters, ServletContext servletContext) {
        return EventUtil.getFilterList(filters, servletContext);
    }
}
