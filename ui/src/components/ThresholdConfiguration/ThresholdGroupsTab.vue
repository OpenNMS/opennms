<template>
  <div class="threshold-groups-tab">
    <div class="intro">
      <p>
        A threshold group collects the thresholds evaluated against one RRD repository. It takes effect once
        a threshd package applies it.
        <InfoIconButton
          ariaLabel="About threshold groups"
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
import InfoIconButton from '@/components/Common/InfoIconButton.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import { GROUP_HELP } from '@/lib/thresholdHelpText'

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
}
</style>
