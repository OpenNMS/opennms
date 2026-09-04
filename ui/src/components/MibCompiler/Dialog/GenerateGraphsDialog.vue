<template>
  <OnmsDialog
    :visible="visible"
    :header="`Generate Graph Templates from ${fileName}`"
    width="70em"
    data-test="generate-graphs-dialog"
    @update:visible="(value: boolean) => !value && close()"
  >
    <div
      v-if="step === 'preview'"
      class="preview-step"
    >
      <p data-test="graph-count">
        Generated <strong>{{ preview?.graphCount }}</strong> graph template(s) from
        <strong>{{ preview?.mibName }}</strong>.
        Confirming writes them to <code>etc/snmp-graph.properties.d/{{ preview?.fileName }}</code> on the server.
      </p>
      <pre
        class="graph-content"
        data-test="graph-content"
      >{{ preview?.content }}</pre>
    </div>

    <div
      v-if="step === 'done'"
      class="done-step"
      data-test="graphs-saved"
    >
      <p>
        Graph templates written to <code>etc/snmp-graph.properties.d/{{ preview?.fileName }}</code>.
      </p>
    </div>

    <template #footer>
      <OnmsButton
        v-if="step === 'preview'"
        label="Write Graph Templates"
        :disabled="isLoading || !preview?.graphCount"
        data-test="write-button"
        @click="write"
      />
      <OnmsButton
        :label="step === 'done' ? 'Close' : 'Cancel'"
        variant="outlined"
        data-test="cancel-button"
        @click="close"
      />
    </template>
  </OnmsDialog>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'
import useSnackbar from '@/composables/useSnackbar'
import { generateGraphTemplates } from '@/services/mibCompilerService'
import { MibGraphTemplatesResult } from '@/types/mibCompiler'
import { getGeneralErrorMessage } from '../mibFilesValidator'

const props = defineProps<{
  visible: boolean
  fileName: string
}>()

const emit = defineEmits<{
  close: []
  failed: [result: MibGraphTemplatesResult]
}>()

const snackbar = useSnackbar()
const step = ref<'preview' | 'done'>('preview')
const isLoading = ref(false)
const preview = ref<MibGraphTemplatesResult | null>(null)

watch(() => props.visible, async (visible) => {
  if (!visible) {
    return
  }
  step.value = 'preview'
  preview.value = null
  isLoading.value = true
  try {
    const result = await generateGraphTemplates(props.fileName, true)
    if (!result.success) {
      emit('failed', result)
      return
    }
    preview.value = result
  } catch (error: unknown) {
    snackbar.showSnackBar({
      msg: getGeneralErrorMessage(error, `Failed to generate graph templates from '${props.fileName}'.`),
      error: true
    })
    close()
  } finally {
    isLoading.value = false
  }
})

const write = async () => {
  isLoading.value = true
  try {
    const result = await generateGraphTemplates(props.fileName, false)
    if (result.written) {
      preview.value = result
      snackbar.showSnackBar({ msg: `Graph templates for '${result.mibName}' written successfully.` })
      step.value = 'done'
    }
  } catch (error: unknown) {
    snackbar.showSnackBar({
      msg: getGeneralErrorMessage(error, 'Failed to write the graph templates.'),
      error: true
    })
  } finally {
    isLoading.value = false
  }
}

const close = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
.preview-step {
  .graph-content {
    font-family: monospace;
    font-size: 13px;
    white-space: pre-wrap;
    max-height: 350px;
    overflow: auto;
    padding: 10px;
    border: 1px solid var(--p-content-border-color);
    border-radius: 4px;
  }
}
</style>
