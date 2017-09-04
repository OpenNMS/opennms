/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd.pd;

import com.google.common.base.Throwables;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.notifd.pd.client.api.PDClient;
import org.opennms.netmgt.notifd.pd.client.api.PDClientFactory;
import org.opennms.netmgt.notifd.pd.client.api.PDEvent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PagerDutyNotificationStrategyTest {

    @Test
    public void canSendEvent() throws IOException {
        OnmsEvent event = new OnmsEvent();
        event.setEventSeverity(OnmsSeverity.MAJOR.getId());
        PagerDutyNotice notice = new PagerDutyNotice(event, 1, "subject", "body");

        PDEvent eventSent = triggerNotice(notice);

        assertThat(eventSent.getClient(), equalTo("OpenNMS"));
    }

    private static PDEvent triggerNotice(PagerDutyNotice notice) {
        PDClient client = mock(PDClient.class);
        PDClientFactory clientFactory = mock(PDClientFactory.class);
        when(clientFactory.getClient()).thenReturn(client);

        PagerDutyNotificationStrategy strategy = new PagerDutyNotificationStrategy();
        strategy.setClientFactory(clientFactory);

        strategy.send(notice);

        ArgumentCaptor<PDEvent> argument = ArgumentCaptor.forClass(PDEvent.class);
        try {
            verify(client).sendEvent(argument.capture());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return argument.getValue();
    }
}
