let log = require('./support/mylogger')

function respTimeLog(req, res, next) {
  const startHrTime = process.hrtime()

  res.on("finish", () => {
    const elapsedHrTime = process.hrtime(startHrTime)
    const elapsedTimeInMs = elapsedHrTime[0] * 1000 + elapsedHrTime[1] / 1e6 
    log.h(`..................... [TIME MEASURE] ${req.path} : ${elapsedTimeInMs}ms`)
  })

  next()
}

module.exports = respTimeLog