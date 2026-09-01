<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="group-editor-container">
    <div class="page-header">
      <OnmsButton variant="text" class="back-button" data-test="back-button" @click="goBack">
        <OnmsIcon :icon="ArrowBack" />
        Go Back
      </OnmsButton>
      <h1 class="page-title" data-test="editor-title">
        {{ isEditing ? `Edit Group: ${groupName}` : 'Create New Group' }}
      </h1>
    </div>

    <TableCard class="editor-card">
      <div
        v-if="errorText"
        class="page-error"
        role="alert"
        data-test="editor-error"
      >{{ errorText }}</div>

      <div v-if="notFound" class="not-found" data-test="editor-not-found">
        <OnmsButton variant="outlined" label="Back to Manage Groups" @click="goBack" />
      </div>

      <div v-else class="form-column">
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

        <div class="actions">
          <OnmsButton
            :label="isEditing ? 'Save' : 'Create'"
            :disabled="!isValid || saving"
            data-test="save-button"
            @click="save"
          />
          <OnmsButton
            variant="text"
            label="Cancel"
            data-test="cancel-button"
            @click="goBack"
          />
        </div>
      </div>
    </TableCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { OnmsButton, OnmsIcon, OnmsIconButton, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

import Add from '@opennms/onms-ui/icons/action/Add.vue'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'
import Cancel from '@opennms/onms-ui/icons/navigation/Cancel.vue'
import KeyboardArrowDown from '@opennms/onms-ui/icons/hardware/KeyboardArrowDown.vue'
import KeyboardArrowUp from '@opennms/onms-ui/icons/hardware/KeyboardArrowUp.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import { validateAdminComments, validateAdminName } from '@/lib/adminValidation'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { useMenuStore } from '@/stores/menuStore'
import { ManagedGroup } from '@/types/groupAdmin'
import { BreadCrumb } from '@/types'

// ':groupName' is 'create' for a new group, per the SNMP source-detail route
const CREATE_MARKER = 'create'

const route = useRoute()
const router = useRouter()
const store = useGroupAdminStore()
const menuStore = useMenuStore()

const groupName = computed(() => decodeURIComponent(String(route.params.groupName ?? '')))
const isEditing = computed(() => groupName.value !== CREATE_MARKER)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Manage Groups', to: '/admin/groups' },
  { label: isEditing.value ? groupName.value : 'Create New Group', to: '#', position: 'last' }
])

const name = ref('')
const comments = ref('')
const members = ref<string[]>([])
const memberToAdd = ref<string | null>(null)
const saving = ref(false)
const errorText = ref('')
const notFound = ref(false)
// the loaded group, so fields this form doesn't expose (duty schedules)
// round-trip untouched on save
let original: ManagedGroup | null = null

const addableUsers = computed(() => store.memberCandidates.filter(u => !members.value.includes(u)))

const nameProblem = computed(() => (isEditing.value ? null : validateAdminName(name.value, 'group name')))

// a hand-edited comment that predates this page stays editable: only
// changed input is validated (the server grandfathers unchanged values too)
const commentsProblem = computed(() =>
  comments.value.trim() === (original?.comments ?? '').trim() ? null : validateAdminComments(comments.value))

const isValid = computed(() =>
  (isEditing.value || !!name.value.trim()) && !nameProblem.value && !commentsProblem.value)

onMounted(async () => {
  await store.populate()
  if (!isEditing.value) {
    return
  }
  const group = store.groups.find((g: ManagedGroup) => g.name === groupName.value)
  if (!group) {
    notFound.value = true
    errorText.value = `Group "${groupName.value}" was not found. It may have been deleted or renamed.`
    return
  }
  original = group
  comments.value = group.comments ?? ''
  members.value = [...(group.users ?? [])]
})

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
  errorText.value = ''
  try {
    const base = original ?? {}
    const payload: ManagedGroup = {
      ...base,
      name: isEditing.value ? groupName.value : name.value.trim(),
      comments: comments.value.trim(),
      users: [...members.value]
    }
    const error = isEditing.value ? await store.updateGroup(payload) : await store.createGroup(payload)
    if (error === null) {
      goBack()
    } else {
      errorText.value = error
    }
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push({ path: '/admin/groups' })
}
</script>

<style lang="scss" scoped>
.group-editor-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.25rem;
}

.back-button {
  padding-left: 0;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.editor-card {
  padding: 25px;
  max-width: 48rem;
}

.page-error {
  margin-bottom: 1rem;
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}

.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;

  :deep(input),
  :deep(.p-select) {
    width: 100%;
  }
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

.actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
}
</style>
