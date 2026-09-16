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
        <OnmsButton variant="outlined" data-test="threshd-reload" @click="isReloadConfirmVisible = true">
          Reload threshd configuration
        </OnmsButton>
      </div>
    </div>

    <ThreshdGeneralPanel />
    <ThresholdPackagesTable />

    <OnmsConfirmationDialog
      v-if="isReloadConfirmVisible"
      :visible="true"
      title="Reload the threshd configuration?"
      actionButtonText="Reload"
      data-test="threshd-reload-confirm"
      @ok="onReload"
      @cancel="isReloadConfirmVisible = false"
    >
      <template #content>
        <p>Threshd will re-read its daemon configuration and rebuild its schedules.</p>
      </template>
    </OnmsConfirmationDialog>

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
import ThreshdGeneralPanel from '@/components/ThresholdConfiguration/ThreshdGeneralPanel.vue'
import ThresholdPackagesTable from '@/components/ThresholdConfiguration/ThresholdPackagesTable.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useSnackbar from '@/composables/useSnackbar'
import { PACKAGE_HELP } from '@/lib/thresholdHelpText'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { OnmsButton, OnmsConfirmationDialog, OnmsIcon } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const store = useThreshdConfigurationStore()
const { showSnackBar } = useSnackbar()

const isHelpVisible = ref(false)
const isReloadConfirmVisible = ref(false)

const onReload = async () => {
  isReloadConfirmVisible.value = false

  const result = await store.reloadThreshdConfiguration()

  showSnackBar({
    msg: result.success ? 'Threshd has been asked to reload its configuration.' : result.message,
    error: !result.success
  })
}
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
