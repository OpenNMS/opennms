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
package org.opennms.netmgt.dao.mock;

import java.io.InputStream;

import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.springframework.dao.DataAccessException;

public class MockRrdDao implements RrdDao {

    @Override
    public double getPrintValue(OnmsAttribute attribute, String cf, long start, long end) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public double[] getPrintValues(OnmsAttribute attribute, String rraConsolidationFunction, long startTimeInMillis, long endTimeInMillis, String... printFunctions) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public InputStream createGraph(String command) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getGraphTopOffsetWithText() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getGraphLeftOffset() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int getGraphRightOffset() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Double getLastFetchValue(OnmsAttribute attribute, int interval) throws DataAccessException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Double getLastFetchValue(OnmsAttribute attribute, int interval, int range) throws DataAccessException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
