"""
This is a helper class that creates an XML-RPC server that will accept events
from the ONMS server.
"""

from SimpleXMLRPCServer import SimpleXMLRPCServer

# This is the port that the XML-RPC server will run on
SERVER_PORT = 9191

# This is the IP address that the XML-RPC server is running on
SERVER_IP = '192.168.1.100'

# These are possible events that can be received in response to configuration
class EVENT:
    Received = 1
    Failure = 2
    Success = 3
    
# This is a collection of data that is returned via XML-RPC
class ReturnData:
    event = None
    txNo = None
    uei = None
    message = None


def log(msg):
	msg = msg

# Workaround so we don't get can't use socket errors
class XMLRPCServer(SimpleXMLRPCServer):
    pass
XMLRPCServer.allow_reuse_address = True
            
class TestXMLRPCServer:
    def __init__(self):
        # Call the base constructor
        self.server = XMLRPCServer((SERVER_IP, SERVER_PORT), logRequests=False)
        # Register the functions that are available
        self.server.register_function(self.notifyReceivedEvent)
        self.server.register_function(self.notifyFailure)
        self.server.register_function(self.notifySuccess)
        self.server.register_function(self.sendServiceDownEvent)
        self.server.register_function(self.sendServiceUpEvent)
        self.server.register_function(self.sendInterfaceDownEvent)
        self.server.register_function(self.sendInterfaceUpEvent)
        self.server.register_function(self.sendNodeDownEvent)
        self.server.register_function(self.sendNodeUpEvent)
        self.complete = False
        self.transaction = None
        self.returnData = ReturnData()
    
    def sendServiceDownEvent(self,
            node_label, node_ip_address, 
            service_name, poll_method, 
            poller_hostname, event_time):
        log('sendServiceDownEvent')
        return 1
        
    def sendServiceUpEvent(self,
            node_label, node_ip_address, 
            service_name, poll_method, 
            poller_hostname, event_time):
        log('sendServiceUpEvent')
        return 1
        
    def sendInterfaceDownEvent(self,
            node_label, node_ip_address, 
            poller_hostname, event_time):
        log('sendInterfaceDownEvent')
        return 1        
                
    def sendInterfaceUpEvent(self,
            node_label, node_ip_address, 
            poller_hostname, event_time):
        log('sendInterfaceUpEvent')
        return 1
        
    def sendNodeDownEvent(self,
            node_label, 
            poller_hostname, event_time):
        log('sendNodeDownEvent')
        return 1
                
    def sendNodeUpEvent(self,
            node_label, 
            poller_hostname, event_time):
        log('sendNodeUpEvent')
        return 1
        
    def notifyReceivedEvent(self, txNo, uei, message):
        self.returnData.event = EVENT.Received
        self.returnData.txNo = txNo
        self.returnData.uei = uei
        self.returnData.message = message
        log('notifyReceivedEvent')
        return 1
        
    def notifyFailure(self, txNo, uei, message):
        self.returnData.event = EVENT.Failure
        self.returnData.txNo = txNo
        self.returnData.uei = uei
        self.returnData.message = message
        # If this is the right transaction number, then we can stop the server
        log('notifyFailure')
        if self.transaction == int(txNo):
          self.complete = True
        return 1
        
    def notifySuccess(self, txNo, uei, message):
        log('notifySuccess')
        self.returnData.event = EVENT.Success
        self.returnData.txNo = txNo
        self.returnData.uei = uei
        self.returnData.message = message
        # If this is the right transaction number, then we can stop the server
        if self.transaction == int(txNo):
          self.complete = True
        return 1
        
    def run(self, transaction):
        # Note that this will run until it gets a success or failure event for
        # the givien transaction
        self.complete = False
        self.transaction = transaction
        while not self.complete:
            self.server.handle_request()
        return self.returnData

if __name__=='__main__':
    server = TestXMLRPCServer()
    server.run(0)

