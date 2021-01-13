const express = require('express')
const bodyParser = require('body-parser')
const cors = require('cors')
const respTimeLog = require('./resp-time-logger')

let _globalVars = require('./global-vars')
let mongoConn = require('./mongo-conn')

let log = require('./support/mylogger')

let app = express()
app.use(cors())
app.use(bodyParser.urlencoded({extended: true}))
app.use(express.json())
app.use(respTimeLog)

const {
  // Test
  API_URL_TEST_ADD_USERS,
  API_URL_TEST_ADD_OUTLETS,
  // OK
  API_URL_LOGIN,
  API_URL_GET_USER,
  API_URL_GET_USERS,
  API_URL_UPDATE_PROFILE,
  API_URL_GET_LATEST_ATTENDANCE_IN,
  API_URL_GET_ATTENDANCE_ALL,
  API_URL_GET_ATTENDANCES,
  API_URL_UPLOAD_ATTENDANCE_IN_PHOTO,
  API_URL_GET_ATTENDANCE_IN_PHOTO,
  API_URL_ATTENDANCE_IN,
  API_URL_ATTENDANCE_OUT,
  API_URL_GET_OUTLETS,

  //
  API_URL_TEST,

  // constant
  ATTENDANCE_IN
} = require('./constant')

let testAttendance = require('./controller/test')


mongoConn.connectToServer(function( err, client) {
  if (err) {
    log.e(JSON.stringify(err))
    throw err
  }
  log.i('DB OK, Start Controller')

  let reg = require('./controller/registration')
  let attendance = require('./controller/attendance')

  let fileStorage = require('./file-storage-handle')

  // Data util
  let dataUtil = require('./controller/data-util')

  // *
  // **
  // TEST
  app.post(API_URL_TEST, (req, res) => { testAttendance.postTest(req, res) })
  app.get(API_URL_TEST, (req, res) => { testAttendance.getTest(req, res) })
  // **
  // *


  // *
  // **********************************************************
  // Attendance
  // **********************************************************
  // *
  // Auth
  app.post(API_URL_LOGIN, (req, res) => { reg.login(req, res) })


  // User
  app.post(API_URL_UPDATE_PROFILE, (req, res) => { reg.updateProfile(req, res) })

  app.get(API_URL_GET_USER, (req, res) => { reg.getUser(req, res) })

  app.get(API_URL_GET_USERS, (req, res) => { reg.getUsers(req, res) })


  // Attendance
  app.post(API_URL_ATTENDANCE_IN, (req, res) => { attendance.attendanceIn(req, res) })

  app.post(API_URL_ATTENDANCE_OUT, (req, res) => { attendance.attendanceOut(req, res) })

  app.get(API_URL_GET_LATEST_ATTENDANCE_IN, (req, res) => { attendance.getLatestAttendanceIn(req, res) })

  app.get(API_URL_GET_ATTENDANCE_ALL, (req, res) => { attendance.getAttendanceAll(req, res) })

  app.get(API_URL_GET_ATTENDANCES, (req, res) => { attendance.getAttendances(req, res) })

  app.post(API_URL_UPLOAD_ATTENDANCE_IN_PHOTO, fileStorage.single(ATTENDANCE_IN), (req, res, next) => {
    attendance.updateAttendanceInPhoto(req, res, next)
  })

  app.get(API_URL_GET_OUTLETS, (req, res) => { attendance.getOutlets(req, res) })


  // Public Access
  app.get(API_URL_GET_ATTENDANCE_IN_PHOTO, (req, res) => { attendance.pubGetAttendanceInPhoto(req, res) })


  // *
  // **********************************************************
  // ADMIN - TEST
  // **********************************************************
  // *
  app.post(API_URL_TEST_ADD_USERS, (req, res) => { dataUtil.testAddUsersIfEmpty(req, res) })

  app.post(API_URL_TEST_ADD_OUTLETS, (req, res) => { dataUtil.testAddOutlets(req, res) })


  // Start server
  let apiPort = parseInt(process.env.API_PORT)

  // HTTP
  let server = app.listen(apiPort, function () {
    let port = server.address().port
    log.i('HTTP api on port: ' + port)
  })
  // HTTP ... end

  process.on('SIGTERM', () => {
    log.i('** SIGTERM')
    server.close(() => {
      log.i('************************** TERMINATED ************************** ')
    })
  })

  process.on('SIGINT', () => {
    log.i('** SIGINT')
    server.close(() => {
      log.i('************************** TERMINATED ************************** ')
    })
    process.exit()
  })

  require('./env-parse').parse()

  // Current Time
  let current = new Date()
  _globalVars.startTime = current.getTime()

  log.v('*')
  log.v('*')
  log.v('DATE USED : ' + current)
  log.v('DATE OFFSIDE : ' + current.getTimezoneOffset())
  log.v('STARTED                               ( ._.)v')
  log.v('*')
  log.v('*')
})