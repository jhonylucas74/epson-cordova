
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
  printTest: function(port, texts, callback) {
    exec(function(result){
      callback(null, result);
    }, function(error){
      callback(error)
    }, PLUGIN_NAME, 'printTest', [port, texts]);
  },
  builder: function (options) {
    return new Builder(options);
  }
};


function Builder(options){
    if(!options) options = {};

    this.paperWidth = options.width || 384;
    this.commands = [];

    function error(str){
      throw new Error(str);
    }

    this.text = function(input, style){
      var _style     = style          || {};
      _style.size    = _style.size    || 4;
      _style.color   = _style.color   || 'black';
      _style.font    = _style.font    || 'FONT_A';
      _style.weight  = _style.weight  || 'normal';
      _style.align   = _style.align   || 'left';
      _style.bgcolor = _style.bgcolor || 'white';

      var text = input ? input: '';

      this.commands.push({
        type: 'text',
        text: text,
        style: _style
      });

      return this;
    };

    this.image = function(input, style){
      var _style     = style          || {};
      _style.x       = _style.x       || 0;
      _style.y       = _style.y       || 0;
      _style.width   = _style.width   || 256;
      _style.height  = _style.height  || 256;

      this.commands.push({
        type: 'image',
        image: input,
        x: _style.x,
        y: _style.y,
        width: _style.width,
        height: _style.height
      });

      return this;
    }

    this.openCashDrawer = function(){
      this.commands.push({ type: 'opencash' });
      return this;
    }

    this.cutPaper = function(){
      this.commands.push({ type: 'cutpaper' });
      return this;
    }

    this.print = function(port, callback){
        var args = [this.commands, port];

        exec(function (result) {
            callback(null, result)
        },
        function (error) {
            callback(error)
        }, 'StarIOPlugin', 'print', args);
    }
  }

module.exports = CordovaPlugin;
