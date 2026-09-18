<template>
  <OnmsDialog
    :visible="visible"
    :header="`Compilation of ${fileName} failed`"
    width="60em"
    data-test="compile-errors-dialog"
    @update:visible="(value: boolean) => !value && emit('close')"
  >
    <div
      v-if="result?.missingDependencies?.length"
      class="missing-dependencies"
      data-test="missing-dependencies"
    >
      <p>
        The following MIB dependencies could not be found in the compiled directory.
        Upload and compile them first, then compile this MIB again.
      </p>
      <div class="chips">
        <OnmsChip
          v-for="dependency in result.missingDependencies"
          :key="dependency"
          :label="dependency"
        />
      </div>
    </div>
    <div
      v-if="result?.errors"
      class="errors"
    >
      <p>Parser output:</p>
      <pre data-test="compile-errors">{{ result.errors }}</pre>
    </div>
    <template #footer>
      <OnmsButton
        label="Close"
        variant="outlined"
        data-test="close-button"
        @click="emit('close')"
      />
    </template>
  </OnmsDialog>
</template>

<script lang="ts" setup>
import { OnmsButton, OnmsChip, OnmsDialog } from '@opennms/onms-ui'
import { MibParseResult } from '@/types/mibCompiler'

defineProps<{
  visible: boolean
  fileName: string
  result: MibParseResult | null
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<style lang="scss" scoped>
.missing-dependencies {
  margin-bottom: 15px;

  .chips {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 8px;
  }
}

.errors {
  pre {
    font-family: monospace;
    font-size: 13px;
    white-space: pre-wrap;
    max-height: 300px;
    overflow: auto;
    padding: 10px;
    border: 1px solid var(--p-content-border-color);
    border-radius: 4px;
  }
}
</style>
