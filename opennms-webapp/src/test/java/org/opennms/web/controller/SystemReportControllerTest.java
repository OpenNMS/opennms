package org.opennms.web.controller;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Response;
import okhttp3.Request;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



public class SystemReportControllerTest{




    @Before
    public void setUp() {

    }


    @Test
    public void testMockMvcHandleRequest() throws Exception {

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create the request body with form-encoded data
        RequestBody formBody = new FormBody.Builder()
                .add("plugins", "Java")
                .add("formatter", "text")
                .add("output", "johndoe")
                .add("operation", "run")
                .build();

        // Create the request
        Request request = new Request.Builder()
                .url("http://localhost:8980/opennms/admin/support/systemReport.htm")
                .post(formBody)
                .build();

        // Send the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Get the Content-Disposition header
                String contentDisposition = response.header("Content-Disposition");
                Assert.assertNotNull(contentDisposition);
                if (contentDisposition != null) {
                    // Extract the filename from the header
                    String filename = extractFileName(contentDisposition);

                    Assert.assertEquals("abc.txt",filename);
                    if ("abc.txt".equals(filename)) {
                        System.out.println("The file name matches 'abc.txt'.");
                    } else {
                        System.out.println("The file name does not match 'abc.txt'.");
                    }
                } else {

                    System.out.println("Content-Disposition header is not present.");
                }
            } else {
                System.out.println("Request failed: " + response.code());
            }
        }
    }
    // Extract filename from Content-Disposition header
    private static String extractFileName(String contentDisposition) {
        String[] parts = contentDisposition.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                // Remove the "filename=" part and the surrounding quotes
                return part.substring(9).replace("\"", "");
            }
        }
        return null;
    }

}
