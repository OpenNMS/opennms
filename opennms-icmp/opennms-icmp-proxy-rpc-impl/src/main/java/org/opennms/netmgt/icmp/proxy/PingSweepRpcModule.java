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
package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.core.utils.IteratorUtils;
import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Component
public class PingSweepRpcModule extends AbstractXmlRpcModule<PingSweepRequestDTO, PingSweepResponseDTO> {

    public static final String RPC_MODULE_ID = "PING-SWEEP";

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("ping-sweep-%d")
            .build();

    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    @Autowired
    private PingerFactory pingerFactory;

    public PingSweepRpcModule() {
        super(PingSweepRequestDTO.class, PingSweepResponseDTO.class);
    }

    @Override
    public CompletableFuture<PingSweepResponseDTO> execute(PingSweepRequestDTO request) {
        final Pinger pinger = pingerFactory.getInstance();
        final PingSweepResultTracker tracker = new PingSweepResultTracker();

        String location = request.getLocation();
        int packetSize = request.getPacketSize();
        List<IPPollRange> ranges = new ArrayList<>();
        for (IPRangeDTO dto : request.getIpRanges()) {
            IPPollRange pollRange = new IPPollRange(null, location, dto.getBegin(), dto.getEnd(), dto.getTimeout(), dto.getRetries());
            ranges.add(pollRange);
        }

        // Use a RateLimiter to limit the ping packets per second that we send
        RateLimiter limiter = RateLimiter.create(request.getPacketsPerSecond());

        List<IPPollAddress> addresses = StreamSupport.stream(getAddresses(ranges).spliterator(), false)
                .filter(j -> j.getAddress() != null).collect(Collectors.toList());

        return CompletableFuture.supplyAsync(() -> {
            addresses.stream().forEach(pollAddress -> {
                try {
                    tracker.expectCallbackFor(pollAddress.getAddress());
                    limiter.acquire();
                    pinger.ping(pollAddress.getAddress(), pollAddress.getTimeout(), pollAddress.getRetries(), packetSize, 1, tracker);
                } catch (Exception e) {
                    tracker.handleError(pollAddress.getAddress(), null, e);
                    tracker.completeExceptionally(e);
                }
            });

            try {
                tracker.getLatch().await();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
            tracker.complete();
            return tracker.getResponse();
        } , executor);

    }

    private static class PingSweepResultTracker extends CompletableFuture<PingSweepResponseDTO>
            implements PingResponseCallback {

        private final Set<InetAddress> waitingFor = Sets.newConcurrentHashSet();
        private final CountDownLatch m_doneSignal = new CountDownLatch(1);
        private final PingSweepResponseDTO responseDTO = new PingSweepResponseDTO();

        public void expectCallbackFor(InetAddress address) {
            waitingFor.add(address);
        }

        @Override
        public void handleResponse(InetAddress address, EchoPacket response) {
            if (response != null) {
                PingSweepResultDTO sweepResult = new PingSweepResultDTO();
                sweepResult.setAddress(address);
                sweepResult.setRtt(response.elapsedTime(TimeUnit.MILLISECONDS));
                responseDTO.addPingSweepResult(sweepResult);
            }
            afterHandled(address);
        }

        @Override
        public void handleTimeout(InetAddress address, EchoPacket request) {
            afterHandled(address);
        }

        @Override
        public void handleError(InetAddress address, EchoPacket request, Throwable t) {
            afterHandled(address);
        }

        private void afterHandled(InetAddress address) {
            waitingFor.remove(address);
            if (waitingFor.isEmpty()) {
                m_doneSignal.countDown();
            }
        }

        public void complete() {
            complete(responseDTO);
        }

        public PingSweepResponseDTO getResponse() {
            return responseDTO;
        }

        public CountDownLatch getLatch() {
            return m_doneSignal;
        }

    }

    @Override
    public PingSweepResponseDTO createResponseWithException(Throwable ex) {
        return new PingSweepResponseDTO(ex);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = pingerFactory;
    }
    
    public Iterable<IPPollAddress> getAddresses(List<IPPollRange> ranges) {
        final List<Iterator<IPPollAddress>> iters = new ArrayList<>();
        for(final IPPollRange range : ranges) {
            iters.add(range.iterator());
        }
        return IteratorUtils.concatIterators(iters);
    }

}
