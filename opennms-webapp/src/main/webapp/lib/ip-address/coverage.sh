#!/usr/bin/env sh

./node_modules/.bin/istanbul cover ./node_modules/mocha/bin/_mocha \
  -- -R spec && \
  cat ./coverage/coverage.json |\
  ./node_modules/codecov.io/bin/codecov.io.js && \
  rm -rf .coverage
