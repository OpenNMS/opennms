/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.fileutils;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

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
        await().atMost(5, SECONDS).pollDelay(0, SECONDS).pollInterval(2, SECONDS)
        .untilTrue(reloadCalled);
        fileWatcher.destroy();
    }

}
