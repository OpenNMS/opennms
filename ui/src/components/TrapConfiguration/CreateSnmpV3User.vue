<template>
  <TableCard
    class="snmpv3-user-management-container"
    data-test="create-snmpv3-user"
    v-if="store.createUserDrawerState.visible"
  >
    <div class="header">
      <div class="section-left">
        <div class="title">
          <FeatherButton
            icon="Back"
            data-test="text-button"
            @click="store.closeCreateUserDrawer"
          >
            <FeatherIcon :icon="ChevronLeft"> </FeatherIcon>
          </FeatherButton>
          <h3>New SNMPv3 User Management</h3>
        </div>
      </div>
    </div>
    <div class="content">
      <div class="username-version-row">
        <div class="left">
          <FeatherSelect
            label="Username"
            :options="[
            // { label: 'MD5', value: 'MD5' },
            // { label: 'SHA', value: 'SHA' },
          ]"
          />
        </div>
        <div class="right">
          <FeatherSelect
            label="Version"
            :options="[
            // { label: 'DES', value: 'DES' },
            // { label: 'AES', value: 'AES' },
          ]"
          />
        </div>
      </div>
      <div class="row">
        <h1>Credential properties</h1>
      </div>
      <div class="properties-row">
        <div class="left">
          <FeatherSelect
            label="Security Level"
            :options="[
            // { label: 'MD5', value: 'MD5' },
            // { label: 'SHA', value: 'SHA' },
          ]"
          />
        </div>
        <div class="right"></div>
      </div>
      <div class="row">
        <div class="left">
          <FeatherSelect
            label="Auth Protocol"
            :options="[
            // { label: 'MD5', value: 'MD5' },
            // { label: 'SHA', value: 'SHA' },
          ]"
          />
        </div>
        <div class="right">
          <FeatherSelect
            label="Auth Passphrase"
            :options="[
            // { label: 'DES', value: 'DES' },
            // { label: 'AES', value: 'AES' },
          ]"
          />
          <FeatherButton
            icon="Save"
            data-test="text-button"
            @click="store.openCredentialDrawer"
          >
            <FeatherIcon :icon="Security"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
      <div class="row">
        <div class="left">
          <FeatherSelect
            label="Privacy Protocol"
            :options="[
            // { label: 'MD5', value: 'MD5' },
            // { label: 'SHA', value: 'SHA' },
          ]"
          />
        </div>
        <div class="right">
          <FeatherSelect
            label="Privacy Passphrase"
            :options="[
            // { label: 'DES', value: 'DES' },
            // { label: 'AES', value: 'AES' },
          ]"
          />
          <FeatherButton
            icon="Save"
            data-test="text-button"
            @click="store.openCredentialDrawer"
          >
            <FeatherIcon :icon="Security"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="footer">
      <FeatherButton
        secondary
        data-test="text-button"
        @click="store.closeCreateUserDrawer"
      >
        Cancel
      </FeatherButton>
      <FeatherButton
        primary
        data-test="text-button"
        @click="createUser"
      >
        Create New User
      </FeatherButton>
    </div>
    <SearchExistingCredential />
  </TableCard>
</template>

<script setup lang="ts">
import { useTrapConfigStore } from '@/stores/trapConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Security from '@featherds/icon/hardware/Security'
import ChevronLeft from '@featherds/icon/navigation/ChevronLeft'
import { FeatherSelect } from '@featherds/select'
import TableCard from '../Common/TableCard.vue'
import SearchExistingCredential from './SearchExistingCredential.vue'

const store = useTrapConfigStore()

const createUser = () => {
  // Logic to create SNMPv3 user goes here
  console.log('Creating SNMPv3 user...')  
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.snmpv3-user-management-container {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      .title {
        display: flex;
        align-items: center;
        gap: 10px;

        h3 {
          @include typography.headline3;
          color: var(--feather-text-primary);
        }
      }
    }
  }

  .content {
    .username-version-row {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      width: 50%;

      .left {
        width: 70%;
      }

      .right {
        width: 30%;
      }
    }

    .row {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      width: 50%;

      h1 {
        @include typography.headline4;
        color: var(--feather-text-primary);
      }

      div {
        flex: 1;
      }

      .right {
        display: flex;
        align-items: flex-start;
        gap: 10px;
      }
    }

    .properties-row {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      width: 50%;

      div {
        flex: 1;
      }
    }
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>

