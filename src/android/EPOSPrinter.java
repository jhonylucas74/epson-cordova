/**
 */
package epos.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.epson.eposprint.*;
import com.epson.epsonio.*;
import com.epson.easyselect.*;

import android.util.Log;

import java.util.Date;

public class EPOSPrinter extends CordovaPlugin {
  private static final String TAG = "EPOSPrinter";
  private CallbackContext _callbackContext = null;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing ePOSPlugin");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(_callbackContext == null){
      _callbackContext = callbackContext;
    }

    if (action.equals("checkStatus")) {
      String port = args.getString(0);
      this.checkStatus(port);
      return true;
    }else if (action.equals("portDiscovery")) {
      this.portDiscovery();
      return true;
    }else {
      //this.printReceipt(args, callbackContext);
      return true;
    }
  }

  public void checkStatus(final String port) {

    final CallbackContext callbackContext = _callbackContext;
    // Run this in a thread for not stop all process.
    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        Print printer = null;

        try {
          printer = new Print();
          printer.openPrinter(Print.DEVTYPE_TCP, port);

          // A Sleep is used to get time for the socket to completely open
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            callbackContext.error("Problem with sleep in pool thread : " + e.getMessage());
            Thread.currentThread().interrupt();
          }

        } catch (EposException e) {
          callbackContext.error("Exception with ePOS : " + e.getMessage());
          Thread.currentThread().interrupt();
        }

        int[] status = new int[1];
        int[] battery = new int[1];
        status[0] = 0;
        battery[0] = 0;

        try {
          JSONObject json = new JSONObject();
          // get printer status.
          printer.getStatus(status, battery);

          if ((status[0] & Print.ST_OFF_LINE) != Print.ST_OFF_LINE) {
            // online
            json.put("online", true);
            callbackContext.success(json);
          } else if ((status[0] & Print.ST_OFF_LINE) == Print.ST_OFF_LINE) {
            // offline
            json.put("offline", true);
            callbackContext.success(json);
          } else {
            // unknown

          }
          // close printer.
          printer.closePrinter();
        } catch (EposException e) {
          callbackContext.error("Exception with ePOS : " + e.getMessage());
          Thread.currentThread().interrupt();
        } catch (JSONException e) {
          e.printStackTrace();
        } finally {
          Thread.currentThread().interrupt();
        }

      }
    });

  }

  private void portDiscovery() {

    JSONArray result = new JSONArray();
    DeviceInfo[] mList = null;

    try {
      Finder.start(this.cordova.getActivity(), DevType.TCP, "255.255.255.255");
      mList = Finder.getDeviceInfoList(FilterOption.PARAM_DEFAULT);

      for (DeviceInfo discovery : mList) {
        JSONObject port = new JSONObject();
        port.put("deviceType", discovery.getDeviceType());
        port.put("printerName", discovery.getPrinterName());
        port.put("deviceName", discovery.getDeviceName());
        port.put("ipAddress", discovery.getIpAddress());
        port.put("macAddress", discovery.getMacAddress());

        result.put(port);
      }

      Finder.stop();

    } catch (EpsonIoException exception) {
      _callbackContext.error(exception.getMessage());

    } catch (JSONException e) {
      e.printStackTrace();
    } finally {
      Log.d("Discovered devices : ", result.toString());
      _callbackContext.success(result);
    }
  }

}
