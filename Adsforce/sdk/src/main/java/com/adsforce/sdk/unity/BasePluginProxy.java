package com.adsforce.sdk.unity;

import android.app.Activity;
import android.text.TextUtils;

import com.adsforce.sdk.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BasePluginProxy {
    protected static final String TAG = "AdsforceProxy ===>";

    protected final static String Function_GET_DEEPLINK    = "getdeeplink_callback";

    private static WeakReference<Activity> sActivity;

    protected static Activity getActivity() {
        return sActivity == null ? null : sActivity.get();
    }

    protected static void setsActivity(Activity activity) {
        if (activity == null) {
            return;
        }

        if (sActivity != null) {
            sActivity.clear();
        }

        sActivity = new WeakReference<Activity>(activity);
    }

    protected static HashMap<String, String> jsonToHashMap(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.optJSONArray("array");
            if (array != null) {
                HashMap<String, String> map = new HashMap<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    map.put(o.getString("k"), o.getString("v"));
                }
                pluginLogi(TAG,"call jsonToHashMap():" + map.size());
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static ArrayList<String> jsonToList(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.optJSONArray("array");
            if (array != null) {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.getString(i));
                }
                pluginLogi(TAG,"call jsonToList():" + list.size());
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static void pluginLogi(String tag, String msg) {
        LogUtils.info(tag + msg);
        //Log.i(tag, msg);
    }

    public static Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
