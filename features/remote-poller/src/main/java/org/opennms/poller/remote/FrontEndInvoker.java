/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.poller.remote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.opennms.netmgt.model.ScanReport;
import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.opennms.netmgt.poller.remote.PollerFrontEnd.PollerFrontEndStates;
import org.opennms.netmgt.poller.remote.support.ScanReportPollerFrontEnd.ScanReportProperties;

public class FrontEndInvoker extends SwingWorker<ScanReport, Integer> implements PropertyChangeListener {
    private final CountDownLatch m_latch = new CountDownLatch(1);
    private final PollerFrontEnd m_frontEnd;
    private final ScanReportHandler m_handler;
    private final String m_locationName;
    private ScanReport m_scanReport;

    public FrontEndInvoker(final PollerFrontEnd pfe, final ScanReportHandler handler, final String locationName) {
        m_frontEnd = pfe;
        m_handler = handler;
        m_locationName = locationName;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ScanReportProperties.percentageComplete.toString())) {
            int percentComplete = Long.valueOf(Math.round((Double)evt.getNewValue() * 100.0)).intValue();
            setProgress(percentComplete);
        } else if (evt.getPropertyName().equals(PollerFrontEndStates.exitNecessary.toString())) {
            final ScanReport report = (ScanReport)evt.getNewValue();
            System.out.println("Finished scan: " + report);
            m_scanReport = report;
            m_latch.countDown();
        } else {
            System.err.println("Unhandled property change event: " + evt.getPropertyName());
        }
    }

    @Override
    protected ScanReport doInBackground() throws Exception {
        m_frontEnd.addPropertyChangeListener(this);
        m_frontEnd.initialize();
        m_frontEnd.register(m_locationName);
        m_latch.await();
        return m_scanReport;
    }

    @Override
    protected void done() {
        m_frontEnd.removePropertyChangeListener(this);
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                m_handler.scanComplete(m_scanReport);
            }
        });
    }

}