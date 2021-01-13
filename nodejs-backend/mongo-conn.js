const mongoClient = require('mongodb').MongoClient
const url = 'mongodb://localhost:27017'
const schemaAttendance = 'attendance'

var dbAttendance

module.exports = {
  connectToServer : function( callback ) {
    mongoClient.connect(url, { useNewUrlParser : true, useUnifiedTopology: true }, function(err, client) {
      dbAttendance = client.db(schemaAttendance)

      return callback(err)
    })
  },

  getDBAttendance : function() {
    return dbAttendance
  }
}