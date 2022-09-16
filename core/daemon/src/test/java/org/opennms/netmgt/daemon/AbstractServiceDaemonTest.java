package org.opennms.netmgt.daemon;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.jayway.awaitility.Duration;

public class AbstractServiceDaemonTest {
    @Test
    public void testInit() {
        var daemon = new FakeServiceDaemon();
        daemon.init();
        Assert.assertTrue(daemon.initialized);
        Assert.assertEquals("Fake", daemon.getName());
    }

    @Test
    public void testStartStop() throws Exception {
        var daemon = new FakeServiceDaemon();

        new Thread() {
            public void run() {
                daemon.start();
            }
        }.start();
        Assert.assertEquals(FakeServiceDaemon.START_PENDING, daemon.getStatus());
        await()
            .atMost(Duration.ONE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::isStarting);
        Assert.assertFalse(daemon.isRunning());
        await()
            .atMost(Duration.ONE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::isRunning);
        Assert.assertFalse(daemon.isStarting());

        new Thread() {
            public void run() {
                daemon.stop();
            }
        }.start();
        await()
            .atMost(Duration.ONE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::getStatus, equalTo(FakeServiceDaemon.STOP_PENDING));
        await()
            .atMost(Duration.ONE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::isStopped);
    }

    @Test
    public void testPauseResume() {
        var daemon = new FakeServiceDaemon();

        // pause shouldn't do anything if you're not running
        Assert.assertFalse(daemon.isPaused());
        Assert.assertFalse(daemon.isRunning());

        // resume shouldn't do anything if you're not paused
        daemon.resume();
        Assert.assertFalse(daemon.isPaused());
        Assert.assertFalse(daemon.isRunning());

        new Thread() {
            public void run() {
                daemon.start();
                daemon.pause();
            }
        }.start();
        await()
            .atMost(Duration.FIVE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::getStatus, equalTo(FakeServiceDaemon.PAUSE_PENDING));
        await()
            .atMost(Duration.ONE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::isPaused);
        Assert.assertFalse(daemon.isRunning());

        new Thread() {
            public void run() {
                daemon.resume();
            }
        }.start();
        await()
            .atMost(Duration.ONE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::getStatus, equalTo(FakeServiceDaemon.RESUME_PENDING));
        await()
            .atMost(Duration.ONE_HUNDRED_MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .until(daemon::isRunning);
    }

    public static class FakeServiceDaemon extends AbstractServiceDaemon {
        public boolean initialized = false;

        protected FakeServiceDaemon() {
            super("Fake");
        }

        @Override
        protected void onInit() {
            this.initialized = true;
        }

        @Override
        protected void onStart() {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        protected void onPause() {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        protected void onResume() {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        protected void onStop() {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
