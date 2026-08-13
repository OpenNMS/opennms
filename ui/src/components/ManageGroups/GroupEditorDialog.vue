<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit Group: ${originalName}` : 'Add New Group'"
    class="group-editor-dialog"
    width="min(640px, 95vw)"
    data-test="group-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div
        v-if="errorText"
        class="dialog-error"
        role="alert"
        data-test="dialog-error"
      >{{ errorText }}</div>
      <FormField
        v-if="!isEditing"
        label="Group Name"
        for="group-editor-name"
        required
        :error="nameProblem || undefined"
      >
        <OnmsInputText
          id="group-editor-name"
          v-model="name"
          :invalid="!!nameProblem"
          data-test="group-name-input"
        />
      </FormField>
      <FormField
        label="Comments"
        for="group-editor-comments"
        :error="commentsProblem || undefined"
      >
        <OnmsInputText
          id="group-editor-comments"
          v-model="comments"
          :invalid="!!commentsProblem"
          data-test="group-comments-input"
        />
      </FormField>

      <div class="members-section">
        <div class="members-header">
          <span class="members-title">Members</span>
          <span class="members-hint">Order matters: it is the notification escalation order.</span>
        </div>
        <div class="add-member-row">
          <FormField
            label="Add user"
            for="group-editor-add-member"
          >
            <OnmsSelect
              v-model="memberToAdd"
              labelId="group-editor-add-member"
              :options="addableUsers"
              filter
              data-test="add-member-select"
            />
          </FormField>
          <OnmsIconButton
            variant="outlined"
            :icon="Add"
            aria-label="Add member"
            data-test="add-member-button"
            :disabled="!memberToAdd"
            @click="addMember"
          />
        </div>
        <ul
          v-if="members.length"
          class="member-list"
          data-test="member-list"
        >
          <li
            v-for="(member, index) in members"
            :key="member"
            class="member-row"
          >
            <span class="member-order">{{ index + 1 }}.</span>
            <span class="member-name">{{ member }}</span>
            <span class="member-actions">
              <OnmsIconButton
                :icon="KeyboardArrowUp"
                :aria-label="`Move ${member} up`"
                data-test="move-up-button"
                :disabled="index === 0"
                @click="move(index, -1)"
              />
              <OnmsIconButton
                :icon="KeyboardArrowDown"
                :aria-label="`Move ${member} down`"
                data-test="move-down-button"
                :disabled="index === members.length - 1"
                @click="move(index, 1)"
              />
              <OnmsIconButton
                severity="danger"
                :icon="Cancel"
                :aria-label="`Remove ${member}`"
                data-test="remove-member-button"
                @click="members.splice(index, 1)"
              />
            </span>
          </li>
        </ul>
        <p
          v-else
          class="no-members"
        >No members yet.</p>
      </div>
    </div>

    <template #footer>
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <OnmsButton
        :label="isEditing ? 'Save Group' : 'Add Group'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsIconButton, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import Add from '@/components/icons/action/Add.vue'
import Cancel from '@/components/icons/navigation/Cancel.vue'
import KeyboardArrowDown from '@/components/icons/hardware/KeyboardArrowDown.vue'
import KeyboardArrowUp from '@/components/icons/hardware/KeyboardArrowUp.vue'
import { validateAdminComments, validateAdminName } from '@/lib/adminValidation'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { ManagedGroup } from '@/types/groupAdmin'

const props = defineProps<{
  visible: boolean
  group: ManagedGroup | null
}>()

const emit = defineEmits(['update:visible'])

const store = useGroupAdminStore()

const name = ref('')
const comments = ref('')
const members = ref<string[]>([])
const memberToAdd = ref<string | null>(null)
const saving = ref(false)
const errorText = ref('')

const isEditing = computed(() => props.group !== null)
const originalName = computed(() => props.group?.name ?? '')

const addableUsers = computed(() => store.memberCandidates.filter(u => !members.value.includes(u)))

const nameProblem = computed(() => (isEditing.value ? null : validateAdminName(name.value, 'group name')))

// a hand-edited comment that predates this page stays editable: only
// changed input is validated (the server grandfathers unchanged values too)
const commentsProblem = computed(() =>
  comments.value.trim() === (props.group?.comments ?? '').trim() ? null : validateAdminComments(comments.value))

const isValid = computed(() =>
  (isEditing.value || !!name.value.trim()) && !nameProblem.value && !commentsProblem.value)

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    errorText.value = ''
    memberToAdd.value = null
    if (props.group) {
      name.value = props.group.name
      comments.value = props.group.comments ?? ''
      members.value = [...(props.group.user ?? [])]
    } else {
      name.value = ''
      comments.value = ''
      members.value = []
    }
  }
)

const addMember = () => {
  if (memberToAdd.value && !members.value.includes(memberToAdd.value)) {
    members.value.push(memberToAdd.value)
  }
  memberToAdd.value = null
}

const move = (index: number, delta: number) => {
  const target = index + delta
  if (target < 0 || target >= members.value.length) {
    return
  }
  const list = [...members.value]
  const [item] = list.splice(index, 1)
  list.splice(target, 0, item)
  members.value = list
}

const save = async () => {
  saving.value = true
  try {
    // spread the original so fields this form doesn't expose (duty
    // schedules) round-trip untouched; default-map is preserved server-side
    const base = props.group ?? {}
    const payload: ManagedGroup = {
      ...base,
      name: isEditing.value ? originalName.value : name.value.trim(),
      comments: comments.value.trim(),
      user: [...members.value]
    }
    const error = isEditing.value ? await store.updateGroup(payload) : await store.createGroup(payload)
    if (error === null) {
      emit('update:visible', false)
    } else {
      errorText.value = error
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;

  :deep(input),
  :deep(.p-select) {
    width: 100%;
  }
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}

.members-section {
  .members-header {
    display: flex;
    align-items: baseline;
    gap: 0.75rem;
    margin-bottom: 0.5rem;

    .members-title {
      font-weight: 600;
    }

    .members-hint {
      font-size: 0.85rem;
      color: var(--p-text-muted-color);
    }
  }

  .add-member-row {
    display: flex;
    gap: 0.5rem;
    align-items: flex-end;
    margin-bottom: 0.5rem;

    .form-field {
      flex: 1;
    }
  }

  .member-list {
    list-style: none;
    margin: 0;
    padding: 0;
    border: 1px solid var(--p-content-border-color);
    border-radius: 6px;

    .member-row {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.25rem 0.75rem;

      & + .member-row {
        border-top: 1px solid var(--p-content-border-color);
      }

      .member-order {
        color: var(--p-text-muted-color);
        min-width: 1.5rem;
      }

      .member-name {
        flex: 1;
        font-weight: 500;
      }

      .member-actions {
        display: flex;
      }
    }
  }

  .no-members {
    margin: 0;
    color: var(--p-text-muted-color);
  }
}
</style>
