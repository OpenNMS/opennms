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
package org.opennms.core.fileutils;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUpdateWatcherTest {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private AtomicBoolean reloadCalled = new AtomicBoolean(false);
    
    private File testFile;
    
    private FileUpdateWatcher fileWatcher;
    
    @Before
    public void before() throws IOException {
        // filewatcher doesn't work right on OSX
        Assume.assumeFalse(System.getProperty("os.name").startsWith("Mac OS X"));

        testFile = tempFolder.newFile("testWatcher.log");
        fileWatcher = new FileUpdateWatcher(testFile.getAbsolutePath(), fileReload());

    }

    private FileUpdateCallback fileReload() {
        return new FileUpdateCallback() {

            @Override
            public void reload() {
                reloadCalled.set(true);     
            }
            
        };
    }

    @Test
    public void testFileUpdateWatcher() throws IOException {
        
        String hello = "Hello";
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
        writer.write(hello);
        writer.close();
        Awaitility.await().atMost(5, SECONDS).pollDelay(0, SECONDS).pollInterval(2, SECONDS)
        .untilTrue(reloadCalled);
        fileWatcher.destroy();
    }

}
