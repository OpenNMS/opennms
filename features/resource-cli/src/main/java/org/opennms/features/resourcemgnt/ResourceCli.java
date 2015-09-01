package org.opennms.features.resourcemgnt;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.web.rest.v1.ResourceDTO;
import org.opennms.web.rest.v1.ResourceDTOCollection;

public class ResourceCli {

    /**
     * Command line options
     */
    enum Action {
        LIST,
        DELETE
    }

    @Argument(required = true, index = 0, usage = "type of action")
    private Action action;

    @Option(required = false, name = "--url", usage = "URL of your OpenNMS installation")
    private String baseUrl = "http://localhost:8980/opennms";

    @Option(required = false, name = "--username", usage = "username")
    private String username = "admin";

    @Option(required = false, name = "--password", usage = "password")
    private String password = "admin";

    @Option(required = false, name = "--resource", usage = "resource")
    private String resource;

    @Option(name = "--help", usage = "display help and exit")
    private boolean help = false;

    public static void main(final String args[]) {
        ResourceCli resourceCli = new ResourceCli();
        //resourceCli.parseArguments(args);
        resourceCli.execute();
    }

    public void parseArguments(final String args[]) {
        /**
         * Parse the arguments
         */
        final CmdLineParser cmdLineParser = new CmdLineParser(this);

        try {
            cmdLineParser.parseArgument(args);
        } catch (final CmdLineException e) {
            System.err.println(e.getMessage() + "\n");

            displayHelp(cmdLineParser);
            System.exit(-1);
        }

        /**
         * Display help message if "--help" was used
         */
        if (help) {
            displayHelp(cmdLineParser);
            System.exit(0);
        }
    }

    public void execute() {
        /**
         * Initialize the rest stuff
         */
        final DefaultApacheHttpClientConfig defaultApacheHttpClientConfig = new DefaultApacheHttpClientConfig();
        defaultApacheHttpClientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        defaultApacheHttpClientConfig.getProperties().put(defaultApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, Boolean.TRUE);
        defaultApacheHttpClientConfig.getState().setCredentials(null, null, -1, username, password);
        final ApacheHttpClient apacheHttpClient = ApacheHttpClient.create(defaultApacheHttpClientConfig);

        final WebResource webResource = apacheHttpClient.resource(baseUrl + "/rest/resources");

        try {
            ResourceDTOCollection resourceDTOCollection = webResource.header("Accept", "application/xml").get(ResourceDTOCollection.class);

            for (final ResourceDTO resourceDTO : resourceDTOCollection.getObjects()) {
                System.out.println(resourceDTO.getId());
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

    }

    /**
     * Displays the usage help message.
     *
     * @param cmdLineParser the parser instance to be used
     */
    private void displayHelp(final CmdLineParser cmdLineParser) {
        System.err.println("Simple resource management tool\n");
        cmdLineParser.printUsage(System.err);
    }

}
