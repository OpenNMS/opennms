#!/usr/bin/env python3

import socket
import docopt


__doc__ = """Send UDP packets from files

Usage:
    send.py [options] <files>...

Options:
    -h --help             Show this screen
    -t --target <target>  Target to send packets to [default: localhost:50000]
    -p --pause            Pause aufter each packet

"""


if __name__ == '__main__':
    args = docopt.docopt(__doc__)

    target = args['--target'].split(':', 1)
    if len(target) == 1:
        target = (target[0], 50000)
    else:
        target = (target[0], int(target[1]))

    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('', 0))

    print(s.getsockname())
    print()

    for path in args['<files>']:
        with open(path, 'rb') as f:
            msg = f.read()
            s.sendto(msg, target)

            print("%s: %d bytes" % (path, len(msg)))

            if args['--pause']:
                input()

