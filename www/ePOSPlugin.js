
var exec = require('cordova/exec');

var PLUGIN_NAME = 'ePOSPlugin';

var CordovaPlugin = {
  checkStatus: function(port, callback) {
    exec(function(result){
      callback(null, result);
    }, function(error){
      callback(error)
    }, PLUGIN_NAME, 'checkStatus', [port]);
  }
};

module.exports = CordovaPlugin;
