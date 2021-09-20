/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.opennms.core.logging.Logging;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.Detector;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.Parameter;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingSweepRequestBuilder;
import org.opennms.netmgt.icmp.proxy.PingSweepSummary;
import org.opennms.netmgt.model.discovery.IPPollRange;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DiscoveryTaskExecutorImpl implements DiscoveryTaskExecutor {
    
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryTaskExecutorImpl.class);

    @Autowired
    private RangeChunker rangeChunker;

    @Autowired
    private LocationAwarePingClient locationAwarePingClient;

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired(required = false)
    private LocationAwareDetectorClient locationAwareDetectorClient;

    private final AtomicInteger taskIdTracker = new AtomicInteger();


    @Override
    public CompletableFuture<Void> handleDiscoveryTask(DiscoveryConfiguration config) {
        // Use the range chunker to generate a series of jobs, keyed by location
        final Map<String, List<DiscoveryJob>> jobsByLocation = rangeChunker.chunk(config);

        // Avoid any further processing if there are no ranges to scan
        if (jobsByLocation.size() == 0) {
            LOG.info("No IP addresses to discover.");
            return CompletableFuture.completedFuture(null);
        }

        // Generate a unique id for this task, used to correlate the log messages
        final int taskId = taskIdTracker.incrementAndGet();

        // Asynchronously run the jobs at each location:
        //   Each location will be processed in parallel
        //   The jobs at each location will be processed in series
        final List<CompletableFuture<Void>> futures = new ArrayList<>(jobsByLocation.keySet().size());

        // Set the logging context so that our messages always appear in the same log file, even
        // when we are invoked from the web app or another context
        Logging.withPrefix(Discovery.getLoggingCategory(), new Runnable() {
            @Override
            public void run() {
                jobsByLocation.entrySet().stream()
                    .map(e -> triggerJobsAsync(e.getKey(), e.getValue(), taskId))
                    .forEach(futures::add);
            }
        });

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }

    private CompletableFuture<Void> triggerJobsAsync(String location, List<DiscoveryJob> jobs, int taskId) {
        LOG.debug("Processing {} jobs at location {} (on task #{}).", jobs.size(), location, taskId);

        // Track the jobs
        final Queue<DiscoveryJob> queue = new LinkedList<>(jobs);
        final AtomicInteger jobIndexTracker = new AtomicInteger();
        final CompletableFuture<Void> future = new CompletableFuture<>();

        // Trigger the first job, which will automatically trigger the next job when complete
        triggerNextJobAsync(location, queue, jobIndexTracker, jobs.size(), taskId, future);

        return future;
    }

    private void triggerNextJobAsync(String location, Queue<DiscoveryJob> jobs, AtomicInteger jobIndexTracker, int totalNumberOfJobs, int taskId, CompletableFuture<Void> future) {
        final DiscoveryJob job = jobs.poll();
        if (job == null) {
            future.complete(null);
            return;
        }

        // Build the request
        final PingSweepRequestBuilder builder = locationAwarePingClient.sweep()
            .withLocation(job.getLocation())
            .withPacketsPerSecond(job.getPacketsPerSecond());
        for(IPPollRange range : job.getRanges()) {
            try {
                InetAddress begin = InetAddress.getByAddress(range.getAddressRange().getBegin());
                InetAddress end = InetAddress.getByAddress(range.getAddressRange().getEnd());
                builder.withRange(begin, end, range.getRetries(), range.getTimeout(), TimeUnit.MILLISECONDS);
            } catch (UnknownHostException e) {
                LOG.error("Failed to retrieve addresses from range: {}. The range will be skipped.", e);
            }
        }

        final int jobIndex = jobIndexTracker.incrementAndGet();
        LOG.debug("Starting job {} of {} at location {} (on task #{}).", jobIndex, totalNumberOfJobs, location, taskId);
        builder.execute().whenComplete((summary, ex) -> {
            // When finished, used the calling thread to generate the newSuspect events
            Logging.withPrefix(Discovery.getLoggingCategory(), new Runnable() {
                @Override
                public void run() {
                    if (summary != null) {
                        // Perform detection if there are any detectors associated with these IP addresses.
                        CompletableFuture<List<DiscoveryResult>> resultsFuture = performDetection(job.getLocation(), summary, job.getConfig());

                        //Send new suspect events after detection completed for all the IP addresses in the job.
                        resultsFuture.whenComplete((results, throwable) -> {

                            LOG.debug("Job {} of {} at location {} (on task #{}) completed succesfully.",
                                    jobIndex, totalNumberOfJobs, location, taskId);
                            // Generate an event log containing a newSuspect event for every host
                            // that responded to our pings and succeeded detection if there are any detectors associated with an IP Address.
                            final Log eventLog = toNewSuspectEvents(job, results);
                            // Avoid forwarding an empty log
                            if (eventLog.getEvents() != null && eventLog.getEvents().getEventCount() >= 1) {
                                eventForwarder.sendNow(eventLog);
                            }
                            // Recurse until the queue is empty
                            triggerNextJobAsync(location, jobs, jobIndexTracker, totalNumberOfJobs, taskId, future);
                        });

                    } else {
                        LOG.error("An error occurred while processing job {} of {} at location {} (on task #{})."
                                + " No newSuspect events will be generated.", jobIndex, totalNumberOfJobs, location, taskId, ex);
                        // Recurse until the queue is empty
                        triggerNextJobAsync(location, jobs, jobIndexTracker, totalNumberOfJobs, taskId, future);
                    }
                }
            });
        });
    }

    private static Log toNewSuspectEvents(DiscoveryJob job, List<DiscoveryResult> results) {
        final Log eventLog = new Log();
        for (DiscoveryResult entry : results) {
            EventBuilder eb = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, Discovery.DAEMON_NAME);
            eb.setInterface(entry.getAddress());
            eb.addParam("RTT", entry.getPingDuration());
            if (job.getForeignSource() != null) {
                eb.addParam(EventConstants.PARM_FOREIGN_SOURCE, job.getForeignSource());
            }
            if (job.getLocation() != null) {
                eb.addParam(EventConstants.PARM_LOCATION, job.getLocation());
            }
            eventLog.addEvent(eb.getEvent());

        }
        return eventLog;
    }

    /**
     *  This method performs detection for all the IP Addressed that succeeded ping in parallel and returns a
     *  completable future with discovery results that succeeded detection.
     */
    private CompletableFuture<List<DiscoveryResult>> performDetection(String location, PingSweepSummary summary, DiscoveryConfiguration config) {

        List<CompletableFuture<DiscoveryResult>> futures = summary.getResponses().entrySet().stream()
                .map(entry -> launchDetectors(entry.getKey(), entry.getValue(), location, config)).collect(Collectors.toList());
        // Combine all futures.
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        CompletableFuture<List<DiscoveryResult>> futureList = allFutures.thenApply(result -> {
            return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        });
        return futureList.thenApply((results) -> {
             return results.stream().filter(DiscoveryResult::getDetectResult).collect(Collectors.toList());
        });
    }

    /**
     * This method calls all the detectors in parallel for a given IP Address and combines the result of all detectors
     * into single future.
     */
    private CompletableFuture<DiscoveryResult> launchDetectors(InetAddress inetAddress, Double pingDuration, String location, DiscoveryConfiguration config) {

        CompletableFuture<DiscoveryResult> future = new CompletableFuture<>();
        DiscoveryConfigFactory configFactory = new DiscoveryConfigFactory(config);
        List<Detector> detectors = configFactory.getListOfDetectors(inetAddress, location);
        if (detectors.size() > 0) {
            // Run all the detectors.
            try {
                List<CompletableFuture<Boolean>> futures = detectors.stream().map(detector ->
                        detect(detector, inetAddress, location)).collect(Collectors.toList());
                // Combine all futures.
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
                CompletableFuture<List<Boolean>> futureList = allFutures.thenApply(result -> {
                    return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
                });
                future = futureList.thenApply((results) -> {
                    boolean allResult = results.stream().allMatch(result -> result);
                    return new DiscoveryResult(allResult, inetAddress, pingDuration);
                });
                return future;
            } catch (Exception e) {
                LOG.error("Exception while performing detection in discovery for IP Address {} at location {}",
                        inetAddress.getHostAddress(), location, e);
                future.complete(new DiscoveryResult(false, inetAddress, pingDuration));
            }
        }  else {
            //When there are no detectors, The discovery is just ping itself, make DiscoveryResult successful.
            future.complete(new DiscoveryResult(true, inetAddress, pingDuration));
        }
        return future;
    }

    /**
     * This calls detection asynchronously for a given detector and returns a completable future.
     */
    private CompletableFuture<Boolean> detect(Detector detector, InetAddress inetAddress, String location) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            LOG.info("Attemping to detect '{}' service on IP Address {} at location {}", detector.getName(),
                    inetAddress.getHostAddress(), location);
            Map<String, String> attributes = detector.getParameters().stream()
                    .collect(Collectors.toMap(Parameter::getKey, Parameter::getValue));
            CompletableFuture<Boolean> result = getLocationAwareDetectorClient().detect()
                    .withAddress(inetAddress)
                    .withClassName(detector.getClassName())
                    .withLocation(location)
                    .withAttributes(attributes)
                    .execute();
            result.whenComplete((response, ex) -> {
                Logging.withPrefix(Discovery.getLoggingCategory(), new Runnable() {
                    @Override
                    public void run() {
                        if (ex != null) {
                            // Don't throw exception as Future join won't like.
                            LOG.error("Exception while detecting '{}' service on IP Address {} at location {} ",
                                    detector.getName(), inetAddress.getHostAddress(), location, ex);
                            future.complete(false);
                        } else {
                            LOG.info("Service Detection {} for service '{}' on IP Address {} at location {}",
                                    response ? "succeeded" : "failed", detector.getName(), inetAddress.getHostAddress(), location);
                            future.complete(response);
                        }
                    }
                });

            });
        } catch (Exception e) {
            LOG.error("Exception while detecting {} service on IP Address {} at location {} ",
                    detector.getName(), inetAddress.getHostAddress(), location, e);
            future.complete(false);
        }
        return future;
    }

    public void setRangeChunker(RangeChunker rangeChunker) {
        this.rangeChunker = rangeChunker;
    }

    public void setLocationAwarePingClient(LocationAwarePingClient locationAwarePingClient) {
        this.locationAwarePingClient = locationAwarePingClient;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    public void setLocationAwareDetectorClient(LocationAwareDetectorClient locationAwareDetectorClient) {
        this.locationAwareDetectorClient = locationAwareDetectorClient;
    }

    public LocationAwareDetectorClient getLocationAwareDetectorClient() {
        if (this.locationAwareDetectorClient == null) {
            return BeanUtils.getBean("provisiondContext", "locationAwareDetectorClient", LocationAwareDetectorClient.class);
        }
        return locationAwareDetectorClient;
    }
}
