<template>
  <div class="main-content">
    <div class="title">
      <div class="header">
        <div>
          <Button
            text
            data-test="back-button"
            @click="handleCancel(store.selectedSource?.id)"
          >
            <FeatherIcon :icon="ArrowBack" />
            Go Back
          </Button>
        </div>
        <div>
          <h3>
            {{ store.eventModificationState.isEditMode === CreateEditMode.Create ? 'Create New Event Configuration' :
              'Edit Event Configuration Details' }}
          </h3>
        </div>
      </div>
      <div class="action">
        <Button
          label="Create New Event Source"
          @click="showSourceCreationDialog"
          data-test="create-new-event-source-button"
          :disabled="store.selectedSource?.name && store.selectedSource?.id ? true : false"
        />
      </div>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <FormField
      label="Source Name"
      :for="sourceNameId"
      class="my-autocomplete"
    >
      <AutoComplete
        :inputId="sourceNameId"
        :disabled="store.selectedSource?.name && store.selectedSource?.id ? true : false"
        :modelValue="selectedSource"
        @update:model-value="(item: any) => setSelectedSource(item)"
        data-test="source-name"
        :suggestions="results"
        optionLabel="_text"
        dropdown
        forceSelection
        fluid
        @complete="search($event.query)"
      />
    </FormField>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div class="section-content">
        <div>
          <h3>Basic Information</h3>
        </div>
        <div class="spacer"></div>
        <FormField
          label="Event UEI"
          :for="eventUeiId"
          :error="errors.uei"
          hint="e.g., 'uei.opennms.org/vendor/application/eventname'"
        >
          <InputText
            :id="eventUeiId"
            data-test="event-uei"
            :invalid="!!errors.uei"
            :modelValue="eventUei"
            @update:model-value="eventUei = ($event ?? '').trim()"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <FormField
          label="Event Label"
          :for="eventLabelId"
          :error="errors.eventLabel"
          hint="e.g., 'Vendor Application Event Name'"
        >
          <InputText
            :id="eventLabelId"
            data-test="event-label"
            :invalid="!!errors.eventLabel"
            :modelValue="eventLabel"
            @update:model-value="eventLabel = ($event ?? '').trim()"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <FormField
          label="Event Description"
          :for="eventDescriptionId"
          :error="errors.description"
          hint="Provide a detailed description of the event."
        >
          <Textarea
            :id="eventDescriptionId"
            :modelValue="eventDescription"
            @update:model-value="eventDescription = ($event ?? '').trim()"
            :invalid="!!errors.description"
            data-test="event-description"
            rows="10"
            autoResize
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <FormField
          label="Operator Instructions"
          :for="operatorInstructionsId"
          hint="Instructions for operators when this event occurs."
        >
          <Textarea
            :id="operatorInstructionsId"
            :modelValue="operatorInstructions"
            @update:model-value="operatorInstructions = ($event ?? '').trim()"
            data-test="operator-instructions"
            rows="5"
            autoResize
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="dropdown">
          <FormField
            label="Log Message Destination"
            :for="destinationId"
            :error="errors.dest"
            hint="Select the destination for the log message."
          >
            <Select
              :inputId="destinationId"
              data-test="event-destination"
              :invalid="!!errors.dest"
              :options="DestinationOptions"
              optionLabel="_text"
              showClear
              :modelValue="destination?._value ? destination : null"
              @update:model-value="onSelectChange(destination, $event)"
              fluid
            />
          </FormField>
        </div>
        <div class="spacer"></div>
        <FormField
          label="Log Message"
          :for="logMessageId"
          :error="errors.logmsg"
          hint="Provide the log message for this event."
        >
          <Textarea
            :id="logMessageId"
            :modelValue="logMessage"
            @update:model-value="logMessage = ($event ?? '').trim()"
            :invalid="!!errors.logmsg"
            data-test="log-message"
            rows="5"
            autoResize
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="dropdown">
          <FormField
            label="Severity"
            :for="severityId"
            :error="errors.severity"
            hint="Select the severity of the event."
          >
            <Select
              :inputId="severityId"
              data-test="event-severity"
              :invalid="!!errors.severity"
              :options="SeverityOptions"
              optionLabel="_text"
              showClear
              :modelValue="severity?._value ? severity : null"
              @update:model-value="onSelectChange(severity, $event)"
              fluid
            />
          </FormField>
        </div>
        <div class="spacer"></div>
        <div>
          <AlarmDataInfo
            data-test="alarm-data-info"
            :errors="errors"
            :addAlarmData="addAlarmData"
            :reductionKey="reductionKey"
            :alarmType="alarmType"
            :autoClean="autoClean"
            :clearKey="clearKey"
            @setAlarmData="setAlarmData"
          />
        </div>
        <div class="spacer"></div>
        <div>
          <MaskElements
            data-test="mask-elements"
            @setMaskElements="setMaskElements"
            :maskElements="maskElements"
            :errors="errors"
          />
        </div>
        <div class="spacer"></div>
        <div>
          <MaskVarbinds
            data-test="mask-varbinds"
            :varbinds="varbinds"
            :maskElements="maskElements"
            :errors="errors"
            @setVarbinds="setVarbinds"
          />
        </div>
        <div class="spacer"></div>
        <div>
          <VarbindsDecode
            data-test="varbind-decodes"
            :varbindsDecode="varbindsDecode"
            @setVarbindsDecode="setVarbindsDecode"
            :errors="errors"
          />
        </div>
        <div class="spacer"></div>
        <div class="action-container">
          <Button
            outlined
            label="Cancel"
            @click="handleCancel(store.selectedSource?.id)"
            data-test="cancel-event-button"
          />
          <Button
            :label="store.eventModificationState.isEditMode === CreateEditMode.Create ? 'Create Event' : 'Save Changes'"
            @click="handleSaveEvent"
            data-test="save-event-button"
            :disabled="!isValid"
          />
        </div>
      </div>
    </div>
    <Dialog
      v-model:visible="sourceCreationDialogState"
      :header="labels.title"
      :modal="true"
      :draggable="false"
      :closable="false"
      :closeOnEscape="false"
    >
      <div class="modal-body-form">
        <FormField
          label="Event Configuration Source Name"
          :for="configNameId"
          :error="sourceCreationErrors?.name"
        >
          <InputText
            :id="configNameId"
            v-model="configName"
            :invalid="!!sourceCreationErrors?.name"
            data-test="source-name"
            fluid
          />
        </FormField>
        <FormField
          label="Vendor"
          :for="vendorId"
          :error="sourceCreationErrors?.vendor"
        >
          <InputText
            :id="vendorId"
            v-model="vendor"
            :invalid="!!sourceCreationErrors?.vendor"
            data-test="vendor"
            fluid
          />
        </FormField>
      </div>
      <template #footer>
        <Button
          text
          label="Cancel"
          @click="handleSourceCreationCancel"
          data-test="cancel-source-button"
        />
        <Button
          label="Create Source"
          @click="handleSourceCreationSave"
          :disabled="Object.keys(sourceCreationErrors || {}).length > 0"
          data-test="create-source-button"
        />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, useId, watchEffect } from 'vue'
