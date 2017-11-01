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

note: the function ``printreceipt`` doesn't exists anymore. Now you need use ``Builder``

To print a receipt you need create a builder with this code:
```
  var builder = window.plugins.starPrinter.Builder({ width: 384 });
```

The width represent the paper width. With the instance of builder is possible now add commands.

```
  builder.text("Hello world", {});
```
In example a text command was added in pipeline. the second argument is a object that represent a style of text. Below follow all the possible values.

* ``size``  : ``int`` | size of text.
* ``font``  : ``string`` |  font family: ``monospace``, ``sans serife``, ``serife`` or ``default``.
* ``weight``  : ``string`` | weight of text: ``bold``, ``bold italic``, ``italic`` or ``normal``.
* ``align``  : ``string`` | align of text: ``center``, ``opposite`` or ``normal``.

Example with default configuration of style.

```
  builder.text("Hello world", {
    size: 15,
    font: 'default',
    weight: 'normal',
    align: 'normal',
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
    size: 23,
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
* ``align``  : ``string`` | align of image: ``center``, ``left`` or ``right``.

### Cutpaper
Cut the paper.
```
  builder.cutPaper();
```
### Open Cash Drawer
I'm not sure if this work with TSP100.
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

### Connect and listen for hardware events (mPOP on iOS only)
```
window.plugins.starPrinter.connect(portName, function(error, result){
  if (error) {
    console.error(error);
  } else {
    console.log("connect finished");    
  }
});
window.addEventListener('starIOPluginData', function (e) {
  switch (e.dataType) {
    case 'printerCoverOpen':
      break;
    case 'printerCoverClose':
      break;
    case 'printerImpossible':
      break;
    case 'printerOnline':
      break;
    case 'printerOffline':
      break;
    case 'printerPaperEmpty':
      break;
    case 'printerPaperNearEmpty':
      break;
    case 'printerPaperReady':
      break;
    case 'barcodeReaderConnect':
      break;
    case 'barcodeDataReceive':
      break;
    case 'barcodeReaderImpossible':
      break;
    case 'cashDrawerOpen':
      break;
    case 'cashDrawerClose':
      break;
  }
});
```

### Open cash drawer (mPOP on iOS only)
```
window.plugins.starPrinter.openCashDrawer(name, function(error, result){
  if (error) {
    console.error(error);
  } else {
    console.log("openCashDrawer finished");
  }
});
```

[Demo application](https://github.com/InteractiveObject/StarIOPluginDemo)

## License
Copyright (c) 2016 [Interactive Object](https://www.interactive-object.com) . Licensed under the MIT license.
