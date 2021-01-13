let util = require('./support/util')
let _vars = require('./vars')

// ENV PARSE
exports.parse = () => {
  let env = process.env.ENV
  if (!util.isEmpty(env)) {
    if (env.localeCompare('DEV') == 0) {
      _vars.USE_LOGGER = false

      _vars.ENV = 'dev'
      console.log(`>>> Use ENV DEV`)
    } else {
      _vars.USE_LOGGER = true

      _vars.ENV = 'prod'
      console.log(`>>> Use ENV PRODUCTION`)
    }
  } else {
    _vars.ENV = 'prod'
  }
}
