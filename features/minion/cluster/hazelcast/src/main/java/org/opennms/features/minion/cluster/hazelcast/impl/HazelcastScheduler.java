/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.minion.cluster.hazelcast.impl;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.minion.cluster.api.Scheduler;
import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;

public class HazelcastScheduler implements Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastScheduler.class);

    private final HazelcastInstance hazelcast;

    public HazelcastScheduler(final HazelcastInstance hazelcast) {
        this.hazelcast = Objects.requireNonNull(hazelcast);
    }

    @Override
    public Task schedule(final String key,
                         final Duration interval,
                         final Runnable task) {
        return null;
    }

    public class JobStoreImpl implements JobStore {
        @Override
        public void initialize(final ClassLoadHelper classLoadHelper, final SchedulerSignaler schedulerSignaler) throws SchedulerConfigException {

        }

        @Override
        public void schedulerStarted() throws SchedulerException {

        }

        @Override
        public void schedulerPaused() {

        }

        @Override
        public void schedulerResumed() {

        }

        @Override
        public void shutdown() {

        }

        @Override
        public boolean supportsPersistence() {
            return false;
        }

        @Override
        public long getEstimatedTimeToReleaseAndAcquireTrigger() {
            return 0;
        }

        @Override
        public boolean isClustered() {
            return false;
        }

        @Override
        public void storeJobAndTrigger(final JobDetail jobDetail, final OperableTrigger operableTrigger) throws ObjectAlreadyExistsException, JobPersistenceException {

        }

        @Override
        public void storeJob(final JobDetail jobDetail, final boolean b) throws ObjectAlreadyExistsException, JobPersistenceException {

        }

        @Override
        public void storeJobsAndTriggers(final Map<JobDetail, Set<? extends Trigger>> map, final boolean b) throws ObjectAlreadyExistsException, JobPersistenceException {

        }

        @Override
        public boolean removeJob(final JobKey jobKey) throws JobPersistenceException {
            return false;
        }

        @Override
        public boolean removeJobs(final List<JobKey> list) throws JobPersistenceException {
            return false;
        }

        @Override
        public JobDetail retrieveJob(final JobKey jobKey) throws JobPersistenceException {
            return null;
        }

        @Override
        public void storeTrigger(final OperableTrigger operableTrigger, final boolean b) throws ObjectAlreadyExistsException, JobPersistenceException {

        }

        @Override
        public boolean removeTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
            return false;
        }

        @Override
        public boolean removeTriggers(final List<TriggerKey> list) throws JobPersistenceException {
            return false;
        }

        @Override
        public boolean replaceTrigger(final TriggerKey triggerKey, final OperableTrigger operableTrigger) throws JobPersistenceException {
            return false;
        }

        @Override
        public OperableTrigger retrieveTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
            return null;
        }

        @Override
        public boolean checkExists(final JobKey jobKey) throws JobPersistenceException {
            return false;
        }

        @Override
        public boolean checkExists(final TriggerKey triggerKey) throws JobPersistenceException {
            return false;
        }

        @Override
        public void clearAllSchedulingData() throws JobPersistenceException {

        }

        @Override
        public void storeCalendar(final String s, final Calendar calendar, final boolean b, final boolean b1) throws ObjectAlreadyExistsException, JobPersistenceException {

        }

        @Override
        public boolean removeCalendar(final String s) throws JobPersistenceException {
            return false;
        }

        @Override
        public Calendar retrieveCalendar(final String s) throws JobPersistenceException {
            return null;
        }

        @Override
        public int getNumberOfJobs() throws JobPersistenceException {
            return 0;
        }

        @Override
        public int getNumberOfTriggers() throws JobPersistenceException {
            return 0;
        }

        @Override
        public int getNumberOfCalendars() throws JobPersistenceException {
            return 0;
        }

        @Override
        public Set<JobKey> getJobKeys(final GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
            return null;
        }

        @Override
        public Set<TriggerKey> getTriggerKeys(final GroupMatcher<TriggerKey> groupMatcher) throws JobPersistenceException {
            return null;
        }

        @Override
        public List<String> getJobGroupNames() throws JobPersistenceException {
            return null;
        }

        @Override
        public List<String> getTriggerGroupNames() throws JobPersistenceException {
            return null;
        }

        @Override
        public List<String> getCalendarNames() throws JobPersistenceException {
            return null;
        }

        @Override
        public List<OperableTrigger> getTriggersForJob(final JobKey jobKey) throws JobPersistenceException {
            return null;
        }

        @Override
        public Trigger.TriggerState getTriggerState(final TriggerKey triggerKey) throws JobPersistenceException {
            return null;
        }

        @Override
        public void pauseTrigger(final TriggerKey triggerKey) throws JobPersistenceException {

        }

        @Override
        public Collection<String> pauseTriggers(final GroupMatcher<TriggerKey> groupMatcher) throws JobPersistenceException {
            return null;
        }

        @Override
        public void pauseJob(final JobKey jobKey) throws JobPersistenceException {

        }

        @Override
        public Collection<String> pauseJobs(final GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
            return null;
        }

        @Override
        public void resumeTrigger(final TriggerKey triggerKey) throws JobPersistenceException {

        }

        @Override
        public Collection<String> resumeTriggers(final GroupMatcher<TriggerKey> groupMatcher) throws JobPersistenceException {
            return null;
        }

        @Override
        public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
            return null;
        }

        @Override
        public void resumeJob(final JobKey jobKey) throws JobPersistenceException {

        }

        @Override
        public Collection<String> resumeJobs(final GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
            return null;
        }

        @Override
        public void pauseAll() throws JobPersistenceException {

        }

        @Override
        public void resumeAll() throws JobPersistenceException {

        }

        @Override
        public List<OperableTrigger> acquireNextTriggers(final long l, final int i, final long l1) throws JobPersistenceException {
            return null;
        }

        @Override
        public void releaseAcquiredTrigger(final OperableTrigger operableTrigger) {

        }

        @Override
        public List<TriggerFiredResult> triggersFired(final List<OperableTrigger> list) throws JobPersistenceException {
            return null;
        }

        @Override
        public void triggeredJobComplete(final OperableTrigger operableTrigger, final JobDetail jobDetail, final Trigger.CompletedExecutionInstruction completedExecutionInstruction) {

        }

        @Override
        public void setInstanceId(final String s) {

        }

        @Override
        public void setInstanceName(final String s) {

        }

        @Override
        public void setThreadPoolSize(final int i) {

        }
    }
}
