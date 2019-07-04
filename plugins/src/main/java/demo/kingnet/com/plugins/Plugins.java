package demo.kingnet.com.plugins;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import demo.kingnet.com.pluginslibrary.IDelegate;

public class Plugins implements IDelegate {

    @Override
    public void init(Application context) {
        ActivityStack.init(context);
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                FloatViewUtil.getInstance().showFloatView(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                FloatViewUtil.getInstance().hideFloatView(activity);
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
