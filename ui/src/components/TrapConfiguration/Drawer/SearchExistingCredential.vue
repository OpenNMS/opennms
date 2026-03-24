<template>
  <FeatherDrawer
    id="drawer"
    data-test="drawer"
    v-model="store.credentialDrawerState.visible"
    :labels="{ close: 'close', title: 'Use an existing credential' }"
    hide-close
    @hidden="store.closeCredentialDrawer"
    width="40rem"
    class="drawer"
  >
    <div class="container">
      <div class="title">
        <h2>Use an existing credential</h2>
      </div>
      <div class="sub-title">
        <p>Finding existing credentials based on search</p>
      </div>
      <div class="content">
        <div class="search-input-row">
          <div class="key">
            <FeatherSelect
              class="my-select"
              label="Search Key"
            />
          </div>
          <div class="value">
            <FeatherInput
              :modelValue="searchValue"
              @update:modelValue="val => onSearch(val as string)"
              label="Search for credentials"
            >
              <template v-slot:post>
                <FeatherIcon :icon="Search" />
              </template>
            </FeatherInput>
          </div>
        </div>
        <div class="search-row">
          <FeatherButton
            secondary
            data-test="search-button"
          >
            Search
          </FeatherButton>
        </div>
        <div class="table-container">
          <table
            class="data-table"
            aria-label="Events Table"
          >
            <thead>
              <tr>
                <th>Alias</th>
                <th>Key</th>
              </tr>
            </thead>
            <TransitionGroup
              name="data-table"
              tag="tbody"
            >
              <!-- <tr
                v-for="(user, index) in tableRecords"
                :key="index"
              >
                <td>{{ user.alias }}</td>
                <td>{{ user.value }}</td>
              </tr> -->
              <tr
                v-for="(item, index) of filteredResults"
                :key="`${item.alias}-${item.key}-${index}`"
                style="cursor: pointer;"
              >
                <td>
                  {{ item.type === 'alias' ? item.alias : '' }}
                </td>
                <td v-if="item.type === 'key' && item.key">
                  <a @click.prevent="onItemSelected(item)">{{ item.type === 'key' ? item.key : '' }}</a>
                </td>
                <td v-else></td>
              </tr>
            </TransitionGroup>
          </table>
          <div v-if="!tableRecords.length">
            <EmptyList :content="{ msg: 'No credentials found.' }" />
          </div>
        </div>
      </div>
      <div class="footer">
        <FeatherButton
          secondary
          data-test="cancel-button"
          @click="store.closeCredentialDrawer"
        >
          Cancel
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script setup lang="ts">
import EmptyList from '@/components/Common/EmptyList.vue'
import { useScvStore } from '@/stores/scvStore'
import { useTrapConfigStore } from '@/stores/trapConfigStore'
import { ScvSearchItem } from '@/types/scv'
import FeatherButton from '@featherds/button/src/components/FeatherButton.vue'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import Search from '@featherds/icon/action/Search'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect } from '@featherds/select'
import { debounce } from 'lodash'

const emit = defineEmits<{
  (e: 'hidden'): void
  (e: 'item-selected', item: ScvSearchItem): void
}>()

const DEBOUNCE_DELAY = 300
const store = useTrapConfigStore()
const scvStore = useScvStore()
const credentialsLoading = ref(false)
const filteredResults = ref<ScvSearchItem[]>([])
const searchValue = ref<string>('')
const tableRecords = ref<{ alias: string; value: string }[]>([
  { alias: 'credential1', value: 'value1' },
  { alias: 'credential2', value: 'value2' }
])

const onSearch = debounce((query: string) => {
  credentialsLoading.value = true
  searchValue.value = query
  filteredResults.value = scvStore.queryCredentials(query)
  credentialsLoading.value = false
}, DEBOUNCE_DELAY)

const onItemSelected = (item: ScvSearchItem) => {
  emit('item-selected', item)
}

onMounted(() => {
  scvStore.populate()
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.container {
  margin-top: 10px;
  padding: 25px;

  .title {
    padding: 10px;

    h2 {
      @include typography.headline2;
    }
  }

  .sub-title {
    padding: 20px 0px;

    p {
      @include typography.subtitle2;
    }
  }

  .content {
    .search-input-row {
      display: flex;
      gap: 20px;
      padding: 20px 0px;

      .key {
        width: 30%;
      }

      .value {
        width: 70%;
      }
    }

    .table-container {
      table {
        width: 100%;
        padding: 20px 0px;
        @include table.table;

        thead {
          background: var(variables.$background);
          text-transform: uppercase;
        }

        td {
          white-space: nowrap;
          box-shadow: none;
          border-bottom: 1px solid var(variables.$border-on-surface);
        }
      }
    }

    .search-row {
      display: flex;
      justify-content: flex-end;
    }
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    padding-top: 20px;
  }
}
</style>