import { useRouter } from 'vue-router'

import useSnackbar from '@/composables/useSnackbar'
import { addEventConfigSource, createEventConfigEvent, updateEventConfigEventById } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent, EventFormErrors } from '@/types/eventConfig'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherIcon } from '@featherds/icon'
import ArrowBack from '@featherds/icon/navigation/ArrowBack'
import { ISelectItemType } from '@featherds/select'
import AutoComplete from 'primevue/autocomplete'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import vkbeautify from 'vkbeautify'
import FormField from '@/components/Common/FormField.vue'
import AlarmDataInfo from './AlarmDataInfo.vue'
import { AlarmTypeName, AlarmTypeValue, DestinationOptions, MaskVarbindsTypeText, MaskVarbindsTypeValue, MAX_MASK_ELEMENTS, SeverityOptions } from './constants'
import { validateEvent } from './eventValidator'
import MaskElements from './MaskElements.vue'
import MaskVarbinds from './MaskVarbinds.vue'
import VarbindsDecode from './VarbindsDecode.vue'

const timeout = ref<number>(-1)
const results = ref<Array<IAutocompleteItemType>>([])
const selectedSource = ref<IAutocompleteItemType>()
const router = useRouter()
const store = useEventModificationStore()
const eventConfigStore = useEventConfigStore()
const sourceNameId = useId()
const configNameId = useId()
const vendorId = useId()
const eventUeiId = useId()
const eventLabelId = useId()
const eventDescriptionId = useId()
const operatorInstructionsId = useId()
const destinationId = useId()
const logMessageId = useId()
const severityId = useId()
const eventUei = ref('')
const eventLabel = ref('')
const eventDescription = ref('')
const operatorInstructions = ref('')
const logMessage = ref('')
const errors = ref<EventFormErrors>({})
const isValid = ref(false)
const snackbar = useSnackbar()
const destination = ref<ISelectItemType>({ _text: '', _value: '' })
const severity = ref<ISelectItemType>({ _text: '', _value: '' })
const alarmType = ref<ISelectItemType>({ _text: '', _value: '' })
const configName = ref('')
const vendor = ref('')
const sourceCreationDialogState = ref(false)
const maskElements = ref<Array<{ name: ISelectItemType; value: string }>>([
  { name: { _text: '', _value: '' }, value: '' }
])
const addAlarmData = ref(false)
const reductionKey = ref('')
const autoClean = ref(false)
const clearKey = ref('')
const varbinds = ref<Array<{ index: string; value: string, type: ISelectItemType }>>([
  { index: '0', value: '', type: { _text: MaskVarbindsTypeText.vbNumber, _value: MaskVarbindsTypeValue.vbNumber }}
])
const varbindsDecode = ref<Array<{ parmId: string; decode: Array<{ key: string; value: string }> }>>([])
const labels = {
  title: 'Create New Event Source'
}
const sourceCreationErrors = computed(() => {
  let error: any = {}
  if (configName.value.trim() === '') {
    error.name = 'Configuration name is required.'
  }
  if (vendor.value && vendor.value.length > 128) {
    error.vendor = 'Vendor must be less than 128 characters.'
  }
  return Object.keys(error).length > 0 ? error : null
})

