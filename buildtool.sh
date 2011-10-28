#!/bin/bash

COMMAND="$1"; shift
BRANCH=`git name-rev HEAD 2>/dev/null | cut -d' ' -f 2-`

if [ -z "$BRANCH" ]; then
	echo "ERROR: unable to get current git branch!"
	exit 1
fi

TIMESTAMPFILE="$HOME/.buildtool-$BRANCH-timestamp"
REVISIONFILE="$HOME/.buildtool-$BRANCH-revision"

print_stored_timestamp() {
	cat "$TIMESTAMPFILE" 2>/dev/null || echo 0
}

print_stored_revision() {
	cat "$REVISIONFILE" 2>/dev/null || echo 0
}

print_current_timestamp() {
	git log --pretty='format:%cd' --date=short -1 | head -n 1 | sed -e 's,^Date: *,,' -e 's,-,,g'
}

print_current_revision() {
	local STORED_REVISION=`print_stored_revision`

	local STORED_TIMESTAMP=`print_stored_timestamp`
	local CURRENT_TIMESTAMP=`print_current_timestamp`

	local REVISION="$STORED_REVISION"

	if [ "$STORED_TIMESTAMP" = "$CURRENT_TIMESTAMP" ]; then
		REVISION=`expr $REVISION + 1`
	else
		if [ "$REVISION" = "0" ]; then
			REVISION=1
		fi
	fi

	echo $REVISION
}

print_build_id() {
	local CURRENT_TIMESTAMP=`print_current_timestamp`
	local CURRENT_REVISION=`print_current_revision`

	echo "0.$CURRENT_TIMESTAMP.$CURRENT_REVISION"
}

update_build_state() {
	local TIMESTAMP=`print_current_timestamp`
	local REVISION=`print_current_revision`

	echo $TIMESTAMP > "$TIMESTAMPFILE"
	echo $REVISION > "$REVISIONFILE"
}

usage() {
	cat <<END
usage: $0 <command>

available commands:
	get          Get the latest build ID (format: 0.<timestamp>.<revision>)
	get_stamp    Get the latest build timestamp
	get_revision Get the latest build revision
	save         Save the current build ID state
END

}

case "$COMMAND" in
	get)
		print_build_id
		exit 0
		;;
	get_stamp)
		print_current_timestamp
		exit 0
		;;
	get_revision)
		print_current_revision
		exit 0
		;;
	save)
		update_build_state
		exit 0
		;;
esac

usage
exit 1
