/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.helper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
public class Colors {

    private static List<String> tangoColors = new ArrayList<String>();
    private static int colorIndex = 0;

    static {
        tangoColors.add("c4a000");
        tangoColors.add("edd400");
        tangoColors.add("fce94f");

        tangoColors.add("ce5c00");
        tangoColors.add("f57900");
        tangoColors.add("fcaf3e");

        tangoColors.add("8f5902");
        tangoColors.add("c17d11");
        tangoColors.add("e9b96e");

        tangoColors.add("4e9a06");
        tangoColors.add("73d216");
        tangoColors.add("8ae234");

        tangoColors.add("204a87");
        tangoColors.add("3465a4");
        tangoColors.add("729fcf");

        tangoColors.add("5c3566");
        tangoColors.add("75507b");
        tangoColors.add("ad7fa8");

        tangoColors.add("a40000");
        tangoColors.add("cc0000");
        tangoColors.add("ef2929");

        tangoColors.add("babdb6");
        tangoColors.add("d3d7cf");
        tangoColors.add("eeeeec");

        tangoColors.add("2e3436");
        tangoColors.add("555753");
        tangoColors.add("888a85");
    }

    public static String getNextColor() {
        String color = tangoColors.get(colorIndex);
        colorIndex = (colorIndex + 1) % tangoColors.size();
        return color;
    }

    public static void restetColor() {
        colorIndex = 0;
    }
}
