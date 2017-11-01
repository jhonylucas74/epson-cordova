
var exec = require('cordova/exec');

var PLUGIN_NAME = 'EPOSPrinter';

var CordovaPlugin = {
  checkStatus: function(port, callback) {
    exec(function(result){
      callback(null, result);
    }, function(error){
      callback(error)
    }, PLUGIN_NAME, 'checkStatus', [port]);
  },
  portDiscovery: function (callback) {
    exec(function (result) {
      callback(null, result)
    },
    function (error) {
      callback(error)
    }, PLUGIN_NAME, 'portDiscovery', []);
  },
  printTest: function(port, callback) {
    exec(function(result){
      callback(null, result);
    }, function(error){
      callback(error)
    }, PLUGIN_NAME, 'printTest', [port]);
  }
};

module.exports = CordovaPlugin;
