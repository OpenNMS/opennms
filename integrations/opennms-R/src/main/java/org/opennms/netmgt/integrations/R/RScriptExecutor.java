/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.integrations.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.input.CharSequenceInputStream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.RowSortedTable;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * The RScriptExecutor provides an interface for invoking  R scripts via
 * system commands. This provides a simple alternative to using rJava,
 * which relies on JNI bits.
 * 
 * The interface supports passing and retrieving values in a tabular format,
 * as well as arbitrary arguments.
 * 
 * The values are sent and retrieved from the script via stdin/stdout.
 * 
 * The arguments are baked into the script using Freemarker tags. 
 * 
 * @author jwhite
 */
public class RScriptExecutor {
    /**
     * The Rscript binary used to execute the .R scripts.
     */
    public static final String RSCRIPT_BINARY = System.getProperty("rscript.binary", "Rscript");

    /**
     * Maximum runtime of the Rscript process in milliseconds before failing and
     * throwing an exception.
     */
    public static final long SCRIPT_TIMEOUT_MS = 60000;

    private Configuration m_freemarkerConfiguration;

    public RScriptExecutor() {
        setupFreemarker();
    }

    private void setupFreemarker() {
        m_freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_21);
        m_freemarkerConfiguration.setTemplateLoader(new HybridTemplateLoader());
        m_freemarkerConfiguration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        m_freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * Executes by given script by:
     *   - Searching both the classpath and the filesystem for the path
     *   - Copying the script at the given path to a temporary file and
     *     performing variable substitution with the arguments using Freemarker.
     *   - Invoking the script with commons-exec
     *   - Converting the input table to CSV and passing this to the process via stdin
     *   - Parsing stdout, expecting CSV output, and converting this to an immutable table
     */
    public RScriptOutput exec(String script, RScriptInput input) throws RScriptException {
        Preconditions.checkNotNull(script, "script argument");
        Preconditions.checkNotNull(input, "input argument");

        // Grab the script/template
        Template template;
        try {
            template = m_freemarkerConfiguration.getTemplate(script);
        } catch (IOException e) {
            throw new RScriptException("Failed to read the script.", e);
        }

        // Create a temporary file
        File scriptOnDisk;
        try {
            scriptOnDisk = File.createTempFile("Rcsript", "R");
            scriptOnDisk.deleteOnExit();
        } catch (IOException e) {
            throw new RScriptException("Failed to create a temporary file.", e);
        }

        // Perform variable substitution and write the results to the temporary file
        try (FileOutputStream fos = new FileOutputStream(scriptOnDisk);
                Writer out = new OutputStreamWriter(fos);) {
            template.process(input.getArguments(), out);
        } catch (IOException | TemplateException e) {
            scriptOnDisk.delete();
            throw new RScriptException("Failed to process the template.", e);
        }

        // Convert the input matrix to a CSV string which will be passed to the script via stdin.
        // The table may be large, so we try and avoid writing it to disk
        final StringBuilder inputTableAsCsv;
        try {
            inputTableAsCsv = toCsv(input.getTable());
        } catch (IOException e) {
            scriptOnDisk.delete();
            throw new RScriptException("Failed to convert the input table to CSV.", e);
        }

        // Invoke Rscript against the script (located in a temporary file)
        CommandLine cmdLine = new CommandLine(RSCRIPT_BINARY);
        cmdLine.addArgument(scriptOnDisk.getAbsolutePath());

        // Use commons-exec to execute the process
        DefaultExecutor executor = new DefaultExecutor();

        // Use the CharSequenceInputStream in order to avoid explicitly converting
        // the StringBuilder a string and then an array of bytes.
        InputStream stdin = new CharSequenceInputStream(inputTableAsCsv, StandardCharsets.UTF_8);
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr, stdin));

        // Fail if we get a non-zero exit code
        executor.setExitValue(0);

        // Fail if the process takes too long
        ExecuteWatchdog watchdog = new ExecuteWatchdog(SCRIPT_TIMEOUT_MS);
        executor.setWatchdog(watchdog);

        // Execute
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            scriptOnDisk.delete();
            throw new RScriptException("An error occured while executing Rscript, or the requested script.",
                    inputTableAsCsv.toString(), stderr.toString(), stdout.toString(), e);
        }

        // Parse and return the results
        try {
            ImmutableTable<Long, String, Double> table = fromCsv(stdout.toString());
            return new RScriptOutput(table);
        } catch (Throwable t) {
            throw new RScriptException("Failed to parse the script's output.",
                    inputTableAsCsv.toString(), stderr.toString(), stdout.toString(), t);
        } finally {
            scriptOnDisk.delete();
        }
    }

    /**
     * Convert the CSV string to an immutable table.
     */
    protected static ImmutableTable<Long, String, Double> fromCsv(final String csv) throws IOException {
        ImmutableTable.Builder<Long, String, Double> builder = ImmutableTable.builder();
        try (StringReader reader = new StringReader(csv);
                CSVParser parser = new CSVParser(reader,
                        CSVFormat.RFC4180.withHeader());) {
            long rowIndex = 0;
            Map<String, Integer> headerMap = parser.getHeaderMap();
            for (CSVRecord record : parser) {
                for (String key : headerMap.keySet()) {
                    Double value;
                    try {
                        value = Double.valueOf(record.get(key));
                    } catch (NumberFormatException e) {
                        value = Double.NaN;
                    }

                    builder.put(rowIndex, key, value);
                }
                rowIndex++;
            }
        }
        return builder.build();
    }

    /**
     * Convert the table to a CSV string.
     */
    protected static StringBuilder toCsv(final RowSortedTable<Long, String, Double> table) throws IOException {
        final String columnNames[] = table.columnKeySet().toArray(new String[]{});

        final StringBuilder sb = new StringBuilder();
        final CSVPrinter printer = CSVFormat.RFC4180.withHeader(columnNames).print(sb);

        for (long rowIndex : table.rowKeySet()) {
            for (String columnName : columnNames) {
                Double value = table.get(rowIndex, columnName);
                if (value == null) {
                    value = Double.NaN;
                }
                printer.print(value);
            }
            printer.println();
        }

        return sb;
    }
}
