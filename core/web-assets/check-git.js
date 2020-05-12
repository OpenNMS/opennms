#!/usr/bin/env node

const child = require('child_process');

const git = child.execSync('git --version', {
  stdio: ['pipe'],
});

if (git.toString().match(/git version 1\./)) {
  console.log(`Please upgrade your git client to version 2.x or higher: ${git}`);
  console.log('For details, see: https://npm.community/t/npm-install-ignores-version-for-git-ssh-dependency-and-gets-master-instead-when-git-client-is-1-x-x/4473');
  console.log('');
  process.exit(1);
} else {
  console.log(`git good: ${git}`);
}
