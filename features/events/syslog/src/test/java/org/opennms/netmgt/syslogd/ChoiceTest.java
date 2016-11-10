package org.opennms.netmgt.syslogd;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import static org.apache.camel.component.mock.MockEndpoint.expectsMessageCount;

/**
 * @version 
 */
public class ChoiceTest extends ContextTestSupport {
    protected MockEndpoint x;
    protected MockEndpoint y;
    protected MockEndpoint z;
    protected MockEndpoint end;

    public void testSendToFirstWhen() throws Exception {
        String body = "<one/>";
        x.expectedBodiesReceived(body);
        end.expectedBodiesReceived(body);
        // The SpringChoiceTest.java can't setup the header by Spring configure file
        // x.expectedHeaderReceived("name", "a");
        expectsMessageCount(1, y, z);

        sendMessage("bar", body);

        assertMockEndpointsSatisfied();
    }

   /* public void testSendToSecondWhen() throws Exception {
        String body = "<two/>";
        y.expectedBodiesReceived(body);
        end.expectedBodiesReceived(body);
        expectsMessageCount(0, x, z);

        sendMessage("cheese", body);

        assertMockEndpointsSatisfied();
    }*/

   /* public void testSendToOtherwiseClause() throws Exception {
        String body = "<three/>";
        z.expectedBodiesReceived(body);
        end.expectedBodiesReceived(body);
        expectsMessageCount(0, x, y);

        sendMessage("somethingUndefined", body);

        assertMockEndpointsSatisfied();
    }*/

    protected void sendMessage(final Object headerValue, final Object body) throws Exception {
        template.sendBodyAndHeader("direct:start", body, "foo", headerValue);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        x = getMockEndpoint("mock:x");
        y = getMockEndpoint("mock:y");
        z = getMockEndpoint("mock:z");
        end = getMockEndpoint("mock:end");
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start").choice()
                  .when().xpath("$foo = 'b'").to("log:hi.............######################################")
                  .when().xpath("$foo = 'bar'").to("log:bye.............######################################")
                  .otherwise().to("mock:z").end().to("mock:end");
            }
        };
    }

}