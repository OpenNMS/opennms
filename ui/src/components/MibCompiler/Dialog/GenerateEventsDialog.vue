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
      <div
        v-if="duplicateUeis.length > 0"
        class="duplicate-ueis"
        data-test="duplicate-ueis"
      >
        <p>
          <strong>{{ duplicateUeis.length }}</strong> of these UEIs are already defined in other event sources.
          OpenNMS uses only the definition from the source that it evaluates first.
          Before the save, the dialog shows which definitions OpenNMS will use.
        </p>
        <p
          v-if="hasDisabledDuplicates"
          data-test="disabled-duplicates-hint"
        >
          OpenNMS does not use disabled definitions. If you enable one again, it competes with the new definition.
        </p>
        <DuplicateUeiTable :items="duplicateUeis" />
      </div>
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

    <!-- Step 3 (conditional): confirm the replacement of the source and the effect on duplicate UEIs -->
    <div
      v-if="step === 'confirm'"
      class="confirm-step"
      data-test="confirm-save"
    >
      <p
        v-if="targetExists"
        data-test="confirm-replace"
      >
        An event source named <strong>{{ sourceName }}</strong> already exists in the database.
        Saving replaces all of its event definitions, including changes made after the source was created.
      </p>
      <div
        v-if="overriddenUeis.length > 0"
        class="duplicate-ueis"
        data-test="confirm-overridden-ueis"
      >
        <p>
          <template v-if="targetExists">
            <strong>{{ sourceName }}</strong> is evaluated before the sources below,
          </template>
          <template v-else>
            A new event source is evaluated before all existing sources,
          </template>
          so OpenNMS will no longer use their definitions of these
          <strong>{{ overriddenUeis.length }}</strong> UEIs:
        </p>
        <DuplicateUeiTable :items="overriddenUeis" />
        <p>
          The generated definitions have the severity Indeterminate and do not contain alarm data.
          If an existing definition creates or clears alarms, these traps will no longer do that.
        </p>
      </div>
      <div
        v-if="shadowedUeis.length > 0"
        class="duplicate-ueis"
        data-test="confirm-shadowed-ueis"
      >
        <p>
          Sources that are evaluated before <strong>{{ sourceName }}</strong> also define these
          <strong>{{ shadowedUeis.length }}</strong> UEIs, so OpenNMS will continue to use those definitions
          and will not use the new ones:
        </p>
        <DuplicateUeiTable :items="shadowedUeis" />
      </div>
      <p>
        To change the evaluation order, use the Event Configuration page after the save.
      </p>
    </div>

    <!-- Step 4: saved -->
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
        v-if="step === 'confirm'"
        :label="overriddenUeis.length > 0 ? 'Save Anyway' : 'Replace Events'"
        :disabled="isLoading"
        data-test="confirm-save-button"
        @click="doSave"
      />
      <OnmsButton
        v-if="step === 'confirm'"
        label="Back"
        variant="outlined"
        :disabled="isLoading"
        data-test="back-button"
        @click="step = 'preview'"
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
import { getOrderedEventConfigSources, uploadEventConfigFiles } from '@/services/eventConfigService'
import { generateEvents } from '@/services/mibCompilerService'
import { EventConfigSource } from '@/types/eventConfig'
import { MibDuplicateUei, MibEventsPreview } from '@/types/mibCompiler'
import { getGeneralErrorMessage, isWellFormedXml } from '../mibFilesValidator'
import DuplicateUeiTable from './DuplicateUeiTable.vue'

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

const step = ref<'input' | 'preview' | 'confirm' | 'done'>('input')
const isLoading = ref(false)
const ueiBase = ref('')
const preview = ref<MibEventsPreview | null>(null)
const eventsXml = ref('')
const validationError = ref('')
// the sources in evaluation order (first entry evaluated first), read when the user saves
const orderedSources = ref<EventConfigSource[]>([])

const baseName = computed(() => props.fileName.replace(/\.[^.]+$/, ''))
const sourceName = computed(() => (preview.value?.suggestedFileName ?? `${baseName.value}.events.xml`).replace(/\.xml$/, ''))

// the UEIs in the (possibly edited) XML; null when it does not parse, so the warning keeps every duplicate
const currentUeis = computed(() => {
  const parsed = new DOMParser().parseFromString(eventsXml.value, 'application/xml')
  if (parsed.querySelector('parsererror') !== null) {
    return null
  }
  return new Set(Array.from(parsed.getElementsByTagName('uei')).map(element => element.textContent?.trim() ?? ''))
})

// the server reports duplicates for the generated UEIs; an edit that changes a UEI resolves its duplicate
const duplicateUeis = computed<MibDuplicateUei[]>(() =>
  Object.entries(preview.value?.existingUeis ?? {})
    .filter(([uei]) => currentUeis.value === null || currentUeis.value.has(uei))
    .map(([uei, sources]) => ({ uei, sources })))

