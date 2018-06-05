/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.jobs;

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
            this.taskToRun = taskToRun;
        }

        @Override
        public void run() {
            try {
                final Object result = taskToRun.execute();

                UI.getCurrent().access(new Runnable() {
                    @Override
                    public void run() {
                        UIHelper.getCurrent(JmxConfigGeneratorUI.class).hideProgressWindow();
                        taskToRun.onSuccess(result);
                    }
                });
            } catch (TaskRunException trex) {
                handleError(trex);
            } catch (Exception ex) {
                handleError(ex);
            } finally {
               UI.getCurrent().access(new Runnable() {
                   @Override
                   public void run() {
                       UIHelper.getCurrent(JmxConfigGeneratorUI.class).hideProgressWindow();
                   }
               });
            }
        }

        private void handleError(final TaskRunException trex) {
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
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
                }
            });
        }

        private void handleError(final Exception ex) {
           UI.getCurrent().access(new Runnable() {
               @Override
               public void run() {
                   LOG.error("Unknown Error occurred", ex);
                   UIHelper.showNotification("Unknown Error", ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                   taskToRun.onError();
               }
           });
        }

    }

    public static class TaskRunException extends Exception {

        private String shortDescription;

        public TaskRunException(String shortDescription, String message, Throwable cause) {
            super(message, cause);
            this.shortDescription = shortDescription;
        }

        public String getShortDescription() {
            return shortDescription;
        }

        public TaskRunException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public interface Task<T> {

        T execute() throws TaskRunException;

        void onSuccess(T result);

        void onError();

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
