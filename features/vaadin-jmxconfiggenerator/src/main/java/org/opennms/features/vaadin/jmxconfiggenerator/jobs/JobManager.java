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
package org.opennms.features.vaadin.jmxconfiggenerator.jobs;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opennms.netmgt.vaadin.core.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.JmxConfigGeneratorUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class JobManager {

    private final static Logger LOG = LoggerFactory.getLogger(JobManager.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Future<?> runningJob;

    // Abstraction to run a Task
    private static class TaskRunnable implements Runnable {

        private final Task taskToRun;

        private TaskRunnable(Task taskToRun) {
            this.taskToRun = Objects.requireNonNull(taskToRun);
        }

        @Override
        public void run() {
            final JmxConfigGeneratorUI ui = taskToRun.getUI();
            try {
                final Object result = taskToRun.execute();
                ui.access(() -> {
                    ui.hideProgressWindow();
                    taskToRun.onSuccess(result);
                });
            } catch (TaskRunException trex) {
                handleError(ui, trex);
            } catch (Exception ex) {
                handleError(ui, ex);
            } finally {
               ui.access(ui::hideProgressWindow);
            }
        }

        private void handleError(final UI ui, final TaskRunException trex) {
            ui.access(() -> {
                LOG.error(trex.getMessage(), trex.getCause());

                final StringBuilder errorMessage = new StringBuilder(100);
                errorMessage.append(trex.getMessage());
                if (trex.getCause() != null) {
                    errorMessage.append("<br/><br/>").append(trex.getCause().getMessage());
                }

                UIHelper.showNotification(trex.getShortDescription() != null ? trex.getShortDescription() : "Error",
                        errorMessage.toString().trim(),
                        Notification.Type.ERROR_MESSAGE,
                        5000);
                taskToRun.onError();
            });
        }

        private void handleError(final UI ui, final Exception ex) {
          ui.access(() -> {
              LOG.error("Unknown Error occurred", ex);
              UIHelper.showNotification("Unknown Error", ex.getMessage(), Notification.Type.ERROR_MESSAGE);
              taskToRun.onError();
          });
        }

    }

    public void enqueue(Task task) {
        if (runningJob != null && !runningJob.isDone()) {
            LOG.warn("A job is already running. Try cancelling...");
            boolean success = runningJob.cancel(true);
            LOG.warn("Job was cancelled: {}", success);
        }

        runningJob = executorService.submit(new TaskRunnable(task));
    }

    public void cancelAllJobs() {
        if (runningJob != null) {
            runningJob.cancel(true);
            if (!runningJob.isDone()) {
                LOG.error("The currently running Job could not be stopped.", runningJob);
            }
        }
    }
}
