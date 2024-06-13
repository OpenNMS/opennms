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

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReading;
import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos.PerformanceDataReadings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-timeseries-tcp.xml"
})
public class TcpOutputStrategyTest {

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private PersisterFactory persisterFactory;

    private static List<PerformanceDataReadings> allReadings = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() {
        // Setup a quick Netty TCP server that decodes the protobuf messages
        // and appends these to a list when received
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossLoopGroup, workerLoopGroup)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline().addLast(new ProtobufDecoder(PerformanceDataReadings.getDefaultInstance())).addLast(new PerfDataServerHandler());
                            }
                        });

        Channel channel = bootstrap.bind(new InetSocketAddress(0)).channel();
        InetSocketAddress addr = (InetSocketAddress)channel.localAddress();

        // Point the TCP exporter to our server
        System.setProperty("org.opennms.rrd.tcp.host", addr.getHostString());
        System.setProperty("org.opennms.rrd.tcp.port", Integer.toString(addr.getPort()));
        // Always use queueing during these tests
        System.setProperty("org.opennms.rrd.usequeue", Boolean.TRUE.toString());
        // Use the temporary folder as the base directory
        System.setProperty("rrd.base.dir", tempFolder.getRoot().getAbsolutePath());
    }

    public static class PerfDataServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object obj) {
            allReadings.add((PerformanceDataReadings) obj);
        }
    }

    @Test
    public void persistAndReceiveProtobufMessages() {
        Date start = new Date();

        // Build a collection set with both numeric and string attributes
        String owner = "192.168.1.1";
        MockCollectionAgent agent = new MockCollectionAgent(1, "n1", InetAddressUtils.addr(owner));
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        NodeLevelResource node = new NodeLevelResource(agent.getNodeId());  
        InterfaceLevelResource eth0 = new InterfaceLevelResource(node, "eth0");
        builder.withNumericAttribute(eth0, "mib2-interfaces", "ifInErrors", 0.0, AttributeType.COUNTER);
        builder.withStringAttribute(eth0, "mib2-interfaces", "ifSpeed", "10000000");
        builder.withStringAttribute(eth0, "mib2-interfaces", "ifHighSpeed", "10");
        CollectionSet collectionSet = builder.build();

        // Make sure we start with an empty set of readings
        allReadings.clear();

        // Persist without storeByGroup
        persist(collectionSet, false);

        // Wait for the server to receive the readings
        await().until(() -> allReadings.size(), equalTo(1));
        PerformanceDataReadings readings = allReadings.get(0);

        // The reading should contain three messages
        assertEquals(3, readings.getMessageCount());

        PerformanceDataReading reading = readings.getMessage(0);
        assertEquals(PerformanceDataReading.newBuilder()
                .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "1", "eth0", "ifInErrors").toString())
                .setOwner(owner)
                .setTimestamp(reading.getTimestamp())
                .addAllDblValue(Arrays.asList(Double.valueOf(0.0)))
                .addAllStrValue(Collections.emptyList())
                .build(), reading);

        reading = readings.getMessage(1);
        assertEquals(PerformanceDataReading.newBuilder()
            .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "1", "eth0", "ifSpeed").toString())
            .setOwner(owner)
            .setTimestamp(reading.getTimestamp())
            .addAllDblValue(Collections.emptyList())
            .addAllStrValue(Arrays.asList("10000000"))
            .build(), reading);

        reading = readings.getMessage(2);
        assertEquals(PerformanceDataReading.newBuilder()
            .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "1", "eth0", "ifHighSpeed").toString())
            .setOwner(owner)
            .setTimestamp(reading.getTimestamp())
            .addAllDblValue(Collections.emptyList())
            .addAllStrValue(Arrays.asList("10"))
            .build(), reading);

        // Persist with storeByGroup
        persist(collectionSet, true);

        // Wait for the server to receive the readings
        await().until(() -> allReadings.size(), equalTo(2));
        readings = allReadings.get(1);

        // The reading should contain 1 message
        assertEquals(1, readings.getMessageCount());

        reading = readings.getMessage(0);
        assertEquals(PerformanceDataReading.newBuilder()
                .setPath(Paths.get(tempFolder.getRoot().getAbsolutePath(), "1", "eth0", "mib2-interfaces").toString())
                .setOwner(owner)
                .setTimestamp(reading.getTimestamp())
                .addAllDblValue(Arrays.asList(Double.valueOf(0.0)))
                .addAllStrValue(Arrays.asList("10", "10000000"))
                .build(), reading);

        // The reading should be a timestamp in milliseconds
        Date dateFromReading = new Date(reading.getTimestamp());
        assertTrue(String.format("%s <= %s", start, dateFromReading), start.compareTo(dateFromReading) <= 0);
    }

    public void persist(CollectionSet collectionSet, boolean forceStoreByGroup) {
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(tempFolder.getRoot());
        Persister persister = persisterFactory.createPersister(params, repo, false, forceStoreByGroup, false);
        collectionSet.visit(persister);
    }
}
