package org.opennms.netmgt.rrd.newts;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class NewtsRrdStrategyTest {

    @Test
    public void createOpenUpdateCloseRead() throws Exception {
        String opennmsHome = "/opt/opennms";
        System.setProperty("opennms.home", opennmsHome);

        MockSampleRepository mockSampleRepository = new MockSampleRepository();

        // Disable delay checks and use a small batch size
        NewtsRrdStrategy rrdStrategy = new NewtsRrdStrategy(1, 0);
        rrdStrategy.setSampleRepository(mockSampleRepository);

        // Go through the life-cycle of creating and updating an .rrd file
        RrdDataSource ds1 = new RrdDataSource("x", "GAUGE", 900, "0", "100");
        RrdDataSource ds2 = new RrdDataSource("y", "GAUGE", 900, "0", "100");
        RrdDef def = rrdStrategy.createDefinition("test", opennmsHome + "/share/rrd/snmp/1", "loadavg", 1,
                Lists.newArrayList(ds1, ds2),
                Lists.newArrayList("RRA:AVERAGE:0.5:1:1000"));

        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("!key!", "#value#");
        rrdStrategy.createFile(def, attributes);

        // Add metrics to the file we created above
        String fileName = opennmsHome + "/share/rrd/snmp/1/loadavg.newts";
        RrdDb db = rrdStrategy.openFile(fileName);

        long timestampInSeconds = Timestamp.now().asSeconds();
        rrdStrategy.updateFile(db, "test", String.format("%d:U:1.0", timestampInSeconds));
        rrdStrategy.updateFile(db, "test", String.format("%d:1.0:2.0", 1 + timestampInSeconds));
        rrdStrategy.updateFile(db, "test", String.format("%d:2.0:3.0", 2 + timestampInSeconds));
        rrdStrategy.closeFile(db);

        List<Collection<Sample>> inserts = mockSampleRepository.getInsertedSamples();
        assertEquals(6, inserts.size());
        assertEquals(1, inserts.get(0).size());
    }

    private static class MockSampleRepository implements SampleRepository {

        private List<Collection<Sample>> m_samples = Lists.newLinkedList();

        @Override
        public Results<Measurement> select(Resource resource,
                Optional<Timestamp> start, Optional<Timestamp> end,
                ResultDescriptor descriptor, Duration resolution) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Results<Sample> select(Resource resource,
                Optional<Timestamp> start, Optional<Timestamp> end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(Collection<Sample> samples) {
            m_samples.add(samples);
        }

        @Override
        public void insert(Collection<Sample> samples, boolean calculateTimeToLive) {
            throw new UnsupportedOperationException();
        }

        public List<Collection<Sample>> getInsertedSamples() {
            return m_samples;
        }
    }
}
