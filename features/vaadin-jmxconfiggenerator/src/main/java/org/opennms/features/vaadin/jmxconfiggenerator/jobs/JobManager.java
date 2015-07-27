package org.opennms.features.vaadin.jmxconfiggenerator.jobs;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
                       UIHelper.getCurrent().hideProgressWindow();
                   }
               });
            }
        }

        private void handleError(final TaskRunException trex) {
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                    LOG.error(trex.getMessage(), trex.getCause());

                    StringBuilder errorMessage = new StringBuilder(100);
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
