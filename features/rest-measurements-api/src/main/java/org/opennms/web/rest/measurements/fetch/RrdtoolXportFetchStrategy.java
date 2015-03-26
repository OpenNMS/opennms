/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.measurements.fetch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.jrobin.core.RrdException;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.rrd.model.RrdXport;
import org.opennms.netmgt.rrd.model.XRow;
import org.opennms.web.rest.measurements.model.Source;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.collect.Maps;

/**
 * Used to fetch measurements from RRD files by invoking
 * rrdtool via exec.
 *
 * As of Feb 24th the jrrd library does not support 'xport' commands.
 * See http://issues.opennms.org/browse/JRRD-3.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
public class RrdtoolXportFetchStrategy extends AbstractRrdBasedFetchStrategy {

    /**
     * Maximum runtime of 'rrdtool xport' in milliseconds before failing and
     * throwing an exception.
     */
    public static final long XPORT_TIMEOUT_MS = 120000;

    public RrdtoolXportFetchStrategy(final ResourceDao resourceDao) {
        super(resourceDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FetchResults fetchMeasurements(long start, long end, long step, int maxrows,
            Map<Source, String> rrdsBySource, Map<String, Object> constants) throws RrdException {

        String rrdBinary = System.getProperty("rrd.binary");
        if (rrdBinary == null) {
            throw new RrdException("No RRD binary is set.");
        }

        final long startInSeconds = (long) Math.floor(start / 1000);
        final long endInSeconds = (long) Math.floor(end / 1000);

        long stepInSeconds = (long) Math.floor(step / 1000);
        // The step must be strictly positive
        if (stepInSeconds <= 0) {
            stepInSeconds = 1;
        }

        final CommandLine cmdLine = new CommandLine(rrdBinary);
        cmdLine.addArgument("xport");
        cmdLine.addArgument("--step");
        cmdLine.addArgument("" + stepInSeconds);
        cmdLine.addArgument("--start");
        cmdLine.addArgument("" + startInSeconds);
        cmdLine.addArgument("--end");
        cmdLine.addArgument("" + endInSeconds);
        if (maxrows > 0) {
            cmdLine.addArgument("--maxrows");
            cmdLine.addArgument("" + maxrows);
        }

        int k = 0;
        for (final Map.Entry<Source, String> entry : rrdsBySource.entrySet()) {
            final Source source = entry.getKey();
            final String rrdFile = entry.getValue();

            cmdLine.addArgument(String.format("DEF:%s=%s:%s:%s",
                    k, rrdFile, source.getAttribute(),
                    source.getAggregation()));
            cmdLine.addArgument(String.format("XPORT:%s:%s", k,
                    source.getLabel()));
            k++;
        }

        // Use commons-exec to execute rrdtool
        final DefaultExecutor executor = new DefaultExecutor();

        // Capture stdout/stderr
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr, null));

        // Fail if we get a non-zero exit code
        executor.setExitValue(0);

        // Fail if the process takes too long
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(XPORT_TIMEOUT_MS);
        executor.setWatchdog(watchdog);

        // Export
        RrdXport rrdXport;
        try {
            executor.execute(cmdLine);

            final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            final SAXSource source = new SAXSource(xmlReader, new InputSource(
                    new StringReader(stdout.toString())));
            final JAXBContext jc = JAXBContext.newInstance(RrdXport.class);
            final Unmarshaller u = jc.createUnmarshaller();
            rrdXport = (RrdXport) u.unmarshal(source);
        } catch (IOException e) {
            throw new RrdException("An error occured while executing '"
                    + StringUtils.join(cmdLine.toStrings(), " ")
                    + "' with stderr: " + stderr.toString(), e);
        } catch (SAXException | JAXBException e) {
            throw new RrdException("Failed to parse the RRD export output.", e);
        }

        final int numRows = rrdXport.getRows().size();
        final int numColumns = rrdXport.getMeta().getLegends().size();

        final long timestamps[] = new long[numRows];
        final double values[][] = new double[numColumns][numRows];

        // Convert rows to columns
        int i = 0;
        for (final XRow row : rrdXport.getRows()) {
            timestamps[i] = row.getTimestamp() * 1000;
            for (int j = 0; j < numColumns; j++) {
                values[j][i] = row.getValues().get(j);
            }
            i++;
        }

        // Map the columns by label
        // The legend entries are in the same order as the column values
        final Map<String, double[]> columns = Maps.newHashMapWithExpectedSize(numColumns);
        i = 0;
        for (String label : rrdXport.getMeta().getLegends()) {
            columns.put(label, values[i++]);
        }

        return new FetchResults(timestamps, columns, rrdXport.getMeta().getStep() * 1000, constants);
    }
}
