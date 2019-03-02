/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.generator.util;

public class MacAddressGenerator {

    private int last = 0;

    public String next() {
        String s = String.format("%1$" + 12 + "s", Integer.toHexString(last)); // example: "           1"
        s = s.replace(' ', '0'); // example: "000000000001"
        last++;
        return s;
    }

    public String nextWithSeparator() {

        String s = next();
        s = s.substring(0, 2)
                + ':' + s.substring(2, 4)
                + ':' + s.substring(4, 6)
                + ':' + s.substring(6, 8)
                + ':' + s.substring(8, 10)
                + ':' + s.substring(10, 12);
        last++;
        return s;
    }
}
