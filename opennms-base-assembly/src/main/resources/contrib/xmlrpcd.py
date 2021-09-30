# Server code

import SimpleXMLRPCServer

class StringFunctions:
    def __init__(self):
        # Make all of the Python string functions available through
        # python_string.func_name
        import string
        self.python_string = string

    def _privateFunction(self):
        # This function cannot be called through XML-RPC because it
        # starts with an '_'
        pass
    
    def chop_in_half(self, astr):
        return astr[:len(astr)/2]

    def repeat(self, astr, times):
        return astr * times
    
class Events:
	def notifySuccess(self, txid, uei, message):
		print "notifySuccess: transaction id = " + txid + ", uei = " + uei + ", message = " + message + "\n"
		return True

	def notifyFailure(self, txid, uei, message):
		print "notifyFailure: transaction id = " + txid + ", uei = " + uei + ", message = " + message + "\n"
		return True

	def notifyReceivedEvent(self, txid, uei, message):
		print "notifyReceivedEvent: transaction id = " + txid + ", uei = " + uei + ", message = " + message + "\n"
		return True

	def sendServiceDownEvent(self, nodelabel, interface, service, unused, eventhost, time):
		print "sendServiceDownEvent: nodelabel = " + nodelabel + ", interface = " + interface + ", service = " + service + ", eventhost = " + eventhost + ", time = " + time + "\n";
		return True

	def sendServiceUpEvent(self, nodelabel, interface, service, unused, eventhost, time):
		print "sendServiceUpEvent: nodelabel = " + nodelabel + ", interface = " + interface + ", service = " + service + ", eventhost = " + eventhost + ", time = " + time + "\n";
		return True

	def sendInterfaceDownEvent(self, nodelabel, interface, eventhost, time):
		print "sendInterfaceDownEvent: nodelabel = " + nodelabel + ", interface = " + interface + ", eventhost = " + eventhost + ", time = " + time + "\n";
		return True

	def sendInterfaceUpEvent(self, nodelabel, interface, host, eventhost, time):
		print "sendInterfaceUpEvent: nodelabel = " + nodelabel + ", interface = " + interface + ", host = " + host + ", eventhost = " + eventhost + ", time = " + time + "\n";
		return True

	def sendNodeDownEvent(self, nodelabel, eventhost, time):
		print "sendNodeDownEvent: nodelabel = " + nodelabel + ", eventhost = " + eventhost + ", time = " + time + "\n"
		return True

	def sendNodeUpEvent(self, nodelabel, eventhost, time):
		print "sendNodeUpEvent: nodelabel = " + nodelabel + ", eventhost = " + eventhost + ", time = " + time + "\n"
		return True

	def sendEvent(self, event):
		print "sendEvent: event = " + event + "\n"
		return True

server = SimpleXMLRPCServer.SimpleXMLRPCServer(("localhost", 8000))
server.register_instance(Events())
server.serve_forever()
