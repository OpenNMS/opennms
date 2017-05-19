/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.echo;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;

public class EchoRpcModule extends AbstractXmlRpcModule<EchoRequest, EchoResponse> {

    public static final String RPC_MODULE_ID = "Echo";

    public EchoRpcModule() {
        super(EchoRequest.class, EchoResponse.class);
    }

    public void beforeRun() { }

    @Override
    public CompletableFuture<EchoResponse> execute(EchoRequest request) {
        final CompletableFuture<EchoResponse> future = new CompletableFuture<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                beforeRun();
                if (request.getDelay() != null) {
                    try {
                        Thread.sleep(request.getDelay());
                    } catch (InterruptedException e) {
                        future.completeExceptionally(e);
                        return;
                    }
                }
                if (request.shouldThrow()) {
                    future.completeExceptionally(new MyEchoException(request.getMessage()));
                } else {
                    future.complete(new EchoResponse(request.getMessage()));
                }
            }
        }).start();
        return future;
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public EchoResponse createResponseWithException(Throwable ex) {
        return new EchoResponse(ex);
    }
}
