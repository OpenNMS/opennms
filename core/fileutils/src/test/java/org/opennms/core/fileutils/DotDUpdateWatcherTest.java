package org.opennms.core.fileutils;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DotDUpdateWatcherTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File file1;
    private File file2;
    private File file3;
    private DotDUpdateWatcher dotDUpdateWatcher;
    private boolean reloadTriggered = false;

    @Before
    public void before() throws IOException {
        tempFolder.delete();
        tempFolder.create();
        file1 = tempFolder.newFile("file1.txt");
        file2 = tempFolder.newFile("file2.txt");
        file3 = tempFolder.newFile("file3.txt");
        Files.writeString(file1.toPath(), "foo1");
        Files.writeString(file2.toPath(), "foo2");
        Files.writeString(file3.toPath(), "foo3");

        if (dotDUpdateWatcher != null) {
            dotDUpdateWatcher.destroy();
            dotDUpdateWatcher = null;
        }
        dotDUpdateWatcher = new DotDUpdateWatcher(tempFolder.getRoot().getAbsolutePath(), (dir, name) -> name.endsWith(".txt"),
        new FileUpdateCallback(){
            @Override
            public void reload() {
                reloadTriggered = true;
                System.out.println("Triggred");
            }
        });

        reloadTriggered = false;
    }

    @Test
    public void testDeletion() {
        Assert.assertFalse(reloadTriggered);
        Assert.assertTrue(file2.exists());
        file2.delete();
        Assert.assertFalse(file2.exists());

        Awaitility.await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).
                until(() -> reloadTriggered);
    }

    @Test
    public void testUpdates() throws IOException {
        Assert.assertFalse(reloadTriggered);
        Assert.assertTrue(file1.exists());
        Files.writeString(file1.toPath(), "bar1");
        Assert.assertTrue(file1.exists());

        Awaitility.await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).
                until(() -> reloadTriggered);
    }

    @Test
    public void testAddition() throws IOException {
        Assert.assertFalse(reloadTriggered);
        final File file4 = new File(tempFolder.getRoot(), "file4.txt");
        Assert.assertFalse(file4.exists());
        Files.writeString(file4.toPath(), "foo4");
        Assert.assertTrue(file2.exists());
        Awaitility.await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).
                until(() -> reloadTriggered);
    }

    @Test
    public void testWrongExtension() throws IOException, InterruptedException {
        Assert.assertFalse(reloadTriggered);
        final File file5 = new File(tempFolder.getRoot(), "file5.xml");
        Assert.assertFalse(file5.exists());
        Files.writeString(file5.toPath(), "foo5");
        Assert.assertTrue(file5.exists());
        Thread.sleep(2000);
        Assert.assertFalse(reloadTriggered);
    }
}
