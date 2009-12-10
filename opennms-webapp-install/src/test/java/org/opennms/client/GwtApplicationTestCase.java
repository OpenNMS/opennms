package org.opennms.client;

// import java.io.File;
// import java.io.IOException;

// import org.apache.log4j.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GwtApplicationTestCase extends GWTTestCase {
    
    public String getModuleName() {
        return "org.opennms.Application";
    }

    public void testOwnershipFile() {
        final InstallServiceAsync installService = (InstallServiceAsync)GWT.create(InstallService.class);

        // Add a delay to allow the async call to complete
        this.delayTestFinish(10000);

        installService.checkOwnershipFileExists(new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                if (result) {
                    fail("Unexpected success when checking ownership file.");
                } else {
                    installService.getOwnershipFilename(new AsyncCallback<String>() {
                        public void onSuccess(String result) {
                            final String filename = result;
                            // Touch the ownership file
                            /*
                            File pwn = new File(new File("target"), result);
                            Logger.getLogger(this.getClass()).info("Creating file: " + pwn.getPath());
                            try {
                                pwn.createNewFile();
                            } catch (IOException e) {
                                fail("Exception thrown while creating ownership file: " + e.getMessage());
                            }
                            Logger.getLogger(this.getClass()).info("File created: " + pwn.getPath());
                            */

                            installService.checkOwnershipFileExists(new AsyncCallback<Boolean>() {
                                public void onSuccess(Boolean result) {
                                    // assertTrue("File " + filename + " does not exist!", result);
                                    // Logger.getLogger(this.getClass()).info("File check succeeded");
                                    finishTest();
                                    return;
                                }

                                public void onFailure(Throwable e) {
                                    fail("Unexpected failure when checking ownership file.");
                                }
                            });
                        }

                        public void onFailure(Throwable e) {
                            fail("Unexpected failure when retrieving ownership filename.");
                        }
                    });
                }
            }

            public void onFailure(Throwable e) {
                fail("Unexpected failure when checking ownership file.");
            }
        });
    }
}
