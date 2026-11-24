<template>
  <OnmsIcon
    v-onms-tooltip.top="TAB_PROBLEM_TEXT"
    :icon="ErrorIcon"
    :title="TAB_PROBLEM_TEXT"
    class="tab-problem-marker"
    data-test="tab-problem-marker"
  />
</template>

<script setup lang="ts">
import { OnmsIcon } from '@opennms/onms-ui'
import ErrorIcon from '@opennms/onms-ui/icons/notification/Error.vue'

// Goes after a tab's label while a field on that tab fails its check: an editor's
// Save stays disabled whichever tab is showing, so the cue must show from the others.
const TAB_PROBLEM_TEXT = 'Fix the errors on this tab to save.'
</script>

<style lang="scss" scoped>
.tab-problem-marker {
  margin-left: 0.375rem;
  color: var(--onms-error);
  vertical-align: text-bottom;
}
</style>
