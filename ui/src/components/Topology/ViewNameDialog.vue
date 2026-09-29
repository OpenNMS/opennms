<!--

Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.

-->
<template>
  <OnmsDialog
    :visible="visible"
    :header="title"
    :closable="false"
    width="26rem"
    @update:visible="onVisibleChange"
    @show="focusField"
  >
    <div class="vnd-body">
      <label
        class="vnd-label"
        :for="fieldId"
      >View name</label>
      <OnmsInputText
        :id="fieldId"
        v-model="name"
        :invalid="collision"
        :fluid="true"
        :aria-describedby="collision ? errorId : undefined"
        maxlength="255"
        autocomplete="off"
        @keyup.enter="submit"
      />
      <!-- The row is always present, so the message appearing does not resize
           the dialog and shift the buttons under the pointer. -->
      <p
        :id="errorId"
        class="vnd-error"
        role="alert"
      >
        {{ collision ? `A view named "${trimmed}" already exists.` : '' }}
      </p>
    </div>
    <template #footer>
      <OnmsButton
        :label="actionLabel"
        :disabled="!submittable"
        @click="submit"
      />
      <OnmsButton
        variant="text"
        label="Cancel"
        @click="cancel"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, useId, watch } from 'vue'
import { OnmsButton, OnmsDialog, OnmsInputText } from '@opennms/onms-ui'

// Naming a view: New, Save As and Rename all ask for one string against the
// same rules, so they share this instead of three window.prompt calls. The
// browser dialog also carried a real fault: Chromium's "prevent additional
// dialogs" checkbox suppresses every later prompt, and a suppressed prompt
// returns null, so the three actions silently did nothing until a reload.
const props = withDefaults(defineProps<{
  visible: boolean
  title: string
  actionLabel: string
  initialName?: string
  // Names that collide. The caller decides what counts: Save As must create a
  // new entry so the open view's own name is a conflict, while Rename may keep
  // it.
  takenNames?: string[]
}>(), {
  initialName: '',
  takenNames: () => []
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [name: string]
}>()

const fieldId = useId()
const errorId = `${fieldId}-error`

const name = ref(props.initialName)

const trimmed = computed(() => name.value.trim())
const collision = computed(() => props.takenNames.includes(trimmed.value))
const submittable = computed(() => trimmed.value !== '' && !collision.value)

// Reseed on open rather than on prop change, so a caller that leaves
// initialName bound to a store value cannot rewrite the field mid-edit.
watch(() => props.visible, (open) => {
  if (open) {
    name.value = props.initialName
  }
})

const focusField = () => {
  // Appended to body, so the field is not inside this component's subtree.
  const el = document.getElementById(fieldId) as HTMLInputElement | null
  el?.focus()
  el?.select()
}

const close = () => emit('update:visible', false)

const submit = () => {
  if (!submittable.value) {
    return
  }
  emit('submit', trimmed.value)
  close()
}

const cancel = () => close()

const onVisibleChange = (value: boolean) => {
  if (!value) {
    close()
  }
}
</script>

<style scoped>
.vnd-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.vnd-label {
  font-size: 0.875rem;
  color: var(--onms-secondary-text-on-surface);
}

.vnd-error {
  min-height: 1.25rem;
  margin: 0;
  font-size: 0.8125rem;
  color: var(--onms-error);
}
</style>
