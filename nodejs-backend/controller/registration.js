let log = require('../support/mylogger')
let util = require('../support/util')
let _const = require('../constant')
let _vars = require('../vars')
let sha1 = require('js-sha1')
let uniqid = require('uniqid')
const Long = require('mongodb').Long

let _resp = require('../enums/api-response')
let _userType = require('../enums/user-type')

let userTypeStr = [
  "Karyawan",
  "HRD"
]

/* ********************************************************************************************** */
/* ************************************************** MONGO CONNECTION ************************** */
const mongoConn = require('../mongo-conn')
const mongoIncrement = require("mongodb-autoincrement")
let dbAttendance = mongoConn.getDBAttendance()


/* ********************************************************************************************** */
/* ************************************************** COMMON ************************************ */

let checkUserCred = (uid, token, callback) => {
  let users = dbAttendance.collection('users')

  users.find({$and:[{uid : uid}, {token : token}]}).sort({idx : -1}).toArray(function(err, docs) {
    if (err) throw err

    if (docs.length > 0) {
      callback(true)
    } else {
      callback(false)
    }
  })
}

let checkEnableApi = (res, tag) => {
  // SERVER FLAG
  return true
}

/* ********************************************************************************************** */
/* ************************************************** LOGIN ************************************* */
exports.login = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'login')) return

  let mail = req.headers[_const.API_PARAM_MAIL]
  let pwd = req.headers[_const.API_PARAM_PWD]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[login] [' + ip +']')
  log.v(`---> PARAM: mail=${mail} pwd=${pwd}`)

  // Sanitize
  if (util.isEmpty(mail) || util.isEmpty(pwd)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! login CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  let users = dbAttendance.collection('users')
  users.findOne({mail : mail}, { projection: {_id : 0, uid : 1, pwd : 1, user_type : 1} }, function(err, doc) {
    if (err) {
      log.e('login : ' + JSON.stringify(err))

      res.end(JSON.stringify(
        { 'code': _resp.DEV_MESSED_QUERY,
          'result' : _resp.DEV_MESSED_QUERY_STR
        })
      )

      throw err
    }

    if (doc != null) {
      if (doc.pwd.localeCompare(pwd) == 0) {
        let current = new Date()
        let tokenNew = util.randomStr(_const.SESSION_KEY_LENGTH) // Generate token

        users.aggregate([
          // Query
          { $match: { uid: doc.uid } },
          // Profile
          { $lookup: { from: 'profiles', localField: 'uid', foreignField: 'uid', as: 'profile' } },
          { $unwind: "$profile" },
          // Fields
          { $project: {
              // User data
              _id : 0, uid : 1, user_type : 1,
              // Profile
              name : "$profile.name", bio : "$profile.bio"
            }
          }
          ]).toArray(function(errProfile, resultProfile) {
          if (errProfile) {
            log.e('queryGetProfile : ' + JSON.stringify(errProfile))

            res.end(JSON.stringify(
              { 'code': _resp.DEV_MESSED_QUERY,
                'result' : _resp.DEV_MESSED_QUERY_STR
              })
            )

            throw errProfile
          }

          // console.log(resultProfile)
          let updateVal = { $set: {token : tokenNew, updated_at : current} }
          users.updateMany({mail : mail}, updateVal, function(errUpdate, resUpdate) {
            if (errUpdate) {
              log.e('updateToken : ' + JSON.stringify(errUpdate))

              res.end(JSON.stringify(
                { 'code': _resp.DEV_MESSED_QUERY,
                  'result' : _resp.DEV_MESSED_QUERY_STR
                })
              )

              throw errUpdate
            }

            log.i(`user ${mail} logged in`)
            res.end(JSON.stringify(
              { 'code': _resp.SUCCESS,
                'uid' : doc.uid,
                'token': tokenNew,
                'name' : resultProfile[0].name,
                'bio' : resultProfile[0].bio,
                'user_type' : doc.user_type,
                'user_type_str' : userTypeStr[doc.user_type]
              })
            )
          })
        })
      } else {
        res.end(JSON.stringify(
          { 'code': _resp.WRONG_PASSWORD,
            'result' : _resp.WRONG_PASSWORD_STR
          })
        )
      }
    } else {
      res.end(JSON.stringify(
        { 'code': _resp.USER_NOT_FOUND,
          'result' : _resp.USER_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** GET USER ********************************** */
exports.getUser = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'getUser')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]
  let fuid = req.headers[_const.API_PARAM_FUID]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[getUser] [' + ip +']')
  log.v(`---> PARAM: uid=${uid} fuid=${fuid}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess) || util.isEmpty(fuid)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getUser CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let users = dbAttendance.collection('users')
      users.aggregate([
        // Query
        { $match: { uid: fuid } },
        // Profile
        { $lookup: { from: 'profiles', localField: 'uid', foreignField: 'uid', as: 'profile' } },
        { $unwind: "$profile" },
        // Fields
        { $project: {
            // User data
            _id : 0, uid : 1, user_type : 1, mail : 1,
            // Profile
            name : "$profile.name", bio : "$profile.bio", area : "$profile.area", address : "$profile.address", phone : "$profile.phone"
          }
        }
        ]).toArray(function(errProfile, resultProfile) {
        if (errProfile) {
          log.e('queryGetProfile : ' + JSON.stringify(errProfile))

          res.end(JSON.stringify(
            { 'code': _resp.DEV_MESSED_QUERY,
              'result' : _resp.DEV_MESSED_QUERY_STR
            })
          )

          throw errProfile
        }

        if (resultProfile.length > 0) {
          res.end(JSON.stringify(
            { 'code': _resp.SUCCESS,
              'uid' : fuid,
              'name' : resultProfile[0].name,
              'bio' : resultProfile[0].bio,
              'user_type' : resultProfile[0].user_type,
              'user_type_str' : userTypeStr[resultProfile[0].user_type],
              'mail' : resultProfile[0].mail,
              'phone' : resultProfile[0].phone,
              'area' : resultProfile[0].area,
              'address' : resultProfile[0].address,
              'ui' : buildUIList(resultProfile[0].user_type)
            })
          )
        } else {
          res.end(JSON.stringify(
            { 'code': _resp.ENTRY_NOT_FOUND,
              'result' : _resp.ENTRY_NOT_FOUND_STR
            })
          )
        }
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getUser TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** UPDATE PROFILE **************************** */
exports.updateProfile = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'updateProfile')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[updateProfile] [' + ip +']')
  log.v(`---> PARAM: uid=${uid}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! updateProfile CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let profiles = dbAttendance.collection('profiles')
      let profileObj = req.body
      let current = new Date()

      // Profile
      let profile = {
        name : profileObj.name,
        bio : profileObj.bio,
        updated_at : current
      }

      let updateVal = { $set: profile }
      profiles.updateOne({ uid : uid }, updateVal, function(errUpdate, resUpdate) {
        if (errUpdate) {
          res.end(JSON.stringify(
            { 'code': _resp.DEV_MESSED_QUERY,
              'result' : _resp.DEV_MESSED_QUERY_STR
            })
          )

          throw errUpdate
        }

        log.i('done update profile')
        res.end(JSON.stringify(
          { 'code': _resp.SUCCESS })
        )
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! updateProfile TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** GET USERS ********************************* */
exports.getUsers = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'getUsers')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]
  let all = req.headers[_const.API_PARAM_ALL]
  let count = req.headers[_const.API_PARAM_COUNT]
  let start = req.headers[_const.API_PARAM_START]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[getUsers] [' + ip +']')
  log.v(`---> PARAM: uid=${uid} all=${all} count=${count} start=${start}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getUsers CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let users = dbAttendance.collection('users')

      users.estimatedDocumentCount({}, function(errCount, docCount) {
        if (errCount) throw errCount

        users.aggregate([
          // Sort
          { $sort: {idx: 1} },
          // Query
          { $match: {
              $and:[{idx : {$gte : parseInt(start)}}, {$nor: [ { user_type : _userType.ADMIN } ]}]
            }
          },
          // Profile
          { $lookup: { from: 'profiles', localField: 'uid', foreignField: 'uid', as: 'profile' } },
          { $unwind: "$profile" },
          // Fields
          { $project: {
              // User data
              _id : 0, uid : 1, idx : 1, user_type : 1, mail : 1, created_at : 1,
              // Profile
              name : "$profile.name", bio : "$profile.bio", area : "$profile.area", address : "$profile.address",
              phone : "$profile.phone", updated_at : "$profile.updated_at",
            }
          },
          // Limit
          { $limit : parseInt(count) },
          ]).toArray(function(errProfile, resultProfile) {
          if (errProfile) {
            log.e('queryGetProfile : ' + JSON.stringify(errProfile))

            res.end(JSON.stringify(
              { 'code': _resp.DEV_MESSED_QUERY,
                'result' : _resp.DEV_MESSED_QUERY_STR
              })
            )

            throw errProfile
          }

          res.end(JSON.stringify(
            { 'code' : _resp.SUCCESS,
              'doc_count' : docCount - 1, // dont count admin user
              'count' : resultProfile.length,
              'data' : resultProfile
            })
          )
        })
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getUsers TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

log.i('REGISTRATION')
