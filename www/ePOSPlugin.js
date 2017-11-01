
var exec = require('cordova/exec');

var PLUGIN_NAME = 'ePOSPlugin';

var CordovaPlugin = {
  echo: function(phrase, cb) {
    exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
  },
  getDate: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'getDate', []);
  }
};

module.exports = CordovaPlugin;
