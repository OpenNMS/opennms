<template>
  <TableCard
    class="group-editor-panel"
    data-test="group-editor-panel"
  >
    <div class="panel-header">
      <OnmsButton
        variant="text"
        class="back-button"
        data-test="back-button"
        @click="emit('close')"
      >
        <OnmsIcon :icon="ArrowBack" />
        Back
      </OnmsButton>
      <h2 class="card-title">{{ isEditing ? `Edit Group: ${originalName}` : 'Add New Group' }}</h2>
    </div>
    <div class="editor-body">
      <div
        v-if="errorText"
        class="error-banner"
        role="alert"
        data-test="save-error"
      >{{ errorText }}</div>
      <OnmsTabs v-model:value="activeTab" data-test="group-editor-tabs">
        <OnmsTabList>
          <OnmsTab value="group" data-test="tab-group">
            Group<TabProblemMarker v-if="groupTabProblem" />
          </OnmsTab>
          <OnmsTab value="duty" data-test="tab-duty-schedules">
            Duty Schedules<TabProblemMarker v-if="!dutyValid" />
          </OnmsTab>
          <OnmsTab value="categories" data-test="tab-categories">Categories</OnmsTab>
        </OnmsTabList>
        <OnmsTabPanels>
          <OnmsTabPanel value="group">
            <div class="form-column">
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
                <OnmsTextarea
                  id="group-editor-comments"
                  v-model="comments"
                  :rows="3"
                  autoResize
                  fluid
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
                    label="Add users"
                    for="group-editor-add-member"
                  >
                    <OnmsMultiSelect
                      v-model="membersToAdd"
                      inputId="group-editor-add-member"
                      :options="addableUsers"
                      display="chip"
                      :showToggleAll="false"
                      filter
                      fluid
                      data-test="add-member-select"
                    />
                  </FormField>
                  <OnmsButton
                    variant="outlined"
                    label="Add"
                    data-test="add-member-button"
                    :disabled="!membersToAdd.length"
                    @click="addMembers"
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
          </OnmsTabPanel>
          <OnmsTabPanel value="duty">
            <FormField
              label="Duty Schedules"
              hint="Pick the days and the begin/end times for each coverage window."
            >
              <DutyScheduleEditor
                :key="dutyEditorKey"
                v-model="dutySchedules"
                v-model:valid="dutyValid"
              />
            </FormField>
          </OnmsTabPanel>
          <OnmsTabPanel value="categories">
            <FormField
              label="Categories"
              for="group-editor-categories"
              hint="Used when ACLs are enabled (org.opennms.web.aclsEnabled): members of this group see only nodes in these categories."
            >
              <OnmsMultiSelect
                v-model="categories"
                inputId="group-editor-categories"
                :options="categoryOptions"
                display="chip"
                :showToggleAll="false"
                filter
                fluid
                data-test="categories-select"
              />
            </FormField>
          </OnmsTabPanel>
        </OnmsTabPanels>
      </OnmsTabs>
    </div>
    <div class="panel-footer">
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('close')"
      />
      <OnmsButton
        :label="isEditing ? 'Save Group' : 'Add Group'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import {
  OnmsButton,
  OnmsIcon,
  OnmsIconButton,
  OnmsInputText,
  OnmsMultiSelect,
  OnmsTab,
  OnmsTabList,
  OnmsTabPanel,
  OnmsTabPanels,
  OnmsTabs,
  OnmsTextarea
} from '@opennms/onms-ui'

import DutyScheduleEditor from '@/components/Common/DutyScheduleEditor.vue'
import FormField from '@/components/Common/FormField.vue'
import TabProblemMarker from '@/components/Common/TabProblemMarker.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'
import Cancel from '@opennms/onms-ui/icons/navigation/Cancel.vue'
import KeyboardArrowDown from '@opennms/onms-ui/icons/hardware/KeyboardArrowDown.vue'
import KeyboardArrowUp from '@opennms/onms-ui/icons/hardware/KeyboardArrowUp.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'
import { validateAdminComments, validateAdminName, validateReservedName } from '@/lib/adminValidation'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { ManagedGroup, ManagedGroupCreate, PROTECTED_GROUP_NAMES } from '@/types/groupAdmin'

// Rendered in place of the table (see ManageGroups); `group` is null when creating.
// Emits `close` on Back, Cancel, a successful save, or a save rejected as stale.
const props = defineProps<{
  group: ManagedGroup | null
}>()

const emit = defineEmits<{
  close: []
}>()

const store = useGroupAdminStore()
const { withSpinner } = useActionFeedback()
const { reportWrite } = useAdminWriteFeedback()

