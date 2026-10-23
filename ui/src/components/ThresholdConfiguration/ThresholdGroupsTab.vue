<template>
  <div class="threshold-groups-tab">
    <div class="intro">
      <p>
        A threshold group collects the thresholds evaluated against one RRD repository. It takes effect once
        a threshd package applies it.
        <OnmsIconButton
          :icon="InfoIcon"
          iconSize="1.2em"
          class="info-icon-button"
          title="About threshold groups"
          aria-label="About threshold groups"
          data-test="threshold-groups-info-icon"
          @click="isHelpVisible = true"
        />
      </p>
    </div>

    <ThresholdGroupsTable />

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="Threshold groups"
      :text="GROUP_HELP"
      @close="isHelpVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ThresholdGroupsTable from '@/components/ThresholdConfiguration/ThresholdGroupsTable.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import { GROUP_HELP } from '@/lib/thresholdHelpText'
import { OnmsIconButton } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const isHelpVisible = ref(false)
</script>

<style lang="scss" scoped>
@use '@/components/ThresholdConfiguration/Common/info-icon-button' as *;

.info-icon-button {
  @include info-icon-button;
}

.threshold-groups-tab {
  .intro {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1em;
    margin-bottom: 1em;

    p {
      margin: 0;
    }
  }
}
</style>
