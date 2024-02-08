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
package org.opennms.web.alarm;

import org.opennms.web.filter.Filter;
import org.opennms.web.filter.NormalizedAcknowledgeType;
import org.opennms.web.filter.QueryParameters;

import java.util.List;

/**
 * Convenience data structure for holding the arguments to an event query.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @deprecated use {@link QueryParameters} instead.
 */
@Deprecated
public class AlarmQueryParms implements QueryParameters {
    public SortStyle sortStyle;

    public AcknowledgeType ackType;

    public List<Filter> filters;

    public int limit;

    public int multiple;
    
    public String display;

    @Override
    public String getSortStyleShortName() {
        return sortStyle != null  ? sortStyle.getShortName() : null;
    }

    @Override
    public NormalizedAcknowledgeType getAckType() {
        return ackType != null ? ackType.toNormalizedAcknowledgeType() : null;
    }

    @Override
    public List<Filter> getFilters() {
        return this.filters;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getMultiple() {
        return multiple;
    }

    @Override
    public String getDisplay() {
        return display;
    }
}
