<template>
  <div class="search-help" data-test="search-help">
    <p>
      Searching by name is a case-insensitive, inclusive search. For example, searching on <em>serv</em> would find any of
      <em>serv</em>, <em>Service</em>, <em>Reserved</em>, <em>NTSERV</em>, <em>UserVortex</em>, etc. The underscore character
      acts as a single character wildcard. The percent character acts as a multiple character wildcard.
    </p>
    <p>
      Searching by TCP/IP address uses a very flexible search format, allowing you to separate the four or eight (in case of
      IPv6) fields of a TCP/IP address into specific searches. An asterisk (*) in place of any octet matches any value for
      that octet. Ranges are indicated by two numbers separated by a dash (-), and commas are used for list demarcation.
    </p>
    <p>
      For example, the following search fields are all valid and would each create the same result set — all TCP/IP addresses
      from 192.168.0.0 through 192.168.255.255:
    </p>
    <ul>
      <li>192.168.*.*</li>
      <li>192.168.0-255.0-255</li>
      <li>192.168.0,1,2,3-255.*</li>
      <li>2001:6a8:3c80:8000-8fff:*:*:*:*</li>
      <li>fc00,fe80:*:*:*:*:*:*:*</li>
    </ul>
    <p>
      A search for ifAlias, ifName, or ifDescr "contains" will find nodes with interfaces that match the given search string.
      This is a case-insensitive inclusive search similar to the "name" search described above. If the search modifier is
      "equals" rather than "contains" an exact match must be found.
    </p>
    <p>To search by Service, click the down arrow and select the service you would like to search for.</p>
    <p>
      Searching by MAC Address allows you to find interfaces with hardware (MAC) addresses matching the search string.
      This is a case-insensitive partial string match. For example, you can find all interfaces with a specified
      manufacturer's code by entering the first 6 characters of the mac address. Octet separators (dash or colon) are optional.
    </p>
    <p>
      Searching for assets allows you to search for all assets which have been associated with a particular category, as well
      as to select a specific asset field (with all available fields listed in the drop-down list box) and search for text
      which matches its current value. The latter search is very similar to the text search for node names described above.
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.search-help {
  p {
    margin: 0 0 0.75rem 0;
  }

  ul {
    margin: 0 0 0.75rem 1.25rem;
  }

  em {
    font-style: italic;
  }
}
</style>
