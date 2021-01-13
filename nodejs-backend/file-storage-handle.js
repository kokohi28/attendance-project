let multer = require('multer')
let _const = require('./constant')

// Set Storage handle
let storage = multer.diskStorage({
  fileFilter: function (req, file, cb) {
    // TODO file MIME check
    cb(null, true)
  },
  destination: function (req, file, cb) {
    // File
    if (file.fieldname.localeCompare(_const.ATTENDANCE_IN) == 0) {
      cb(null, './files-in/')
    } else {
      cb(null, './files')
    }
  },
  filename: function (req, file, cb) {
    cb(null, file.fieldname + '-' + Date.now())
  }
})

let upload = multer({ storage: storage })

module.exports = upload
