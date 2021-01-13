const XMLHttpRequest = require('xmlhttprequest').XMLHttpRequest
let exec = require('child_process').exec

exports.randomStr = function(length) {
  let result           = ''
  let characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let charactersLength = characters.length
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength))
  }
  return result
}

String.prototype.replaceAt = function(index, replacement) {
  return this.substr(0, index) + replacement + this.substr(index + replacement.length)
}

exports.isEmpty = function(str) {
  return (!str || 0 === str.length)
}

exports.getRandom = function(min, max) {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

exports.mapToObj = function(map) {
  let obj = {}
  map.forEach(function(v, k) {
    obj[k] = v
  })
  return obj
}

exports.dateToStr = function(date) {
  return "" + date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " " +
    date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds()
}

exports.getDateNowStr = function() {
  let date = new Date()
  let year = date.getFullYear().toString().substr(2, 2)
  return "" + year + "/" + (date.getMonth() + 1) + "/" + date.getDate() + " " +
    date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds()
}

exports.getDateOnlyNowStr = function() {
  let date = new Date()
  return "" + date.getFullYear() + "/" + (date.getMonth() + 1) + "/" + date.getDate()
}

exports.formatTimeLapse = function(millisLapse) {
  var d = Math.abs(new Date().getTime() - millisLapse) / 1000 // delta
  var r = {} // result
  var s = {
      year: 31536000,
      // month: 2592000,
      week: 604800,
      day: 86400,
      hour: 3600,
      minute: 60,
      second: 1
  }

  Object.keys(s).forEach(function(key) {
      r[key] = Math.floor(d / s[key])
      d -= r[key] * s[key]
  })

  if (r.year > 0) {
    return `y=${r.year} w=${r.week} d=${r.day} h=${r.hour}`
  } else if (r.week > 0) {
    return `w=${r.week} d=${r.day} h=${r.hour}`
  } else if (r.day > 0) {
    return `d=${r.day} h=${r.hour}`
  } else {
    return `${r.hour}:${r.minute}:${r.second}`
  }
}

exports.dateToStrWTick = function(date) {
  return "'" + date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " " +
    date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "'"
}

exports.replaceAllStr = function(str, find, replace) {
    return str.replace(new RegExp(find, 'g'), replace)
}

exports.httpReq = function(url, method) {
  let xmlHttp = new XMLHttpRequest()
  xmlHttp.open(method, url, false) // false for synchronous request
  xmlHttp.send(null)
  return xmlHttp.responseText
}

exports.formatNumber = function(num) {
  return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,')
}

exports.reverseStr = function(str) {
  return str.split('').reverse().join('')
}

exports.execute = (command, callback) => {
  exec(command, function(error, stdout, stderr) {
    callback(stdout, stderr, error) 
  })
}