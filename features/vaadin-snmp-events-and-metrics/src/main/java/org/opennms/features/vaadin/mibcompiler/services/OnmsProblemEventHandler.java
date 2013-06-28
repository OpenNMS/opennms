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
package org.opennms.features.vaadin.mibcompiler.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsmiparser.parser.SmiDefaultParser;
import org.jsmiparser.util.location.Location;
import org.jsmiparser.util.problem.DefaultProblemReporterFactory;
import org.jsmiparser.util.problem.ProblemEvent;
import org.jsmiparser.util.problem.ProblemEventHandler;
import org.jsmiparser.util.problem.ProblemReporterFactory;
import org.jsmiparser.util.problem.annotations.ProblemSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Implementation of the ProblemEventHandler interface for OpenNMS.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class OnmsProblemEventHandler implements ProblemEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OnmsProblemEventHandler.class);

    /** The Constant FILE_PREFIX. */
    private static final String FILE_PREFIX = "file://";

    /** The Constant DEPENDENCY_PATERN. */
    private static final Pattern DEPENDENCY_PATERN = Pattern.compile("Cannot find module ([^,]+)", Pattern.MULTILINE);

    /** The severity counters. */
    private int[] m_severityCounters = new int[ProblemSeverity.values().length];

    /** The total counter. */
    private int m_totalCounter;

    /** The output stream. */
    private ByteArrayOutputStream m_outputStream = new ByteArrayOutputStream();

    /** The print stream. */
    private PrintStream m_out;

    /**
     * Instantiates a new OpenNMS problem event handler.
     *
     * @param parser the parser
     */
    public OnmsProblemEventHandler(SmiDefaultParser parser) {
        m_out = new PrintStream(m_outputStream);
        ProblemReporterFactory problemReporterFactory = new DefaultProblemReporterFactory(getClass().getClassLoader(), this);
        parser.setProblemReporterFactory(problemReporterFactory);
    }

    /* (non-Javadoc)
     * @see org.jsmiparser.util.problem.ProblemEventHandler#handle(org.jsmiparser.util.problem.ProblemEvent)
     */
    @Override
    public void handle(ProblemEvent event) {
        m_severityCounters[event.getSeverity().ordinal()]++;
        m_totalCounter++;
        print(m_out, event.getSeverity().toString(), event.getLocation(), event.getLocalizedMessage());
    }

    /* (non-Javadoc)
     * @see org.jsmiparser.util.problem.ProblemEventHandler#isOk()
     */
    @Override
    public boolean isOk() {
        for (int i = 0; i < m_severityCounters.length; i++) {
            if (i >= ProblemSeverity.ERROR.ordinal()) {
                int severityCounter = m_severityCounters[i];
                if (severityCounter > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.jsmiparser.util.problem.ProblemEventHandler#isNotOk()
     */
    @Override
    public boolean isNotOk() {
        return !isOk();
    }

    /* (non-Javadoc)
     * @see org.jsmiparser.util.problem.ProblemEventHandler#getSeverityCount(org.jsmiparser.util.problem.annotations.ProblemSeverity)
     */
    @Override
    public int getSeverityCount(ProblemSeverity severity) {
        return m_severityCounters[severity.ordinal()];
    }

    /* (non-Javadoc)
     * @see org.jsmiparser.util.problem.ProblemEventHandler#getTotalCount()
     */
    @Override
    public int getTotalCount() {
        return m_totalCounter;
    }

    /**
     * Prints the error message
     *
     * @param stream the stream
     * @param sev the severity
     * @param location the location
     * @param localizedMessage the localized message
     */
    private void print(PrintStream stream, String sev, Location location, String localizedMessage) {
        LOG.debug("[{}] Location: {}, Message: {}", sev, location, localizedMessage);
        int n = localizedMessage.indexOf(FILE_PREFIX);
        if (n > 0) {
            String source = localizedMessage.substring(n).replaceAll(FILE_PREFIX, "");
            String message = localizedMessage.substring(0,n) + source.split(":")[3];
            processMessage(stream, sev, source, message);
        } else {
            if (location == null) {
                stream.println(sev + ": " + localizedMessage);
            } else {
                String source = location.toString().replaceAll(FILE_PREFIX, "");
                String message = localizedMessage;
                processMessage(stream, sev, source, message);
            }
        }
    }

    /**
     * Process the error message
     *
     * @param stream the stream
     * @param sev the severity
     * @param source the location source
     * @param message the message
     */
    // TODO This implementation might be expensive.
    private void processMessage(PrintStream stream, String sev, String source, String message) {
        String[] data = source.split(":");
        File file = new File(data[0]);
        stream.println(sev + ": " + message + ", Source: " + file.getName() + ", Row: " + data[1] + ", Col: " + data[2]);
        try {
            if (!file.exists()) {
                LOG.warn("File {} doesn't exist", file);
                return;
            }
            FileInputStream fs= new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            int line = Integer.parseInt(data[1]);
            for (int i = 1; i < line; i++)
                br.readLine();
            stream.println(br.readLine());
            br.close();
            stream.println(String.format("%" + data[2] + "s", "^"));
        } catch (Exception e) {
            LOG.warn("Can't retrieve line {} from file {}", data[1], file);
        }
    }

    /**
     * Reset.
     */
    public void reset() {
        m_outputStream.reset();
        m_severityCounters = new int[ProblemSeverity.values().length];
        m_totalCounter = 0;
    }

    /**
     * Gets the dependencies.
     *
     * @return the dependencies
     */
    public List<String> getDependencies() {
        List<String> dependencies = new ArrayList<String>();
        if (m_outputStream.size() > 0) {
            Matcher m = DEPENDENCY_PATERN.matcher(m_outputStream.toString());
            while (m.find()) {
                final String dep = m.group(1);
                if (!dependencies.contains(dep))
                    dependencies.add(dep);
            }
        }
        return dependencies;
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public String getMessages() {
        return m_outputStream.size() > 0 ? m_outputStream.toString() : null;
    }

    /**
     * Adds a new error message.
     *
     * @param errorMessage the error message
     */
    public void addError(String errorMessage) {
        m_out.println(errorMessage);
    }

}
