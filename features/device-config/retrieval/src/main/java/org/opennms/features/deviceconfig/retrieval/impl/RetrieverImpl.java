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
package org.opennms.features.deviceconfig.retrieval.impl;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.opennms.core.concurrent.FutureUtils;
import org.opennms.features.deviceconfig.retrieval.api.Retriever;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;
import org.opennms.features.deviceconfig.tftp.TftpFileReceiver;
import org.opennms.features.deviceconfig.tftp.TftpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.vavr.control.Either;

/**
 * Orchestrates device config retrieval.
 * <p>
 * Device config uploads are triggered using the SshScriptingService and files received by a tft server are processed.
 */
public class RetrieverImpl implements Retriever, AutoCloseable {

    private static Logger LOG = LoggerFactory.getLogger(RetrieverImpl.class);
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    // when a
    private static String SCRIPT_VAR_FILENAME_SUFFIX = "filenameSuffix";
    private static String SCRIPT_VAR_TFTP_SERVER_PORT = "tftpServerPort";
    private static String SCRIPT_VAR_CONFIG_TYPE = "configType";

    final SshScriptingService sshScriptingService;
    private final TftpServer tftpServer;
    private final ExecutorService executor;

    public RetrieverImpl(SshScriptingService sshScriptingService, TftpServer tftpServer) {
        this.sshScriptingService = sshScriptingService;
        this.tftpServer = tftpServer;
        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("device-config-retriever-%d").build());
    }

    @Override
    public CompletionStage<Either<Failure, Success>> retrieveConfig(
            Protocol protocol,
            String script,
            String user,
            String password,
            final String authKey,
            final SocketAddress target,
            final String hostKeyFingerprint,
            final String shell,
            String configType,
            Map<String, String> vars,
            Duration timeout
    ) {
        LOG.debug("retrieve config: " + target);
        var vs = new HashMap<String, String>();
        vs.putAll(vars);
        // generate a unique filename suffix
        // -> the filename is used to distinguish incoming files
        // -> scripts must use "tftp put <localfile> <removefile>.${filenameSuffix}" (or similar) to upload the device config
        // -> the appended ".${filenameSuffix}" is stripped from the incoming file
        var filenameSuffix = uniqueFilenameSuffix();
        vs.put(SCRIPT_VAR_FILENAME_SUFFIX, filenameSuffix);

        // set the ip address and port of the tftp server
        vs.put(SCRIPT_VAR_TFTP_SERVER_PORT, String.valueOf(tftpServer.getPort()));
        vs.put(SCRIPT_VAR_CONFIG_TYPE, configType);

        if (protocol == Protocol.TFTP) {
            // Keep timeout common between TFTP server and scripting service
            Instant timeoutInstant = Instant.now().plus(timeout);
            // the file receiver is registered with the tftp server
            // -> it triggers the file upload as soon as the future that is returned is created
            // -> it waits for the file or for a timeout
            // -> it is unregistered from the tftp server when the future is completed
            var tftpFileReceiver = new TftpFileReceiverImpl(
                    target,
                    filenameSuffix,
                    () -> sshScriptingService.execute(script, user, password, authKey, target, hostKeyFingerprint, shell, vs, Duration.between(Instant.now(), timeoutInstant).minusSeconds(1))
            );

            try {
                tftpServer.register(tftpFileReceiver);

                try {
                    return FutureUtils.completionStage(
                            tftpFileReceiver::completeNowOrLater,
                            Duration.between(Instant.now(), timeoutInstant),
                            tftpFileReceiver::onTimeout,
                            executor
                    ).whenComplete((e, t) -> tftpServer.unregister(tftpFileReceiver));
                } catch (RuntimeException e) {
                    // make sure the file receiver is unregistered in case no future is returned
                    tftpServer.unregister(tftpFileReceiver);
                    throw e;
                }
            } catch (Exception e) {
                var message = "could not trigger device config retrieval - target: " + target;
                LOG.error(message, e);
                return CompletableFuture.completedFuture(Either.left(new Failure(message)));
            }

        } else {

            var message = "unsupported protocol for device config retrieval - target: " + target + "; protocol: " + protocol;
            LOG.error(message);
            return CompletableFuture.completedFuture(Either.left(new Failure(message)));
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    private class TftpFileReceiverImpl implements TftpFileReceiver, FutureUtils.Completer<Either<Failure, Success>> {

        private final SocketAddress target;
        private final String fileNameSuffix;
        private final Supplier<SshScriptingService.Result> uploadTrigger;

        private CompletableFuture<SshScriptingService.Result> scriptFuture = null;

        private volatile CompletableFuture<Either<Failure, Success>> future;

        public TftpFileReceiverImpl(
                final SocketAddress target,
                String filenameSuffix,
                Supplier<SshScriptingService.Result> uploadTrigger
        ) {
            this.target = Objects.requireNonNull(target);
            this.fileNameSuffix = Objects.requireNonNull(filenameSuffix);
            this.uploadTrigger = uploadTrigger;
        }

        private void fail(String msg, Optional<String> stdout, Optional<String> stderr, String debug) {
            if (!future.isDone()) {
                LOG.error(msg);
                future.complete(Either.left(new Failure(msg, stdout, stderr, debug)));
            }
            else {
                LOG.debug("TftpFileReceiverImpl attempting to fail an already completed future, msg \"{}\"- ignoring...", msg);
            }
        }

        @Override
        public void completeNowOrLater(CompletableFuture<Either<Failure, Success>> future) {
            // store the future in order to complete it when a matching file is received
            this.future = future;
            // trigger the upload
            // -> if triggering the upload failed then complete the future with that failure
            try {
                scriptFuture = CompletableFuture.supplyAsync(uploadTrigger);
                SshScriptingService.Result result = scriptFuture.get();
                if (result.isFailed()) {
                    fail(scriptingFailureMsg(this.target, result.message), result.stdout, result.stderr, result.scriptOutput);
                }
            } catch (Throwable e) {
                var msg = scriptingFailureMsg(this.target, e.getMessage());
                LOG.error(msg, e);
                fail(msg, Optional.empty(), Optional.empty(), sshScriptingService.getScriptOutput());
            }
        }

        public void onTimeout(CompletableFuture<Either<Failure, Success>> future) {
            this.future = future;
            fail(timeoutFailureMsg(this.target), Optional.empty(), Optional.empty(), sshScriptingService.getScriptOutput());
        }

        @Override
        public void onFileReceived(InetAddress address, String fileName, byte[] content) {
            if (fileName.endsWith(fileNameSuffix) || fileName.contains(fileNameSuffix)) {
                // it is unlikely, that the file receiver receives a file (with matching filename!) before the file
                // upload was triggered
                // -> just to be sure check that the future is set
                if (future != null) {
                    LOG.debug("received config - target: " + this.target + "; address: " + address.getHostAddress());
                    // At this point the script has successfully sent the file via TFTP, but the tftp command
                    // and any following lines may not have been fully processed by the scripting service yet.
                    // Give it one second to finish up any remaining trivial commands and complete preparing debug output.
                    String scriptOutput = sshScriptingService.getScriptOutput();
                    if (scriptFuture != null && !scriptFuture.isDone()) {
                        try {
                            scriptOutput = scriptFuture.get(1, TimeUnit.SECONDS).scriptOutput;
                        }
                        catch (Exception e) {}
                    }
                    // strip the '.' and filenameSuffix from the filename
                    future.complete(Either.right(new Success(content, fileName.replace(fileNameSuffix, ""), scriptOutput)));
                }
            }
            else {
                LOG.warn("Received file from {} without a matching 'filenameSuffix' token; Ignoring...", address.getHostAddress());
            }
        }
    }

    static String scriptingFailureMsg(final SocketAddress target, String msg) {
        return "could not trigger device config upload - target: " + target + "; msg: " + msg;
    }
    static String timeoutFailureMsg(final SocketAddress target) {
        return "device config was not received in time. target: " + target;
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static String uniqueFilenameSuffix() {
        // the term "monitor" is required to not consume it from the sink module, see class DeviceConfigDispatcher
        return BASE64_URL_ENCODER.encodeToString(uuidToBytes(UUID.randomUUID())) + "-monitor";
    }
}
