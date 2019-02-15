package com.adsforce.sdk.unity;

import android.app.Activity;
import android.util.Log;

import com.adsforce.sdk.AdsforceSdk;
import com.adsforce.sdk.deeplink.AdsforceDeepLink;
import com.adsforce.sdk.deeplink.AdsforceDeepLinkCallback;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UnityPlugin extends BasePluginProxy {

    private static Object sUnityPlayerObject;
    private static String sGameObjectName;
    private static String sGameFunctionName;
    private static Method sUnitySendMessageMethod;

    private static String androidid = null;

    public static void initSdk(String gamename, String function, String devKey, String publicKey, String trackUrl,
                               String channelId) {

        pluginLogi(TAG, "iniSDK, sGameObjectName :" + gamename);
        pluginLogi(TAG, "iniSDK, sGameFunctionName :" + function);

        setGameObjectName(gamename);
        setGameFunctionName(function);

        tryToGetGameActivity();

        if (androidid != null) {
            AdsforceSdk.setAndroidId(getActivity(), androidid);
            androidid = null;
        }

        if (getActivity() != null) {
            AdsforceSdk.initSdk(getActivity(), devKey, publicKey, trackUrl, channelId);
            Log.i(TAG, "---- unity initSdk(" + devKey + ",\n" + publicKey + ",\n" + publicKey + ",\n" + channelId + ") Susscess-----");
        } else {
            Log.i(TAG, "---- unity initSdk(" + devKey + ",\n" + publicKey + ",\n" + publicKey + ",\n" + channelId + ") fail-----");
        }

    }

    private static void tryToGetGameActivity() {
        if (getActivity() == null) {
            ivokeUnityActivity();
        }
    }

    private static void ivokeUnityActivity() {

        try {

            Class<?> sUnityPlayer = null;
            if (sUnityPlayer == null) {
                pluginLogi(TAG, "try to load UnityPlayer");
                sUnityPlayer = Class.forName("com.unity3d.player.UnityPlayer");
            }

            if (sUnityPlayer != null) {
                pluginLogi(TAG, "try to get currentActivity");
                Field field = sUnityPlayer.getDeclaredField("currentActivity");
                field.setAccessible(true);
                Object o = field.get(null);
                if (o != null
                        && o instanceof Activity) {
                    pluginLogi(TAG, "find the currentActivity");
                    setsActivity((Activity) o);
                } else {
                    pluginLogi(TAG, "will find the currentActivity in all fields.");
                    Field[] fields = sUnityPlayer.getDeclaredFields();
                    if (fields != null) {
                        for (Field f : fields) {
                            String typename = f.getType().getName();
                            pluginLogi(TAG, typename);
                            if (typename.equals("android.app.Activity")) {
                                f.setAccessible(true);
                                o = f.get(null);
                                setsActivity((Activity) o);
                                pluginLogi(TAG, "has found the currentActivity");
                                break;
                            }
                        }
                    }
                }

                Activity gameActivity = getActivity();
                if (gameActivity == null) {
                    gameActivity = getCurrentActivity();
                }

                if (gameActivity != null) {
                    pluginLogi(TAG, "will find the avail unityplayer object.");

                    Object unityPlayerObject = null;

                    for (Class<?> acls = gameActivity.getClass(); acls != null; acls = acls.getSuperclass()) {
                        try {
                            Field[] flds = acls.getDeclaredFields();
                            if (flds != null) {
                                for (Field f : flds) {
                                    String typename = f.getType().getName();
                                    if ("com.unity3d.player.UnityPlayer".equals(typename)) {
                                        f.setAccessible(true);
                                        unityPlayerObject = f.get(gameActivity);
                                        pluginLogi(TAG, "has found the unityplayer object: " + unityPlayerObject);
                                        break;
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }

                    if (unityPlayerObject != null) {
                        pluginLogi(TAG, "try to find the unityplayer UnitySendMessage method");
                        Method[] ms = unityPlayerObject.getClass().getDeclaredMethods();
                        if (ms != null) {
                            for (Method m : ms) {
                                String name = m.getName();
                                if (name.equals("UnitySendMessage")) {
                                    Method sendMessageMethod = m;
                                    sendMessageMethod.setAccessible(true);
                                    setUnitySendMessageMethod(sendMessageMethod);
                                    setUnityPlayerObject(unityPlayerObject);
                                    if (getActivity() == null) {
                                        setsActivity(gameActivity);
                                    }
                                    pluginLogi(TAG, "has found the unityplayer UnitySendMessage method:" + getUnitySendMessageMethod());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void setGameObjectName(String objectName) {
        sGameObjectName = objectName;
    }

    private static String getGameObjectName() {
        return sGameObjectName;
    }

    private static void setGameFunctionName(String functionName) {
        sGameFunctionName = functionName;
    }

    private static void setUnityPlayerObject(Object playerObject) {
        sUnityPlayerObject = playerObject;
    }

    private static Object getUnityPlayerObject() {
        return sUnityPlayerObject;
    }

    private static void setUnitySendMessageMethod(Method messageMethod) {
        sUnitySendMessageMethod = messageMethod;
    }

    private static Method getUnitySendMessageMethod() {
        return sUnitySendMessageMethod;
    }

    public static void thirdPayWithProductPrice(String price, String currency, String productId, String productType) {
        pluginLogi(TAG, "call thirdPayWithProductPrice()...");
        AdsforceSdk.thirdPayWithProductPrice(price, currency, productId, productType);
    }

    public static void googlePayWithProductPrice(String price, String currency, String publicKey, String dataSignature,
                                                 String purchaseData, String params) {
        pluginLogi(TAG, "call googlePayWithProductPrice() dataSignature:" + dataSignature);
        pluginLogi(TAG, "call googlePayWithProductPrice() purchaseData:" + purchaseData);
        pluginLogi(TAG, "call googlePayWithProductPrice() params:" + params);
        AdsforceSdk.googlePayWithProductPrice(price, currency, publicKey, dataSignature, purchaseData, jsonToHashMap(params));
    }

    public static void customerEventWithValue(String key, String value) {
        pluginLogi(TAG, "call customerEventWithValue():" + key + ", list:" + value);
        AdsforceSdk.customerEventWithValue(key, value);
    }

    public static void customerEventWithMap(String key, String jsonmap) {
        pluginLogi(TAG, "call customerEventWithMap():" + key + ", list:" + jsonmap);
        AdsforceSdk.customerEventWithMap(key, jsonToHashMap(jsonmap));
    }

    public static void customerEventWithList(String key, String jsonlist) {
        pluginLogi(TAG, "call customerEventWithList():" + key + ", list:" + jsonlist);
        AdsforceSdk.customerEventWithList(key, jsonToList(jsonlist));
    }

    public static void setAndroidId(String androidId) {
        pluginLogi(TAG, "call setAndroidId():" + androidId);
        if (getActivity() != null) {
            AdsforceSdk.setAndroidId(getActivity(), androidId);
        } else {
            androidid = androidId;
        }
    }

    public static void enableCustomerEvent(boolean enable) {
        pluginLogi(TAG, "call enableCustomerEvent():" + enable);
        AdsforceSdk.enableCustomerEvent(enable);
    }

    public static void enableLogger(boolean enable) {
        pluginLogi(TAG, "call enableLogger():" + enable);
        AdsforceSdk.enableLogger(enable);
    }

    public static boolean isDnsModeEnable() {
        pluginLogi(TAG, "call isDnsModeEnable()...");
        return AdsforceSdk.isDnsModeEnable();
    }

    public static void enableDnsMode(boolean enable) {
        pluginLogi(TAG, "call enableDnsMode():" + enable);
        AdsforceSdk.enableDnsMode(enable);
    }

    public static void addDnsMappingServers(String domain, String json) {
        pluginLogi(TAG, "call addDnsMappingServers():" + domain + "," + json);
        AdsforceSdk.addDnsMappingServers(domain, jsonToList(json));
    }

    public static void setDeepLink() {
        pluginLogi(TAG, "call setDeepLink()");
        AdsforceDeepLinkCallback callback = new AdsforceDeepLinkCallback() {
            @Override
            public void onFetchDeepLink(AdsforceDeepLink deepLink) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("uri", deepLink.getTargetUri());
                    jsonObject.put("link", deepLink.getLinkArgs());
                    polyProxyCallback.invokeMessage(Function_GET_DEEPLINK, jsonObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        AdsforceSdk.getDeepLink(getActivity(), callback);
    }

    private static PluginCallback polyProxyCallback = new PluginCallback() {

        @Override
        public void invokeMessage(String function, String message) {
            if (sUnitySendMessageMethod != null) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("function", function);
                    json.put("message", message);
                    String jsontext = json.toString();

                    sUnitySendMessageMethod.invoke(sUnityPlayerObject, new Object[]{sGameObjectName, sGameFunctionName, jsontext});
                    pluginLogi(TAG, "invokeUnityMethodMessage :" + jsontext);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
