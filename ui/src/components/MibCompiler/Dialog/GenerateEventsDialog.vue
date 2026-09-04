<template>
  <OnmsDialog
    :visible="visible"
    :header="`Generate Events from ${fileName}`"
    width="70em"
    data-test="generate-events-dialog"
    @update:visible="(value: boolean) => !value && close()"
  >
    <!-- Step 1: choose the UEI base -->
    <div
      v-if="step === 'input'"
      class="uei-step"
    >
      <p>
        Event UEIs are built as <code>&lt;UEI base&gt;/&lt;trap name&gt;</code>.
        Adjust the base if needed, then generate a preview.
      </p>
      <FormField>
        <label :for="ueiInputId">UEI Base</label>
        <OnmsInputText
          :id="ueiInputId"
          v-model="ueiBase"
          class="uei-input"
          data-test="uei-base-input"
        />
      </FormField>
    </div>

    <!-- Step 2: review/edit the generated events XML -->
    <div
      v-if="step === 'preview'"
      class="preview-step"
    >
      <p data-test="event-count">
        Generated <strong>{{ preview?.eventCount }}</strong> event definition(s) from
        <strong>{{ preview?.mibName }}</strong>. Review and edit the XML below before saving.
        Saving stores the events in the database as source
        <strong>{{ sourceName }}</strong>.
      </p>
      <p
        v-if="preview?.eventCount === 0"
        class="no-traps"
        data-test="no-traps-message"
      >
        The MIB does not contain any notification or trap definitions, so there is nothing to save.
      </p>
      <OnmsTextarea
        v-if="(preview?.eventCount ?? 0) > 0"
        v-model="eventsXml"
        :rows="20"
        class="xml-editor"
        data-test="events-xml"
        aria-label="Generated events XML"
      />
      <p
        v-if="validationError"
        class="validation-error"
        data-test="validation-error"
      >
        {{ validationError }}
      </p>
    </div>

    <!-- Step 3: saved -->
    <div
      v-if="step === 'done'"
      class="done-step"
      data-test="events-saved"
    >
      <p>
        Event definitions saved to the database as source <strong>{{ sourceName }}</strong>.
        You can fine-tune them in the Event Configuration page.
      </p>
    </div>

    <template #footer>
      <OnmsButton
        v-if="step === 'input'"
        label="Generate Preview"
        :disabled="isLoading || !ueiBase.trim()"
        data-test="generate-button"
        @click="generatePreview"
      />
      <OnmsButton
        v-if="step === 'preview' && (preview?.eventCount ?? 0) > 0"
        label="Save Events"
        :disabled="isLoading"
        data-test="save-button"
        @click="save"
      />
      <OnmsButton
        v-if="step === 'done'"
        label="Go to Event Configuration"
        data-test="go-to-event-config-button"
        @click="goToEventConfig"
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
import { computed, ref, useId, watch } from 'vue'
import { useRouter } from 'vue-router'
import { OnmsButton, OnmsDialog, OnmsInputText, OnmsTextarea } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import useSnackbar from '@/composables/useSnackbar'
import { uploadEventConfigFiles } from '@/services/eventConfigService'
import { generateEvents } from '@/services/mibCompilerService'
import { MibEventsPreview } from '@/types/mibCompiler'
import { getGeneralErrorMessage, isWellFormedXml } from '../mibFilesValidator'

const props = defineProps<{
  visible: boolean
  fileName: string
}>()

const emit = defineEmits<{
  close: []
  failed: [result: MibEventsPreview]
}>()

const router = useRouter()
const snackbar = useSnackbar()
const ueiInputId = useId()

const step = ref<'input' | 'preview' | 'done'>('input')
const isLoading = ref(false)
const ueiBase = ref('')
const preview = ref<MibEventsPreview | null>(null)
const eventsXml = ref('')
const validationError = ref('')

const baseName = computed(() => props.fileName.replace(/\.[^.]+$/, ''))
const sourceName = computed(() => (preview.value?.suggestedFileName ?? `${baseName.value}.events.xml`).replace(/\.xml$/, ''))

watch(() => props.visible, (visible) => {
  if (visible) {
    step.value = 'input'
    isLoading.value = false
    preview.value = null
    eventsXml.value = ''
    validationError.value = ''
    ueiBase.value = `uei.opennms.org/traps/${baseName.value}`
  }
})

const generatePreview = async () => {
  isLoading.value = true
  try {
    const result = await generateEvents(props.fileName, ueiBase.value.trim())
    if (!result.success) {
      emit('failed', result)
      return
    }
    preview.value = result
    eventsXml.value = result.eventsXml ?? ''
    step.value = 'preview'
  } catch (error: unknown) {
    snackbar.showSnackBar({
      msg: getGeneralErrorMessage(error, `Failed to generate events from '${props.fileName}'.`),
      error: true
    })
  } finally {
    isLoading.value = false
  }
}

const save = async () => {
  validationError.value = ''
  const xml = eventsXml.value.trim()
  if (!isWellFormedXml(xml)) {
    validationError.value = 'The events XML is not well-formed. Fix it before saving.'
    return
  }
  isLoading.value = true
  try {
    const file = new File([xml], `${sourceName.value}.xml`, { type: 'application/xml' })
    const response = await uploadEventConfigFiles([file])
    if (response.errors?.length) {
      validationError.value = response.errors.map(item => `${item.file}: ${item.error}`).join('; ')
      return
    }
    snackbar.showSnackBar({ msg: `Event definitions from '${preview.value?.mibName}' saved successfully.` })
    step.value = 'done'
  } catch (error: unknown) {
    validationError.value = getGeneralErrorMessage(error, 'Failed to save the event definitions.')
  } finally {
    isLoading.value = false
  }
}

const goToEventConfig = () => {
  close()
  router.push('/event-config')
}

const close = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
.uei-step {
  .uei-input {
    width: 100%;
  }
}

.preview-step {
  .xml-editor {
    width: 100%;
    font-family: monospace;
    font-size: 13px;
  }

  .validation-error {
    color: var(--p-red-600);
  }

  .no-traps {
    color: var(--p-orange-600);
  }
}
</style>
