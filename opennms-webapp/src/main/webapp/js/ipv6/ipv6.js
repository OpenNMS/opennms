var v6 = {};
var v4 = {};

v6.GROUPS = 8;

v6.RE_BAD_CHARACTERS = /[^0-9a-f:\/]/i;
v6.RE_BAD_ADDRESS = /[0-9a-f]{5,}|:{3,}|[^:]:$/i;

v4.Address = function(address) {

};

v4.Address.fromHex = function(hex) {
   var padded = String("00000000" + hex).slice(-8);

   var ip = [];

   for (var i = 0; i < 8; i += 2) {
      var h = padded.slice(i, i + 2);

      ip.push(parseInt(h, 16));
   }

   return ip.join('.');
};

v6.Address = function(address, groups) {
   this.address = address;

   if (groups == undefined) {
      this.groups = v6.GROUPS;
   } else {
      this.groups = groups;
   }

   this.parsed_address = this.parse(address);

   this.correct = address == this.correct_form();
   this.canonical = address == this.canonical_form();
};

v6.Address.compact = function(address, slice) {
   var s1 = [];
   var s2 = [];

   for (var i = 0; i < address.length; i++) {
      if (i < slice[0]) {
         s1.push(address[i]);
      } else if (i > slice[1]) {
         s2.push(address[i]);
      }
   }

   return s1.concat(['compact']).concat(s2);
}

v6.Address.prototype.isValid = function() {
   return this.valid;
};

v6.Address.prototype.isCorrect = function() {
   return this.correct;
};

v6.Address.prototype.isCanonical = function() {
   return this.canonical;
};

v6.Address.prototype.isTeredo = function() {
   if (/2001:0000/.test(this.canonical_form())) {
      return true;
   }

   return false;
};

v6.Address.prototype.correct_form = function() {
   var groups = [];

   var zero_counter = 0;
   var zeroes = [];

   var last_value = null;

   if (!this.parsed_address) {
      return;
   }

   for (var i = 0; i < this.parsed_address.length; i++) {
      var value = parseInt(this.parsed_address[i], 16);

      if (value === 0) {
         zero_counter++;
      }

      if (value !== 0 && zero_counter > 0) {
         if (zero_counter > 1) {
            zeroes.push([i - zero_counter, i - 1]);
         }

         zero_counter = 0;
      }

      last_value = value;
   }

   // Do we end with a string of zeroes?
   if (zero_counter > 1) {
      zeroes.push([this.parsed_address.length - zero_counter, this.parsed_address.length - 1]);
   }

   var zero_lengths = [];
   for ( var i = 0; i < zeroes.length; i++ ) {
	   var zero = zeroes[i];
	   zero_lengths[i] = (zero[1] - zero[0]) + 1;
   }

   last_value = null;
   var different = false;

   if (zeroes.length > 0) {
      var max = Math.max.apply(Math, zero_lengths);
      var index = zero_lengths.indexOf(max);

      groups = v6.Address.compact(this.parsed_address, zeroes[index]);
   } else {
      groups = this.parsed_address;
   }

   for (var i = 0; i < groups.length; i++) {
      if (groups[i] != 'compact') {
         groups[i] = parseInt(groups[i], 16).toString(16);
      }
   }

   var correct = groups.join(':');

   correct = correct.replace(/^compact$/, '::');
   correct = correct.replace(/^compact|compact$/, ':');
   correct = correct.replace(/compact/, '');

   return correct;
}

v6.Address.prototype.zeroPad = function() {
   var s = this.bigInteger().toString(2);

   return String(
      "0000000000000000" +
      "0000000000000000" +
      "0000000000000000" +
      "0000000000000000" +
      "0000000000000000" +
      "0000000000000000" +
      "0000000000000000" +
      "0000000000000000" + s).slice(-128);
};

v6.Address.prototype.parse = function(address) {
   if (v6.RE_BAD_CHARACTERS.test(address)) {
      this.valid = false;

      return;
   }

   if (v6.RE_BAD_ADDRESS.test(address)) {
      this.valid = false;

      return;
   }

   var address_array = address.split('::');
   var quads;

   if (address_array.length == 2) {
      var first = address_array[0].split(':');
      var last = address_array[1].split(':');

      var remaining = this.groups - (first.length + last.length);

      for (var i = 0; i < first.length; i++) {
         if (first[i] == "") {
            first[i] = 0;
         }
      }

      for (var i = 0; i < last.length; i++) {
         if (last[i] == "") {
            last[i] = 0;
         }
      }

      for (var i = 0; i < remaining; i++) {
         first.push(0);
      }

      quads = first.concat(last);
   } else if (address_array.length == 1) {
      quads = address.split(':');

      if (quads.length != this.groups) {
         this.valid = false;

         return;
      }
   } else {
      this.valid = false;

      return;
   }

   for (var i = 0; i < quads.length; i++) {
      if (quads[i].length > 4) {
         this.valid = false;

         return;
      }
   }

   this.valid = true;

   return quads;
};

