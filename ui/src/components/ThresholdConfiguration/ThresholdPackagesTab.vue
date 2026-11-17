<template>
  <div class="threshd-packages-tab">
    <div class="intro">
      <p>
        A threshd package selects interfaces with a filter and names the threshold groups applied to them.
        <OnmsIconButton
          :icon="InfoIcon"
          iconSize="1.2em"
          class="info-icon-button"
          title="About threshd packages"
          aria-label="About threshd packages"
          data-test="threshd-packages-info-icon"
          @click="isHelpVisible = true"
        />
      </p>
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
import { OnmsIconButton } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const isHelpVisible = ref(false)
</script>

<style lang="scss" scoped>
@use '@/components/ThresholdConfiguration/Common/info-icon-button' as *;

.info-icon-button {
  @include info-icon-button;
}

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
}
</style>
