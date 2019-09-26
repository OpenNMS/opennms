require('ipaddress-js');

test('isValidIPAddress(abc)', () => {
	expect(window.isValidIPAddress('abc')).toBeFalsy();
});
test('isValidIPAddress(1.2.3.4)', () => {
	expect(window.isValidIPAddress('1.2.3.4')).toBeTruthy();
});
test('isValidIPAddress(::1)', () => {
	expect(window.isValidIPAddress('::1')).toBeTruthy();
});