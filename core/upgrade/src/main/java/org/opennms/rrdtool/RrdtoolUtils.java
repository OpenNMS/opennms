/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.rrdtool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jrobin.core.RrdDb;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.rrdtool.old.RrdOld;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.util.FileCopyUtils;

/**
 * The Class RrdtoolUtils.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class RrdtoolUtils {

    /**
     * Instantiates a new RRDtool Utils.
     */
    private RrdtoolUtils() {}

    /**
     * Dumps a JRB.
     *
     * @param sourceFile the source file
     * @return the RRD Object (old version)
     * @throws Exception the exception
     */
    public static RrdOld dumpJrb(File sourceFile) throws Exception {
        RrdDb jrb = new RrdDb(sourceFile, true);
        RrdOld rrd = JaxbUtils.unmarshal(RrdOld.class, jrb.getXml());
        jrb.close();
        return rrd;
    }

    /**
     * Dumps a RRD.
     *
     * @param sourceFile the source file
     * @return the RRD Object
     * @throws Exception the exception
     */
    public static RRD dumpRrd(File sourceFile) throws Exception {
        String rrdBinary = System.getProperty("rrd.binary");
        if (rrdBinary == null) {
            throw new IllegalArgumentException("rrd.binary property must be set");
        }
        String command = rrdBinary + " dump " + sourceFile.getAbsolutePath();
        String[] commandArray = StringUtils.createCommandArray(command, '@');
        RRD rrd = null;
        Process process = Runtime.getRuntime().exec(commandArray);
        byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());
        String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
        if (errors.length() > 0) {
            throw new OnmsUpgradeException("RRDtool command fail: " + errors);
        }
        BufferedReader reader = null;
        try {
            InputStream is = new ByteArrayInputStream(byteArray);
            reader = new BufferedReader(new InputStreamReader(is));
            rrd = JaxbUtils.unmarshal(RRD.class, reader);
        } finally {
            reader.close();
        }
        return rrd;
    }

    /**
     * Restore a JRB.
     *
     * @param rrd the RRD object (old version)
     * @param targetFile the target file
     * @throws Exception the exception
     */
    public static void restoreJrb(RrdOld rrd, File targetFile) throws Exception {
        final File outputXmlFile = new File(targetFile + ".xml");
        JaxbUtils.marshal(rrd, new FileWriter(outputXmlFile));
        RrdDb targetJrb = new RrdDb(targetFile.getCanonicalPath(), RrdDb.PREFIX_XML + outputXmlFile.getAbsolutePath());
        targetJrb.close();
        outputXmlFile.delete();
    }

    /**
     * Restores a RRD.
     *
     * @param rrd the RRD object
     * @param targetFile the target file
     * @throws Exception the exception
     */
    public static void restoreRrd(RRD rrd, File targetFile) throws Exception {
        String rrdBinary = System.getProperty("rrd.binary");
        if (rrdBinary == null) {
            throw new IllegalArgumentException("rrd.binary property must be set");
        }
        File xmlDest = new File(targetFile + ".xml");
        JaxbUtils.marshal(rrd, new FileWriter(xmlDest));
        String command = rrdBinary + " restore " + xmlDest.getAbsolutePath() + " " + targetFile.getAbsolutePath();
        String[] commandArray = StringUtils.createCommandArray(command, '@');
        Process process = Runtime.getRuntime().exec(commandArray);
        String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
        if (errors.length() > 0) {
            throw new OnmsUpgradeException("RRDtool command fail: " + errors);
        }
        xmlDest.delete();
    }

}
