<template>
  <Dialog
    :visible="visible"
    modal
    maximizable
    :header="`Manage Nodes: ${categoryName}`"
    class="category-nodes-dialog"
    :style="{ width: '900px', maxWidth: '95vw' }"
    data-test="category-nodes-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div v-if="loading" class="loading" data-test="nodes-loading">Loading nodes…</div>
    <div v-else-if="loadError" class="loading" data-test="nodes-load-error">
      Could not load nodes for this category. Close the dialog and try again.
    </div>
    <template v-else>
      <p class="hint">
        Select nodes and move them between <strong>Available</strong> and
        <strong>In this category</strong>, then Save. Use the search box on either
        list to narrow it down. Changes are applied per node against the category
        membership API.
      </p>
      <p v-if="hasRequisitioned" class="warn" data-test="requisitioned-warning">
        Nodes marked <em>(requisitioned)</em> are managed by a requisition —
        category changes on them are overwritten on the next provisioning
        synchronization. Set their categories in the requisition instead.
      </p>
      <div class="dual-list" data-test="node-dual-list">
        <div class="list-col">
          <div class="list-title">Available nodes ({{ available.length }})</div>
          <Listbox
            v-model="selectedAvailable"
            :options="available"
            optionLabel="label"
            multiple
            filter
            :filterFields="['label']"
            filterPlaceholder="Search available nodes"
            listStyle="height: 320px"
            :virtualScrollerOptions="{ itemSize: 34 }"
            data-test="available-listbox"
          >
            <template #option="{ option }">
              <span class="node-label">{{ option.label }}</span>
              <span v-if="option.requisitioned" class="req-tag">(requisitioned)</span>
            </template>
          </Listbox>
        </div>

        <div class="list-actions">
          <Button
            icon="pi pi-angle-right"
            :disabled="!selectedAvailable.length"
            aria-label="Add selected nodes to the category"
            title="Add selected"
            data-test="add-selected"
            @click="addSelected"
          />
          <Button
            icon="pi pi-angle-double-right"
            :disabled="!available.length"
            aria-label="Add all listed available nodes to the category"
            title="Add all"
            data-test="add-all"
            @click="addAll"
          />
          <Button
            icon="pi pi-angle-left"
            :disabled="!selectedMembers.length"
            aria-label="Remove selected nodes from the category"
            title="Remove selected"
            data-test="remove-selected"
            @click="removeSelected"
          />
          <Button
            icon="pi pi-angle-double-left"
            :disabled="!members.length"
            aria-label="Remove all listed nodes from the category"
            title="Remove all"
            data-test="remove-all"
            @click="removeAll"
          />
        </div>

        <div class="list-col">
          <div class="list-title">In this category ({{ members.length }})</div>
          <Listbox
            v-model="selectedMembers"
            :options="members"
            optionLabel="label"
            multiple
            filter
            :filterFields="['label']"
            filterPlaceholder="Search nodes in this category"
            listStyle="height: 320px"
            :virtualScrollerOptions="{ itemSize: 34 }"
            data-test="member-listbox"
          >
            <template #option="{ option }">
              <span class="node-label">{{ option.label }}</span>
              <span v-if="option.requisitioned" class="req-tag">(requisitioned)</span>
            </template>
          </Listbox>
        </div>
      </div>
    </template>

    <template #footer>
      <Button text label="Close" data-test="close-button" @click="emit('update:visible', false)" />
      <Button
        label="Save"
        :disabled="loading || loadError || saving || !dirty"
        data-test="save-button"
        @click="save"
      />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'

import useSnackbar from '@/composables/useSnackbar'
import API from '@/services'
import { CategoryNode } from '@/services/categoryAdminService'

const props = defineProps<{
  visible: boolean
  categoryName: string
}>()

const emit = defineEmits(['update:visible'])

const { showSnackBar } = useSnackbar()

const available = ref<CategoryNode[]>([])
const members = ref<CategoryNode[]>([])
const selectedAvailable = ref<CategoryNode[]>([])
const selectedMembers = ref<CategoryNode[]>([])
const originalMemberIds = ref<Set<number>>(new Set())
const loading = ref(true)
const loadError = ref(false)
const saving = ref(false)
// bumped on every load(); a stale in-flight load compares unequal and bails out
// so a slow category-A fetch can't seat its baseline while B is showing
let loadToken = 0

