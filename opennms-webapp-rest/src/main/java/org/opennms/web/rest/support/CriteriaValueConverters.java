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

    public static final Function<String,Boolean> BOOLEAN_CONVERTER = new Function<String,Boolean>() {
        @Override
        public Boolean apply(final String t) {
            return Boolean.parseBoolean(t);
        }

        @Override
        public String toString() {
            return "BOOLEAN_CONVERTER";
        }
    };

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

    public static final Function<String,Character> CHARACTER_CONVERTER = new Function<String,Character>() {
        @Override
        public Character apply(final String t) {
            return t.charAt(0);
        }

        /**
         * Override {@link #toString()} on this functional interface
         * to make it identifiable inside a debugger.
         */
        @Override
        public String toString() {
            return "CHARACTER_CONVERTER";
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

    public static final Function<String,String> STRING_CONVERTER = new Function<String,String>() {
        @Override
        public String apply(final String t) {
            return t;
        }

        /**
         * Override {@link #toString()} on this functional interface
         * to make it identifiable inside a debugger.
         */
        @Override
        public String toString() {
            return "STRING_CONVERTER";
        }
    };

}
