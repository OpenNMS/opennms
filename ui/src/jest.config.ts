import type { Config } from '@jest/types'

const config: Config.InitialOptions = {
  preset: 'vite-jest',
  testMatch: ['**/spec/**/*.spec.ts'],
  testEnvironment: 'jest-environment-jsdom'
}

export default config
