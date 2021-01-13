let sha256 = require('sha256')
let log = require('../support/mylogger')

/* ********************************************************************************************** */
exports.postTest = function(req, res) {
  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[test POST] [' + ip +']')
  res.send(`CVSU : ${sha256(new Date().getTime().toString())}'\n`)
}

/* ********************************************************************************************** */
exports.getTest = function(req, res) {
  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[test GET] [' + ip +']')
  res.send(`CVSU : GET\n`)
}