// PrimeVue Select emits the selected option object (or null when cleared);
// normalise back into the {_text,_value} shape the rest of the form expects.
const onSelectChange = (target: ISelectItemType, value: ISelectItemType | null) => {
  if (value) {
    target._text = value._text
    target._value = value._value
  } else {
    target._text = ''
    target._value = ''
  }
}

const xmlContent = computed(() => {
  return vkbeautify.xml(
    `<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
        ${maskElements.value.length > 0 ? `
        <mask>
          ${maskElements.value.map(me => `
            <maskelement>
              <mename>${me.name._value}</mename>
              <mevalue>${me.value}</mevalue>
            </maskelement>`).join('')}
          ${varbinds.value.map(vb => `
            <varbind>
              ${vb.type._value === MaskVarbindsTypeValue.vbNumber ? `<vbnumber>${vb.index}</vbnumber><vbvalue>${vb.value}</vbvalue>` : ''}
              ${vb.type._value === MaskVarbindsTypeValue.vbOid ? `<vboid>${vb.index}</vboid><vbvalue>${vb.value}</vbvalue>` : ''}
            </varbind>`).join('')}
        </mask>` : ''}
        ${varbindsDecode.value.map(vb => `
            <varbindsdecode>
              <parmid>${vb.parmId}</parmid>
              ${vb.decode.map(d => `
                <decode varbinddecodedstring="${d.key}" varbindvalue="${d.value}" />`).join('')}
            </varbindsdecode>`).join('')}
        <uei>${eventUei.value}</uei>
        <event-label>${eventLabel.value}</event-label>
        <descr><![CDATA[${eventDescription.value}]]></descr>
        <operinstruct><![CDATA[${operatorInstructions.value}]]></operinstruct>
        <logmsg dest="${destination.value._value}"><![CDATA[${logMessage.value}]]></logmsg>
        <severity>${severity.value._value}</severity>
        ${addAlarmData.value ? `
        <alarm-data
          reduction-key="${reductionKey.value}"
          alarm-type="${alarmType.value._value}"
          auto-clean="${autoClean.value}"
          ${clearKey.value ? `clear-key="${clearKey.value}"` : ''}
        />` : ''}
    </event>`.trim()
  )
})

const resetValues = () => {
  eventUei.value = ''
  eventLabel.value = ''
  eventDescription.value = ''
  severity.value = { _text: '', _value: '' }
  destination.value = { _text: '', _value: '' }
  logMessage.value = ''
  addAlarmData.value = false
  reductionKey.value = ''
  alarmType.value = { _text: '', _value: '' }
  autoClean.value = false
  clearKey.value = ''
  maskElements.value = []
  varbinds.value = []
  varbindsDecode.value = []
  selectedSource.value = { _text: '', _value: -1 }
}

