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

package org.opennms.netmgt.provision.service.jmx;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.opennms.netmgt.provision.service.Provisioner;
import org.opennms.netmgt.provision.service.jmx.statistics.QuartzJobStatistics;
import org.opennms.netmgt.provision.service.jmx.statistics.ThreadPoolExecutorStatistics;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;

public class Provisiond extends AbstractSpringContextJmxServiceDaemon<Provisioner> implements ProvisiondMBean {

    @Override
    public CompositeData getRequisitionImportScheduleMetaData() {
        try {
            SchedulerMetaData metaData = getDaemon().getImportSchedule().getScheduler().getMetaData();
            CompositeData compositeData = JmxUtils.toCompositeData(metaData);
            return compositeData;
        } catch (SchedulerException e) {
            // swallow
        }
        return null;
    }
    
    @Override
    public TabularData getRequisitionImportScheduleDetails() {
        final Scheduler scheduler = getDaemon().getImportSchedule().getScheduler();
        final List<QuartzJobStatistics> jobStatistics = new ArrayList<>();
        try {
            for (String jobGroup : scheduler.getJobGroupNames()) {
                for (String jobName : scheduler.getJobNames(jobGroup)) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobName, jobGroup);
                    Trigger trigger = scheduler.getTrigger(jobName, jobGroup);

                    if (jobDetail != null && trigger != null) {
                        jobStatistics.add(new QuartzJobStatistics(jobDetail, trigger));
                    }
                }
            }
        } catch (SchedulerException ex) {
            // swallow
        }
        TabularData tableData = JmxUtils.toTabularData(jobStatistics);
        return tableData;
    }

    // TODO MVR NodeScan's sheduled (can be defined by the scan interval in foreign source, should probably also be exported)
    @Override
    public long getScheduleLength() {
        return getDaemon().getScheduleLength();
    }

    @Override
    public String getDefaultThreadPoolExecutor() {
        DefaultTaskCoordinator taskCoordinator = getDaemon().getTaskCoordinator();
        return taskCoordinator.getDefaultExecutor();
    }

    // TODO MVR should return useful data (ProvisiondScheduler Queue)
    @Override
    public BlockingQueue<Future<Runnable>> getProvisiondSchedulerQueue() {
        DefaultTaskCoordinator taskCoordinator = getDaemon().getTaskCoordinator();
        return taskCoordinator.getQueue();
    }

    @Override
    public TabularData getThreadPoolExecutorStatistics() {
        final List<ThreadPoolExecutorStatistics> statistics = new ArrayList<>();
        final ConcurrentHashMap<String, CompletionService<Runnable>> taskCompletionServices = getDaemon().getTaskCoordinator().getTaskCompletionServices();
        for (Map.Entry<String, CompletionService<Runnable>> entry : taskCompletionServices.entrySet()) {
            // now it gets ugly
            try {
                Field executorField = entry.getValue().getClass().getDeclaredField("executor");
                executorField.setAccessible(true); // force access
                Executor executor = (Executor) executorField.get(entry.getValue());
                if (executor instanceof ThreadPoolExecutor) {
                    statistics.add(new ThreadPoolExecutorStatistics(entry.getKey(), (ThreadPoolExecutor) executor));
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                // TODO MVR logging
                e.printStackTrace();
            }
        }
        final TabularData tableData = JmxUtils.toTabularData(statistics);
        return tableData;
    }

    @Override
    protected String getSpringContext() {
        return "provisiondContext";
    }

    @Override
    protected String getLoggingPrefix() {
        return "provisiond";
    }

}
