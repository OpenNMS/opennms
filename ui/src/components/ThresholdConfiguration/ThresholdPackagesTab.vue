<template>
  <div class="threshd-packages-tab">
    <div class="intro">
      <p>
        A threshd package selects interfaces with a filter and names the threshold groups applied to them.
      </p>
      <div class="intro-actions">
        <OnmsIcon
          :icon="InfoIcon"
          class="info-icon"
          data-test="threshd-packages-info-icon"
          role="button"
          tabindex="0"
          aria-label="About threshd packages"
          @click="isHelpVisible = true"
          @keyup.enter="isHelpVisible = true"
        />
      </div>
    </div>

    <ThresholdPackagesTable />

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="Threshd packages"
      :text="PACKAGE_HELP"
      @close="isHelpVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ThresholdPackagesTable from '@/components/ThresholdConfiguration/ThresholdPackagesTable.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import { PACKAGE_HELP } from '@/lib/thresholdHelpText'
import { OnmsIcon } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const isHelpVisible = ref(false)
</script>

<style lang="scss" scoped>
.threshd-packages-tab {
  display: flex;
  flex-direction: column;
  gap: 1em;

  .intro {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1em;

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