const name = ref('')
const comments = ref('')
const members = ref<string[]>([])
const membersToAdd = ref<string[]>([])
const dutySchedules = ref<string[]>([])
const categories = ref<string[]>([])
const dutyValid = ref(true)
// a new group gets a fresh schedule editor: an unfinished row is not part of the value, so two groups
// without schedules look alike to the editor, and it would keep the row of the previous one
const dutyEditorKey = ref(0)
const saving = ref(false)
const errorText = ref('')

type EditorTab = 'group' | 'duty' | 'categories'
const activeTab = ref<EditorTab>('group')

const isEditing = computed(() => props.group !== null)
const originalName = computed(() => props.group?.name ?? '')

const addableUsers = computed(() => store.memberCandidates.filter(u => !members.value.includes(u)))

const nameProblem = computed(() => (isEditing.value ? null : validateAdminName(name.value, 'group name') || validateReservedName(name.value, PROTECTED_GROUP_NAMES, 'group name')))

// a hand-edited comment that predates this page stays editable: only
// changed input is validated (the server grandfathers unchanged values too)
const commentsProblem = computed(() =>
  comments.value.trim() === (props.group?.comments ?? '').trim() ? null : validateAdminComments(comments.value))

// categories sorted ignoring case, close to the server's order (Java's CASE_INSENSITIVE_ORDER differs for accents)
const ignoringCase = (a: string, b: string) => a.localeCompare(b, undefined, { sensitivity: 'base' })

// a stored category the list lacks (created after the list was loaded, or the list failed to load) is
// still offered, so that it shows as chosen and is not dropped unseen
const categoryOptions = computed(() => {
  const missing = (props.group?.categories ?? []).filter(c => !store.availableCategories.includes(c))
  if (!missing.length) {
    return store.availableCategories
  }
  return [...store.availableCategories, ...missing].sort(ignoringCase)
})

// the checks that show a message on the Group tab; marks the tab, since Save is disabled from any tab
const groupTabProblem = computed(() => !!(nameProblem.value || commentsProblem.value))

const isValid = computed(() =>
  dutyValid.value && (isEditing.value || !!name.value.trim()) && !nameProblem.value && !commentsProblem.value)

watch(
  () => props.group,
  () => {
    errorText.value = ''
    activeTab.value = 'group'
    dutyEditorKey.value++
    membersToAdd.value = []
    if (props.group) {
      name.value = props.group.name
      comments.value = props.group.comments ?? ''
      members.value = [...(props.group.users ?? [])]
      dutySchedules.value = [...(props.group.dutySchedules ?? [])]
      categories.value = [...(props.group.categories ?? [])]
    } else {
      name.value = ''
      comments.value = ''
      members.value = []
      dutySchedules.value = []
      categories.value = []
    }
  },
  { immediate: true }
)

// the chosen users join the end of the list in the order the candidates are listed
const addMembers = () => {
  const chosen = store.memberCandidates.filter(u => membersToAdd.value.includes(u) && !members.value.includes(u))
  members.value.push(...chosen)
  membersToAdd.value = []
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
    // spread the original so a group that came without categories (absent means
    // "leave alone") is not given an empty list unless the user chose some;
    // default-map is preserved server-side
    const fields: ManagedGroupCreate = {
      name: isEditing.value ? originalName.value : name.value.trim(),
      comments: comments.value.trim(),
      users: [...members.value],
      dutySchedules: dutySchedules.value.map(s => s.trim()).filter(Boolean)
    }
    if (props.group?.categories !== undefined || categories.value.length > 0 || !props.group) {
      fields.categories = [...categories.value].sort(ignoringCase)
    }
    const result = await withSpinner(() => props.group
      ? store.updateGroup({ ...props.group, ...fields })
      : store.createGroup(fields))
    reportWrite(result, props.group ? `Group '${fields.name}' updated.` : `Group '${fields.name}' created.`, {
      close: () => emit('close'),
      banner: (msg) => {
        errorText.value = msg
      }
    })
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.group-editor-panel {
  padding: 25px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;

  .back-button {
    padding-left: 0;
  }

  .card-title {
    font-size: 1.1rem;
    font-weight: 600;
    margin: 0;
  }
}

.editor-body,
.panel-footer {
  max-width: 1040px;
}

.panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.5rem;
}

.error-banner {
  margin-bottom: 1rem;
  padding: 0.6rem 0.85rem;
  border: 1px solid var(--p-red-400, #e57373);
  border-radius: 4px;
  color: var(--p-red-400, #e57373);
  background: color-mix(in srgb, var(--p-red-400, #e57373) 12%, transparent);
}

.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;

  :deep(input),
  :deep(textarea),
  :deep(.p-multiselect) {
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
</style>
