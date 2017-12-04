/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.test.elastic;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit rule which starts an embedded elastic-search instance.
 */
public class ElasticSearchRule implements TestRule {

    /** An elastic-search cluster consisting of one node. */
    private EmbeddedElasticSearchServer eserver;

    private final ElasticSearchServerConfig config;

    public ElasticSearchRule(ElasticSearchServerConfig config) {
        this.config = config;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (!config.isManualStartup()) {
                    startServer();
                }
                try {
                    base.evaluate(); // execute the unit test
                } finally {
                    stopServer();
                    if (!config.isKeepElasticHomeAfterShutdown()) {
                        deleteHomeDirectory();
                    }
                }
            }
        };
    }

    public void startServer() throws Exception {
        createHomDirectory();

        eserver = new EmbeddedElasticSearchServer(config);
        if (config.getStartDelay() > 0) {
            startServerWithDelay(config.getStartDelay());
        } else {
            eserver.start();
        }
    }

    private void startServerWithDelay(long delayInMs) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            try { Thread.sleep(delayInMs);  } catch (InterruptedException e) { }
            try {
                eserver.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void stopServer() throws IOException {
        if (eserver != null) {
            eserver.shutdown();
        }
    }


    private void createHomDirectory() {
        deleteHomeDirectory();
        new File(config.getHomeDirectory()).mkdirs();
    }

    private void deleteHomeDirectory() {
        recursiveDelete(new File(config.getHomeDirectory()));
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        file.delete();
    }
}