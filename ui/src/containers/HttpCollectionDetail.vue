<template>
  <div class="http-collection-detail">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <template v-if="store.currentCollection">
      <div class="collection-header">
        <div class="left">
          <OnmsButton variant="text" data-test="http-collection-back" @click="router.push(COLLECTIONS_TAB_PATH)">
            <OnmsIcon :icon="ArrowBack" />
            Go Back
          </OnmsButton>
          <div class="titles">
            <h2 data-test="http-collection-name">{{ store.currentCollection.name }}</h2>
            <p class="subtitle" data-test="http-collection-rrd">
              Step {{ store.currentCollection.rrd?.step }} s, {{ store.currentCollection.rrd?.rras?.length ?? 0 }} RRAs
            </p>
          </div>
        </div>
        <div class="right">
          <OnmsButton variant="outlined" data-test="http-collection-edit" @click="router.push(collectionEditPath(collectionName))">
            Edit collection
          </OnmsButton>
          <OnmsButton
            variant="outlined"
            severity="danger"
            data-test="http-collection-delete"
            @click="isDeleteConfirmVisible = true"
          >
            Delete collection
          </OnmsButton>
        </div>
      </div>

      <TableCard class="http-uri-card">
        <div class="table-header">
          <h3>URIs</h3>
          <OnmsButton data-test="http-uri-create" @click="router.push(uriCreatePath(collectionName))">New URI</OnmsButton>
        </div>

        <OnmsTable :value="store.currentUris" data-test="http-uri-table">
          <OnmsColumn header="Name" field="name" />
          <OnmsColumn header="Request">
            <template #body="{ data }">
              <span data-test="http-uri-request">{{ requestOf(data) }}</span>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Attributes">
            <template #body="{ data }">{{ attributesOf(data) }}</template>
          </OnmsColumn>
          <OnmsColumn header="Actions">
            <template #body="{ index }">
              <div class="actions">
                <OnmsIconButton
                  :icon="EditIcon"
                  tooltip="Edit URI"
                  aria-label="Edit URI"
                  data-test="http-uri-edit"
                  @click="router.push(uriEditPath(collectionName, index))"
                />
                <OnmsIconButton
                  :icon="DeleteIcon"
                  severity="danger"
                  tooltip="Delete URI"
                  aria-label="Delete URI"
                  data-test="http-uri-delete"
                  @click="pendingDeleteIndex = index"
                />
              </div>
            </template>
          </OnmsColumn>
        </OnmsTable>

        <p v-if="!store.currentUris.length" class="empty" data-test="http-uri-empty">
          This collection does not request any URI yet.
        </p>
      </TableCard>

      <OnmsConfirmationDialog
        v-if="isDeleteConfirmVisible"
        :visible="true"
        title="Delete this HTTP collection?"
        actionButtonText="Delete"
        data-test="http-collection-detail-delete-confirm"
        @ok="onDeleteCollection"
        @cancel="isDeleteConfirmVisible = false"
      >
        <template #content>
          <p>'{{ collectionName }}' and all of its URIs will be removed.</p>
        </template>
      </OnmsConfirmationDialog>

      <OnmsConfirmationDialog
        v-if="pendingDeleteIndex >= 0"
        :visible="true"
        title="Delete this URI?"
        actionButtonText="Delete"
        data-test="http-uri-delete-confirm"
        @ok="onDeleteUri"
        @cancel="pendingDeleteIndex = -1"
      >
        <template #content>
          <p>'{{ store.currentUris[pendingDeleteIndex]?.name }}' and its attributes will be removed.</p>
        </template>
      </OnmsConfirmationDialog>
    </template>

    <EmptyList
      v-else-if="!store.isLoading"
      :content="{ msg: `No HTTP collection named '${collectionName}' exists.` }"
      data-test="http-collection-not-found"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import {
  COLLECTIONS_TAB_PATH,
  HTTP_COLLECTION_ROOT,
  collectionEditPath,
  nameFromParam,
  uriCreatePath,
  uriEditPath
} from '@/lib/httpCollectionRoutes'
import { useHttpCollectionStore } from '@/stores/httpCollectionStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import type { HttpUri } from '@/types/httpCollectionConfig'
import {
  OnmsButton,
  OnmsColumn,
  OnmsConfirmationDialog,
  OnmsIcon,
  OnmsIconButton,
  OnmsTable
} from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'
import ArrowBack from '@opennms/onms-ui/icons/navigation/ArrowBack.vue'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const store = useHttpCollectionStore()
const { report } = useActionFeedback()

const isDeleteConfirmVisible = ref(false)
const pendingDeleteIndex = ref(-1)

const collectionName = computed(() => nameFromParam(route.params.name))
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'HTTP Data Collection Configuration', to: HTTP_COLLECTION_ROOT },
  { label: collectionName.value, to: '#', position: 'last' }
]))

/** The request as a URL, so the table shows at a glance what is fetched. */
const requestOf = (uri: HttpUri) => {
  const url = uri.url
  const port = url.port ? `:${url.port}` : ''
  const query = url.query ? `?${url.query}` : ''
  return `${url.method ?? 'GET'} ${url.scheme ?? 'http'}://${url.host ?? '${ipaddr}'}${port}${url.path}${query}`
}

const attributesOf = (uri: HttpUri) => {
  const attributes = uri.attributes ?? []
  return attributes.length ? attributes.map(attrib => attrib.alias).join(', ') : '--'
}

const load = async () => {
  report(await store.fetchCollection(collectionName.value))
}

onMounted(load)

watch(collectionName, load)

const onDeleteCollection = async () => {
  isDeleteConfirmVisible.value = false

  const result = await store.deleteCollection(collectionName.value, store.currentCollection?.version)

  if (report(result, 'HTTP collection deleted.')) {
    router.push(COLLECTIONS_TAB_PATH)
  }
}

const onDeleteUri = async () => {
  const index = pendingDeleteIndex.value
  pendingDeleteIndex.value = -1

  report(await store.deleteUri(index), 'URI deleted.')
}
</script>

<style lang="scss" scoped>
.http-collection-detail {
  padding: 1.5em;

  .collection-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1em;
    margin-bottom: 1.25em;

    .left {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .titles h2 {
      margin: 0;
    }

    .subtitle {
      margin: 0;
      color: var(--p-text-muted-color);
    }

    .right {
      display: flex;
      gap: 0.5em;
    }
  }

  .http-uri-card {
    padding: 25px;
    border: 1px solid var(--p-content-border-color);
    border-radius: 5px;
  }

  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 0 0.75em 0;

    h3 {
      margin: 0;
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 0.25em;
  }

  .empty {
    color: var(--p-text-muted-color);
  }
}
</style>
