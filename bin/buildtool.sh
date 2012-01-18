#!/bin/bash

PROJECT="$1"; shift
COMMAND="$1"; shift
BRANCH=`git name-rev HEAD 2>/dev/null | cut -d' ' -f 2-`

if [ -z "$BRANCH" ]; then
	echo "ERROR: unable to get current git branch!"
	exit 1
fi

TIMESTAMPFILE="$HOME/.buildtool-$PROJECT-$BRANCH-timestamp"
REVISIONFILE="$HOME/.buildtool-$PROJECT-$BRANCH-revision"
GITHASHFILE="$HOME/.buildtool-$PROJECT-$BRANCH-githash"

print_stored_timestamp() {
	cat "$TIMESTAMPFILE" 2>/dev/null || echo 0
}

print_stored_revision() {
	cat "$REVISIONFILE" 2>/dev/null || echo 0
}

print_stored_githash() {
	cat "$GITHASHFILE" 2>/dev/null || echo ""
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
		REVISION=1
	fi

	echo $REVISION
}

print_current_githash() {
	git show | head -n 1 | awk '{ print $2 }'
}

print_build_id() {
	local CURRENT_TIMESTAMP=`print_current_timestamp`
	local CURRENT_REVISION=`print_current_revision`

	echo "0.$CURRENT_TIMESTAMP.$CURRENT_REVISION"
}

has_hash_changed() {
	local STORED=`print_stored_githash`
	local CURRENT=`print_current_githash`
	if [ "$STORED" = "$CURRENT" ]; then
		exit 1
	else
		exit 0
	fi
}

update_build_state() {
	local TIMESTAMP=`print_current_timestamp`
	local REVISION=`print_current_revision`
	local GITHASH=`print_current_githash`

	echo $TIMESTAMP > "$TIMESTAMPFILE"
	echo $REVISION  > "$REVISIONFILE"
	echo $GITHASH   > "$GITHASHFILE"
}

usage() {
	cat <<END
usage: $0 <command>

available commands:
	get              Get the latest build ID (format: 0.<timestamp>.<revision>)
	get_stamp        Get the latest build timestamp
	get_revision     Get the latest build revision
	has_hash_changed Return 0 (true) if git has been modified since last save,
	                 1 if not.
	save             Save the current build ID state
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
	has_hash_changed)
		has_hash_changed
		exit $?
		;;
	save)
		update_build_state
		exit 0
		;;
esac

usage
exit 1
