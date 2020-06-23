const ipaddress = require('ip-address');

const verifyIPv4Address = function(ip) {
	const addr = new ipaddress.Address4(ip);
	return addr.isValid();
};

const verifyIPv6Address = function(ip) {
	const addr = new ipaddress.Address6(ip);
	return addr.isValid();
};

const isValidIPAddress = function(ip) {
	return verifyIPv4Address(ip) || verifyIPv6Address(ip);
};

const checkIpRange = function(ip1, ip2){
    if (verifyIPv4Address(ip1) && verifyIPv4Address(ip2)) {
        const a = new ipaddress.Address4(ip1).bigInteger();
        const b = new ipaddress.Address4(ip2).bigInteger();
        return b >= a;
    }
    if (verifyIPv6Address(ip1) && verifyIPv6Address(ip2)) {
        const a = new ipaddress.Address6(ip1).bigInteger();
        const b = new ipaddress.Address6(ip2).bigInteger();
        return b.compareTo(a) >= 0;
    }
    return false;
};

console.log('init: ipaddress-js'); // eslint-disable-line no-console

module.exports = {
	Address4: ipaddress.Address4,
	Address6: ipaddress.Address6,
	v6: ipaddress.v6,
	verifyIPv4Address: verifyIPv4Address,
	verifyIPv6Address: verifyIPv6Address,
	isValidIPAddress: isValidIPAddress,
	checkIpRange: checkIpRange
};

Object.assign(window, module.exports);