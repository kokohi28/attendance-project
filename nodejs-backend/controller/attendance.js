let log = require('../support/mylogger')
let util = require('../support/util')
let _const = require('../constant')
let _vars = require('../vars')
let uniqid = require('uniqid')
let fs = require('fs')
let compessImage = require('compress-images')
let path = require('path')
const Long = require('mongodb').Long

let _resp = require('../enums/api-response')
let _attdcType = require('../enums/attendance-type')

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
/* ************************************************** ATTENDANCE IN ***************************** */
exports.attendanceIn = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'attendanceIn')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[attendanceIn] [' + ip +']')
  log.v(`---> PARAM: uid=${uid}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! attendanceIn CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let attendances = dbAttendance.collection('attendances')
      let attendanceObj = req.body

      mongoIncrement.getNextSequence(dbAttendance, 'attendance', function (err, index) {
        let current = new Date()
        let id = uniqid()

        let attendance = {
          idx : index,
          id : id,
          uid : uid,
          type : attendanceObj.type,
          outlet : attendanceObj.outlet,
          shift : attendanceObj.shift,
          completed : false,
          created_at : current,
        }
        attendances.insertOne(attendance)
        log.d(`Attendance: ${JSON.stringify(attendance)} inserted!`)

        res.end(JSON.stringify(
          { 'code': _resp.SUCCESS,
            'id' : id,
            'idx' : index
          })
        )
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! attendanceIn TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** ATTENDANCE OUT ***************************** */
exports.attendanceOut = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'attendanceOut')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[attendanceOut] [' + ip +']')
  log.v(`---> PARAM: uid=${uid}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! attendanceOut CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let attendances = dbAttendance.collection('attendances')
      let attendanceObj = req.body

      attendances.findOne({ id : attendanceObj.ref }, { projection: {_id : 0, created_at : 1} }, function(errAttdc, docAttdc) {
        if (errAttdc) {
          log.e('getAttdc : ' + JSON.stringify(errAttdc))

          res.end(JSON.stringify(
            { 'code': _resp.DEV_MESSED_QUERY,
              'result' : _resp.DEV_MESSED_QUERY_STR
            })
          )

          throw errAttdc
        }

        if (docAttdc != null) {
          mongoIncrement.getNextSequence(dbAttendance, 'attendance', function (err, index) {
            let current = new Date()
            let id = uniqid()

            let attendance = {
              idx : index,
              id : id,
              uid : uid,
              in_at : docAttdc.created_at,
              income: Long.fromNumber(attendanceObj.income),
              ref : attendanceObj.ref,
              type : attendanceObj.type,
              outlet : attendanceObj.outlet,
              shift : attendanceObj.shift,
              created_at : current,
            }
            attendances.insertOne(attendance)
            log.d(`Attendance: ${JSON.stringify(attendance)} inserted!`)

            let inVal = {
              income: Long.fromNumber(attendanceObj.income),
              completed : true,
              updated_at : current
            }

            let updateVal = { $set: inVal }
            attendances.updateOne({ id : attendanceObj.ref }, updateVal, function(errUpdate, resUpdate) {
              if (errUpdate) {
                res.end(JSON.stringify(
                  { 'code': _resp.DEV_MESSED_QUERY,
                    'result' : _resp.DEV_MESSED_QUERY_STR
                  })
                )

                throw errUpdate
              }

              log.i('done update in attendance')
              res.end(JSON.stringify(
                { 'code': _resp.SUCCESS,
                  'id' : id,
                  'idx' : index
                })
              )
            })
          })
        } else {
          res.end(JSON.stringify(
            { 'code': _resp.ENTRY_NOT_FOUND,
              'result' : _resp.ENTRY_NOT_FOUND_STR
            })
          )
        }
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! attendanceOut TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** GET LATEST ATTENDANCE IN ****************** */
exports.getLatestAttendanceIn = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'getLatestAttendanceIn')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[getLatestAttendanceIn] [' + ip +']')
  log.v(`---> PARAM: uid=${uid}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getLatestAttendanceIn CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let attendances = dbAttendance.collection('attendances')
      attendances.aggregate([
        // Query
        { $match: {
            $and: [ {uid : uid}, {type : _attdcType.IN}, {completed : false} ]
          }
        },
        // Fields
        { $project: {
            _id : 0, idx : 1, id : 1, uid : 1, outlet : 1, shift : 1, created_at : 1
          }
        },
        // Sort
        {$sort: {idx: -1}},
        // Limit
        { $limit : 1 }
      ]).toArray(function(errAttdc, resultAttdc) {
        if (errAttdc) {
          log.e('queryGetAttendance : ' + JSON.stringify(errAttdc))

          res.end(JSON.stringify(
            { 'code': _resp.DEV_MESSED_QUERY,
              'result' : _resp.DEV_MESSED_QUERY_STR
            })
          )

          throw errAttdc
        }

        if (resultAttdc.length > 0) {
          let outlets = dbAttendance.collection('outlets')
          outlets.findOne({idx: resultAttdc[0].outlet}, { projection: {_id : 0, outlet : 1, area : 1} }, function(errOutlet, docOutlet) {
            if (errOutlet) {
              log.e('getOutlet : ' + JSON.stringify(errOutlet))

              res.end(JSON.stringify(
                { 'code': _resp.DEV_MESSED_QUERY,
                  'result' : _resp.DEV_MESSED_QUERY_STR
                })
              )

              throw errOutlet
            }

            let resAttdc = resultAttdc[0]
            resAttdc.outlet_name = docOutlet.outlet
            resAttdc.outlet_area = docOutlet.area

            if (docOutlet != null) {
              res.end(JSON.stringify(
                { 'code': _resp.SUCCESS,
                  'data' : resAttdc
                })
              )
            } else {
              res.end(JSON.stringify(
                { 'code': _resp.SUCCESS,
                  'data' : resultAttdc[0]
                })
              )
            }
          })
        } else {
          res.end(JSON.stringify(
            { 'code': _resp.ENTRY_NOT_FOUND,
              'result' : _resp.ENTRY_NOT_FOUND_STR
            })
          )
        }
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getLatestAttendanceIn TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** GET ATTENDANCE ALL ************************ */
exports.getAttendanceAll = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'getAttendanceAll')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]
  let user = req.headers[_const.API_PARAM_USER]
  let first = req.headers[_const.API_PARAM_FIRST]
  let start = req.headers[_const.API_PARAM_START]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[getAttendanceAll] [' + ip +']')
  log.v(`---> PARAM: uid=${uid} user=${user} first=${first} start=${start}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getAttendanceAll CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let attendances = dbAttendance.collection('attendances')

      let queryObj = { uid : 'xxx' } // give no result
      if (!util.isEmpty(user)) {
        if (user.localeCompare('all') == 0) {
          // queryObj = {$or:[ {type : _attdcType.OUT}, { completed : false}]}
          if (parseInt(first) > 0) {
            queryObj = {$and:[
              { $or:[ {type : _attdcType.OUT}, { completed : false}] }
            ]}
          } else {
            queryObj = {$and:[
              { $or:[ {type : _attdcType.OUT}, { completed : false}] },
              { idx : {$lte : parseInt(start) + 1} }
            ]}
          }
        } else if (user.localeCompare(uid) == 0) {
          if (parseInt(first) > 0) {
            queryObj = {$and:[
              { uid : uid }
            ]}
          } else {
            queryObj = {$and:[
              { uid : uid },
              { idx : {$lte : parseInt(start) + 1} }
            ]}
          }

          // queryObj = { uid : uid }
        }
      }

      attendances.estimatedDocumentCount({}, function(errCount, docCount) {
        if (errCount) throw errCount

        attendances.aggregate([
          // Sort
          { $sort: {created_at : -1} },
          // Query
          { $match: queryObj },
          // Profile
          { $lookup: { from: 'profiles', localField: 'uid', foreignField: 'uid', as: 'profile' } },
          { $unwind: "$profile" },
          // Outlet
          { $lookup: { from: 'outlets', localField: 'outlet', foreignField: 'idx', as: 'outlets' } },
          { $unwind: "$outlets" },
          // Fields
          { $project: {
              // User data
              _id : 0, idx : 1, id : 1, uid : 1, type : 1, outlet : 1, shift : 1,
              ref : 1, in_at : 1, income : 1, created_at : 1, updated_at : 1,
              // Profile
              name : "$profile.name",
              // Ourlet
              area : "$outlets.area", outlet_name : "$outlets.outlet"
            }
          },
          ]).toArray(function(errAttdcs, resAttdcs) {
          if (errAttdcs) {
            log.e('queryAttendance : ' + JSON.stringify(errAttdcs))

            res.end(JSON.stringify(
              { 'code': _resp.DEV_MESSED_QUERY,
                'result' : _resp.DEV_MESSED_QUERY_STR
              })
            )

            throw errAttdcs
          }

          if (resAttdcs.length > 0) {
            res.end(JSON.stringify(
              { 'code': _resp.SUCCESS,
                'doc_count' : docCount,
                'count' : resAttdcs.length,
                'data' : resAttdcs
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
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getAttendanceAll TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** GET ATTENDANCES *************************** */
// TODO WIP
exports.getAttendances = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'getAttendances')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]
  let user = req.headers[_const.API_PARAM_USER]
  let year = req.headers[_const.API_PARAM_YEAR]
  let month = req.headers[_const.API_PARAM_MONTH]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[getAttendances] [' + ip +']')
  log.v(`---> PARAM: uid=${uid} user=${user} year=${year} month=${month}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getAttendances CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let attendances = dbAttendance.collection('attendances')

      let queryObj = { uid : 'xxx' } // give no result
      if (!util.isEmpty(user)) {
        if (user.localeCompare('all') == 0) {
          queryObj = {$or:[ {type : _attdcType.OUT}, { completed : false}]}
        } else {
          queryObj = { uid : user }
        }
      }

      attendances.aggregate([
        // Query
        { $match: queryObj },
        // Fields
        { $project: {
            _id : 0, idx : 1, id : 1, uid : 1, type : 1, outlet : 1, shift : 1,
            ref : 1, in_at : 1, income : 1, created_at : 1, updated_at : 1
          }
        },
        // Sort
        {$sort: {created_at : -1}},
        // Limit
        {$limit : _vars.LIMIT_GET }
      ]).toArray(function(err, docs) {
        if (err) throw err

        log.i(JSON.stringify(docs, null, 2))

        if (docs.length > 0) {
          res.end(JSON.stringify(
            { 'code': _resp.SUCCESS,
              'count' : docs.length,
              'data' : docs
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
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getAttendances TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** GET OUTLETS ******************************* */
exports.getOutlets = function(req, res) {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'getOutlets')) return

  let uid = req.headers[_const.API_PARAM_UID]
  let sess = req.headers[_const.API_PARAM_TOKEN]

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[getOutlets] [' + ip +']')
  log.v(`---> PARAM: uid=${uid}`)

  // Sanitize
  if (util.isEmpty(uid) || util.isEmpty(sess)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getOutlets CREDENTIAL_EMPTY')

    res.end(JSON.stringify(
      { 'code': _resp.CREDENTIAL_EMPTY,
        'result' : _resp.CREDENTIAL_EMPTY_STR
      })
    )

    return
  }

  checkUserCred(uid, sess, (valid) => {
    if (valid) {
      let outlets = dbAttendance.collection('outlets')

      outlets.estimatedDocumentCount({}, function(errCount, docCount) {
        if (errCount) throw errCount

        outlets.aggregate([
          // Fields
          { $project: {
              _id : 0, idx : 1, outlet : 1, area : 1, location : 1, description : 1, created_at : 1
            }
          },
          // Sort
          {$sort: {area: 1, outlet: 1}}
        ]).toArray(function(errLoc, resLoc) {
          if (errLoc) {
            log.e('queryGetOutlet : ' + JSON.stringify(errLoc))

            res.end(JSON.stringify(
              { 'code': _resp.DEV_MESSED_QUERY,
                'result' : _resp.DEV_MESSED_QUERY_STR
              })
            )

            throw errLoc
          }

          if (resLoc.length > 0) {
            res.end(JSON.stringify(
              { 'code': _resp.SUCCESS,
                'doc_count' : docCount,
                'count' : resLoc.length,
                'data' : resLoc
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
      })
    } else {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! getOutlets TOKEN_NOT_FOUND')
      res.end(JSON.stringify(
        { 'code': _resp.TOKEN_NOT_FOUND,
          'result' : _resp.TOKEN_NOT_FOUND_STR
        })
      )
    }
  })
}

/* ********************************************************************************************** */
/* ************************************************** UPDATE ATTENDANCE IN PHOTO **************** */
exports.updateAttendanceInPhoto = (req, res, next) => {
  res.setHeader('Content-Type', 'application/json')
  if (!checkEnableApi(res, 'updateAttendanceInPhoto')) return

  const file = req.file
  // console.log(file)
  if (!file) {
    log.v('updateAttendanceInPhoto NO PIC FILE!')
    res.end(JSON.stringify(
      { 'code': _resp.FILE_NOT_FOUND,
        'result' : _resp.FILE_NOT_FOUND_STR
      })
    )
  } else {
    let uid = req.headers[_const.API_PARAM_UID]
    let sess = req.headers[_const.API_PARAM_TOKEN]
    let type = req.headers[_const.API_PARAM_TYPE]
    let shift = req.headers[_const.API_PARAM_SHIFT]
    let outlet = req.headers[_const.API_PARAM_OUTLET]
    let year = req.headers[_const.API_PARAM_YEAR]
    let month = req.headers[_const.API_PARAM_MONTH]
    let day = req.headers[_const.API_PARAM_DAY]
    let compress = req.headers[_const.API_PARAM_COMPRESS] || 1

    let ip = req.headers['x-real-api'] || req.connection.remoteAddress
    log.h('[updateAttendanceInPhoto] [' + ip +']')
    log.v(`---> PARAM: uid=${uid} compress=${compress} outlet=${outlet} type=${type} shift=${shift} year=${year} month=${month} day=${day}`)

    // Size check
    if (file.size >= _vars.MAX_PP_FILE_SIZE) {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! updateAttendanceInPhoto FILE_SIZE_TOO_BIG')

      fs.unlink(file.path, (err) => {
        if (err) throw err
        log.v(file.path + ' deleted!')
      })

      res.end(JSON.stringify(
        { 'code': _resp.FILE_SIZE_TOO_BIG,
          'result' : _resp.FILE_SIZE_TOO_BIG_STR
        })
      )

      return
    }

    // Sanitize
    if (util.isEmpty(uid) || util.isEmpty(sess) || util.isEmpty(type) || util.isEmpty(shift)
      || util.isEmpty(outlet) || util.isEmpty(year) || util.isEmpty(month) || util.isEmpty(day)) {
      log.v('!!!!!!!!!!!!!!!!!!!!!!!!! updateAttendanceInPhoto CREDENTIAL_EMPTY')

      fs.unlink(file.path, (err) => {
        if (err) throw err
        log.v(file.path + ' deleted!')
      })

      res.end(JSON.stringify(
        { 'code': _resp.CREDENTIAL_EMPTY,
          'result' : _resp.CREDENTIAL_EMPTY_STR
        })
      )

      return
    }

    checkUserCred(uid, sess, (valid) => {
      if (valid) {
        let fName = `${year}_${month}_${day}_${outlet}_${type}_${shift}.jpg`
        let newFile = `./files-in/${fName}`
        fs.rename(file.path, newFile, function(err) {
          if (err) log.v('ERROR: ' + err)

          if (parseInt(compress) > 0) {
            log.i('Try compress image')
            let fileMini = `./files-in/COMPRESS/${fName}`
            try {
              if (fs.existsSync(fileMini)) {
                fs.unlink(fileMini, (errUnlink) => {
                  if (errUnlink) throw errUnlink
                })
                // log.d('delete old one')
              }
            } catch (errCheckFile) {
              log.e(errCheckFile)
            }

            compessImage(newFile, './files-in/COMPRESS/',
              {compress_force: false, statistic: true, autoupdate: true}, false,
              {jpg: {engine: 'mozjpeg', command: ['-quality', '75']}},
              {png: {engine: 'pngquant', command: ['--quality=50-70']}},
              {svg: {engine: 'svgo', command: '--multipass'}},
              {gif: {engine: 'gifsicle', command: ['--colors', '64', '--use-col=web']}},
              function (err, completed) {
                if (err) log.v('ERROR COMPRESS: ' + err)

                if (completed === true) {
                  fs.rename(fileMini, newFile, function(errRename) {
                    if (errRename) log.v('ERROR: ' + errRename)
                    log.v('COMPRESSED-MOVED')
                  })
                }
              }
            )
          }
        })

        log.i('Success upload')
        res.end(JSON.stringify(
          { 'code': _resp.SUCCESS })
        )
      } else {
        log.v('!!!!!!!!!!!!!!!!!!!!!!!!! updateAttendanceInPhoto TOKEN_NOT_FOUND')

        fs.unlink(file.path, (err) => {
          if (err) throw err
          log.v(file.path + ' deleted!')
        })

        res.end(JSON.stringify(
          { 'code': _resp.TOKEN_NOT_FOUND,
            'result' : _resp.TOKEN_NOT_FOUND_STR
          })
        )
      }
    })
  }
}

/* ********************************************************************************************** */
/* ************************************************** PUBLIC GET ATTENDANCE IN PHOTO ************ */
// ex: http://localhost:6004/api/v1/getAttdcInPhoto?d=2020-12-25&o=2&t=0&s=1
exports.pubGetAttendanceInPhoto = (req, res) => {
  res.setHeader('Content-Type', 'application/json')

  let date = req.query.d
  let outlet = req.query.o
  let type = req.query.t
  let shift = req.query.s

  let ip = req.headers['x-real-api'] || req.connection.remoteAddress
  log.h('[pubGetAttendanceInPhoto] [' + ip +']')
  log.v(`---> PARAM: date=${date} outlet=${outlet} type=${type} shift=${shift}`)

  // Sanitize
  if (util.isEmpty(date) || util.isEmpty(outlet) || util.isEmpty(type) || util.isEmpty(shift)) {
    log.v('!!!!!!!!!!!!!!!!!!!!!!!!! pubGetAttendanceInPhoto CREDENTIAL_EMPTY')
    res.end(null)

    return
  }

  let dateParse = new Date(date)
  if (dateParse.toString() === 'Invalid Date') {
    res.end(null)
  } else {
    let fName = `${dateParse.getFullYear()}_${dateParse.getMonth() + 1}_${dateParse.getDate()}_${outlet}_${type}_${shift}.jpg`
    let pic = './files-in/' + fName
    fs.readFile(pic, (err, data) => {
      if (err) {
        log.v(`!!!!!!!!!!!!!!!!!!!!!!!!! pubGetAttendanceInPhoto FILE_NOT_FOUND, f:${fName}`)
        res.end(null)
      } else {
        let ext = path.parse(pic).ext
        res.setHeader('Content-type', _vars.mimeType[ext] || 'text/plain')
        res.end(data)
      }
    })
  }
}

log.i('ATTENDANCE')
