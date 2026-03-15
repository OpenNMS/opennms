// ScriptPolicy for Delta-V passive monitoring E2E test.
// Sets the node label to "The Internet" for all discovered nodes.
// Used in conjunction with LoopDetector-assigned passive services
// (GoogleCloud, Azure, AWS) monitored by PassiveServiceMonitor.

node.setLabel("The Internet")
return node
