<template>
  <TableCard>
    <div class="general-panel">
      <FormField
        label="Threads"
        for="threshd-threads"
        required
        :error="threadsError"
        hint="Worker threads the threshd daemon uses to evaluate thresholds."
      >
        <OnmsInputNumber
          inputId="threshd-threads"
          data-test="threshd-threads"
          :modelValue="threads"
          :invalid="!!threadsError"
          @update:modelValue="threads = $event"
        />
      </FormField>

      <OnmsButton data-test="threshd-threads-save" :disabled="!!threadsError" @click="onSave">Save</OnmsButton>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { OnmsButton, OnmsInputNumber } from '@opennms/onms-ui'

const store = useThreshdConfigurationStore()
const { showSnackBar } = useSnackbar()

// OnmsInputNumber emits number | null, so the null has to be handled before it reaches the server.
const threads = ref<number | null>(store.config.threads)

const threadsError = computed(() => {
  if (threads.value === null || threads.value === undefined) {
    return 'Threads is required.'
  }
  if (!Number.isInteger(threads.value) || threads.value < 1) {
    return 'Threads must be a whole number greater than 0.'
  }
  return undefined
})

watch(
  () => store.config.threads,
  (value) => {
    threads.value = value
  }
)

const onSave = async () => {
  if (threads.value === null) {
    return
  }

  const result = await store.saveThreads(threads.value)

  showSnackBar({ msg: result.success ? 'Threshd configuration saved.' : result.message, error: !result.success })
}
</script>

<style lang="scss" scoped>
.general-panel {
  display: flex;
  align-items: flex-end;
  gap: 1em;
  padding: 0 1em;
}
</style>
