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
package org.opennms.features.apilayer.common.detectors;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.integration.api.v1.detectors.DetectResults;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;

/**
 * Maps {@link DetectResults} future with {@link DetectFuture}
 */
public class DetectorFutureImpl implements DetectFuture {

    final CompletableFuture<DetectResults> future;
    private boolean serviceDetected = false;
    private Throwable throwable;

    public DetectorFutureImpl(CompletableFuture<DetectResults> future) {
        this.future = future;
    }

    @Override
    public Throwable getException() {
        return throwable;
    }

    @Override
    public void setServiceDetected(boolean serviceDetected) {
        this.serviceDetected = serviceDetected;
    }

    @Override
    public void setException(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void awaitFor() throws InterruptedException {
    }

    @Override
    public void awaitForUninterruptibly() {
        // pass
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public DetectFuture addListener(DetectFutureListener<DetectFuture> listener) {
        try {
            DetectResults detectResults = future.get();
            setServiceDetected(detectResults.isServiceDetected());
        } catch (InterruptedException | ExecutionException e) {
            setServiceDetected(false);
            setException(e);
        }
        listener.operationComplete(this);
        return this;
    }

    @Override
    public boolean isServiceDetected() {
        return this.serviceDetected;
    }

    @Override
    public Map<String, String> getServiceAttributes() {
        return null;
    }
}
