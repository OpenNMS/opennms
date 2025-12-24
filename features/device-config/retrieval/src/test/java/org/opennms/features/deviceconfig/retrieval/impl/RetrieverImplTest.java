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

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.features.deviceconfig.retrieval.api.Retriever;
import org.opennms.features.deviceconfig.sshscripting.SshScriptingService;
import org.opennms.features.deviceconfig.tftp.TftpFileReceiver;
import org.opennms.features.deviceconfig.tftp.TftpServer;

public class RetrieverImplTest {

    @Test
    public void shouldRetrieveConfiguration() throws Exception {

        var sshScriptingService = mock(SshScriptingService.class);
        var tftpServer = mock(TftpServer.class);

        // mock the behavior of the scripting service and the tftp server
        // -> capture the map of variables when the script is execute
        //    (because it contains the generated filename for the upload)
        // -> capture the registered file receiver
        //    (because it must be called with file content)
        var varsCaptor = ArgumentCaptor.forClass(Map.class);
        var receiverCaptor = ArgumentCaptor.forClass(TftpFileReceiver.class);
        when(sshScriptingService.execute(any(), any(), any(), any(), any(), any(), any(), varsCaptor.capture(), any())).thenReturn(SshScriptingService.Result.success("Success"));
        doNothing().when(tftpServer).register(receiverCaptor.capture());

        var retriever = new RetrieverImpl(sshScriptingService, tftpServer);

        var configType = "runtime.cfg";

        var future = retriever.retrieveConfig(
                Retriever.Protocol.TFTP, "", "", "", null, new InetSocketAddress("host", 80), null, null, configType,
                Collections.emptyMap(),
                Duration.ofMillis(1000)
        ).toCompletableFuture();

        // wait until the variables and receiver are captured
        var vars = waitFor(varsCaptor);
        var receiver = waitFor(receiverCaptor);

        var filenameSuffix = (String)vars.get("filenameSuffix");

        assertThat(filenameSuffix, notNullValue());

        var bytes = new byte[]{1, 2, 3};
        var filename = "config.gz";

        // signal the receiver of some incoming file with the expected filename
        receiver.onFileReceived(InetAddress.getLocalHost(), filename + filenameSuffix, bytes);

        await().until(future::isDone);

        var either = future.get();
        assertTrue(either.isRight());

        var success = either.get();

        assertThat(success.config, is(bytes));
        assertThat(success.filename, is(filename));

        verify(tftpServer, times(1)).unregister(receiver);
    }

