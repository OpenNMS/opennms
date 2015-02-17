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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

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
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.model.RrdXport;
import org.opennms.netmgt.rrd.model.XRow;
import org.opennms.web.rest.measurements.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.collect.Maps;

/**
 * Used to fetch measurements from RRD files.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
public class JniRrdFetchStrategy implements MeasurementFetchStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(JrbFetchStrategy.class);

    private final ResourceDao m_resourceDao;

    /**
     * Maximum runtime of 'rrdtool xport' in milliseconds before failing and
     * throwing an exception.
     */
    public static final long XPORT_TIMEOUT_MS = 120000;

    public JniRrdFetchStrategy(final ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FetchResults fetch(long step, long start, long end,
            List<Source> sources) throws RrdException {
        String rrdBinary = System.getProperty("rrd.binary");
        if (rrdBinary == null) {
            throw new RrdException("No RRD binary is set.");
        }

        final CommandLine cmdLine = new CommandLine(rrdBinary);
        cmdLine.addArgument("xport");
        cmdLine.addArgument("--step");
        cmdLine.addArgument("" + step);
        cmdLine.addArgument("--start");
        cmdLine.addArgument("" + start);
        cmdLine.addArgument("--end");
        cmdLine.addArgument("" + end);

        for (final Source source : sources) {
            final OnmsResource resource = m_resourceDao.getResourceById(source
                    .getResourceId());
            if (resource == null) {
                LOG.error("No resource with id: {}", source.getResourceId());
                return null;
            }

            final RrdGraphAttribute rrdGraphAttribute = resource
                    .getRrdGraphAttributes().get(source.getAttribute());
            if (rrdGraphAttribute == null) {
                LOG.error("No attribute with name: {}", source.getAttribute());
                return null;
            }

            final String rrdFile = System.getProperty("rrd.base.dir")
                    + File.separator + rrdGraphAttribute.getRrdRelativePath();

            cmdLine.addArgument(String.format("DEF:%s=%s:%s:%s",
                    source.getLabel(), rrdFile, source.getAttribute(),
                    source.getAggregation()));
            cmdLine.addArgument(String.format("XPORT:%s:%s", source.getLabel(),
                    source.getLabel()));
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

        // Format
        final SortedMap<Long, Map<String, Double>> rows = Maps.newTreeMap();
        for (final XRow row : rrdXport.getRows()) {
            Map<String, Double> values = Maps.newHashMap();
            int k = 0;
            for (String column : rrdXport.getMeta().getLegends()) {
                values.put(column, row.getValues().get(k++));
            }
            rows.put(row.getTimestamp(), values);
        }

        return new FetchResults(rows, rrdXport.getMeta().getStep());
    }
}
