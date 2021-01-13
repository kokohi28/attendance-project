const API_ROOT = '/api/v1'

module.exports = {
  // Header params // DON'T USE UNDERSCORE
  API_PARAM_EMAIL : 'email',
  API_PARAM_MAIL : 'mail',
  API_PARAM_ID : 'id',
  API_PARAM_IDX : 'idx',
  API_PARAM_UID : 'uid',
  API_PARAM_FUID : 'fuid',
  API_PARAM_TOKEN : 'token',
  API_PARAM_CODE : 'code',
  API_PARAM_PWD : 'pwd',
  API_PARAM_OLD_PWD : 'oldpwd',
  API_PARAM_ACT : 'act',
  API_PARAM_ALL : 'all',
  API_PARAM_USER : 'user',
  API_PARAM_COUNT : 'count',
  API_PARAM_NAME : 'name',
  API_PARAM_TYPE : 'type',
  API_PARAM_KEY : 'key',
  API_PARAM_BIO : 'bio',
  API_PARAM_TS : 'ts',
  API_PARAM_STATUS : 'status',
  API_PARAM_SEQ : 'seq',
  API_PARAM_STATE : 'state',
  API_PARAM_DURATION : 'dur',
  API_PARAM_START : 'start',
  API_PARAM_FIRST : 'first',
  API_PARAM_END : 'end',
  API_PARAM_YEAR : 'year',
  API_PARAM_MONTH : 'month',
  API_PARAM_DAY : 'day',
  API_PARAM_COMPRESS : 'compress',
  API_PARAM_SHIFT : 'shift',
  API_PARAM_OUTLET : 'outlet',
  API_PARAM_EXT : 'ext',

  ATTENDANCE_IN : 'attdc_in',

  // TEST
  API_URL_TEST : API_ROOT + '/test', //

  // USER
  API_URL_GET_USER : API_ROOT + '/getUser',
  API_URL_GET_USERS : API_ROOT + '/getUsers',
  API_URL_UPDATE_PROFILE : API_ROOT + '/updateProfile',

  // SESSION
  API_URL_LOGIN : API_ROOT + '/login', //

  // ATTENDANCE
  API_URL_GET_LATEST_ATTENDANCE_IN : API_ROOT + '/getLatestAttendanceIn', //
  API_URL_GET_ATTENDANCE_ALL : API_ROOT + '/getAttendanceAll', //
  API_URL_GET_ATTENDANCES : API_ROOT + '/getAttendances',
  API_URL_ATTENDANCE_IN : API_ROOT + '/setAttendanceIn', //
  API_URL_ATTENDANCE_OUT : API_ROOT + '/setAttendanceOut', //
  API_URL_UPLOAD_ATTENDANCE_IN_PHOTO : API_ROOT + '/uploadInPhoto', //
  API_URL_GET_ATTENDANCE_IN_PHOTO : API_ROOT + '/getInPhoto', //

  API_URL_GET_OUTLETS : API_ROOT + '/getOutlets', //

  // ADMIN - TESTING
  API_URL_TEST_ADD_USERS : API_ROOT + '/testAddUsers', // ADM-TEST
  API_URL_TEST_ADD_OUTLETS : API_ROOT + '/testAddOutlets', // ADM-TEST

  SESSION_KEY_LENGTH : 60
}
