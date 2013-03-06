#!/bin/bash

grep -n TODO leaflet-search.js  | sed 's/^\([0-9]*\):.*\/\/TODO\(.*\)$/\.\2, row \1\n/g' > TODO

grep -n FIXME leaflet-search.js  | sed 's/^\([0-9]*\):.*\/\/FIXME\(.*\)$/\.\2, row \1\n/g' > BUGS

