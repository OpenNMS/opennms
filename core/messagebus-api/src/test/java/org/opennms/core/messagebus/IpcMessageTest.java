package org.opennms.core.messagebus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.Test;

public class IpcMessageTest {

    @Test
    public void shouldCreateWithRequiredFields() {
        IpcMessage msg = new IpcMessage("reloadDaemonConfig", "webui");
        assertThat(msg.getType()).isEqualTo("reloadDaemonConfig");
        assertThat(msg.getSource()).isEqualTo("webui");
        assertThat(msg.getTimestamp()).isPositive();
        assertThat(msg.getParameters()).isEmpty();
    }

    @Test
    public void shouldCreateWithParameters() {
        IpcMessage msg = new IpcMessage("reloadDaemonConfig", "webui",
                Map.of("daemonName", "pollerd"));
        assertThat(msg.getParameter("daemonName")).isEqualTo("pollerd");
    }

    @Test
    public void shouldRejectNullType() {
        assertThatThrownBy(() -> new IpcMessage(null, "webui"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldReturnUnmodifiableParameters() {
        IpcMessage msg = new IpcMessage("test", "src", Map.of("k", "v"));
        assertThatThrownBy(() -> msg.getParameters().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
