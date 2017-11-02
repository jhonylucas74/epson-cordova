# ePOS Printer Plugin

Cordova plugin for ePOS-Print. Only works with Android.


How to use :


* Install the plugin: `cordova plugin add https://github.com/jhonylucas74/epson-cordova`

If this not work. Try install plugin with ``--nofetch`` flag.

## API

### Printer discovery

Actualy, only LAN are suported.

```
window.plugins.EPOSPrinter.portDiscovery(function(error, printerList){
  if (error) {
    console.error(error);
  } else {
    console.log(printerList[0].name);
    console.log(printerList[0].macAddress);
  }
});
```


### Check Printer status

The format of port is ``192.168.192.168``.

```
window.plugins.EPOSPrinter.checkStatus(port, function(error, result){
  if (error) {
    console.error(error);
  } else {
    console.log(result.offline ? "printer is offline : "printer is online);
  }
});
```

# Print receipt
## Builder

to print you need use ``Builder``.

To print a receipt you need create a builder with this code:
```
  var builder = window.plugins.starPrinter.Builder({ width: 384 });
```

The width represent the paper width. With the instance of builder is possible now add commands.

```
  builder.text("Hello world", {});
```
In example a text command was added in pipeline. the second argument is a object that represent a style of text. Below follow all the possible values.

* ``size``  : ``int`` | size of text, default is 4.
* ``font``  : ``string`` |  font: ``FONT_A``, ``FONT_B``,``FONT_C``,``FONT_D``,``FONT_E``.
* ``weight``  : ``string`` | weight of text: ``bold`` or ``normal``.
* ``align``  : ``string`` | align of text: ``center``, ``right`` or ``left``.

Example with default configuration of style.

```
  builder.text("Hello world", {
    size: 4,
    font: 'FONT_A',
    weight: 'normal',
    align: 'left',
  });
```

Full example with 3 lines:

```
  window.plugins.starPrinter.Builder({ width: 384 })
     .text("Hello world", {})
     .text("This is a example", {})
     .text("Say, good bye!", {})
     .cutPaper()
     .print(portName, function(err, result){
       if (err) return console.log(err);
       // code here...
     });
```

Example with shared style:


```
  var myStyle = {
    size: 8,
    weight: 'bold',
    align: 'center'
  };

  window.plugins.starPrinter.Builder({ width: 384 })
     .text("Title in center", myStyle)
     .text("I'm center too", myStyle)
     .text("all is center", myStyle)
     .cutPaper()
     .print(portName, function(err, result){
       if (err) return console.log(err);
       // code here...
     });
```

### Other functions

### Image
Add a image to builder. The first input must be a base64 encoded image. 
```
  builder.image('data:image/jpg;base64, ....');
```

The second param is optional, but you can change the width anda the align when pass a style.

```
  builder.image('data:image/jpg;base64, ....', {
    align: 'center'
  });
```

Style image options: 
* ``width``  : ``int`` | size of image.
* ``height``  : ``int`` | size of image.
* ``x``  : ``int`` | x of image.
* ``y``  : ``int`` | y of image.

### Cutpaper
Cut the paper.
```
  builder.cutPaper();
```
### Open Cash Drawer
```
  builder.openCashDrawer();
```

### print

Finally for print, just call ``print`` command.
```
builder.print(portName, function(error, result){
  if (error) {
    console.error(error);
  } else {
    console.log("printReceipt finished");
  }
});
```


