package demo.kingnet.com.plugins;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * activity管理工具
 */
public final class ActivityStack {

    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;

    private static final LinkedList<Activity> ACTIVITY_LIST = new LinkedList<>();

    private static Application.ActivityLifecycleCallbacks mCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            ACTIVITY_LIST.remove(activity);
        }
    };

    private ActivityStack() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void init(@NonNull final Context context) {
        init((Application) context.getApplicationContext());
    }


    public static void init(@NonNull final Application app) {
        ActivityStack.sApplication = app;
        ActivityStack.sApplication.registerActivityLifecycleCallbacks(mCallbacks);
    }

    public static Application getApp() {
        if (sApplication != null) return sApplication;
        throw new NullPointerException("u should init first");
    }

    private static void setTopActivity(final Activity activity) {
        if (ACTIVITY_LIST.contains(activity)) {
            if (!ACTIVITY_LIST.getLast().equals(activity)) {
                ACTIVITY_LIST.remove(activity);
                ACTIVITY_LIST.addLast(activity);
            }
        } else {
            ACTIVITY_LIST.addLast(activity);
        }
    }

    public static LinkedList<Activity> getActivityList() {
        return ACTIVITY_LIST;
    }

    public static Context getTopActivityOrApp() {
        if (isAppForeground()) {
            Activity topActivity = getTopActivity();
            return topActivity == null ? ActivityStack.getApp() : topActivity;
        } else {
            return ActivityStack.getApp();
        }
    }

    public static boolean isAppForeground() {
        ActivityManager am =
                (ActivityManager) ActivityStack.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return aInfo.processName.equals(ActivityStack.getApp().getPackageName());
            }
        }
        return false;
    }

    public static Activity getTopActivity() {
        if (!ACTIVITY_LIST.isEmpty()) {
            final Activity topActivity = ACTIVITY_LIST.getLast();
            if (topActivity != null) {
                return topActivity;
            }
        }
        // using reflect to get top activity
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            if (activities == null) return null;
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    ActivityStack.setTopActivity(activity);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
