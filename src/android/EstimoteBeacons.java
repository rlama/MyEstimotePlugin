
package me.habel.MyEstimotePlugin;

import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EstimoteBeacons extends CordovaPlugin {
    // Method names exposed to JavaScript
    public static final String START_ESTIMOTE_BEACONS_DISCOVERY_FOR_REGION = "startEstimoteBeaconsDiscoveryForRegion";
    public static final String STOP_ESTIMOTE_BEACON_DISCOVERY_FOR_REGION = "stopEstimoteBeaconsDiscoveryForRegion";
    public static final String START_RANGING_BEACONS_IN_REGION = "startRangingBeaconsInRegion";
    public static final String STOP_RANGING_BEACON_IN_REGION = "stopRangingBeaconsInRegion";
    public static final String GET_BEACONS = "getBeacons";
    // Constants
    private static final String REGION_ID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    // Variables
    private BeaconManager iBeaconManager;
    private Region currentRegion;
    private List<Beacon> beacons = new ArrayList<Beacon>();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        iBeaconManager = new BeaconManager(this.cordova.getActivity().getApplicationContext());
        currentRegion = new Region("regionId", REGION_ID, null, null);
        iBeaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                if (beacons == null || beacons.size() < 1) {
                    Log.d("DEBUG", "No beacons");
                } else {
                    EstimoteBeacons.this.beacons = beacons;
                }
            }
        });
        iBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    iBeaconManager.startRanging(currentRegion);
                } catch (RemoteException e) {
                    Log.e("DEBUG", "Cannot start ranging", e);
                }
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        Log.d(EstimoteBeacons.class.toString(), "action -> " + action);

        try {
            if (action.equalsIgnoreCase(START_ESTIMOTE_BEACONS_DISCOVERY_FOR_REGION)) {
                startEstimoteBeaconsDiscoveryForRegion();
                callbackContext.success(callbackContext.getCallbackId());
                return true;
            }

            if (action.equalsIgnoreCase(STOP_ESTIMOTE_BEACON_DISCOVERY_FOR_REGION)) {
                stopEstimoteBeaconsDiscoveryForRegion();
                callbackContext.success(callbackContext.getCallbackId());
                return true;
            }

            if (action.equalsIgnoreCase(START_RANGING_BEACONS_IN_REGION)) {
                startRangingBeaconsInRegion();
                callbackContext.success(callbackContext.getCallbackId());
                return true;
            }

            if (action.equalsIgnoreCase(STOP_RANGING_BEACON_IN_REGION)) {
                stopRangingBeaconsInRegion();
                callbackContext.success(callbackContext.getCallbackId());
                return true;
            }

            if (action.equalsIgnoreCase(GET_BEACONS)) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                        listToJSONArray(beacons)));
                return true;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
        return false;
    }

    private void startEstimoteBeaconsDiscoveryForRegion() throws RemoteException {
        iBeaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d("DEBUG", "iBeacons " + beacons.size());
            }
        });
    }

    private void stopEstimoteBeaconsDiscoveryForRegion() throws RemoteException {
        iBeaconManager.stopRanging(currentRegion);
    }

    private void startRangingBeaconsInRegion() throws RemoteException {
        iBeaconManager.startRanging(currentRegion);
    }

    private void stopRangingBeaconsInRegion() throws RemoteException {
        iBeaconManager.stopRanging(currentRegion);
    }

    /**
     * Convert list of beacons(@com.estimote.sdk.Beacon) to @JSONArray
     *
     * @param beacons - list of beacons (@com.estimote.sdk.Beacon)
     * @return JSONArray
     * @throws JSONException
     */
    private JSONArray listToJSONArray(List<Beacon> beacons) throws JSONException {
        JSONArray jArray = new JSONArray();
        for (Beacon beacon : beacons) {
            jArray.put(beaconToJSONObject(beacon));
        }
        return jArray;
    }

    /**
     * Convert beacon (@com.estimote.sdk.Beacon) to @JSONObject
     *
     * @param beacon - beacon(@com.estimote.sdk.Beacon)
     * @return JSONObject
     * @throws JSONException
     */
    private JSONObject beaconToJSONObject(Beacon beacon) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("proximityUUID", beacon.getProximityUUID());
        object.put("major", beacon.getMajor());
        object.put("minor", beacon.getMinor());
        object.put("rssi", beacon.getRssi());
        object.put("macAddress", beacon.getMacAddress());
        object.put("measuredPower", beacon.getMeasuredPower());
        return object;
    }
}
