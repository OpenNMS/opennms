package org.opennms.core.tsid;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates 64-bit time-sorted unique IDs (Snowflake-style).
 *
 * Layout (64 bits):
 *   [1 bit unused] [41 bits: ms since epoch] [10 bits: nodeId] [12 bits: sequence]
 *
 * - 41 bits of time: ~69 years from custom epoch
 * - 10 bits of node: 1024 distinct JVMs
 * - 12 bits of sequence: 4096 IDs per millisecond per node
 */
public class TsidFactory {

    private static final long CUSTOM_EPOCH = 1704067200000L; // 2024-01-01T00:00:00Z
    private static final int NODE_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final long MAX_NODE_ID = (1L << NODE_BITS) - 1;  // 1023
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1; // 4095
    private static final int NODE_SHIFT = SEQUENCE_BITS;
    private static final int TIMESTAMP_SHIFT = NODE_BITS + SEQUENCE_BITS;

    private final long nodeId;
    private final AtomicLong state; // packed: [timestamp | sequence]

    public TsidFactory(long nodeId) {
        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            throw new IllegalArgumentException(
                    "nodeId must be between 0 and " + MAX_NODE_ID + ", got: " + nodeId);
        }
        this.nodeId = nodeId;
        this.state = new AtomicLong(packState(currentTimestamp(), 0));
    }

    public long create() {
        while (true) {
            long oldState = state.get();
            long oldTs = unpackTimestamp(oldState);
            long oldSeq = unpackSequence(oldState);
            long now = currentTimestamp();

            long newTs;
            long newSeq;
            if (now > oldTs) {
                newTs = now;
                newSeq = 0;
            } else {
                // Same millisecond or clock went backwards
                newSeq = oldSeq + 1;
                if (newSeq > MAX_SEQUENCE) {
                    // Sequence exhausted for this ms, advance to next
                    newTs = oldTs + 1;
                    newSeq = 0;
                } else {
                    newTs = oldTs;
                }
            }

            long newState = packState(newTs, newSeq);
            if (state.compareAndSet(oldState, newState)) {
                return (newTs << TIMESTAMP_SHIFT) | (nodeId << NODE_SHIFT) | newSeq;
            }
            // CAS failed, retry
        }
    }

    private long currentTimestamp() {
        return System.currentTimeMillis() - CUSTOM_EPOCH;
    }

    private static long packState(long timestamp, long sequence) {
        return (timestamp << SEQUENCE_BITS) | sequence;
    }

    private static long unpackTimestamp(long state) {
        return state >>> SEQUENCE_BITS;
    }

    private static long unpackSequence(long state) {
        return state & MAX_SEQUENCE;
    }
}
