/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
package org.opennms.tools;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The Class JRobin to RRD Converter
 * <p>
 *     This is the main class used to start the conversion from JRobins jrb-Files to
 *     RRDtool file format. The program is used to run from command line. This tool
 *     requires the existence of the rrdtool binary.
 * </p>
 * <p>
 *     The conversion can be done as multithreaded job. Converting JRB to RRD files
 *     needs the following two steps:
 * </p>
 * <p>
 *     <ol>
 *         <li>Export JRB data into XML format with JRobin</li>
 *         <li>Re-create the RRD file from the XML file</li>
 *     </ol>
 * </p>
 *
 * @author <a href="mailto:Christian.Pape@informatik.hs-fulda.de">Christian Pape</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
public class JrbToRrdConverter {

    /**
     * Constants for file type and corresponding extensions
     */
    public static enum FILE_TYPE {
        JRB(".jrb"), RRD(".rrd"), XML(".xml");

        private final String ext;

        private FILE_TYPE(String e) {
            ext = e;
        }

        public String ext() {
            return ext;
        }
    }

    /**
     * Constants for available command line options
     */
    public static enum CMD_OPTION {
        RRDTOOL("rrdtool"), THREADS("threads");

        private final String cmd_option_name;

        private CMD_OPTION(String c) {
            cmd_option_name = c;
        }

        public String option() {
            return cmd_option_name;
        }
    }

    /**
     * Amount of default threads for conversion
     */
    public static final int DEFAULT_THREADS = 4;

    /**
     * Default path to RRDtool binary
     */
    public static final String DEFAULT_RRDTOOL = "/usr/bin/rrdtool";

    /**
     * Default time for sleep of the monitoring thread to calculate the
     * converted JRobin files per second
     */
    private static final int m_statusThreadSleep = 1000;

    /**
     * Initialize with amount of default threads
     */
    private int m_threadCount = DEFAULT_THREADS;

    /**
     * Initialize no files to convert
     */
    private int m_fileCount = 0;

    /**
     * Initialize no files converted
     */
    private int m_filesConverted = 0;

    /**
     * Initialize no skipped files
     */
    private int m_skippedFiles = 0;

    /**
     * Initialize no previously files converted. Variable used to
     * calculate converted JRobin files per second.
     */
    private int m_oldFilesConverted = 0;

    /**
     * Initialize path to RRDtool binary
     */
    private String m_rrdTool = DEFAULT_RRDTOOL;

    /**
     * Initialize search for JRobin files to be converted
     */
    private boolean m_searchDone = false;

    /**
     * Initialize array of threads for conversion
     */
    private JrbToXml[] m_threads;

    /**
     * Initialize path to search for and convert JRobin files
     */
    private String m_path;

    /**
     * Default constructor for JRobin to RRDtool converter
     *
     * @param path Path with JRobin files to convert as {@link java.lang.String}
     * @param rrdTool Path to rrdtool binary as {@link java.lang.String}
     * @param threadCount Amount of threads to convert JRobin files
     */
    public JrbToRrdConverter(String path, String rrdTool, int threadCount) {
        m_path = path;
        m_threadCount = threadCount;
        m_rrdTool = rrdTool;
    }

