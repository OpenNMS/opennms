import sys,socket
import time

DEFAULT_PORT = 5817

DEBUG = True

def log(text):
    """ Simple function that prints text to the screen if in DEBUG mode """
    if DEBUG:
        print text

class OpenNMSException(Exception):
    """Base class for all exceptions raised by this module."""

class OpenNMSServerDisconnected(OpenNMSException):
    """Not connected to any OpenNMS server.

    This exception is raised when the server unexpectedly disconnects,
    or when an attempt is made to use the OpenNMSConnection instance before
    connecting it to a server.
    """



class OpenNMSConnection(object):

    debuglevel = 0 

    def setDebug(self, debug):
        """Set the debug output level.

        A non-false value results in debug messages for connection and for all
        messages sent to and received from the server.

        """
        self.debuglevel = debug

    def __init__(self, send_host = 'localhost'):
        """Initialize a new instance."""
        self.sock = None
        self.source = None
        self.setSendHost(send_host)

    def setSource(self, src):
        """ sets the SOURCE string reported to OpenNMS """
        self.source = src

    def setSendHost(self, host):
        """ Sets the hostname reported to OpenNMS as the originator of these events. """
        self.send_host = host

    def connect(self, host, port = 0):
        """Connect to a host on a given port.

        If the hostname ends with a colon (`:') followed by a number, and
        there is no port specified, that suffix will be stripped off and the
        number interpreted as the port number to use.

        Note: This method is automatically invoked by __init__, if a host is
        specified during instantiation.

        """
        #if app_config.test_system \
        #        and host not in ['64.49.215.244', '64.49.215.245']:
        #    raise ValueError, 'Test system must use test opennms: %s' % host
        if not port and (host.find(':') == host.rfind(':')):
            i = host.rfind(':')
            if i >= 0:
                host, port = host[:i], host[i+1:]
                try: 
                    port = int(port)
                except ValueError:
                    raise socket.error, "nonnumeric port"
        if not port: 
            port = DEFAULT_PORT
        if self.debuglevel > 0: 
            print 'connect:', (host, port)
        msg = "getaddrinfo returns an empty list"
        self.sock = None
        for res in socket.getaddrinfo(host, port, 0, socket.SOCK_STREAM):
            af, socktype, proto, canonname, sa = res
            try:
                self.sock = socket.socket(af, socktype, proto)
                if self.debuglevel > 0: 
                    print 'connect:', (host, port)
                self.sock.connect(sa)
            except socket.error, msg:
                if self.debuglevel > 0: 
                    print 'connect fail:', (host, port)
                if self.sock:
                    self.sock.close()
                self.sock = None
                continue
            break
        if not self.sock:
            raise socket.error, msg

    def close(self):
        """Close the connection to the openNMS server."""
        if self.debuglevel > 0: 
            print 'closed connection.'
        if self.sock:
            self.sock.close()
        self.sock = None

    def send(self, str):
        """Send `str' to the server."""
        if self.debuglevel > 0: 
            print 'send:\n', str
        if self.sock:
            try:
                self.sock.sendall(str)
            except socket.error:
                self.close()
                raise OpenNMSServerDisconnected('Server not connected')
        else:
            raise OpenNMSServerDisconnected('please run connect() first')



    def _formatTime(self, time_t):
        """Formats a unix timestamp (sec from epoch) in format OpenNMS expects"""
        from time import gmtime,strftime
        time_struct = gmtime(time_t)
        year = time_struct.tm_year - 1900
        month = time_struct.tm_mon - 1
        time_string = strftime('%A[%w], %B[%%(month)s] %d, %%(year)s %I:%M:%S %p GMT', time_struct)
        # this is done because OpenNMS and python's time.strftime() have different ideas of what numeric
        # years and months should be. 
        time_string = time_string % { 'year' : year, 'month' : month }
        return time_string

    def getTime(self):
       from time import time
       return self._formatTime(time())


    def getMessage(self, uei, interface = None, service = None, **kw):
        """ This generates an XML formatted message string to send to OpenNMS """
        iface_str = ""
        serv_str = ""
        if interface:
            iface_str = "<interface>%s</interface>\n" % interface
        if service:
            serv_str = "<service>%s</service>\n" % service 
        params = []
        for key, val in kw.items():
            params.append("""<parm>
                                  <parmName>%s</parmName>
                                  <value type="string" encoding="text">%s</value>
                             </parm>""" % ( key, val))
        parm_str = ""
        if params:
            parm_str = """<parms>
                            %s
                          </parms>""" % "\n".join(params)
 
        args = {'source' : self.source, 'send_host' : self.send_host,
                'time'   : self.getTime(), 'uei' : uei, 
                'iface'  : iface_str, 'serv' : serv_str, 
                'params' : parm_str }
        message = """<log>
 <events>
  <event>
   <source>%(source)s</source>
   <host>%(send_host)s</host>
   <time>%(time)s</time>
   <uei>%(uei)s</uei>
   %(iface)s%(serv)s%(params)s
  </event>
 </events>
</log>
""" % args
        return message

    def addInterface(self, interface, label, tx_num):
        """Adds an interface to a node in Open NMS
           interface is the IP address (as a string)
           label is a string 
           tx_num is a unique transaction number
        """        
        uei = 'uei.opennms.org/internal/capsd/updateServer'
        message = self.getMessage(uei, interface, nodelabel=label, 
            txno = tx_num, action = 'add')
        self.send(message)

    def deleteInterface(self, interface, label, tx_num):
        """Removes an interface from a node in Open NMS
           interface is the IP address (as a string)
           label is a string 
           tx_num is a unique transaction number
        """        
        uei = 'uei.opennms.org/internal/capsd/updateServer'
        message = self.getMessage(uei, interface, nodelabel=label, 
            txno = tx_num, action = 'delete')
        self.send(message)

    def addService(self, interface, service, tx_num, node_label):
        """
           interface is the IP address (as a string)
           service is the name of a service (like http)
           tx_num is a unique transaction number
           node_label is the computer number  
        """        
        uei = 'uei.opennms.org/internal/capsd/updateService'
        action = 'add'
        message = self.getMessage(uei, interface, service, 
                                  txno = tx_num,
                                  action = action,
                                  nodelabel = node_label)
        self.send(message)

    def deleteService(self, interface, service, tx_num, node_label):
        """
           interface is the IP address (as a string)
           service is the name of a service (like http)
           tx_num is a unique transaction number
           node_label is the computer number  
        """        
        uei = 'uei.opennms.org/internal/capsd/updateService'
        action = 'delete'
        message = self.getMessage(uei, interface, service, 
                                  txno = tx_num,
                                  action = action,
                                  nodelabel = node_label)
        self.send(message)

if __name__ == '__main__':
    tx_base = int(time.time())
    c = OpenNMSConnection()
    c.setDebug(1)
    c.setSendHost('core.example.com')
    c.setSource('CORE')
    c.connect('onms1.sortova.com')
    #c.addNode('10.1.8.10', '21730', tx_base)
    #c.addNode('10.1.8.10', '555', tx_base + 1)
    c.addService('10.1.8.12', 'ICMP', tx_base + 3, '21730')
    #c.changeService('64.49.215.244','DNS', tx_base + 2 , 'DNS')
    c.close()

