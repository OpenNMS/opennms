package org.opennms.tools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RrdGenerator {

    private static final String OWNER = "rrd generator";

    public static <D, F> void generateRrd(final RrdStrategy<D, F> strategy,
                                           final Path file,
                                           final long start) throws Exception {

        final D command = strategy.createDefinition(OWNER,
                                                    file.getParent().toString(),
                                                    file.getFileName().toString(),
                                                    300,
                                                    ImmutableList.<RrdDataSource>builder()
                                                            .add(new RrdDataSource("ifInOctets",
                                                                                   "COUNTER",
                                                                                   600,
                                                                                   "U", "U"))
                                                            .build(),
                                                    ImmutableList.<String>builder()
                                                            .add("RRA:AVERAGE:0.5:1:2016")
                                                            .add("RRA:AVERAGE:0.5:12:1488")
                                                            .add("RRA:AVERAGE:0.5:288:366")
                                                            .add("RRA:MAX:0.5:288:366")
                                                            .add("RRA:MIN:0.5:288:366")
                                                            .build());

        strategy.createFile(command, ImmutableMap.<String, String>builder()
                                                 .put("GROUP", "mib2-interfaces")
                                                 .build());

        final F f = strategy.openFile(file + strategy.getDefaultFileExtension());

        strategy.updateFile(f, OWNER, String.format("%d:%f", start, 42.23));

        strategy.closeFile(f);
    }

}
