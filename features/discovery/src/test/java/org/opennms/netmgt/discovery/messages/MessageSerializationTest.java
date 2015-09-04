package org.opennms.netmgt.discovery.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class MessageSerializationTest {

    /*
    @Test
    public void canSerializeJob() throws IOException {
        IPPollRange range = new IPPollRange("4.2.2.2", "4.2.2.2", 1, 1);
        List<IPPollRange> ranges = Lists.newArrayList(range);
        DiscoveryJob job = new DiscoveryJob(ranges, "x", "y");
        serialize(job);
    }
    */

    @Test
    public void canSerializeResults() throws IOException {
        Map<InetAddress, Long> responses = Maps.newConcurrentMap();
        responses.put(InetAddress.getByName("4.2.2.2"), 1L);
        DiscoveryResults results = new DiscoveryResults(responses, "x", "y");
        serialize(results);
    }

    private static void serialize(Object o) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream());
            out.writeObject(o);
            out.close();
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }
}
