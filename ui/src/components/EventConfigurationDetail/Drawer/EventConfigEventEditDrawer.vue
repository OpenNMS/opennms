<template>
  <FeatherDrawer
    id="column-selection-drawer"
    data-test="column-selection-drawer"
    @shown="() => store.eventModificationDrawerState.visible"
    @hidden="store.closeEventModificationDrawer()"
    v-model="store.eventModificationDrawerState.visible"
    :labels="{ close: 'close', title: 'Customize Columns' }"
    width="55em"
  >
    <div
      class="feather-drawer-custom-padding"
      v-if="store.eventModificationDrawerState.eventConfigEvent"
    >
      <section>
        <h3>
          {{ store.eventModificationDrawerState.isEditMode === CreateEditMode.Create ? 'Create New Event' : 'Edit Event Details' }}
        </h3>
      </section>
      <div>
        <div class="spacer-large"></div>
        <FeatherRadioGroup
          :label="'Specify document format:'"
          v-model="selected"
        >
          <FeatherRadio
            v-for="item in EventConfigurationDocTypes"
            :value="item.value"
            :key="item.name"
          >
            {{ item.name }}
          </FeatherRadio>
        </FeatherRadioGroup>
        <div class="spacer-large"></div>
      </div>
      <div
        v-if="selected === EventConfigurationDocType.Json"
        class="drawer-content"
      >
        <div class="spacer-large"></div>
        <FeatherInput
          label="Event UEI"
          type="search"
          data-test="event-label"
          v-model.trim="eventUei"
        ></FeatherInput>
        <div class="spacer-medium"></div>
        <div class="spacer-medium"></div>
        <FeatherInput
          label="Event Label"
          type="search"
          data-test="event-label"
          v-model.trim="eventLabel"
        >
        </FeatherInput>
        <div class="spacer-medium"></div>
        <div class="spacer-medium"></div>
        <FeatherTextarea
          v-model.trim="eventDescription"
          label="Event Description"
          placeholder="Type your event description here..."
          rows="10"
          cols="40"
          auto
          clear
        ></FeatherTextarea>
        <div class="spacer-medium"></div>
        <div class="spacer-medium"></div>
        <FeatherSelect
          label="Select Severity"
          :options="severityOptions"
          v-model="selectedEventSeverity"
        >
          <FeatherIcon :icon="MoreVert" />
        </FeatherSelect>
        <div class="spacer-large"></div>
        <div class="spacer-large"></div>
        <h3>XML Preview</h3>
        <VAceEditor
          v-model:value="xmlPreviewContent"
          lang="xml"
          theme="chrome"
          class="editor"
          :options="editorOptions"
        />
        <div class="spacer-large"></div>
        <div class="spacer-large"></div>
      </div>
      <div
        v-if="selected === EventConfigurationDocType.Xml"
        class="drawer-content"
      >
        <div class="spacer-large"></div>
        <VAceEditor
          v-model:value="xmlContent"
          lang="xml"
          theme="chrome"
          class="editor"
          :options="editorOptions"
        />
        <div class="spacer-large"></div>
        <div class="spacer-large"></div>
      </div>
      <div>
        <FeatherButton
          primary
          @click="handleSave()"
        >
          Save Changes
        </FeatherButton>
        <FeatherButton
          secondary
          @click="store.closeEventModificationDrawer()"
        >
          Close
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { mapEventConfigEventToServer } from '@/mappers/eventConfig.mapper'
import { createEventConfigEventJson, createEventConfigEventXml, updateEventConfigEventByIdJson, updateEventConfigEventByIdXml } from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { FeatherTextarea } from '@featherds/textarea'
import 'ace-builds/src-noconflict/ext-language_tools'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/theme-chrome'
import { XMLValidator } from 'fast-xml-parser'
import vkbeautify from 'vkbeautify'
import { VAceEditor } from 'vue3-ace-editor'
import { EventConfigurationDocTypes, Severity, severityOptions, EventConfigurationDocType } from '../constants'
import { validateEventDetailsJson, validateEventDetailsXml } from '../eventXmlValidator'

const snackbar = useSnackbar()
const store = useEventConfigDetailStore()
const eventDescription = ref(store.eventModificationDrawerState.eventConfigEvent?.description || '')
const eventLabel = ref(store.eventModificationDrawerState.eventConfigEvent?.eventLabel || '')
const eventUei = ref(store.eventModificationDrawerState.eventConfigEvent?.uei || '')
const selected = ref(EventConfigurationDocType.Json)
const xmlContent = ref(store.eventModificationDrawerState.eventConfigEvent?.xmlContent || '')
const selectedEventSeverity = ref<ISelectItemType>({
  _text: store.eventModificationDrawerState.eventConfigEvent?.severity ? Severity[store.eventModificationDrawerState.eventConfigEvent?.severity as keyof typeof Severity] : '',
  _value: store.eventModificationDrawerState.eventConfigEvent?.severity ? Severity[store.eventModificationDrawerState.eventConfigEvent?.severity as keyof typeof Severity] : ''
})
const xmlPreviewContent = computed(() => {
  return `<event xmlns="http://xmlns.opennms.org/xsd/eventconf">
  <uei>${eventUei.value}</uei>
  <event-label>${eventLabel.value}</event-label>
  <descr>${eventDescription.value}</descr>
  <severity>${selectedEventSeverity.value._value}</severity>
</event>`.trim()
})
const editorOptions = computed(() => {
  return {
    showPrintMargin: false,
    tabSize: 2,
    useSoftTabs: true,
    enableLiveAutocompletion: true,
    showLineNumbers: true,
    fontSize: 18,
    readOnly: selected.value === EventConfigurationDocType.Json
  }
})

