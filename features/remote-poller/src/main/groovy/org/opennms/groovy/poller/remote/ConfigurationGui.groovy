package org.opennms.groovy.poller.remote;

import groovy.swing.SwingBuilder;

class ConfigurationGui {
	def swing = new SwingBuilder();
	def gui = swing.frame(title:'Test', size:[200,100]) {
		label(text:"I'm detecting a lack of awesomeness.");
	}
	
	public static void main(String[] args) {
		println "you suck!\n"
		def gui = new ConfigurationGui();
		gui.gui.show();
	}
}
