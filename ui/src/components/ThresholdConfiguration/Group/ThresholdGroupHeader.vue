<template>
  <div class="group-header">
    <div class="left">
      <OnmsIconButton
        :icon="BackIcon"
        tooltip="Back to threshold groups"
        aria-label="Back to threshold groups"
        data-test="threshold-group-back"
        @click="router.push('/threshold-config')"
      />
      <div class="titles">
        <h2 data-test="threshold-group-name">{{ group.name }}</h2>
        <p class="subtitle" data-test="threshold-group-repository">{{ group.rrdRepository }}</p>
      </div>
      <OnmsTag v-if="group.readOnly" value="Read only" data-test="threshold-group-readonly" />
      <OnmsIcon
        :icon="InfoIcon"
        class="info-icon"
        data-test="threshold-group-info-icon"
        role="button"
        tabindex="0"
        aria-label="About threshold groups"
        @click="isHelpVisible = true"
        @keyup.enter="isHelpVisible = true"
      />
    </div>

    <div class="right">
      <OnmsButton
        variant="outlined"
        data-test="threshold-group-edit"
        :disabled="group.readOnly"
        @click="emit('edit')"
      >
        Edit group
      </OnmsButton>
      <OnmsButton
        variant="outlined"
        severity="danger"
        data-test="threshold-group-delete"
        :disabled="group.readOnly"
        @click="emit('delete')"
      >
        Delete group
      </OnmsButton>
    </div>

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
import { useRouter } from 'vue-router'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import { GROUP_HELP } from '@/lib/thresholdHelpText'
import type { ThresholdGroup } from '@/types/thresholdConfig'
import { OnmsButton, OnmsIcon, OnmsIconButton, OnmsTag } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import BackIcon from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'

defineProps<{
  group: ThresholdGroup
}>()

const emit = defineEmits<{
  edit: []
  delete: []
}>()

const router = useRouter()
const isHelpVisible = ref(false)
</script>

<style lang="scss" scoped>
.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1em;
  margin-bottom: 1.25em;

  .left {
    display: flex;
    align-items: center;
    gap: 0.75em;
  }

  .titles h2 {
    margin: 0;
  }

  .subtitle {
    margin: 0;
    color: var(--p-text-muted-color);
  }

  .info-icon {
    cursor: pointer;
    color: var(--p-text-muted-color);
  }

  .right {
    display: flex;
    gap: 0.5em;
  }
}
</style>
