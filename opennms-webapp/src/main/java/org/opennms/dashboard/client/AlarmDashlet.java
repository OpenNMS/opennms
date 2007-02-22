package org.opennms.dashboard.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;

public class AlarmDashlet extends Dashlet {
    
    private AlarmView m_view = new AlarmView();
    private AlarmLoader m_loader = new AlarmLoader();
    
    class AlarmLoader extends DashletLoader {

        public void load(final SurveillanceIntersection intersection) {
            setStatus("Loading...");
            Timer timer = new Timer() {

                public void run() {
                    loaded(intersection);
                }
                
            };
            timer.schedule(3000);
            
        }
        
        public void loaded(SurveillanceIntersection intersection) {
            m_view.setText("Alarms for "+intersection);
            setStatus("");
        }
        
    }
    
    class AlarmView extends DashletView {
        
        Label m_label = new Label();
        
        AlarmView() {
            initWidget(m_label);
        }
        
        public void setText(String text) {
            m_label.setText(text);
        }
        
    }
    
    AlarmDashlet() {
        super("Alarms");
        setLoader(m_loader);
        setView(m_view);
    }

    public void setIntersection(SurveillanceIntersection intersection) {
        m_loader.load(intersection);
    }

}
