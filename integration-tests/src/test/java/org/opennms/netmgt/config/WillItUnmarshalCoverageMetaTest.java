package org.opennms.netmgt.config;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import static org.opennms.core.test.ConfigurationTestUtils.getDaemonEtcDirectory;

/**
 * This is a meta test, testing the coverage of {@link WillItUnmarshalTest}.
 *
 * The {@link WillItUnmarshalTest} uses parameterized tests to check
 * unmarshalling of provided config files. To ensure the checked list of files
 * covers all files in the example directories, this test fetches the file list
 * of the directories to test and checks them against the defined files to test.
 *
 * @author Dustin Frisch<fooker@lab.sh>
 */
@RunWith(value = Parameterized.class)
public class WillItUnmarshalCoverageMetaTest {
    
    /**
     * A set of test parameters to execute.
     * 
     * See {@link #files()} for detailed information.
     */
    private static Set<File> FILES = new HashSet<File>();
    
    /**
     * Adds all .xml files in the given director to the list of files to test
     * coverage for.
     * 
     * @param directory the directory to scan for .xml files
     * 
     * @param recursive iff true, the directory is scanned recursively
     */
    private static void addDirectory(final File directory, final boolean recursive) {
        for (final File file : FileUtils.listFiles(directory,
                                                   new String[] { "xml" },
                                                   recursive)) {
            FILES.add(file);
        }
    }
    
    /**
     * Ignores a file in the coverage test by removing it from the list of files
     * to check coverage for.
     * 
     * @param file the file to ignore
     */
    private static void ignoreFile(final File file) {
        assert FILES.remove(file);
    }
    
    static {
        addDirectory(getDaemonEtcDirectory(), true);
        
        addDirectory(new File(getDaemonEtcDirectory(), "examples"), true);
        addDirectory(new File(getDaemonEtcDirectory(), "events"), true);
        addDirectory(new File(getDaemonEtcDirectory(), "datacollection"), true);
        
        ignoreFile(new File(getDaemonEtcDirectory(), "examples/correlation-engine.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "examples/drools-engine.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "examples/nodeParentRules-context.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "examples/nsclient-config.xml"));
        
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/ApacheHTTPD.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/LinuxKernel.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/OpenSSH.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/POSIX.syslog.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "syslog/Sudo.syslog.xml"));
        
        ignoreFile(new File(getDaemonEtcDirectory(), "log4j2.xml"));
        ignoreFile(new File(getDaemonEtcDirectory(), "log4j2-archive-events.xml"));
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
        return ImmutableList.copyOf(Collections2.transform(FILES, new Function<File, Object[]>() {
            @Override
            public Object[] apply(final File file) {
                return new Object[] {file};
            }
        }));
    }
    
    /**
     * The set of files covered by the {@link WillItUnmarshalTest}.
     */
    final static Set<File> COVERED_FILES = new HashSet<File>();
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Get the constructor of test to create the instances - we can assume
        // that there is only one constructor as JUnit requires it.
        final Constructor<WillItUnmarshalTest> constructor = (Constructor<WillItUnmarshalTest>) WillItUnmarshalTest.class.getConstructors()[0];
        
        // Build set of covered files
        for (final Object[] parameters : WillItUnmarshalTest.files()) {
            // Create instance of test
            final WillItUnmarshalTest test = constructor.newInstance(parameters);
            
            // Get the file for the resource used by the test instance and add
            // it to the set of covered files
            COVERED_FILES.add(test.createResource().getFile());
        }
    }
    
    /**
     * The file to test coverage for.
     */
    private final File file;

    public WillItUnmarshalCoverageMetaTest(final File file) {
        this.file = file;
    }

    /**
     * Tests if the current file is in the set of covered files.
     * 
     * If this test fails, you should add an entry for the uncovered file to
     * {@link WillItUnmarshalTest#FILES} in the static constructor of
     * {@link WillItUnmarshalTest} or mark the file as ignored by calling
     * {@link #ignoreFile(java.io.File)} in the static constructor of
     * {@link WillItUnmarshalMetaTest}.
     */
    @Test
    public void testCoverage() {
        // Check if the file is in the set of covvered files
        assertTrue("File is not covered: " + file, COVERED_FILES.contains(file));
    }
}