const loadInitialValues = (val: EventConfigEvent | null) => {
  if (store.selectedSource) {
    const source = eventConfigStore.uploadedSources?.find(s => s.id === store.selectedSource?.id)
    selectedSource.value = { _text: source?.name, _value: source?.id }
  } else {
    selectedSource.value = { _text: '', _value: -1 }
  }
  if (val) {
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(val.xmlContent || '', 'application/xml')
    const logmsgElement = xmlDoc.getElementsByTagName('logmsg')[0]
    logMessage.value = logmsgElement ? logmsgElement.textContent || '' : ''
    const destAttr = logmsgElement?.getAttribute('dest') || ''
    destination.value = {
      _text: destAttr,
      _value: destAttr
    }
    eventDescription.value = val.description || ''
    operatorInstructions.value = xmlDoc.getElementsByTagName('operinstruct')[0]?.textContent || ''
    eventUei.value = val.uei || ''
    eventLabel.value = val.eventLabel || ''
    severity.value = {
      _text: val.severity || '',
      _value: val.severity || ''
    }
    addAlarmData.value = xmlDoc.getElementsByTagName('alarm-data')[0] ? true : false
    if (addAlarmData.value) {
      const alarmDataElement = xmlDoc.getElementsByTagName('alarm-data')[0]
      reductionKey.value = alarmDataElement?.getAttribute('reduction-key') || ''
      const alarmTypeAttr = alarmDataElement?.getAttribute('alarm-type') as AlarmTypeValue
      const matchedKey = Object.keys(AlarmTypeValue).find(
        key => AlarmTypeValue[key as keyof typeof AlarmTypeValue] === alarmTypeAttr
      ) as keyof typeof AlarmTypeValue
      alarmType.value = {
        _text: AlarmTypeName[matchedKey] || '',
        _value: alarmTypeAttr || ''
      }
      autoClean.value = alarmDataElement?.getAttribute('auto-clean') === 'true' ? true : false
      clearKey.value = alarmDataElement?.getAttribute('clear-key') || ''
    }
    const maskElementList = xmlDoc.getElementsByTagName('maskelement')
    maskElements.value = []
    for (let i = 0; i < maskElementList.length; i++) {
      maskElements.value.push({
        name: {
          _text: maskElementList[i].getElementsByTagName('mename')[0].textContent || '',
          _value: maskElementList[i].getElementsByTagName('mename')[0].textContent || ''
        },
        value: maskElementList[i].getElementsByTagName('mevalue')[0].textContent || ''
      })
    }
    const varbindList = xmlDoc.getElementsByTagName('varbind')
    varbinds.value = []
    for (let i = 0; i < varbindList.length; i++) {
      const vbnumberElement = varbindList[i].getElementsByTagName('vbnumber')[0]
      const vboidElement = varbindList[i].getElementsByTagName('vboid')[0]
      const vbvalueElement = varbindList[i].getElementsByTagName('vbvalue')[0]
      if (vbnumberElement?.textContent) {
        varbinds.value.push({
          index: vbnumberElement.textContent || '',
          value: vbvalueElement?.textContent || '',
          type: { _text: MaskVarbindsTypeText.vbNumber, _value: MaskVarbindsTypeValue.vbNumber }
        })
      } else if (vboidElement?.textContent) {
        varbinds.value.push({
          index: vboidElement.textContent || '',
          value: vbvalueElement?.textContent || '',
          type: { _text: MaskVarbindsTypeText.vbOid, _value: MaskVarbindsTypeValue.vbOid }
        })
      }
    }
    const varbindsDecodeList = xmlDoc.getElementsByTagName('varbindsdecode')
    varbindsDecode.value = []
    for (let i = 0; i < varbindsDecodeList.length; i++) {
      const decodeList = varbindsDecodeList[i].getElementsByTagName('decode')
      varbindsDecode.value.push({
        parmId: varbindsDecodeList[i].getElementsByTagName('parmid')[0].textContent || '',
        decode: []
      })
      for (let j = 0; j < decodeList.length; j++) {
        varbindsDecode.value[i].decode.push({
          key: decodeList[j].getAttribute('varbinddecodedstring') || '',
          value: decodeList[j].getAttribute('varbindvalue') || ''
        })
      }
    }
  } else {
    resetValues()
  }
}

