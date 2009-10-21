package org.opennms.client;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GwtApplicationTestCase extends GWTTestCase {
    
    public String getModuleName() {
        return "org.opennms.Application";
    }

    public void testOwnershipFile() {
        final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

        installService.checkOwnershipFileExists(new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean arg0) {
                fail("Unexpected success when checking ownership file.");
            }

            public void onFailure(Throwable arg0) {
                installService.getOwnershipFilename(new AsyncCallback<String>() {
                    public void onSuccess(String result) {
                        // Touch the ownership file
                        File pwn = new File(new File("target"), result);
                        Logger.getLogger(this.getClass()).info("Creating file: " + pwn.getPath());
                        try {
                            pwn.createNewFile();
                        } catch (IOException e) {
                            fail("Exception thrown while creating ownership file: " + e.getMessage());
                        }
                        Logger.getLogger(this.getClass()).info("File created: " + pwn.getPath());

                        installService.checkOwnershipFileExists(new AsyncCallback<Boolean>() {
                            public void onSuccess(Boolean result) {
                                assertTrue("File does not exist!", result);
                                Logger.getLogger(this.getClass()).info("File check succeeded");
                                return;
                            }

                            public void onFailure(Throwable e) {
                                fail("Unexpected failure when checking ownership file.");
                            }
                        });
                    }

                    public void onFailure(Throwable arg0) {
                        fail("Unexpected failure when retrieving ownership filename.");
                    }
                });
            }
        });
    }
}
