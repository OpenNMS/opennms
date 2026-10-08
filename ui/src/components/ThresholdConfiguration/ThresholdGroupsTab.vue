<template>
  <div class="threshold-groups-tab">
    <div class="intro">
      <p>
        A threshold group collects the thresholds evaluated against one RRD repository. It takes effect once
        a threshd package applies it.
      </p>
      <div class="intro-actions">
        <OnmsIcon
          :icon="InfoIcon"
          class="info-icon"
          data-test="threshold-groups-info-icon"
          role="button"
          tabindex="0"
          aria-label="About threshold groups"
          @click="isHelpVisible = true"
          @keyup.enter="isHelpVisible = true"
        />
      </div>
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
import { OnmsIcon } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const isHelpVisible = ref(false)
</script>

<style lang="scss" scoped>
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

  .intro-actions {
    display: flex;
    align-items: center;
    gap: 0.75em;
  }

  .info-icon {
    cursor: pointer;
    color: var(--p-text-muted-color);
  }
}
</style>
