/*
 * 
 */
package org.opennms.tools.jmxconfiggenerator.helper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tak
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
