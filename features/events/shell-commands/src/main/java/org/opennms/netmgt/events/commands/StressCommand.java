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

package org.opennms.netmgt.events.commands;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Used to stress the event bus.
 *
 * @author jwhite
 */
@Command(scope = "events", name = "stress", description="Stress the event bus with generated events.",detailedDescription=
        "Generate newSuspect events with increasing IP addresses:\n"
        + "\tevents:stress -u uei.opennms.org/internal/discovery/newSuspect -e 10 -s 1 -j \"i=i+1\" -j \"eb.setInterface(iputils:int2ip(167837696 + i))\"\n"
	+ "Trigger 100 alarms and reduce additional events against these:\n"
	+ "\tevents:stress -x -t 10 -e 100 -u uei.opennms.org/alarms/trigger -j \"eb.addParam('node', math:floor(math:random() * 100))\"")
@Service
public class StressCommand implements Action {

    private static final String EVENT_SOURCE = "stress";

    @Reference
    public EventForwarder eventForwarder;

    @Option(name="-e", aliases="--eps", description="events per seconds to generate per thread, defaults to 300", required=false, multiValued=false)
    int eventsPerSecondPerThread = 300;

    @Option(name="-t", aliases="--threads", description="number of threads used to generated events, defaults to 1", required=false, multiValued=false)
    int numberOfThreads = 1;

    @Option(name="-u", aliases="--uei", description="events uei", required=false, multiValued=false)
    String eventUei = "uei.opennms.org/alarms/trigger";

    @Option(name="-s", aliases="--seconds", description="number of seconds to run, defaults to 60", required=false, multiValued=false)
    int numSeconds = 60;

    @Option(name="-r", aliases="--report", description="number of seconds after which the report should be generated, defaults to 15", required=false, multiValued=false)
    int reportIntervalInSeconds = 15;

    @Option(name="-j", aliases="--jexl", description="JEXL expressions", required=false, multiValued=true)
    List<String> jexlExpressions = null;

    @Option(name="-b", aliases="--batch-size", description="The size of the log (batch size)", required=false, multiValued = false)
    int batchSize = 1;

    @Option(name="-n", aliases="--node-id", description="The node id to associate with the generated event")
    Integer eventNodeId = null;

    @Option(name="-i", aliases="--interface", description="The ip interface to associate with the generated event")
    String eventIpInterface = null;

    @Option(name="-x", aliases="--sync", description="Use synchronous instead of asynchronous calls", required=false, multiValued = false)
    boolean isSynchronous = false;

    private final MetricRegistry metrics = new MetricRegistry();

    private final Meter eventsGenerated = metrics.meter("events-generated");

    private class JexlEventGenerator extends EventGenerator {
        private final JexlContext context = new MapContext();
        private final List<Expression> expressions = new ArrayList<>();

        public JexlEventGenerator(List<String> jexlExpressions) {
            JexlEngine engine = new JexlEngine();

            Map<String, Object> functions = Maps.newHashMap();
            functions.put("iputils", IpUtils.class);
            functions.put("math", Math.class);
            engine.setFunctions(functions);

            for (String jexlExpression : jexlExpressions) {
                expressions.add(engine.createExpression( jexlExpression ));
            }
        }

        @Override
        public Event getNextEvent() {
            final EventBuilder eb = new EventBuilder(eventUei, EVENT_SOURCE);
            context.set("eb", eb);
            for (Expression expression : expressions) {
                expression.evaluate(context);
            }
            return eb.getEvent();
        }
    }

    private class EventGenerator implements Runnable {
        @Override
        public void run() {
            final RateLimiter rateLimiter = RateLimiter.create(eventsPerSecondPerThread);
            while (true) {
                Log log = new Log();
                log.setEvents(new Events());
                for (int i=0; i<batchSize; i++) {
                    log.getEvents().getEventCollection().add(getNextEvent());
                }

                rateLimiter.acquire(batchSize);
                if (isSynchronous) {
                    eventForwarder.sendNowSync(log);
                } else {
                    eventForwarder.sendNow(log);
                }
                eventsGenerated.mark(batchSize);
                if (Thread.interrupted()) {
                    break;
                }
            }
        }

        public Event getNextEvent() {
            final EventBuilder eb = new EventBuilder(eventUei, EVENT_SOURCE);
            if (eventNodeId != null) {
                eb.setNodeid(eventNodeId.intValue());
            }
            if (eventIpInterface != null) {
                eb.setInterface(InetAddressUtils.addr(eventIpInterface));
            }
            return eb.getEvent();
        }
    }

    @Override
    public Object execute() {
        // Apply sane lower bounds to all of the configurable options
        eventsPerSecondPerThread = Math.max(1, eventsPerSecondPerThread);
        numberOfThreads = Math.max(1, numberOfThreads);
        numSeconds = Math.max(1, numSeconds);
        reportIntervalInSeconds = Math.max(1, reportIntervalInSeconds);
        batchSize = Math.max(1, batchSize);
        boolean useJexl = jexlExpressions != null && jexlExpressions.size() > 0;

        // Display the effective settings and rates
        double eventsPerSecond = (double)eventsPerSecondPerThread * (double)numberOfThreads;
        System.out.printf("Generating %d events per second accross %d threads for %d seconds\n",
                eventsPerSecondPerThread, numberOfThreads, numSeconds);
        System.out.printf("\t with UEI: %s\n", eventUei);
        System.out.printf("\t with batch size: %d\n", batchSize);
        System.out.printf("\t with synchronous calls: %s\n", isSynchronous);
        System.out.printf("Which will yield an effective\n");
        System.out.printf("\t %.2f events per second\n", eventsPerSecond);
        System.out.printf("\t %.2f total events\n", eventsPerSecond * numSeconds);
        if (useJexl) {
            System.out.printf("Using JEXL expressions:\n");
            for (String jexlExpression : jexlExpressions) {
                System.out.printf("\t%s\n", jexlExpression);
            }
        }

        // Setup the reporter
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        // Setup the executor
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Event Generator #%d")
            .build();
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads, threadFactory);

        System.out.println("Starting.");
        try {
            reporter.start(reportIntervalInSeconds, TimeUnit.SECONDS);
            for (int i = 0; i < numberOfThreads; i++) {
                final EventGenerator eventGenerator = useJexl ?
                        new JexlEventGenerator(jexlExpressions) : new EventGenerator();
                executor.execute(eventGenerator);
            }
            System.out.println("Started.");

            // Wait until we timeout or get interrupted
            try {
                Thread.sleep(SECONDS.toMillis(numSeconds));
            } catch (InterruptedException e) { }

            // Stop!
            try {
                System.out.println("Stopping.");
                executor.shutdownNow();
                if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
                    System.err.println("The threads did not stop in time.");
                } else {
                    System.out.println("Stopped.");
                }
            } catch (InterruptedException e) { }
        } finally {
            // Make sure we always stop the reporter
            reporter.stop();
        }

        // And display one last report...
        reporter.report();
        return null;
    }

    /**
     * Convenience methods added to the JEXL context
     */
    public static class IpUtils {
        public static InetAddress int2ip(int k) {
            final byte[] bytes = BigInteger.valueOf(k).toByteArray();
            try {
                return InetAddress.getByAddress(bytes);
            } catch (UnknownHostException e) {
                System.err.printf("Failed to convert %d to an InetAddress.", k);
                return null;
            }
        }
    }
}
