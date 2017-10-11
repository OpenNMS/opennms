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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertTrue;
import static org.opennms.core.test.ConfigurationTestUtils.getDaemonEtcDirectory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * This is a meta test, testing the coverage of {@link WillItUnmarshalIT}.
 *
 * The {@link WillItUnmarshalIT} uses parameterized tests to check
 * unmarshalling of provided config files. To ensure the checked list of files
 * covers all files in the example directories, this test fetches the file list
 * of the directories to test and checks them against the defined files to test.
 *
 * @author Dustin Frisch<fooker@lab.sh>
 */
@RunWith(value = Parameterized.class)
public class WillItUnmarshalCoverageMetaIT {
    private static final Logger LOG = LoggerFactory.getLogger(WillItUnmarshalCoverageMetaIT.class);

    /**
     * A set of test parameters to execute.
     * 
     * See {@link #files()} for detailed information.
     */
    private static SortedSet<String> FILES = new TreeSet<>();
    
    /**
     * Adds all .xml files in the given directory to the list of files to test
     * coverage for.
     * 
     * @param directory the directory to scan for .xml files
     * 
     * @param recursive iff true, the directory is scanned recursively
     * @throws IOException 
     */
    private static void addDirectory(final File directory, final boolean recursive) {
        for (final File file : FileUtils.listFiles(directory, new String[] { "xml" }, recursive)) {
            try {
                String canonicalPath = file.getCanonicalPath();
                FILES.add(canonicalPath);
            } catch (final IOException e) {
                LOG.error("Failed to get canonical file for {}", file, e);
                assert false;
            }
        }
    }
    
    /**
     * Ignores a file in the coverage test by removing it from the list of files
     * to check coverage for.
     * 
     * @param file the file to ignore
     */
    private static void ignoreFile(final File file) {
        try {
            final String canonicalPath = file.getCanonicalPath();
            LOG.debug("removing {}", canonicalPath);
            FILES.remove(canonicalPath);
            assert !FILES.contains(canonicalPath);
        } catch (final IOException e) {
            LOG.error("Failed to get canonical file for {}", file, e);
            assert false;
        }
    }
    
    static {
        addDirectory(getDaemonEtcDirectory(), true);

        LOG.debug("FILES.size() = {}", FILES.size());
        final File droolsDirectory = new File(getDaemonEtcDirectory().getAbsolutePath() + File.separator + "examples" + File.separator + "drools-engine.d" + File.separator + "nodeParentRules");
        System.err.println("drools directory = " + droolsDirectory);
        ignoreFile(new File(droolsDirectory, "drools-engine.xml"));
        ignoreFile(new File(droolsDirectory, "nodeParentRules-context.xml"));
        ignoreFile(new File(droolsDirectory, "locationMonitorRules-context.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "examples/nsclient-config.xml"));

        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/ApacheHTTPD.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/LinuxKernel.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/NetgearProsafeSmartSwitch.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/OpenSSH.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/OpenWrt.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/Procmail.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/POSIX.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/Postfix.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/Sudo.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "log4j2.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "opennms-activemq.xml"));
        LOG.debug("FILES.size() = {}", FILES.size());
    }
    
    /**
     * Returns the list of files to check coverage for.
     * 
     * The returned file list is stored in {@link #FILES} which is filled in the
     * static constructor.
     * 
     * For each XML file to test, this method must return an entry in the list.
     * Each entry consists of the following parts:
     * <ul>
     *   <li>The {@link File} to check coverage for</li>
     * </ul>
     * 
     * @return list of parameters for the test
     */
    @Parameterized.Parameters
    public static Collection<Object[]> files() {
        ImmutableList<Object[]> list = ImmutableList.copyOf(Collections2.transform(FILES, new Function<String, Object[]>() {
            @Override
            public Object[] apply(final String file) {
                return new Object[] {file};
            }
        }));
        return list;
    }
    
    /**
     * The set of files covered by the {@link WillItUnmarshalIT}.
     */
    final static Set<File> COVERED_FILES = new HashSet<>();
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Get the constructor of test to create the instances - we can assume
        // that there is only one constructor as JUnit requires it.
        @SuppressWarnings("unchecked")
        final Constructor<WillItUnmarshalIT> constructor = (Constructor<WillItUnmarshalIT>) WillItUnmarshalIT.class.getConstructors()[0];
        
        // Build set of covered files
        for (final Object[] parameters : WillItUnmarshalIT.files()) {
            // Create instance of test
            final WillItUnmarshalIT test = constructor.newInstance(parameters);
            
            // Get the file for the resource used by the test instance and add
            // it to the set of covered files
            COVERED_FILES.add(test.createResource().getFile());
        }
    }
    
    /**
     * The file to test coverage for.
     */
    private final File file;

    public WillItUnmarshalCoverageMetaIT(final String file) {
        this.file = new File(file);
    }

    /**
     * Tests if the current file is in the set of covered files.
     * 
     * If this test fails, you should add an entry for the uncovered file to
     * {@link WillItUnmarshalIT#FILES} in the static constructor of
     * {@link WillItUnmarshalIT} or mark the file as ignored by calling
     * {@link #ignoreFile(java.io.File)} in the static constructor of
     * {@link WillItUnmarshalMetaTest}.
     */
    @Test
    public void testCoverage() {
        // Check if the file is in the set of covered files
        assertTrue("File is not covered: " + file, COVERED_FILES.contains(file));
    }
}
