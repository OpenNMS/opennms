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
package org.opennms.netmgt.timeseries.sampleread.aggregation;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

public enum StandardAggregationFunctions implements Function<Collection<Double>, Double> {
    AVERAGE {
        public Double apply(Collection<Double> input) {
            if (input.isEmpty()) {
                return 0.0D / 0.0;
            } else {
                int count = 0;
                Double sum = 0.0D;
                Iterator var4 = input.iterator();

                while(var4.hasNext()) {
                    Double item = (Double)var4.next();
                    if (!Double.isNaN(item)) {
                        sum = sum + item;
                        ++count;
                    }
                }

                return sum / (double)count;
            }
        }
    },
    MAX {
        public Double apply(Collection<Double> input) {
            if (input.isEmpty()) {
                return 0.0D / 0.0;
            } else {
                Double max = 4.9E-324D;
                Iterator var3 = input.iterator();

                while(var3.hasNext()) {
                    Double item = (Double)var3.next();
                    if (!Double.isNaN(item)) {
                        double diff = item - max;
                        max = diff > 0.0D ? item : max;
                    }
                }

                return max;
            }
        }
    },
    MIN {
        public Double apply(Collection<Double> input) {
            if (input.isEmpty()) {
                return 0.0D / 0.0;
            } else {
                Double min = 1.7976931348623157E308D;
                Iterator var3 = input.iterator();

                while(var3.hasNext()) {
                    Double item = (Double)var3.next();
                    if (!Double.isNaN(item)) {
                        double diff = item - min;
                        min = diff < 0.0D ? item : min;
                    }
                }

                return min;
            }
        }
    };
}

