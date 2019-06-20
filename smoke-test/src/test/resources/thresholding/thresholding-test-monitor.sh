#!/bin/bash
DELAY_IN_MS=$1

re='^[0-9]+$'
if ! [[ $DELAY_IN_MS =~ $re ]] ; then
   echo "error: '$DELAY_IN_MS' is not a number'" >&2; exit 1
fi

DELAY_IN_SECONDS=$(echo "$DELAY_IN_MS / 1000" | bc)
sleep "$DELAY_IN_SECONDS"

echo "OK"
exit 0