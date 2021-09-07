/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support;

import java.net.InetAddress;
import java.util.Date;
import java.util.function.Function;

import org.opennms.core.utils.InetAddressUtils;

/**
 * This class is used to override {@link #toString()} on several
 * string conversion functions so that they are identifiable inside
 * a debugger. Otherwise, you will just see the object hash identifier
 * for the {@link Function}.
 */
public abstract class CriteriaValueConverters {

    public static final Function<String,Date> DATE_CONVERTER = new Function<String,Date>() {
        @Override
        public Date apply(String t) {
            return CriteriaBehaviors.parseDate(t);
        }

        /**
         * Override {@link #toString()} on this functional interface
         * to make it identifiable inside a debugger.
         */
        @Override
        public String toString() {
            return "DATE_CONVERTER";
        }
    };

    public static final Function<String,Float> FLOAT_CONVERTER = new Function<String,Float>() {
        @Override
        public Float apply(String t) {
            return Float.parseFloat(t);
        }

        /**
         * Override {@link #toString()} on this functional interface
         * to make it identifiable inside a debugger.
         */
        @Override
        public String toString() {
            return "FLOAT_CONVERTER";
        }
    };

    public static final Function<String,InetAddress> INET_ADDRESS_CONVERTER = new Function<String,InetAddress>() {
        @Override
        public InetAddress apply(String t) {
            return InetAddressUtils.addr(t);
        }

        /**
         * Override {@link #toString()} on this functional interface
         * to make it identifiable inside a debugger.
         */
        @Override
        public String toString() {
            return "INET_ADDRESS_CONVERTER";
        }
    };

    public static final Function<String,Integer> INT_CONVERTER = new Function<String,Integer>() {
        @Override
        public Integer apply(String t) {
            return Integer.parseInt(t);
        }

        /**
         * Override {@link #toString()} on this functional interface
         * to make it identifiable inside a debugger.
         */
        @Override
        public String toString() {
            return "INT_CONVERTER";
        }
    };

    public static final Function<String,Long> LONG_CONVERTER = new Function<String,Long>() {
        @Override
        public Long apply(String t) {
            return Long.parseLong(t);
        }

        /**
         * Override {@link #toString()} on this functional interface
         * to make it identifiable inside a debugger.
         */
        @Override
        public String toString() {
            return "LONG_CONVERTER";
        }
    };

}
