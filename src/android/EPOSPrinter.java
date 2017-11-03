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

import android.graphics.Bitmap;
import android.util.Base64;
import android.graphics.BitmapFactory;

import java.util.Date;

public class EPOSPrinter extends CordovaPlugin {
    private static final String TAG = "EPOSPrinter";
    private CallbackContext _callbackContext = null;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing ePOSPlugin");
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        this._callbackContext = callbackContext;


        if (action.equals("checkStatus")) {
            String port = args.getString(0);
            this.checkStatus(port, callbackContext);
            return true;
        }

        if (action.equals("portDiscovery")) {
            this.portDiscovery();
            return true;
        }

        if (action.equals("print")) {
            JSONArray params = args.getJSONArray(0);
            String port = args.getString(1);
            this.printFromBuilder(port, params);
            return true;
        }

        if (action.equals("printTest")) {
            String port = args.getString(0);
            JSONArray texts = args.getJSONArray(1);
            this.printReceipt(port, texts);
            return true;
        }

        return true;
    }

    public void checkStatus(final String port, final CallbackContext callbackContext) {

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
                        json.put("unknown", true);
                        callbackContext.success(json);
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

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                _callbackContext.error(e.getMessage());
            }

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

        } catch (EpsonIoException e) {
            Log.d("myapp", Log.getStackTraceString(e));
            _callbackContext.error(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            Log.d("Discovered devices : ", result.toString());
            _callbackContext.success(result);
        }
    }

    private void printReceipt(final String port, JSONArray texts)  throws JSONException {
        //JSONObject params = args.getJSONObject(0);
        //final String port = params.getString("port");


        Print printer = null;
        int[] status = new int[1];
        status[0] = 0;

        try {
            printer = new Print();
            printer.openPrinter(Print.DEVTYPE_TCP, port);

            //Initialize a Builder class instance
            Builder builder = new Builder("TM-T88V", Builder.MODEL_ANK);

            for (int i = 0; i < texts.length(); i++) {
                String text = texts.getString(i);
                builder.addText(text + "\n");
            }

            builder.addCut(Builder.CUT_FEED);

            printer.sendData(builder, 10000, status);

            if((status[0] & Print.ST_PRINT_SUCCESS) == Print.ST_PRINT_SUCCESS) {
                builder.clearCommandBuffer();
            }

            printer.closePrinter();
            _callbackContext.success("Printed");

        } catch (EposException e) {
            _callbackContext.error(e.getMessage());
        }
    }

    private void printFromBuilder(final String port, JSONArray params) throws JSONException  {

        Print printer = null;
        int[] status = new int[1];
        status[0] = 0;

        try {
            printer = new Print();
            printer.openPrinter(Print.DEVTYPE_TCP, port);


            //Initialize a Builder class instance
            Builder builder = new Builder("TM-T88V", Builder.MODEL_ANK);

            pushCommandsToBuilder(builder, params);

            // Print the data.
            printer.sendData(builder, 10000, status);

            if((status[0] & Print.ST_PRINT_SUCCESS) == Print.ST_PRINT_SUCCESS) {
                builder.clearCommandBuffer();
            }

            printer.closePrinter();
            _callbackContext.success("Printed");

        } catch (JSONException e) {
            Log.d("myapp", Log.getStackTraceString(e));
            _callbackContext.error(e.getMessage().toString());
        } catch (EposException e) {
            Log.d("myapp", Log.getStackTraceString(e));
            _callbackContext.error(e.getMessage().toString());
        }

    }

    private void pushCommandsToBuilder(Builder builder, JSONArray commands) throws JSONException {

        for (int i = 0; i < commands.length(); i++) {
            JSONObject command = commands.getJSONObject(i);
            String type = command.getString("type");

            if (type.equals("text")) {
                createText(builder, command);
            }
            else if (type.equals("cutpaper")) {
                cutPaper(builder);
            }
            else if (type.equals("image")) {
                createImage(builder, command);
            }
            else if(type.equals("opencash")) {
                openCashDrawer(builder);
            }

        }

    }

    private void createImage (Builder builder, JSONObject command)  throws JSONException {

        try {
            String encodedImage = command.getString("image");

            int x = command.getInt("x");
            int y = command.getInt("y");
            int width = command.getInt("width");
            int height = command.getInt("height");

            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            builder.addImage(decodedByte, x, y, width, height, Builder.PARAM_DEFAULT,
                    Builder.MODE_MONO, Builder.HALFTONE_DITHER, 1.0,
                    Builder.PARAM_DEFAULT);

        } catch (EposException e) {
            Log.d("myapp", Log.getStackTraceString(e));
        }

    }


    private void createText(Builder builder, JSONObject command) throws JSONException {

        try {
            String textToPrint = command.getString("text");
            JSONObject style = command.getJSONObject("style");

            // font
            String font = style.getString("font");

            if (font.equals("FONT_A")) {
                builder.addTextFont(Builder.FONT_A);
            } else if (font.equals("FONT_B")) {
                builder.addTextFont(Builder.FONT_B);
            } else if (font.equals("FONT_C")) {
                builder.addTextFont(Builder.FONT_C);
            } else if (font.equals("FONT_D")) {
                builder.addTextFont(Builder.FONT_D);
            } else if (font.equals("FONT_E")) {
                builder.addTextFont(Builder.FONT_E);
            } else {
                builder.addTextFont(Builder.FONT_A);
            }

            // Weight

            String weight = style.getString("weight");

            if (weight.equals("bold")) {
                builder.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED,
                        Builder.TRUE, Builder.PARAM_UNSPECIFIED);
            } else {
                builder.addTextStyle(Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED,
                        Builder.PARAM_UNSPECIFIED, Builder.PARAM_UNSPECIFIED);
            }

            // size
            int size = style.getInt("size");

            if(size > 8) {
                size = 8;
            }

            if (size < 1) {
                size = 1;
            }

            builder.addTextSize(size, size);

            // align
            String alignString = style.getString("align");

            if (alignString.equals("center")) {
                builder.addTextAlign(Builder.ALIGN_CENTER);
            }
            else if (alignString.equals("right")) {
                builder.addTextAlign(Builder.ALIGN_RIGHT);
            } else {
                builder.addTextAlign(Builder.ALIGN_LEFT);
            }


            builder.addText(textToPrint);

        } catch (EposException e) {
            Log.d("myapp", Log.getStackTraceString(e));
            _callbackContext.error(e.getStackTrace().toString());
        }

    }


    private void openCashDrawer(Builder builder) {
        try {
            builder.addPulse(Builder.DRAWER_1, Builder.PULSE_100);
        } catch (EposException e) {
            Log.d("myapp", Log.getStackTraceString(e));
        }
    }

    private void cutPaper(Builder builder) {
        try {
            builder.addCut(Builder.CUT_FEED);
        } catch (EposException e) {
            Log.d("myapp", Log.getStackTraceString(e));
        }
    }

}