const setAlarmData = (key: string, value: any) => {
  if (key === 'addAlarmData') {
    addAlarmData.value = value
    if ((value as boolean) === false) {
      reductionKey.value = ''
      alarmType.value = { _text: '', _value: '' }
      autoClean.value = false
    }
  }

  if (key === 'reductionKey') {
    reductionKey.value = value
  }

  if (key === 'alarmType') {
    alarmType.value = {
      _text: value?._text ?? '',
      _value: value?._value ?? ''
    }
  }

  if (key === 'autoClean') {
    autoClean.value = value
  }

  if (key === 'clearKey') {
    clearKey.value = value
  }
}

const setMaskElements = (key: string, value: any, index: number) => {
  if (index === undefined) {
    return
  }

  if (key === 'setName') {
    maskElements.value[index].name = value ?? { _text: '', _value: '' }
  }

  if (key === 'setValue') {
    maskElements.value[index].value = value
  }

  if (key === 'addMaskRow') {
    if (maskElements.value.length < MAX_MASK_ELEMENTS) {
      maskElements.value.push({
        name: { _text: '', _value: '' },
        value: ''
      })
    } else {
      snackbar.showSnackBar({ msg: `Maximum of ${MAX_MASK_ELEMENTS} mask elements allowed.`, error: true })
    }
  }

  if (key === 'removeMaskRow') {
    maskElements.value.splice(index, 1)
  }
}

const setVarbinds = (key: string, value: any, index: number) => {
  if (index === undefined) {
    return
  }

  if (key === 'setVarbindNumber') {
    if (isNaN(Number(value)) || Number(value) < 0) {
      varbinds.value[index].index = '0'
    } else {
      varbinds.value[index].index = value
    }
  }

  if (key === 'setVarbindOid') {
    varbinds.value[index].index = value
  }

  if (key === 'setValue') {
    varbinds.value[index].value = value
  }

  if (key === 'addVarbindRow') {
    varbinds.value.push({ index: '0', value: '', type: { _text: MaskVarbindsTypeText.vbNumber, _value: MaskVarbindsTypeValue.vbNumber }})
  }

  if (key === 'removeVarbindRow') {
    varbinds.value.splice(index, 1)
  }

  if (key === 'clearAllVarbinds') {
    varbinds.value = []
  }

  if (key === 'setVarbindType') {
    varbinds.value[index].type = value ?? { _text: '', _value: '' }
    varbinds.value[index].index = '0'
  }
}

const setVarbindsDecode = (key: string, value: any, index: number, decodeIndex: number) => {
  if (index === undefined) {
    return
  }

  if (key === 'setParmId') {
    varbindsDecode.value[index].parmId = value
  }

  if (key === 'addVarbindDecodeRow') {
    varbindsDecode.value.push({ parmId: '', decode: [] })
  }

  if (key === 'removeVarbindDecodeRow') {
    varbindsDecode.value.splice(index, 1)
  }

  if (key === 'addDecodeRow') {
    varbindsDecode.value[index].decode.push({ key: '', value: '' })
  }

  if (key === 'removeDecodeRow') {
    varbindsDecode.value[index].decode.splice(decodeIndex, 1)
  }

  if (key === 'setDecodeKey') {
    varbindsDecode.value[index].decode[decodeIndex].key = value
  }

  if (key === 'setDecodeValue') {
    if (isNaN(Number(value)) || Number(value) < 0) {
      varbindsDecode.value[index].decode[decodeIndex].value = '0'
    } else {
      varbindsDecode.value[index].decode[decodeIndex].value = value
    }
  }
}

