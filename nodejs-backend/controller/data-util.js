let log = require('../support/mylogger')
let fs = require('fs')
let uniqid = require('uniqid')
const Long = require('mongodb').Long

/* ********************************************************************************************** */
/* ************************************************** MONGO CONNECTION ************************** */
const mongoConn = require('../mongo-conn')
const mongoIncrement = require("mongodb-autoincrement")


/* ********************************************************************************************** */
/* ************************************************** TEST USER ********************************* */
exports.testAddUsersIfEmpty = (req, res) => {
  let dbAttendance = mongoConn.getDBAttendance()

  let users = dbAttendance.collection('users')

  users.findOne({}, function(err, doc) {
    if (err) throw err

    if (doc == null) {
      let current = new Date()

      let data = fs.readFileSync('./data/users.json')
      let usersJSON = JSON.parse(data.toString())

      usersJSON.forEach((userRaw) => {
        mongoIncrement.getNextSequence(dbAttendance, 'uid', function (err, index) {
          // User
          let uid = uniqid()
          let user = {
            idx : index,
            uid : uid,
            mail : userRaw.mail,
            pwd : userRaw.pwd,
            user_type : userRaw.user_type,
            registered : true,
            created_at : current,
            updated_at : current
          }
          users.insertOne(user)
          // log.d(JSON.stringify(user))

          // Profile
          let profile = {
            uid : uid,
            name : userRaw.name,
            bio : userRaw.bio,
            area : userRaw.area,
            address : userRaw.address,
            phone : userRaw.phone,
            updated_at : current
          }
          dbAttendance.collection('profiles').insertOne(profile)
        })
      })

      log.i('insert User')
    }
  })

  res.end('Processing Test Users…\n')
}

/* ********************************************************************************************** */
/* ************************************************** TEST ADD OUTLETS ************************** */
exports.testAddOutlets = (req, res) => {
  let dbAttendance = mongoConn.getDBAttendance()

  let outlets = dbAttendance.collection('outlets')
  outlets.findOne({}, function(err, doc) {
    if (err) throw err

    if (doc == null) {
      let current = new Date()

      let data = fs.readFileSync('./data/outlets.json')
      let outletJSON = JSON.parse(data.toString())

      outletJSON.forEach((outletRaw) => {
        mongoIncrement.getNextSequence(dbAttendance, 'outlet', function (err, index) {
          let outlet = {
            idx : outletRaw.idx,
            outlet : outletRaw.outlet,
            area : outletRaw.area,
            location : outletRaw.location,
            description : outletRaw.description,
            created_at : current
          }
          outlets.insertOne(outlet)
        })
      })

      log.i('insert Outlet')
    }
  })

  res.end('Processing Outlets…\n')
}

log.i('DATA UTIL')