const byLabel = (a: CategoryNode, b: CategoryNode) => a.label.localeCompare(b.label)

const memberIds = computed(() => new Set(members.value.map((n) => n.id)))
const dirty = computed(() => {
  if (memberIds.value.size !== originalMemberIds.value.size) {
    return true
  }
  for (const id of memberIds.value) {
    if (!originalMemberIds.value.has(id)) {
      return true
    }
  }
  return false
})

const hasRequisitioned = computed(() =>
  available.value.some((n) => n.requisitioned) || members.value.some((n) => n.requisitioned))

const addSelected = () => {
  const moving = new Set(selectedAvailable.value.map((n) => n.id))
  members.value = [...members.value, ...selectedAvailable.value].sort(byLabel)
  available.value = available.value.filter((n) => !moving.has(n.id))
  selectedAvailable.value = []
}

const removeSelected = () => {
  const moving = new Set(selectedMembers.value.map((n) => n.id))
  available.value = [...available.value, ...selectedMembers.value].sort(byLabel)
  members.value = members.value.filter((n) => !moving.has(n.id))
  selectedMembers.value = []
}

const addAll = () => {
  members.value = [...members.value, ...available.value].sort(byLabel)
  available.value = []
  selectedAvailable.value = []
}

const removeAll = () => {
  available.value = [...available.value, ...members.value].sort(byLabel)
  members.value = []
  selectedMembers.value = []
}

const load = async () => {
  const token = ++loadToken
  const category = props.categoryName
  loading.value = true
  loadError.value = false
  // clear immediately so the previous category's rows don't flash on reopen
  available.value = []
  members.value = []
  selectedAvailable.value = []
  selectedMembers.value = []
  const [all, member] = await Promise.all([API.listAllNodes(), API.listCategoryNodes(category)])
  // a newer load (or a category switch) superseded this one — discard its result
  if (token !== loadToken || category !== props.categoryName) {
    return
  }
  // a failed fetch must NOT look like an empty category — otherwise the diff on
  // Save would try to remove every real member. Surface the error and disable Save.
  if (all === null || member === null) {
    available.value = []
    members.value = []
    originalMemberIds.value = new Set()
    loadError.value = true
    loading.value = false
    return
  }
  const memberSet = new Set(member.map((n) => n.id))
  originalMemberIds.value = memberSet
  // seat the members list from the membership fetch itself (not the intersection
  // with all-nodes) so the Save baseline always matches what's displayed — a node
  // returned as a member but missing from all-nodes can't arm a phantom removal
  members.value = member.slice().sort(byLabel)
  available.value = all.filter((n) => !memberSet.has(n.id)).sort(byLabel)
  loading.value = false
}

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible && props.categoryName) {
      load()
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    const now = memberIds.value
    const toAdd = [...now].filter((id) => !originalMemberIds.value.has(id))
    const toRemove = [...originalMemberIds.value].filter((id) => !now.has(id))
    const results = await Promise.all([
      ...toAdd.map((id) => API.addNodeToCategory(props.categoryName, id)),
      ...toRemove.map((id) => API.removeNodeFromCategory(props.categoryName, id))
    ])
    if (results.every(Boolean)) {
      showSnackBar({ msg: `Category '${props.categoryName}' membership updated.` })
      emit('update:visible', false)
    } else {
      // some calls failed; re-sync from the server so the view is accurate
      showSnackBar({ msg: 'Some membership changes failed; the list has been refreshed.', error: true })
      await load()
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.hint {
  margin: 0 0 0.75rem 0;
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

.warn {
  margin: 0 0 0.75rem 0;
  padding: 0.5rem 0.75rem;
  font-size: 0.85rem;
  border-radius: 4px;
  background: var(--p-yellow-50, #fffbeb);
  border: 1px solid var(--p-yellow-200, #fde68a);
  color: var(--p-yellow-800, #854d0e);
}

.node-label {
  margin-right: 0.4rem;
}

.req-tag {
  font-size: 0.75rem;
  color: var(--p-text-muted-color);
  font-style: italic;
}

.loading {
  padding: 2rem;
  text-align: center;
  color: var(--p-text-muted-color);
}

.dual-list {
  display: flex;
  align-items: center;
  gap: 1rem;

  .list-col {
    flex: 1;
    min-width: 0;
  }

  .list-title {
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  .list-actions {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
