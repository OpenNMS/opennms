[![travis]](http://travis-ci.org/beaugunderson/ip-address)
[![codecov.io](https://codecov.io/github/beaugunderson/ip-address/coverage.svg?branch=master)](https://codecov.io/github/beaugunderson/ip-address?branch=master)
[![downloads]](https://www.npmjs.com/package/ip-address)
[![version]](https://www.npmjs.com/package/ip-address)

[travis]: https://img.shields.io/travis/beaugunderson/ip-address.svg
[coverage]: https://img.shields.io/coveralls/beaugunderson/ip-address.svg
[downloads]: https://img.shields.io/npm/dm/ip-address.svg
[version]: https://img.shields.io/npm/v/ip-address.svg

## ip-address

`ip-address` is a library for validating and manipulating IPv4 and IPv6
addresses in JavaScript.

### Upgrading to 5.0

- `v4.Address` and `v6.Address` have been renamed `Address4` and `Address6`
- `Address4#toV6Group` has been renamed `Address4#toGroup6`
- `Address6#get6to4` has been renamed `Address6#to6to4`
- `Address6#six2four` has been renamed `Address6#inspect6to4`
- `Address6#teredo` has been renamed `Address6#inspectTeredo`
- `Address6#tov4` has been renamed `Address6#to4`
- `Address6#v4inv6` has been renamed `Address6#to4in6`

Deprecation warnings are in place for all of these methods.

### Documentation

Documentation is available at [ip-address.js.org](http://ip-address.js.org/).

### Examples

```js
var Address6 = require('ip-address').Address6;

var address = new Address6('2001:0:ce49:7601:e866:efff:62c3:fffe');

address.isValid(); // true

var teredo = address.inspectTeredo();

teredo.client4;    // '157.60.0.1'
```

### Features

- Parsing of all IPv6 notations
- Parsing of IPv6 addresses and ports from URLs with `Address6.fromURL(url)`
- Validity checking
- Decoding of the [Teredo
  information](http://en.wikipedia.org/wiki/Teredo_tunneling#IPv6_addressing)
  in an address
- Whether one address is a valid subnet of another
- What special properties a given address has (multicast prefix, unique
  local address prefix, etc.)
- Number of subnets of a certain size in a given address
- Display methods
  - Hex, binary, and decimal
  - Canonical form
  - Correct form
  - IPv4-compatible (i.e. `::ffff:192.168.0.1`)
- Works in [node](http://nodejs.org/) and the browser (with browserify)
- ~1,600 test cases

### Used by

- [anon](https://github.com/edsu/anon) which powers
  [@congressedits](https://twitter.com/congressedits), among
  [many others](https://github.com/edsu/anon#community)
- [base85](https://github.com/noseglid/base85): base85 encoding/decoding
- [contrail-web-core](https://github.com/Juniper/contrail-web-core): part of
  Contrail, a network virtualization solution made by Juniper Networks
- [dhcpjs](https://github.com/apaprocki/node-dhcpjs): a DHCP client and server
- [epochtalk](https://github.com/epochtalk/epochtalk): next generation forum
  software
- [geoip-web](https://github.com/tfrce/node-geoip-web): a server for
  quickly geolocating IP addresses
- [hexabus](https://github.com/mysmartgrid/hexabus): an IPv6-based home
  automation bus
- [hubot-deploy](https://github.com/atmos/hubot-deploy): GitHub Flow via hubot
- [heroku-portscanner](https://github.com/robison/heroku-portscanner): nmap
  hosted on Heroku
- [ipfs-swarm](https://github.com/diasdavid/node-ipfs-swarm): a swarm
  implementation based on IPFS
- [javascript-x-server](https://github.com/GothAck/javascript-x-server): an X
  server written in JavaScript
- [libnmap](https://github.com/jas-/node-libnmap): a node API for nmap
- [mail-io](https://github.com/mofux/mail-io): a lightweight SMTP server
- [maxmind-db-reader](https://github.com/PaddeK/node-maxmind-db): a library for
  reading MaxMind database files
- [proxy-protocol-v2](https://github.com/ably/proxy-protocol-v2): a proxy
  protocol encoder/decoder built by [Ably](https://www.ably.io/)
- [Samsara](https://github.com/mariusGundersen/Samsara): a Docker web interface
- [sis-api](https://github.com/sis-cmdb/sis-api): a configuration management
  database API
- [socks5-client](https://github.com/mattcg/socks5-client): a SOCKS v5 client
- [socksified](https://github.com/vially/node-socksified): a SOCKS v5 client
- [socksv5](https://github.com/mscdex/socksv5): a SOCKS v5 server/client
- [ssdapi](https://github.com/rsolomou/ssdapi): an API created by the
  University of Portsmouth
- [SwitchyOmega](https://github.com/FelisCatus/SwitchyOmega): a [Chrome
  extension](https://chrome.google.com/webstore/detail/padekgcemlokbadohgkifijomclgjgif)
  for switching between multiple proxies with ~311k users!
- [swiz](https://github.com/racker/node-swiz): a serialization framework built
  and used by [Rackspace](http://www.rackspace.com/)
