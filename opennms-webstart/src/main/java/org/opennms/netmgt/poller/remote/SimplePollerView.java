package org.opennms.netmgt.poller.remote;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class SimplePollerView implements PollerView {

	private Component m_contents;

	public void showView() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public void setContents(Component contents) {
		m_contents = contents;
	}
	
	protected void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		JFrame frame = new JFrame("OpenNMS Remote Poller");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(m_contents, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
		
	}


}
