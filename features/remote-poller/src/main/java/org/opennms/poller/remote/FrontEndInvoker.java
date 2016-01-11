package org.opennms.poller.remote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.opennms.netmgt.poller.remote.PollerFrontEnd;
import org.opennms.netmgt.poller.remote.PollerFrontEnd.PollerFrontEndStates;
import org.opennms.netmgt.poller.remote.support.ScanReport;
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