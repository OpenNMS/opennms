<template>
  <div class="threshd-packages-tab">
    <div class="intro">
      <p>
        A threshd package selects interfaces with a filter and names the threshold groups applied to them.
        <InfoIconButton
          ariaLabel="About threshd packages"
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
import InfoIconButton from '@/components/Common/InfoIconButton.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import { PACKAGE_HELP } from '@/lib/thresholdHelpText'

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
}
</style>
