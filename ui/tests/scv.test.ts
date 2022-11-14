import { mount } from '@vue/test-utils'
import store from '@/store'
import { test, expect } from 'vitest'
import { SCVCredentials } from '@/types/scv'
import SCV from '@/containers/SecureCredentialsVault.vue'

const mockCredentials: SCVCredentials = {
  alias: 'alias',
  username: 'name',
  password: '******',
  attributes: {}
}

const wrapper = mount(SCV, {
  global: {
    plugins: [store]
  }
})

test('adding an alias should enable the add btn', async () => {
  const addCredsBtn = wrapper.get('[data-test="add-creds-btn"]')
  const aliasInput = wrapper.get('[data-test="alias-input"] .feather-input')

  // expect add btn to start disabled
  expect(addCredsBtn.attributes('aria-disabled')).toBe('true')
  // adding a value to alias should enable the add btn
  await aliasInput.setValue('some alias')
  expect(addCredsBtn.attributes('aria-disabled')).toBeUndefined()
})

test('the user may not add a duplicate alias', async () => {
  const addCredsBtn = wrapper.get('[data-test="add-creds-btn"]')
  const aliasInput = wrapper.get('[data-test="alias-input"] .feather-input')

  // add alias1 to the list of current aliases
  store.commit('scvModule/SAVE_ALIASES', ['alias1'])
  // start to create new with alias1
  await aliasInput.setValue('alias1')
  // expect add btn to remain disabled
  expect(addCredsBtn.attributes('aria-disabled')).toBe('true')
  // replace with alias2
  await aliasInput.setValue('alias2')
  // expect add btn to be enabled
  expect(addCredsBtn.attributes('aria-disabled')).toBeUndefined()
})

test('the update btn should appear and be enabled', async () => {
  const updateCreds = wrapper.find('[data-test="update-creds-btn"]')

  // the update btn should not be available
  expect(updateCreds.exists()).toBeFalsy()
  // simulate clicking on an alias to update
  store.commit('scvModule/SAVE_CREDENTIALS', mockCredentials)
  store.commit('scvModule/SET_IS_EDITING', true)
  await nextTick()

  // the update btn should be there, and enabled
  const updateCredsBtn = wrapper.find('[data-test="update-creds-btn"]')
  expect(updateCredsBtn.attributes('aria-disabled')).toBeUndefined()
})

test('if password is masked and username is being updated, prevent submission', async () => {
  const usernameInput = wrapper.get('[data-test="username-input"] .feather-input')
  const passwordInput = wrapper.get('[data-test="password-input"] .feather-input')

  // simulate clicking on an alias to update
  store.commit('scvModule/SAVE_CREDENTIALS', mockCredentials)
  store.commit('scvModule/SET_IS_EDITING', true)
  await nextTick()

  // the update btn should be there, and be enabled
  const updateCredsBtn = wrapper.get('[data-test="update-creds-btn"]')
  expect(updateCredsBtn.attributes('aria-disabled')).toBeUndefined()

  // modify the username
  await usernameInput.setValue('newusername')
  // the update btn should be disabled, because the password is masked
  expect(updateCredsBtn.attributes('aria-disabled')).toBe('true')
  // modify the password
  await passwordInput.setValue('newpassword')
  // the update btn should be enabled
  expect(updateCredsBtn.attributes('aria-disabled')).toBeUndefined()
})

test('the clear btn', async () => {
  const usernameInput = wrapper.get('[data-test="username-input"] .feather-input')
  const passwordInput = wrapper.get('[data-test="password-input"] .feather-input')
  const aliasInput = wrapper.get('[data-test="alias-input"] .feather-input')
  const clearBtn = wrapper.get('[data-test="clear-btn"]')
  await clearBtn.trigger('click')

  // simulate clicking on an alias to update
  store.commit('scvModule/SAVE_CREDENTIALS', mockCredentials)
  store.commit('scvModule/SET_IS_EDITING', true)
  await nextTick()

  // expect form to be populated
  expect((usernameInput.element as any).value).toBe('name')
  expect((passwordInput.element as any).value).toBe('******')
  expect((aliasInput.element as any).value).toBe('alias')

  // clear
  await clearBtn.trigger('click')
  expect((usernameInput.element as any).value).toBe('')
  expect((passwordInput.element as any).value).toBe('')
  expect((aliasInput.element as any).value).toBe('')
})

test('the add and remove attribute btn', async () => {
  const addAttrBtn = wrapper.get('[data-test="add-attr-btn"]')
  let attrKeyInput = wrapper.find('[data-test="attr-key"]')
  let attrValueInput = wrapper.find('[data-test="attr-value"]')

  // form starts off without attribute inputs
  expect(attrKeyInput.exists()).toBeFalsy()
  expect(attrValueInput.exists()).toBeFalsy()

  // click add attributes btn
  await addAttrBtn.trigger('click')

  // try to find attr inputs, expect they render correctly
  attrKeyInput = wrapper.find('[data-test="attr-key"]')
  attrValueInput = wrapper.find('[data-test="attr-value"]')
  expect(attrKeyInput.exists()).toBeTruthy()
  expect(attrValueInput.exists()).toBeTruthy()

  // click remove attributes btn
  const rmAttrBtn = wrapper.get('[data-test="rm-attr-btn"]')
  await rmAttrBtn.trigger('click')

  // try to find attr inputs, expect they no longer render
  attrKeyInput = wrapper.find('[data-test="attr-key"]')
  attrValueInput = wrapper.find('[data-test="attr-value"]')
  expect(attrKeyInput.exists()).toBeFalsy()
  expect(attrValueInput.exists()).toBeFalsy()
})
