/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A classification engine that does reloads asynchronously.
 * <p>
 * Reloads are triggered oftentimes while editing classification rules. In addition, reloads may take a couple of seconds
 * depending on the enabled rules. In order to keep the front-end responsive, reloads are done asynchronously.
 * Usages of the classification engine are blocked until ongoing reloads did finish. If a reload fails then
 * future usages of this classification engine also fail until a following reload succeeds.
 */
public class AsyncReloadingClassificationEngine implements ClassificationEngine {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncReloadingClassificationEngine.class);

    private enum State {
        READY, RELOADING, FAILED
    }

    private final ClassificationEngine delegate;

    // uses at most one additional thread; if the thread is not used for 60 seconds then it is terminated
    // -> uses no additional resources while being idle
    private final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), // multiple reloads may have been enqueued and cancelled
            runnable -> new Thread(runnable, "AsyncReloadingClassificationEngine")
    );

    private State state = State.READY;
    private Throwable reloadException;
    private Future<?> reloadFuture;

    public AsyncReloadingClassificationEngine(ClassificationEngine delegate) {
        this.delegate = delegate;
        // trigger reload
        // -> blocks classification requests until classification engine is ready
        reload();
    }

    private void setState(State newState) {
        state = newState;
        notifyAll();
    }

    private void waitUntilReadyOrFailed() {
        while (true) {
            switch (state) {
                case READY:
                    return;
                case FAILED:
                    throw new RuntimeException("classification engine can not be used because last reload failed", reloadException);
            }
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doReload() {
        // this method must not modify the state because it not synchronized
        try {
            LOG.debug("reload classification engine");
            delegate.reload();
            LOG.debug("classification engine reloaded");
            onReloadSucceeded();
        } catch (InterruptedException e) {
            LOG.debug("reload was interrupted");
            // another reload is submitted or already under way that changes the state on its completion
        } catch (Throwable e) {
            LOG.error("reload of classification engine failed", e);
            onReloadFailed(e);
        }
    }

    private synchronized void onReloadSucceeded() {
        setState(State.READY);
    }

    private synchronized void onReloadFailed(Throwable e) {
        reloadException = e;
        setState(State.FAILED);
    }

    @Override
    public synchronized String classify(ClassificationRequest classificationRequest) {
        waitUntilReadyOrFailed();
        return delegate.classify(classificationRequest);
    }

    @Override
    public synchronized List<Rule> getInvalidRules() {
        waitUntilReadyOrFailed();
        return delegate.getInvalidRules();
    }

    @Override
    public synchronized void reload() {
        switch (state) {
            case READY:
            case FAILED:
                try {
                    reloadFuture = executorService.submit(this::doReload);
                    setState(State.RELOADING);
                } catch (Throwable t) {
                    LOG.error("could not submit reload task", t);
                    this.reloadException = t;
                    setState(State.FAILED);
                }
                break;
            case RELOADING:
                reloadFuture.cancel(true);
                reloadFuture = executorService.submit(this::doReload);
                break;
        }
    }
}
