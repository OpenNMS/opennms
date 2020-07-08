/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

