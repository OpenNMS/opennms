package org.opennms.webstart;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {
	
	JFrame frame;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	protected static void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		JFrame frame = new JFrame("OpenNMS Remote Poller");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel helloWorld = new JLabel("Welcome to OpenNMS's Remote Poller");
		helloWorld.setPreferredSize(new Dimension(300, 200));
		helloWorld.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(helloWorld, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
		
	}

}
