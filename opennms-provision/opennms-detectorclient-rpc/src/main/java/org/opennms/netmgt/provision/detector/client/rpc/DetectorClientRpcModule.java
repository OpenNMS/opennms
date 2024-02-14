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
package org.opennms.netmgt.provision.detector.client.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.detector.registry.api.ServiceDetectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * RPC module used to execute both {@link SyncServiceDetector} and {@link AsyncServiceDetector} detectors.
 *
 * When running on OpenNMS, detectors are executed in provisiond's scanExecutor thread pool.
 * When running on Minion, detectors are executed in a caching thread pool created by the blueprint.
 *
 * @author jwhite
 */
public class DetectorClientRpcModule extends AbstractXmlRpcModule<DetectorRequestDTO, DetectorResponseDTO> {

    public static final String RPC_MODULE_ID = "Detect";

    @Autowired
    private ServiceDetectorRegistry serviceDetectorRegistry;

    @Autowired
    @Qualifier("scanExecutor")
    private Executor executor;

    public DetectorClientRpcModule() {
        super(DetectorRequestDTO.class, DetectorResponseDTO.class);
    }

    @Override
    public CompletableFuture<DetectorResponseDTO> execute(DetectorRequestDTO request) {
        String className = request.getClassName();
        Map<String, String> attributes = request.getAttributeMap();
        ServiceDetector detector = serviceDetectorRegistry.getDetectorByClassName(className, attributes);
        if (detector == null) {
            throw new IllegalArgumentException("No detector found with class name '" + className + "'.");
        }
        return detectService(detector, request);
    }

    private CompletableFuture<DetectorResponseDTO> detectService(ServiceDetector detector, DetectRequest detectRequest) {
        detector.init();
        if (detector instanceof SyncServiceDetector) {
            final SyncServiceDetector syncDetector = (SyncServiceDetector) detector;
            return CompletableFuture.supplyAsync(new Supplier<DetectorResponseDTO>() {
                @Override
                public DetectorResponseDTO get() {
                    try {
                        detectRequest.preDetect();
                        return new DetectorResponseDTO(syncDetector.detect(detectRequest));
                    } catch (Throwable t) {
                        return new DetectorResponseDTO(t);
                    } finally {
                        syncDetector.dispose();
                    }
                }
            }, executor);
        } else if (detector instanceof AsyncServiceDetector) {
            final CompletableFuture<DetectorResponseDTO> future = new CompletableFuture<>();
            final AsyncServiceDetector asyncDetector = (AsyncServiceDetector) detector;
            try {
                DetectFuture detectFuture = asyncDetector.detect(detectRequest);
                detectRequest.preDetect();
                detectFuture.addListener(new DetectFutureListener<DetectFuture>() {
                    @Override
                    public void operationComplete(DetectFuture detectFuture) {
                        if (detectFuture.getException() != null) {
                            future.complete(new DetectorResponseDTO(detectFuture.getException()));
                        } else {
                            future.complete(new DetectorResponseDTO(detectFuture));
                        }
                    }
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
            return future;
        } else {
            final CompletableFuture<DetectorResponseDTO> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Unsupported detector type."));
            return future;
        }
    }

    @Override
    public DetectorResponseDTO createResponseWithException(Throwable ex) {
        return new DetectorResponseDTO(ex);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    public void setServiceDetectorRegistry(ServiceDetectorRegistry serviceDetectorRegistry) {
        this.serviceDetectorRegistry = serviceDetectorRegistry;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
