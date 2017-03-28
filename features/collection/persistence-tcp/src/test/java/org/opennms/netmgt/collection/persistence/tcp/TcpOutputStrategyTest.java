/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.tcp;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.AttributeType;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReading;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReadings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.protobuf.InvalidProtocolBufferException;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-tcp.xml"
})
public class TcpOutputStrategyTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private PersisterFactory persisterFactory;

    private static List<PerformanceDataReadings> allReadings = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() {
        // Setup a quick Netty TCP server that decodes the protobuf messages
        // and appends these to a list when received
        ChannelFactory factory = new NioServerSocketChannelFactory();
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ProtobufDecoder(PerformanceDataReadings.getDefaultInstance()),
                        new PerfDataServerHandler());
            }
        });  
        Channel channel = bootstrap.bind(new InetSocketAddress(0));
        InetSocketAddress addr = (InetSocketAddress)channel.getLocalAddress();

        // Point the TCP exporter to our server
        System.setProperty("org.opennms.rrd.tcp.host", addr.getHostString());
        System.setProperty("org.opennms.rrd.tcp.port", Integer.toString(addr.getPort()));
    }

    public static class PerfDataServerHandler extends SimpleChannelHandler {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws InvalidProtocolBufferException  {
            allReadings.add((PerformanceDataReadings) e.getMessage());
        }
    }

    @Test
    public void peristAndReceiveProtobufMessages() {
        // Mock the agent
        String owner = "192.168.1.1";
        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageDir()).thenReturn(tempFolder.getRoot());
        when(agent.getHostAddress()).thenReturn(owner);

        // Build a collection set with both numeric and string attributes
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        NodeLevelResource node = new NodeLevelResource(agent.getNodeId());  
        InterfaceLevelResource eth0 = new InterfaceLevelResource(node, "eth0");
        builder.withNumericAttribute(eth0, "mib2-interfaces", "ifInErrors", 0.0, AttributeType.COUNTER);
        builder.withStringAttribute(eth0, "mib2-interfaces", "ifSpeed", "10000000");
        builder.withStringAttribute(eth0, "mib2-interfaces", "ifHighSpeed", "10");
        CollectionSet collectionSet = builder.build();

        // Persist without storeByGroup
        persist(collectionSet, false);

        // Wait for the server to receive the readings
        await().until(() -> allReadings.size() == 1);
        PerformanceDataReadings readings = allReadings.get(0);

        // The reading should contain three messages
        assertEquals(3, readings.getMessageCount());

        PerformanceDataReading reading = readings.getMessage(0);
        assertEquals(PerformanceDataReading.newBuilder()
                .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "eth0", "ifInErrors").toString())
                .setOwner(owner)
                .setTimestamp(reading.getTimestamp())
                .addAllDblValue(Arrays.asList(Double.valueOf(0.0)))
                .addAllStrValue(Collections.emptyList())
                .build(), reading);

        reading = readings.getMessage(1);
        assertEquals(PerformanceDataReading.newBuilder()
            .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "eth0", "ifSpeed").toString())
            .setOwner(owner)
            .setTimestamp(reading.getTimestamp())
            .addAllDblValue(Collections.emptyList())
            .addAllStrValue(Arrays.asList("10000000"))
            .build(), reading);

        reading = readings.getMessage(2);
        assertEquals(PerformanceDataReading.newBuilder()
            .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "eth0", "ifHighSpeed").toString())
            .setOwner(owner)
            .setTimestamp(reading.getTimestamp())
            .addAllDblValue(Collections.emptyList())
            .addAllStrValue(Arrays.asList("10"))
            .build(), reading);

        // Persist with storeByGroup
        persist(collectionSet, true);

        // Wait for the server to receive the readings
        await().until(() -> allReadings.size() == 2);
        readings = allReadings.get(1);

        // The reading should contain 1 message
        assertEquals(1, readings.getMessageCount());

        reading = readings.getMessage(0);
        assertEquals(PerformanceDataReading.newBuilder()
                .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "eth0", "mib2-interfaces").toString())
                .setOwner(owner)
                .setTimestamp(reading.getTimestamp())
                .addAllDblValue(Arrays.asList(Double.valueOf(0.0)))
                .addAllStrValue(Arrays.asList("10", "10000000"))
                .build(), reading);
    }

    public void persist(CollectionSet collectionSet, boolean forceStoreByGroup) {
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(tempFolder.getRoot());
        Persister persister = persisterFactory.createPersister(params, repo, false, forceStoreByGroup, false);
        collectionSet.visit(persister);
    }
}
