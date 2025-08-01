package org.opennms.reporting.availability.render;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;

public class NMS16414_IT {
    private final Path temporaryDirectory = Files.createTempDirectory("NMS16414_IT");

    private final String xslString = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:rt=\"http://xml.apache.org/xalan/java/java.lang.Runtime\" xmlns:ob=\"http://xml.apache.org/xalan/java/java.lang.Object\">\n" +
            "    <xsl:template match=\"/\">\n" +
            "        <xsl:variable name=\"rtobject\" select=\"rt:getRuntime()\"/>\n" +
            "        <xsl:variable name=\"process\" select=\"rt:exec($rtobject,'touch " + temporaryDirectory.toString() + "/foobar.txt')\"/>\n" +
            "        <xsl:variable name=\"processString\" select=\"ob:toString($process)\"/>\n" +
            "        <xsl:value-of select=\"$processString\"/>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>";

    public NMS16414_IT() throws IOException {
    }

    @Test
    public void testRemoteCodeExecution() throws Exception {
        final Reader in = new BufferedReader(new StringReader("<test/>"));
        final OutputStream out = new FileOutputStream(temporaryDirectory.resolve("output.xml").toFile());
        final Reader xslt = new BufferedReader(new StringReader(xslString));
        final HTMLReportRenderer htmlReportRenderer = new HTMLReportRenderer();
        htmlReportRenderer.render(in, out, xslt);
        assertFalse(Files.exists(temporaryDirectory.resolve("foobar.txt")));
    }
}