    /**
     * <p>Usage</p>
     * Print usage to help user the ability using the program on the CLI.
     *
     * @param options Options for the CLI as {@link org.apache.commons.cli.Options}
     * @param cmd Commandline as {@link org.apache.commons.cli.CommandLine}
     * @param error Error message as {@link java.lang.String}
     * @param e Exception handling {@link java.lang.Exception}
     */
    private static void usage(final Options options, final CommandLine cmd, final String error, final Exception e) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.out);
        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }

        // Print usage to standard out
        formatter.printHelp("Usage: JrbToRrdConverter <path>", options);

        if (e != null) {
            pw.println(e.getMessage());
            e.printStackTrace(pw);
        }

        // Close print writer to standard out
        pw.close();
    }

    /**
     * <p>Usage</p>
     * Print usage to help user the ability using the program on the CLI. This is
     * a fall through method using usage(options, cmd, error, exception).
     *
     * @param options Options for the CLI as {@link org.apache.commons.cli.Options}
     * @param cmd Commandline as {@link org.apache.commons.cli.CommandLine}
     */
    private static void usage(final Options options, final CommandLine cmd) {
        usage(options, cmd, null, null);
    }

    /**
     * <p>Get RRDtool</p>
     * Get path to RRDtool binary
     *
     * @return Path to RRDtool binary as {@link java.lang.String}
     */
    public String getRrdTool() {
        return m_rrdTool;
    }

    /**
     * Main method starting the program. Initializing rrdtool binary, amount of threads
     * and parsing the command line arguments. After
     *
     * @param args Arguments from command line
     * @throws ParseException
     */
    public static void main(String args[]) throws ParseException {

        // Init defaults
        String rrdTool = DEFAULT_RRDTOOL;
        int threadCount = DEFAULT_THREADS;

        // current directory
        String path = "./";

        final Options options = new Options();

        // Init options for rrdtool binary and the amount of threads to use
        options.addOption(CMD_OPTION.RRDTOOL.option(), true, "set rrdtool to use for converting XML to RRD, default: '" + DEFAULT_RRDTOOL + "'");
        options.addOption(CMD_OPTION.THREADS.option(), true, "set number of threads to use, default: " + DEFAULT_THREADS);

        final CommandLineParser parser = new PosixParser();
        final CommandLine cmd = parser.parse(options, args);

        @SuppressWarnings("unchecked") List<String> arguments = (List<String>) cmd.getArgList();

        // No arguments given
        if (arguments.size() < 1) {
            usage(options, cmd);
            System.exit(1);
        }

        // Modified rrdtool binary path
        if (cmd.hasOption(CMD_OPTION.RRDTOOL.option())) {
            rrdTool = cmd.getOptionValue(CMD_OPTION.RRDTOOL.option());
        }

        // Modified amount of threads
        if (cmd.hasOption(CMD_OPTION.THREADS.option())) {
            try {
                threadCount = Integer.valueOf(cmd.getOptionValue(CMD_OPTION.THREADS.option()));
            } catch (NumberFormatException numberFormatException) {
                usage(options, cmd);
                System.exit(1);
            }
        }

        path = arguments.remove(0);

        /*
         * Initialize the JRobin to RRDtool converter for a given directory with JRB files
         * to convert, rrdtool binary path and the amount of threads.
         */
        JrbToRrdConverter jrbToRrdConverter = new JrbToRrdConverter(path, rrdTool, threadCount);

        // Run conversion starts also a monitoring thread to calculate how many JRB/s are converted
        jrbToRrdConverter.runConversion();
    }

    /**
     * <p>Increase converted files count</p>
     */
    public synchronized void increaseConvertedFiles() {
        m_filesConverted++;
    }

    /**
     * <p>Search files to convert</p>
     * Recursive search in directory and all sub directories for JRobin files to convert.
     * Building a collection with all files including absolute path to convert from JRobin to RRDtool.
     *
     * @param path Path to search for JRobin files to convert as {@link java.lang.String}
     */
    private void search(String path) {
        File directory = new File(path);
        Set<File> fileSet = new TreeSet<File>();
        Collections.addAll(fileSet, directory.listFiles());

        for (File file : fileSet) {
            if (file.isDirectory()) {
                // Recursion if subdirectory is found
                search(file.getAbsolutePath());
            } else {
                // Check if we have a JRobin file
                if (file.getName().endsWith(FILE_TYPE.JRB.ext())) {
                    // Cut file extension and just keep the absolute path with file name
                    String filename = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - FILE_TYPE.JRB.ext().length());

                    // Check if we have to already dumped the JRobin file into XML
                    if (fileSet.contains(new File(filename + FILE_TYPE.XML.ext())) && fileSet.contains(new File(filename + FILE_TYPE.RRD.ext()))) {
                        m_skippedFiles++;
                    } else {
                        // Not converted - distribute to array of threads
                        m_threads[m_fileCount % m_threadCount].add(filename);
                        m_fileCount++;
                    }
                } else {
                    // ignore
                }
            }
        }
    }

    /**
     * Run the conversion. Initialize a monitoring thread and calculate how many JRobin
     * files are converted per second.
     */
    private void runConversion() {
        // Check if the rrdtool binary is available
        if (!(new File(m_rrdTool)).isFile()) {
            System.err.println("rrdtool '" + m_rrdTool + "' not found!");
            System.exit(1);

            // rrdtool binary not available, exit with error
        }

        System.out.println("Using rrdtool '" + m_rrdTool + "'...");

        System.out.print("Setting up " + m_threadCount + " converter thread(s)...");

        // Initialize array with given amount of threads
        m_threads = new JrbToXml[m_threadCount];

        // Start each thread
        for (int i = 0; i < m_threadCount; i++) {
            m_threads[i] = new JrbToXml(this);
            m_threads[i].start();
        }

        System.out.print(" done!\nSetting up status thread running every " + m_statusThreadSleep + " ms...");

        // Initialize monitoring thread
        Thread monitoringThread = new Thread(new Runnable() {
            public void run() {
                // Run as long we build the files or we have files to convert
                while (!m_searchDone || m_fileCount > m_filesConverted) {
                    try {
                        Thread.sleep(m_statusThreadSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Calculate statistics
                    int convertedPerSecond = (m_filesConverted - m_oldFilesConverted);

                    // reset for next calculation cycle
                    m_oldFilesConverted = m_filesConverted;

                    // print statistics
                    System.out.print("search=" + (m_searchDone ? "finished" : "running") + ", found=" + m_fileCount + ", skipped=" + m_skippedFiles + ", queued=" + (m_fileCount - m_oldFilesConverted) + ", converted=" + m_oldFilesConverted);

                    for (int i = 0; i < m_threadCount; i++) {
                        System.out.print(", #" + i + "=" + m_threads[i].size());
                    }

                    System.out.println(", delta=" + convertedPerSecond);
                }

                // All files converted
                System.out.println("Finished!\n");
            }
        });

        // Start monitoring thread
        monitoringThread.start();

        System.out.println(" done!\nSearching path is '" + m_path + "'");

        // Search recursive in given path for JRB files to convert.
        search(m_path);

        m_searchDone = true;

        // Close queue for each thread
        for (int i = 0; i < m_threadCount; i++) {
            m_threads[i].close();
        }
    }
}
