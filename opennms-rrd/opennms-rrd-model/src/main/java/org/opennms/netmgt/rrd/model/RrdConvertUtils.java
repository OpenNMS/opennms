/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.rrd.model.v1.RRDv1;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The Class RRD Conversion Utilities.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class RrdConvertUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(RrdConvertUtils.class);

    /**
     * Instantiates a new RRDtool Convert Utils.
     */
    private RrdConvertUtils() {}

    /**
     * Dumps a JRB.
     *
     * @param sourceFile the source file
     * @return the RRD Object (old version)
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws RrdException the RRD exception
     */
    public static RRDv1 dumpJrb(File sourceFile) throws IOException, RrdException  {
        RrdDb jrb = new RrdDb(sourceFile, true);
        RRDv1 rrd = JaxbUtils.unmarshal(RRDv1.class, jrb.getXml());
        jrb.close();
        return rrd;
    }

    /**
     * Dumps a RRD.
     *
     * @param sourceFile the source file
     * @return the RRD Object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws RrdException the RRD exception
     */
    public static RRDv3 dumpRrd(File sourceFile) throws IOException, RrdException {
        String rrdBinary = System.getProperty("rrd.binary");
        if (rrdBinary == null) {
            throw new IllegalArgumentException("rrd.binary property must be set");
        }
        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Process process = Runtime.getRuntime().exec(new String[] {rrdBinary, "dump", sourceFile.getAbsolutePath()});
            SAXSource source = new SAXSource(xmlReader, new InputSource(new InputStreamReader(process.getInputStream())));
            JAXBContext jc = JAXBContext.newInstance(RRDv3.class);
            Unmarshaller u = jc.createUnmarshaller();
            return (RRDv3) u.unmarshal(source);
        } catch (Exception e) {
            throw new RrdException("Can't parse RRD Dump", e);
        }
    }

    /**
     * Restore a JRB.
     *
     * @param rrd the RRD object (old version)
     * @param targetFile the target file
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws RrdException the RRD exception
     */
    public static void restoreJrb(RRDv1 rrd, File targetFile) throws IOException, RrdException {
        final File outputXmlFile = new File(targetFile + ".xml");
        JaxbUtils.marshal(rrd, new FileWriter(outputXmlFile));
        RrdDb targetJrb = new RrdDb(targetFile.getCanonicalPath(), RrdDb.PREFIX_XML + outputXmlFile.getAbsolutePath());
        targetJrb.close();
        if(!outputXmlFile.delete()) {
        	LOG.warn("Could not delete file: {}", outputXmlFile.getPath());
        }
    }

    /**
     * Restores a RRD.
     *
     * @param rrd the RRD object
     * @param targetFile the target file
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws RrdException the RRD exception
     */
    public static void restoreRrd(RRDv3 rrd, File targetFile) throws IOException, RrdException {
        String rrdBinary = System.getProperty("rrd.binary");
        if (rrdBinary == null) {
            throw new IllegalArgumentException("rrd.binary property must be set");
        }
        try {
            File xmlDest = new File(targetFile + ".xml");
            JaxbUtils.marshal(rrd, new FileWriter(xmlDest));
            Process process = Runtime.getRuntime().exec(new String[]{ rrdBinary, "restore", xmlDest.getAbsolutePath(), targetFile.getAbsolutePath() });
            process.waitFor();
            if(!xmlDest.delete()) {
            	LOG.warn("Could not delete file: {}", xmlDest.getPath());
            }
        } catch (Exception e) {
            throw new RrdException("Can't restore RRD", e);
        }
    }

    /**
     * Convert from RRDtool to JRobin.
     *
     * @param sourceRrdFile the source RRDtool file
     * @param targetJrobinFile the target JRobin file
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws RrdException the RRD exception
     */
    public static void convertFromRrdToJrobin(File sourceRrdFile, File targetJrobinFile) throws IOException, RrdException {
        RRDv3 rrd = dumpRrd(sourceRrdFile);
        RRDv1 jrb = convert(rrd);
        restoreJrb(jrb, targetJrobinFile);
    }

    /**
     * Convert from JRobin to RRDtool.
     *
     * @param sourceJrobinFile the source JRobin file
     * @param targetRrdFile the target RRDtool file
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws RrdException the RRD exception
     */
    public static void convertFromJrobinToRrd(File sourceJrobinFile, File targetRrdFile) throws IOException, RrdException {
        RRDv1 jrb = dumpJrb(sourceJrobinFile);
        RRDv3 rrd = convert(jrb);
        restoreRrd(rrd, targetRrdFile);
    }

    /**
     * Converts a JRobin object into an RRDtool object.
     *
     * @param jrb the source JRobin object representation
     * @return the RRDtool object representation
     * @throws IllegalArgumentException the illegal argument exception (when the conversion is not possible)
     */
    protected static RRDv3 convert(RRDv1 jrb) throws IllegalArgumentException {
        RRDv3 rrd = new RRDv3();
        rrd.setStep(jrb.getStep());
        rrd.setLastUpdate(jrb.getLastUpdate());
        for (org.opennms.netmgt.rrd.model.v1.RRA rrav1 : jrb.getRras()) {
            org.opennms.netmgt.rrd.model.v3.RRA rrav3 = new org.opennms.netmgt.rrd.model.v3.RRA();
            rrav3.setConsolidationFunction(rrav1.getConsolidationFunction().name());
            rrav3.setPdpPerRow(rrav1.getPdpPerRow());
            rrav3.setRows(rrav1.getRows());
            rrav3.getParameters().setXff(rrav1.getXff());
            for (org.opennms.netmgt.rrd.model.v1.RRADS rradsv1 : rrav1.getDataSources()) {
                org.opennms.netmgt.rrd.model.v3.RRADS rradsv3 = new org.opennms.netmgt.rrd.model.v3.RRADS();
                rradsv3.setUnknownDataPoints(rradsv1.getUnknownDataPoints());
                rradsv3.setValue(rradsv1.getValue());
                rrav3.getDataSources().add(rradsv3);
            }
            rrd.addRRA(rrav3);
        }
        for (org.opennms.netmgt.rrd.model.v1.DS dsv1 : jrb.getDataSources()) {
            org.opennms.netmgt.rrd.model.v3.DS dsv3 = new org.opennms.netmgt.rrd.model.v3.DS();
            dsv3.setName(dsv1.getName());
            dsv3.setLastDs(dsv1.getLastDs());
            dsv3.setMin(dsv1.getMax());
            dsv3.setMax(dsv1.getMax());
            dsv3.setMinHeartbeat(dsv1.getMinHeartbeat());
            dsv3.setUnknownSec(dsv1.getUnknownSec());
            dsv3.setValue(dsv1.getValue());
            dsv3.setType(dsv1.getType().value());
            rrd.addDataSource(dsv3);
        }
        return rrd;
    }

    /**
     * Converts a RRDtool object into a JRobin object.
     *
     * @param rrd the RRDtool object representation
     * @return the JRobin object representation
     * @throws IllegalArgumentException the illegal argument exception (when the conversion is not possible)
     */
    protected static RRDv1 convert(RRDv3 rrd) throws IllegalArgumentException {
        RRDv1 jrb = new RRDv1();
        jrb.setStep(rrd.getStep());
        jrb.setLastUpdate(rrd.getLastUpdate());
        for (org.opennms.netmgt.rrd.model.v3.RRA rrav3 : rrd.getRras()) {
            org.opennms.netmgt.rrd.model.v1.RRA rrav1 = new org.opennms.netmgt.rrd.model.v1.RRA();
            try {
                rrav1.setConsolidationFunction(rrav3.getConsolidationFunction().name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("RRDv1 doesn't support the consolidation function " + rrav3.getConsolidationFunction().value());
            }
            rrav1.setPdpPerRow(rrav3.getPdpPerRow());
            rrav1.setRows(rrav3.getRows());
            rrav1.setXff(rrav3.getParameters().getXff());
            for (org.opennms.netmgt.rrd.model.v3.RRADS rradsv3 : rrav3.getDataSources()) {
                org.opennms.netmgt.rrd.model.v1.RRADS rradsv1 = new org.opennms.netmgt.rrd.model.v1.RRADS();
                rradsv1.setUnknownDataPoints(rradsv3.getUnknownDataPoints());
                rradsv1.setValue(rradsv3.getValue());
                rrav1.getDataSources().add(rradsv1);
            }
            jrb.addRRA(rrav1);
        }
        for (org.opennms.netmgt.rrd.model.v3.DS dsv3 : rrd.getDataSources()) {
            org.opennms.netmgt.rrd.model.v1.DS dsv1 = new org.opennms.netmgt.rrd.model.v1.DS();
            try {
                dsv1.setType(dsv3.getType().value());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("RRDv1 doesn't support the data source type " + dsv3.getType().value());
            }
            dsv1.setName(dsv3.getName());
            dsv1.setLastDs(dsv3.getLastDs());
            dsv1.setMin(dsv3.getMax());
            dsv1.setMax(dsv3.getMax());
            dsv1.setMinHeartbeat(dsv3.getMinHeartbeat());
            dsv1.setUnknownSec(dsv3.getUnknownSec());
            dsv1.setValue(dsv3.getValue());
            jrb.addDataSource(dsv1);
        }
        return jrb;
    }

}
