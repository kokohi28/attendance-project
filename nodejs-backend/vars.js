module.exports = {
  USE_LOGGER : false,

  ENV : 'dev',
  LOG_LEVEL : 'verbose',

  MAX_DOC_FILE_SIZE : 20971520, // 20 * 1024 * 1024, // 20 MB
}

const mimeType = {
  '.ico'  : 'image/x-icon',
  '.html' : 'text/html',
  '.js'   : 'text/javascript',
  '.json' : 'application/json',
  '.css'  : 'text/css',
  '.png'  : 'image/png',
  '.jpg'  : 'image/jpeg',
  '.wav'  : 'audio/wav',
  '.mp3'  : 'audio/mpeg',
  '.svg'  : 'image/svg+xml',
  '.pdf'  : 'application/pdf',
  '.doc'  : 'application/msword',
  '.eot'  : 'appliaction/vnd.ms-fontobject',
  '.ttf'  : 'application/font-sfnt'
}

module.exports.mimeType = mimeType
