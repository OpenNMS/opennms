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
            <FeatherSelect
              class="my-select"
              label="Key Type"
              clear="Clear Selection"
            >
              <template v-slot:pre>
                <FeatherIcon :icon="Search" />
              </template>
            </FeatherSelect>
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
              <tr
                v-for="(user, index) in tableRecords"
                :key="index"
              >
                <td>{{ user.alias }}</td>
                <td>{{ user.value }}</td>
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
import { useTrapConfigStore } from '@/stores/trapConfigStore'
import FeatherButton from '@featherds/button/src/components/FeatherButton.vue'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import Search from '@featherds/icon/action/Search'
import { FeatherSelect } from '@featherds/select'
import EmptyList from '../Common/EmptyList.vue'

const store = useTrapConfigStore()
const tableRecords = ref<{ alias: string; value: string }[]>([
  { alias: 'credential1', value: 'value1' },
  { alias: 'credential2', value: 'value2' }
])
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

