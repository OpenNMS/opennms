import { PrintStatement } from '@/types'
import { assert, test } from 'vitest'
import { tokenizeStatement, TOKENS, formatStatement } from '@/components/Resources/utils/LegendFormatter'

test('Tokenizing a statement', () => {
  let tokens = tokenizeStatement('Max  : %8.2lf %s\\n')
  assert.equal(tokens.length, 5)
  assert.equal(tokens[0].type, TOKENS.Text)
  assert.equal(tokens[0].value, 'Max  : ')
  assert.equal(tokens[1].type, TOKENS.Lf)
  assert.equal(tokens[1].length, 8)
  assert.equal(tokens[1].precision, 2)
  assert.equal(tokens[2].type, TOKENS.Text)
  assert.equal(tokens[2].value, ' ')
  assert.equal(tokens[3].type, TOKENS.Unit)
  assert.equal(tokens[4].type, TOKENS.Newline)

  tokens = tokenizeStatement('%10.5lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, 10)
  assert.equal(tokens[0].precision, 5)

  tokens = tokenizeStatement('%.3lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, null)
  assert.equal(tokens[0].precision, 3)

  tokens = tokenizeStatement('%lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, null)
  assert.equal(tokens[0].precision, null)

  tokens = tokenizeStatement('%7lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, 7)
  assert.equal(tokens[0].precision, null)
})

test('Format statement', () => {
  const renderer = {
    texts: <string[]>[],
    drawText: (text: string) => {
      renderer.texts.push(text)
    },
    drawNewline: () => {
      renderer.texts.push('\n')
    }
  }

  const statement: PrintStatement = {
    format: 'Avg: %8.2lf %s\\n',
    metric: 'test',
    value: 1024
  }

  formatStatement(statement, renderer)

  assert.equal(renderer.texts.length, 5)
  assert.equal(renderer.texts[0], ' Avg: ')
  assert.equal(renderer.texts[1].trim(), '1.02k')
  assert.equal(renderer.texts[4], '\n')
})