const hasDisabledDuplicates = computed(() => duplicateUeis.value.some(item => item.sources.some(source => !source.enabled)))

const targetExists = computed(() => orderedSources.value.some(source => source.name === sourceName.value))

// Splits the duplicates by evaluation order. A new source is evaluated before all existing ones; an
// existing source keeps its position. Sources evaluated after the target lose their definition of the
// UEI (overridden). If a source evaluated before the target defines it, that definition stays in use and the
// new one is ignored (shadowed). Disabled definitions are not loaded, so they take part in neither.
const classifiedDuplicates = computed(() => {
  const position = new Map(orderedSources.value.map((source, index) => [source.name, index]))
  const targetPosition = position.get(sourceName.value) ?? -1
  const overridden: MibDuplicateUei[] = []
  const shadowed: MibDuplicateUei[] = []
  for (const { uei, sources } of duplicateUeis.value) {
    // a source that is not in the list any more (deleted after the preview) is no duplicate
    const known = sources.filter(source => source.enabled && position.has(source.name))
    const before = known.filter(source => (position.get(source.name) ?? 0) < targetPosition)
    if (before.length > 0) {
      shadowed.push({ uei, sources: before })
    } else if (known.length > 0) {
      overridden.push({ uei, sources: known })
    }
  }
  return { overridden, shadowed }
})

const overriddenUeis = computed(() => classifiedDuplicates.value.overridden)
const shadowedUeis = computed(() => classifiedDuplicates.value.shadowed)

// a response that arrives after the dialog closed, or after it reopened for
// another file, must not touch the current state
const requestSeq = ref(0)

watch(() => props.visible, (visible) => {
  requestSeq.value++
  if (visible) {
    step.value = 'input'
    isLoading.value = false
    preview.value = null
    eventsXml.value = ''
    validationError.value = ''
    orderedSources.value = []
    ueiBase.value = `uei.opennms.org/traps/${baseName.value}`
  }
})

const generatePreview = async () => {
  const requestId = requestSeq.value
  isLoading.value = true
  try {
    const result = await generateEvents(props.fileName, ueiBase.value.trim())
    if (requestId !== requestSeq.value) {
      return
    }
    if (!result.success) {
      emit('failed', result)
      return
    }
    preview.value = result
    eventsXml.value = result.eventsXml ?? ''
    step.value = 'preview'
  } catch (error: unknown) {
    if (requestId !== requestSeq.value) {
      return
    }
    snackbar.showSnackBar({
      msg: getGeneralErrorMessage(error, `Failed to generate events from '${props.fileName}'.`),
      error: true
    })
  } finally {
    if (requestId === requestSeq.value) {
      isLoading.value = false
    }
  }
}

const save = async () => {
  validationError.value = ''
  const xml = eventsXml.value.trim()
  if (!isWellFormedXml(xml)) {
    validationError.value = 'The events XML is not well-formed. Fix it before saving.'
    return
  }
  // The upload replaces an existing source wholesale, and the new definitions can override the
  // definitions of other sources for the same UEIs. Both need the user's explicit consent first.
  const requestId = requestSeq.value
  isLoading.value = true
  try {
    const sources = await getOrderedEventConfigSources()
    if (requestId !== requestSeq.value) {
      return
    }
    orderedSources.value = sources
    // shadowed UEIs need an existing target, so they always come with targetExists
    if (targetExists.value || overriddenUeis.value.length > 0) {
      isLoading.value = false
      step.value = 'confirm'
      return
    }
  } catch (error: unknown) {
    if (requestId !== requestSeq.value) {
      return
    }
    isLoading.value = false
    validationError.value = getGeneralErrorMessage(error, 'Failed to check for an existing event source. Try again.')
    return
  }
  await doSave()
}

const doSave = async () => {
  const xml = eventsXml.value.trim()
  const requestId = requestSeq.value
  isLoading.value = true
  try {
    const file = new File([xml], `${sourceName.value}.xml`, { type: 'application/xml' })
    const response = await uploadEventConfigFiles([file])
    if (requestId !== requestSeq.value) {
      return
    }
    if (response.errors?.length) {
      step.value = 'preview'
      validationError.value = response.errors.map(item => `${item.file}: ${item.error}`).join('; ')
      return
    }
    snackbar.showSnackBar({ msg: `Event definitions from '${preview.value?.mibName}' saved successfully.` })
    step.value = 'done'
  } catch (error: unknown) {
    if (requestId !== requestSeq.value) {
      return
    }
    step.value = 'preview'
    validationError.value = getGeneralErrorMessage(error, 'Failed to save the event definitions.')
  } finally {
    if (requestId === requestSeq.value) {
      isLoading.value = false
    }
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

// shared by the preview warning and the confirmation step
.duplicate-ueis > p {
  color: var(--p-orange-600);
}
</style>
