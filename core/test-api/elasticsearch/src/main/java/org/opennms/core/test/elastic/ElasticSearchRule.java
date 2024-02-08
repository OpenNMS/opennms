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
package org.opennms.core.test.elastic;

import static org.opennms.core.test.elastic.ElasticSearchServerConfig.ES_HTTP_PORT;

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

    public ElasticSearchRule() {
        this(new ElasticSearchServerConfig());
    }

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
        createHomeDirectory();

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


    private void createHomeDirectory() {
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

    public String getUrl() {
        return String.format("http://localhost:%s", ES_HTTP_PORT);
    }
}