v6.Address.prototype.canonical_form = function() {
   if (!this.valid) {
      return;
   }

   var temp = [];

   for (var i = 0; i < this.parsed_address.length; i++) {
      temp.push(sprintf("%04x", parseInt(this.parsed_address[i], 16)));
   }

   return temp.join(':');
};

v6.Address.prototype.decimal = function() {
   if (!this.valid) {
      return;
   }

   var temp = [];

   for (var i = 0; i < this.parsed_address.length; i++) {
      temp.push(sprintf("%05d", parseInt(this.parsed_address[i], 16)));
   }

   return temp.join(':');
};

v6.Address.prototype.bigInteger = function() {
   if (!this.valid) {
      return;
   }

   var temp = [];

   for (var i = 0; i < this.parsed_address.length; i++) {
      temp.push(sprintf("%04x", parseInt(this.parsed_address[i], 16)));
   }

   var b = new BigInteger(temp.join(''), 16);

   return b;
};

v6.Address.prototype.v4_form = function() {
   var s = this.zeroPad().split('');

   var v4_address = v4.Address.fromHex(new BigInteger(s.slice(96, 128).join(''), 2).toString(16));
   var v6_address = new v6.Address(this.parsed_address.slice(0, 6).join(':'), 6);

   var c = v6_address.correct_form();

   var infix = '';

   if (!/:$/.test(c)) {
      infix = ':';
   }

   return v6_address.correct_form() + infix + v4_address;
}

v6.Address.prototype.teredo = function() {
   var s = this.zeroPad().split('');

   /*
      - Bits 0 to 31 are set to the Teredo prefix (normally 2001:0000::/32).
      - Bits 32 to 63 embed the primary IPv4 address of the Teredo server that is used.
      - Bits 64 to 79 can be used to define some flags. Currently only the higher order bit is used; it is set to 1 if the Teredo client is located behind a cone NAT, 0 otherwise. For Microsoft's Windows Vista and Windows Server 2008 implementations, more bits are used. In those implementations, the format for these 16 bits is "CRAAAAUG AAAAAAAA", where "C" remains the "Cone" flag. The "R" bit is reserved for future use. The "U" bit is for the Universal/Local flag (set to 0). The "G" bit is Individual/Group flag (set to 0). The A bits are set to a 12-bit randomly generated number chosen by the Teredo client to introduce additional protection for the Teredo node against IPv6-based scanning attacks.
      - Bits 80 to 95 contains the obfuscated UDP port number. This is the port number that is mapped by the NAT to the Teredo client with all bits inverted.
      - Bits 96 to 127 contains the obfuscated IPv4 address. This is the public IPv4 address of the NAT with all bits inverted.
   */

   var prefix = String("00000000" + new BigInteger(s.slice(0, 32).join(''), 2).toString(16)).slice(-8);
   var server_v4 = v4.Address.fromHex(new BigInteger(s.slice(32, 64).join(''), 2).toString(16));

   var flags = s.slice(64, 80);
   var udp_port = new BigInteger(s.slice(80, 96).join(''), 2);

   var udp_port_dec = udp_port.xor(new BigInteger('ffff', 16)).toString();

   var client_v4 = new BigInteger(s.slice(96, 128).join(''), 2);
   var client_v4_ip = v4.Address.fromHex(client_v4.xor(new BigInteger('ffffffff', 16)).toString(16));

   return {
      prefix: sprintf('%s:%s', prefix.slice(0, 4), prefix.slice(4, 8)),
      server_v4: server_v4,
      flags: flags.join(''),
      udp_port: udp_port_dec,
      client_v4: client_v4_ip
   };
}


function isValidIPAddress(address) {
	if (verifyIPv4Address(address)) return true;
	if (verifyIPv6Address(address)) return true;
	return false;
}

function verifyIPv6Address (ipValue) {
	var address = new v6.Address(ipValue);
	return address.isValid();
}

function verifyIPv4Address (ipValue) {
	var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
	var ipArray = ipValue.match(ipPattern); 

	if (ipArray == null) {
		return false;
	} else {
		for (i = 0; i < 4; i++) {
			thisSegment = ipArray[i];
			if (thisSegment > 255) {
				return false;
			}
		}
	}

	return true;
}