const handleSaveEvent = async () => {
  if (!isValid.value) {
    return
  }

  if (selectedSource.value?._value === -1) {
    snackbar.showSnackBar({ msg: 'No source selected. Please select a source from the dropdown or create a new one.', error: true })
    return
  }

  const sourceId = selectedSource.value?._value as number
  if (!sourceId) {
    snackbar.showSnackBar({ msg: 'No source selected. Please select a source from the dropdown or create a new one.', error: true })
    return
  }

  try {
    let response = null
    const isEditMode = store.eventModificationState.isEditMode === CreateEditMode.Edit

    if (isEditMode && store.eventModificationState.eventConfigEvent) {
      response = await updateEventConfigEventById(
        xmlContent.value,
        sourceId,
        store.eventModificationState.eventConfigEvent.id,
        store.eventModificationState.eventConfigEvent.enabled
      )
    }
    if (store.eventModificationState.isEditMode === CreateEditMode.Create) {
      response = await createEventConfigEvent(xmlContent.value, sourceId)
    }

    if (response) {
      const msg = isEditMode ? 'Event updated successfully' : 'Event created successfully'
      snackbar.showSnackBar({ msg, error: false })
      handleCancel(sourceId)
    } else {
      snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
    }
  } catch (error) {
    console.error(error)
    snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
  }
}

const handleCancel = (id?: number) => {
  resetValues()
  store.resetEventModificationState()
  if (id && id > 0) {
    router.push({
      name: 'Event Configuration Detail',
      params: { id }
    })
  } else {
    router.push({ name: 'Event Configuration' })
  }
}

const showSourceCreationDialog = () => {
  configName.value = ''
  vendor.value = ''
  sourceCreationDialogState.value = true
}

const handleSourceCreationSave = async () => {
  try {
    const response = await addEventConfigSource(
      configName.value,
      vendor.value,
      ''
    )
    if (response && typeof response === 'object' && response.status === 201) {
      // Success: response contains { id, name, fileOrder, status: 201 }
      await eventConfigStore.fetchAllSourcesNames()
      selectedSource.value = { _text: response.name, _value: response.id }
      configName.value = ''
      vendor.value = ''
      sourceCreationDialogState.value = false
    } else if (response === 409) {
      // Conflict: duplicate name
      snackbar.showSnackBar({
        msg: 'An event configuration source with this name already exists.',
        error: true
      })
    } else if (response === 400) {
      // Bad request: validation error
      snackbar.showSnackBar({
        msg: 'Invalid request. Please check your input and try again.',
        error: true
      })
    } else {
      // 500 or any other error
      snackbar.showSnackBar({
        msg: 'Failed to create event configuration source. Please try again.',
        error: true
      })
    }
  } catch (_error) {
    snackbar.showSnackBar({
      msg: 'Failed to create event configuration source. Please try again.',
      error: true
    })
  }
}

const handleSourceCreationCancel = () => {
  configName.value = ''
  vendor.value = ''
  sourceCreationDialogState.value = false
}

watchEffect(() => {
  const currentErrors = validateEvent(
    eventUei.value,
    eventLabel.value,
    eventDescription.value,
    severity.value._value as string,
    destination.value._value as string,
    logMessage.value,
    addAlarmData.value,
    reductionKey.value,
    alarmType.value._value as string,
    autoClean.value,
    clearKey.value,
    maskElements.value,
    varbinds.value,
    varbindsDecode.value
  )
  isValid.value = Object.keys(currentErrors).length === 0
  errors.value = currentErrors as EventFormErrors
})

const setSelectedSource = (item: any) => {
  if (item && typeof item === 'object') {
    selectedSource.value = item
  } else {
    selectedSource.value = { _text: '', _value: -1 }
  }
}

const search = (query: string) => {
  clearTimeout(timeout.value)
  timeout.value = window.setTimeout(() => {
    results.value = eventConfigStore.uploadedSources
      .filter(s => s.name.toLowerCase().includes(query.toLowerCase()))
      .map(x => ({ _text: x.name, _value: x.id }))
  }, 500)
}

onMounted(async () => {
  await eventConfigStore.fetchAllSourcesNames()
  loadInitialValues(store.eventModificationState.eventConfigEvent)
})
</script>

<style scoped lang="scss">
.main-content {
  padding: 30px;
  margin: 30px;

  border-radius: 8px;
  background: var(--p-content-background);

  .title {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .header {
      display: flex;
      align-items: center;
      gap: 20px;
    }
  }

  .basic-info {
    border-width: 1px;
    border-style: solid;
    border-color: var(--p-content-border-color);
    padding: 10px;
    border-radius: 8px;

    .section-content {
      width: 50%;
    }

    .dropdown {
      width: 50%;
    }
  }

  .spacer {
    min-height: 0.5em;
  }

  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}

.modal-body-form {
  width: 50rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}
</style>
