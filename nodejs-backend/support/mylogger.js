let util = require('../support/util')

exports.s = (message) => {
  console.log('[' + util.getDateNowStr() + '] [SILLY] ' + message)
}

exports.d = (message) => {
  console.log('[' + util.getDateNowStr() + '] [DEBUG] ' + message)
}

exports.v = (message) => {
  console.log('[' + util.getDateNowStr() + '] [VBOE] ' + message)
}

exports.h = (message) => {
  console.log('[' + util.getDateNowStr() + '] [HTTP] ' + message)
}

exports.i = (message) => {
  console.log('[' + util.getDateNowStr() + '] [INFO] ' + message)
}

exports.w = (message) => {
  console.log('[' + util.getDateNowStr() + '] [WARN] ' + message)
}

exports.e = (message) => {
  console.log('X(')
  console.log('X(')
  console.log('[' + util.getDateNowStr() + '] [ERROR] ' + message)
  console.log('X(')
  console.log('X(')
}
