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

package org.opennms.netmgt.jasper.grafana;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opennms.core.sysprops.SystemProperties;

import com.google.common.base.Throwables;

/**
 * Utility class to rendering an exception as an image.
 *
 * @author jwhite
 */
public class ExceptionToPngRenderer {

    private static final String MAX_STACKTRACE_LINES_SYS_PROP = "org.opennms.netmgt.jasper.grafana.maxStackTraceLines";

    private static final int MAX_STACKTRACE_LINES =  SystemProperties.getInteger(MAX_STACKTRACE_LINES_SYS_PROP, 5);

    /**
     * Use the system default font.
     */
    private static final Font font = new Font(null, Font.PLAIN, 48);

    /**
     * Render the given exception to an image.
     *
     * @param e exception
     * @return a byte array of a .png representing the given exception
     * @throws IOException if an error occurs rendering the .png
     */
    public static byte[] renderExceptionToPng(Exception e) {
        // Build a string representation of the exception
        final StringBuilder sb = new StringBuilder();
        sb.append("Exception occurred: ");
        final String stack = Throwables.getStackTraceAsString(e) ;
        // Limit the length of the stack
        sb.append(getFirstNLines(stack, MAX_STACKTRACE_LINES));
        final String text = sb.toString();
        final String[] lines = text.split("\n");

        // Determine the maximum height of any line in the given text
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        int lineWidth = 0;
        int lineHeight = 0;
        FontMetrics fm = g2d.getFontMetrics();
        for (String line : lines) {
            lineWidth = Math.max(lineWidth, fm.stringWidth(line));
            lineHeight = Math.max(lineHeight, fm.getHeight());
        }
        g2d.dispose();

        // Now render the text to an image line by line
        final int paddingLeft = 10;
        final int paddingTop = 10;
        final int imgWidth = paddingLeft + lineWidth;
        final int imgHeight = paddingTop + lineHeight * lines.length;

        img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        g2d = img.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imgWidth, imgHeight);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);

        int yOffset = 0;
        for (String line : lines) {
            g2d.drawString(line, paddingLeft, yOffset + fm.getAscent());
            yOffset += lineHeight;
        }
        g2d.dispose();

        // Render the Graphics2D context to a PNG
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", baos);
        } catch (IOException ex) {
            // Given that we're writing to a byte array, we don't expect this to ever happen
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    /**
     * Extract the first N lines from the given String.
     */
    private static String getFirstNLines(String s, int N) {
        final String[] lines = s.split("\n");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lines.length, N); i++) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(lines[i]);
        }
        return sb.toString();
    }

}