    @Test
    public void shouldHandleScriptingExceptions() throws Exception {
        var sshScriptingService = mock(SshScriptingService.class);
        var tftpServer = mock(TftpServer.class);
        var receiverCaptor = ArgumentCaptor.forClass(TftpFileReceiver.class);
        var scriptingException = new RuntimeException("scripting exception");

        when(sshScriptingService.execute(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenThrow(scriptingException);
        doNothing().when(tftpServer).register(receiverCaptor.capture());

        var retriever = new RetrieverImpl(sshScriptingService, tftpServer);

        var future = retriever.retrieveConfig(
                Retriever.Protocol.TFTP, "", "", "", null, new InetSocketAddress("host", 80), null, null, "runtime.cfg",
                Collections.emptyMap(),
                Duration.ofMillis(1000)
        ).toCompletableFuture();

        await().until(future::isDone);

        var receiver = waitFor(receiverCaptor);

        var either = future.get();
        assertTrue(either.isLeft());

        var failure = either.getLeft();

        // The script is wrapped in a future, so any exceptions will be wrapped in an ExecutionException.
        // ExecutionExceptions preface the exception's msg with the original exception's full class name.
        assertThat(failure.message, containsString(RetrieverImpl.scriptingFailureMsg(new InetSocketAddress("host", 80),
                String.format("%s: %s", RuntimeException.class.getName(), scriptingException.getMessage()))));

        verify(tftpServer, times(1)).unregister(receiver);
    }

    @Test
    public void shouldHandleScriptingFailures() throws Exception {
        var sshScriptingService = mock(SshScriptingService.class);
        var tftpServer = mock(TftpServer.class);
        var receiverCaptor = ArgumentCaptor.forClass(TftpFileReceiver.class);
        var scriptingFailureMessage = "scripting exception";
        when(sshScriptingService.execute(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(SshScriptingService.Result.failure(scriptingFailureMessage));
        doNothing().when(tftpServer).register(receiverCaptor.capture());

        var retriever = new RetrieverImpl(sshScriptingService, tftpServer);

        var future = retriever.retrieveConfig(
                Retriever.Protocol.TFTP, "", "", "", null, new InetSocketAddress("host", 80), null, null, "runtime.cfg",
                Collections.emptyMap(),
                Duration.ofMillis(1000)
        ).toCompletableFuture();

        await().until(future::isDone);

        var receiver = waitFor(receiverCaptor);

        var either = future.get();
        assertTrue(either.isLeft());

        var failure = either.getLeft();

        assertThat(failure.message, containsString(RetrieverImpl.scriptingFailureMsg(new InetSocketAddress("host", 80), scriptingFailureMessage)));

        verify(tftpServer, times(1)).unregister(receiver);
    }

    @Test
    public void shouldHandleTimeouts() throws Exception {
        var sshScriptingService = mock(SshScriptingService.class);
        var tftpServer = mock(TftpServer.class);
        var receiverCaptor = ArgumentCaptor.forClass(TftpFileReceiver.class);
        when(sshScriptingService.execute(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(SshScriptingService.Result.success("Success"));
        doNothing().when(tftpServer).register(receiverCaptor.capture());

        var retriever = new RetrieverImpl(sshScriptingService, tftpServer);

        var future = retriever.retrieveConfig(
                Retriever.Protocol.TFTP, "", "", "", null, new InetSocketAddress("host", 80), null, null, "runtime.cfg",
                Collections.emptyMap(),
                Duration.ofMillis(1000)
        ).toCompletableFuture();

        await().until(future::isDone);

        var receiver = waitFor(receiverCaptor);

        var either = future.get();
        assertTrue(either.isLeft());

        var failure = either.getLeft();

        assertThat(failure.message, containsString(RetrieverImpl.timeoutFailureMsg(new InetSocketAddress("host", 80))));

        verify(tftpServer, times(1)).unregister(receiver);
    }

    @Test
    public void shouldIgnoreOtherFiles() throws Exception {
        var sshScriptingService = mock(SshScriptingService.class);
        var tftpServer = mock(TftpServer.class);
        var varsCaptor = ArgumentCaptor.forClass(Map.class);
        var receiverCaptor = ArgumentCaptor.forClass(TftpFileReceiver.class);
        when(sshScriptingService.execute(any(), any(), any(), any(), any(), any(), any(), varsCaptor.capture(), any())).thenReturn(SshScriptingService.Result.success("Success"));
        doNothing().when(tftpServer).register(receiverCaptor.capture());

        var retriever = new RetrieverImpl(sshScriptingService, tftpServer);

        var future = retriever.retrieveConfig(
                Retriever.Protocol.TFTP, "", "", "", null, new InetSocketAddress("host", 80), null, null, "runtime.cfg",
                Collections.emptyMap(),
                Duration.ofMillis(1000)
        ).toCompletableFuture();

        var vars = waitFor(varsCaptor);
        var receiver = waitFor(receiverCaptor);

        var filenameSuffix = (String)vars.get("filenameSuffix");

        // signal the receiver of some incoming file that has a different name
        receiver.onFileReceived(InetAddress.getLocalHost(), "config.gz.somerandomstringhuehuehue.other", new byte[] { 1, 2, 3 });

        await().until(future::isDone);

        var either = future.get();
        assertTrue(either.isLeft());

        var failure = either.getLeft();

        assertThat(failure.message, containsString(RetrieverImpl.timeoutFailureMsg(new InetSocketAddress("host", 80))));

        verify(tftpServer, times(1)).unregister(receiver);
    }

    private <T> T waitFor(ArgumentCaptor<T> captor) {
        return await().until(() -> captor.getValue(), t -> true);
    }

}