const handleSave = async () => {
  if (!store.eventModificationDrawerState.eventConfigEvent || !store.selectedSource) {
    return
  }

  if (selected.value === EventConfigurationDocType.Json) {
    await handleSaveJsonFormat()
  }

  if (selected.value === EventConfigurationDocType.Xml) {
    await handleSaveXmlFormat()
  }
}

const handleSaveJsonFormat = async () => {
  if (!store.eventModificationDrawerState.eventConfigEvent || !store.selectedSource) {
    return
  }

  const result = XMLValidator.validate(xmlPreviewContent.value)
  if (!result) {
    snackbar.showSnackBar({ msg: 'Invalid event details', error: true })
    return
  }
  const { isValid, error } = validateEventDetailsJson(store.eventModificationDrawerState.eventConfigEvent, eventUei.value, eventLabel.value, eventDescription.value, selectedEventSeverity.value._value)
  if (!isValid) {
    snackbar.showSnackBar({ msg: error.join(', '), error: true })
    return
  }

  try {
    const event = {
      ...store.eventModificationDrawerState.eventConfigEvent,
      uei: eventUei.value,
      eventLabel: eventLabel.value,
      description: eventDescription.value,
      severity: selectedEventSeverity.value._value
    } as EventConfigEvent

    const newEvent = mapEventConfigEventToServer(event)
    let response
    if (store.eventModificationDrawerState.isEditMode === CreateEditMode.Edit) {
      response = await updateEventConfigEventByIdJson(newEvent, event.id)
    }
    if (store.eventModificationDrawerState.isEditMode === CreateEditMode.Create) {
      response = await createEventConfigEventJson(newEvent, store.selectedSource.id)
    }

    if (response) {
      snackbar.showSnackBar({ msg: store.eventModificationDrawerState.isEditMode === CreateEditMode.Create ? 'Event created successfully' : 'Event updated successfully', error: false })
      await store.fetchEventsBySourceId()
      store.closeEventModificationDrawer()
    } else {
      snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
    }
  } catch (err) {
    console.error(err)
    snackbar.showSnackBar({ msg: 'Error updating event', error: true })
  }
}

const handleSaveXmlFormat = async () => {
  if (!store.eventModificationDrawerState.eventConfigEvent || !store.selectedSource) {
    return
  }

  try {
    const beautifiedXml = vkbeautify.xml(xmlContent.value)
    const { isValid, error } = validateEventDetailsXml(store.eventModificationDrawerState.eventConfigEvent, beautifiedXml)
    if (!isValid) {
      snackbar.showSnackBar({ msg: error.join(', '), error: true })
      return
    }

    let response
    if (store.eventModificationDrawerState.isEditMode === CreateEditMode.Edit) {
      response = await updateEventConfigEventByIdXml(beautifiedXml, store.eventModificationDrawerState.eventConfigEvent.id)
    }
    if (store.eventModificationDrawerState.isEditMode === CreateEditMode.Create) {
      response = await createEventConfigEventXml(beautifiedXml, store.selectedSource.id)
    }

    if (response) {
      snackbar.showSnackBar({ msg: store.eventModificationDrawerState.isEditMode === CreateEditMode.Create ? 'Event created successfully' : 'Event updated successfully', error: false })
      await store.fetchEventsBySourceId()
      store.closeEventModificationDrawer()
    } else {
      snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
    }
  } catch (error) {
    console.error(error)
    snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
  }
}

const loadInitialValues = (val: any) => {
  if (val) {
    eventDescription.value = val.description || ''
    eventUei.value = val.uei || ''
    eventLabel.value = val.eventLabel || ''
    xmlContent.value = vkbeautify.xml(val.xmlContent.trim().replaceAll('&lt;', '<').replaceAll('&gt;', '>')) || ''
    selectedEventSeverity.value = {
      _text: val.severity ? Severity[val.severity as keyof typeof Severity] : '',
      _value: val.enabled ? Severity[val.severity as keyof typeof Severity] : ''
    }
  } else {
    eventUei.value = ''
    eventLabel.value = ''
    eventDescription.value = ''
    selectedEventSeverity.value = { _text: '', _value: '' }
    xmlContent.value = ''
    selected.value = EventConfigurationDocType.Json
  }
}

watch(
  () => store.eventModificationDrawerState.eventConfigEvent,
  (newVal) => {
    loadInitialValues(newVal)
  }, { immediate: true, deep: true }
)
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.feather-drawer-custom-padding {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.spacer-large {
  margin-bottom: 2rem;
}

.spacer-medium {
  margin-bottom: 0.5rem;
}

.footer {
  display: flex;
  padding-top: 20px;
}

button.primary {
  margin-top: 2rem;
  background-color: #1d2f75;
  color: white;
  padding: 0.5em 1.5em;
  border: none;
}

:deep(.feather-input-sub-text) {
  display: none;
}

.drawer-content {
  display: flex;
  flex-direction: column;
}

.textarea {
  padding: 5px;
}

.editor {
  height: 500px;
  width: 100%;
  border-radius: 6px;
  border: 1px solid #ccc
}
</style>

