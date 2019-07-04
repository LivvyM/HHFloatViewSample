package demo.kingnet.com.plugins;

import android.app.Activity;

/**
 * Created by liulb1 on 2019/2/13.
 * 动态获取资源工具类
 */

public class ResourceUtils {


    /**
     * 获取控件id
     *
     * @param resName
     * @return
     */
    public static int getViewId(String resName) {
        Activity activity = ActivityStack.getTopActivity();
        if (null != activity) {
            return activity.getResources().getIdentifier(resName, "id", activity.getPackageName());
        } else {
            return 0;
        }
    }

    /**
     * 获取布局文件
     *
     * @param resName
     * @return
     */
    public static int getLayoutId(String resName) {
        Activity activity = ActivityStack.getTopActivity();
        if (null != activity) {
            return activity.getResources().getIdentifier(resName, "layout", activity.getPackageName());
        } else {
            return 0;
        }
    }

}
