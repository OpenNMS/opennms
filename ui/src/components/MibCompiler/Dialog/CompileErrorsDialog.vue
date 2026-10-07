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
          :label="isInPending(dependency) ? `${dependency} (in Pending)` : dependency"
        />
      </div>
      <p
        v-if="result?.pendingDependencies?.length"
        class="pending-hint"
        data-test="pending-dependencies-hint"
      >
        Dependencies marked "(in Pending)" are already uploaded. Compile them first, then compile this MIB again.
      </p>
    </div>
    <div
      v-if="result?.errors"
      class="errors"
    >
      <!-- the raw parser output also lists modules from parse passes that were later
           resolved, so when the dependency chips tell the real story keep it collapsed -->
      <OnmsButton
        v-if="result?.missingDependencies?.length"
        variant="text"
        :label="showParserOutput ? 'Hide parser output' : 'Show parser output'"
        data-test="toggle-parser-output"
        @click="showParserOutput = !showParserOutput"
      />
      <p v-else>Parser output:</p>
      <pre
        v-if="showParserOutput || !result?.missingDependencies?.length"
        data-test="compile-errors"
      >{{ result.errors }}</pre>
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
import { ref, watch } from 'vue'
import { OnmsButton, OnmsChip, OnmsDialog } from '@opennms/onms-ui'
import { MibParseResult } from '@/types/mibCompiler'

const props = defineProps<{
  visible: boolean
  fileName: string
  result: MibParseResult | null
}>()

const emit = defineEmits<{
  close: []
}>()

const showParserOutput = ref(false)

watch(() => props.visible, (visible) => {
  if (visible) {
    showParserOutput.value = false
  }
})

const isInPending = (dependency: string): boolean => {
  return props.result?.pendingDependencies?.includes(dependency) ?? false
}
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

  .pending-hint {
    margin-top: 8px;
    color: var(--p-text-muted-color);
    font-size: 13px;
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
