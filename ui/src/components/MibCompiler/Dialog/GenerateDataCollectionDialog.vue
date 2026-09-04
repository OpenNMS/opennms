<template>
  <OnmsDialog
    :visible="visible"
    :header="`Generate Data Collection from ${fileName}`"
    width="70em"
    data-test="generate-datacollection-dialog"
    @update:visible="(value: boolean) => !value && close()"
  >
    <div
      v-if="step === 'preview'"
      class="preview-step"
    >
      <p data-test="group-count">
        Generated a data collection group with <strong>{{ preview?.groupCount }}</strong> group(s) from
        <strong>{{ preview?.mibName }}</strong>. Review and edit the XML below before saving.
      </p>
      <p
        v-if="preview?.groupCount === 0"
        class="no-groups"
        data-test="no-groups-message"
      >
        The MIB does not contain any collectible variables, so there is nothing to save.
      </p>
      <OnmsTextarea
        v-if="(preview?.groupCount ?? 0) > 0"
        v-model="dataCollectionXml"
        :rows="18"
        class="xml-editor"
        data-test="datacollection-xml"
        aria-label="Generated data collection XML"
      />
      <div
        v-if="(preview?.groupCount ?? 0) > 0"
        class="profiles-section"
      >
        <FormField>
          <label :for="profilesSelectId">
            Add to SNMP collection profiles
            <span
              v-if="isNewSource"
              class="required-marker"
              data-test="profiles-required"
            >(required)</span>
          </label>
          <OnmsMultiSelect
            :id="profilesSelectId"
            v-model="selectedProfiles"
            :options="profileOptions"
            optionLabel="name"
            dataKey="id"
            display="chip"
            filter
            placeholder="Select profiles"
            class="profiles-select"
            data-test="profiles-select"
          />
        </FormField>
        <p class="profiles-hint">
          A new data collection source must be attached to at least one profile to be used for collection.
        </p>
      </div>
      <p
        v-if="validationError"
        class="validation-error"
        data-test="validation-error"
      >
        {{ validationError }}
      </p>
    </div>

    <div
      v-if="step === 'done'"
      class="done-step"
      data-test="datacollection-saved"
    >
      <p>
        Data collection group saved to the database as source <strong>{{ preview?.mibName }}</strong>.
        You can fine-tune it in the SNMP Data Collection page.
      </p>
    </div>

    <template #footer>
      <OnmsButton
        v-if="step === 'preview' && (preview?.groupCount ?? 0) > 0"
        label="Save Data Collection"
        :disabled="isLoading || (isNewSource && selectedProfiles.length === 0)"
        data-test="save-button"
        @click="save"
      />
      <OnmsButton
        v-if="step === 'done'"
        label="Go to SNMP Data Collection"
        data-test="go-to-datacollection-button"
        @click="goToDataCollection"
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
import { OnmsButton, OnmsDialog, OnmsMultiSelect, OnmsTextarea } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import useSnackbar from '@/composables/useSnackbar'
import { generateDataCollection } from '@/services/mibCompilerService'
import { getAllSnmpCollectionProfiles, getAllSnmpCollectionSourcesNamesAndIds, uploadDataCollectionFiles } from '@/services/snmpDataCollectionService'
import { MibDataCollectionPreview } from '@/types/mibCompiler'
import { getGeneralErrorMessage, isWellFormedXml } from '../mibFilesValidator'

type ProfileOption = { id: number; name: string }

const props = defineProps<{
  visible: boolean
  fileName: string
}>()

const emit = defineEmits<{
  close: []
  failed: [result: MibDataCollectionPreview]
}>()

const router = useRouter()
const snackbar = useSnackbar()
const profilesSelectId = useId()

const step = ref<'preview' | 'done'>('preview')
const isLoading = ref(false)
const preview = ref<MibDataCollectionPreview | null>(null)
const dataCollectionXml = ref('')
const validationError = ref('')
const profileOptions = ref<ProfileOption[]>([])
const selectedProfiles = ref<ProfileOption[]>([])
const existingSourceNames = ref<string[]>([])

// the upload endpoint derives the source name from the file name and requires
// at least one profile when the source does not exist yet
const sourceName = computed(() => (preview.value?.suggestedFileName ?? '').replace(/\.xml$/, ''))
const isNewSource = computed(() =>
  !existingSourceNames.value.some(name => name.toLowerCase() === sourceName.value.toLowerCase()))

watch(() => props.visible, async (visible) => {
  if (!visible) {
    return
  }
  step.value = 'preview'
  isLoading.value = true
  preview.value = null
  dataCollectionXml.value = ''
  validationError.value = ''
  selectedProfiles.value = []
  try {
    const [result, profiles, existingSources] = await Promise.all([
      generateDataCollection(props.fileName),
      getAllSnmpCollectionProfiles(),
      getAllSnmpCollectionSourcesNamesAndIds()
    ])
    profileOptions.value = profiles.map(profile => ({ id: profile.id, name: profile.name }))
    existingSourceNames.value = existingSources.map(source => source.name)
    if (!result.success) {
      emit('failed', result)
      return
    }
    preview.value = result
    dataCollectionXml.value = result.dataCollectionXml ?? ''
  } catch (error: unknown) {
    snackbar.showSnackBar({
      msg: getGeneralErrorMessage(error, `Failed to generate data collection from '${props.fileName}'.`),
      error: true
    })
    close()
  } finally {
    isLoading.value = false
  }
})

const save = async () => {
  validationError.value = ''
  const xml = dataCollectionXml.value.trim()
  if (!isWellFormedXml(xml)) {
    validationError.value = 'The data collection XML is not well-formed. Fix it before saving.'
    return
  }
  isLoading.value = true
  try {
    const fileName = preview.value?.suggestedFileName ?? `${props.fileName.replace(/\.[^.]+$/, '')}.xml`
    const file = new File([xml], fileName, { type: 'application/xml' })
    const response = await uploadDataCollectionFiles([file], selectedProfiles.value.map(profile => profile.name))
    if (response.errors?.length) {
      validationError.value = response.errors.map(item => `${item.file}: ${item.error}`).join('; ')
      return
    }
    snackbar.showSnackBar({ msg: `Data collection group from '${preview.value?.mibName}' saved successfully.` })
    step.value = 'done'
  } catch (error: unknown) {
    validationError.value = getGeneralErrorMessage(error, 'Failed to save the data collection group.')
  } finally {
    isLoading.value = false
  }
}

const goToDataCollection = () => {
  close()
  router.push('/snmp-data-collection')
}

const close = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
.preview-step {
  .xml-editor {
    width: 100%;
    font-family: monospace;
    font-size: 13px;
  }

  .profiles-section {
    margin-top: 15px;

    .profiles-select {
      width: 100%;
    }

    .profiles-hint {
      color: var(--p-text-muted-color);
      font-size: 13px;
    }

    .required-marker {
      color: var(--p-red-600);
      font-size: 12px;
    }
  }

  .validation-error {
    color: var(--p-red-600);
  }

  .no-groups {
    color: var(--p-orange-600);
  }
}
</style>
