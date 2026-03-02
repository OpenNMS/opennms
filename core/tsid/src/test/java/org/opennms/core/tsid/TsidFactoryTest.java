package org.opennms.core.tsid;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class TsidFactoryTest {

    @Test
    public void shouldGeneratePositiveLong() {
        TsidFactory factory = new TsidFactory(1);
        long id = factory.create();
        assertThat(id).isPositive();
    }

    @Test
    public void shouldGenerateMonotonicallyIncreasingIds() {
        TsidFactory factory = new TsidFactory(1);
        long prev = factory.create();
        for (int i = 0; i < 1000; i++) {
            long next = factory.create();
            assertThat(next).isGreaterThan(prev);
            prev = next;
        }
    }

    @Test
    public void shouldGenerateUniqueIdsAcrossThreads() throws Exception {
        TsidFactory factory = new TsidFactory(1);
        int threadCount = 8;
        int idsPerThread = 10_000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                for (int i = 0; i < idsPerThread; i++) {
                    ids.add(factory.create());
                }
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();
        assertThat(ids).hasSize(threadCount * idsPerThread);
    }

    @Test
    public void shouldRejectInvalidNodeId() {
        org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> new TsidFactory(-1));
        org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> new TsidFactory(1024));
    }
}
