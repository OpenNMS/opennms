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
        <OnmsButton variant="outlined" data-test="threshold-reload" @click="isReloadConfirmVisible = true">
          Reload threshold configuration
        </OnmsButton>
      </div>
    </div>

    <ThresholdGroupsTable />

    <OnmsConfirmationDialog
      v-if="isReloadConfirmVisible"
      :visible="true"
      title="Reload the threshold configuration?"
      actionButtonText="Reload"
      data-test="threshold-reload-confirm"
      @ok="onReload"
      @cancel="isReloadConfirmVisible = false"
    >
      <template #content>
        <p>Threshd will re-read the stored thresholding configuration. Thresholds are re-evaluated from scratch.</p>
      </template>
    </OnmsConfirmationDialog>

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
import useSnackbar from '@/composables/useSnackbar'
import { GROUP_HELP } from '@/lib/thresholdHelpText'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { OnmsButton, OnmsConfirmationDialog, OnmsIcon } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const store = useThresholdGroupStore()
const { showSnackBar } = useSnackbar()

const isHelpVisible = ref(false)
const isReloadConfirmVisible = ref(false)

const onReload = async () => {
  isReloadConfirmVisible.value = false

  const result = await store.reloadThresholdConfiguration()

  showSnackBar({
    msg: result.success ? 'Threshd has been asked to reload the threshold configuration.' : result.message,
    error: !result.success
  })
}
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
