package demo.kingnet.com.plugins;

import android.content.Context;
import android.view.View;


public class FloatViewUtil {

    private static FloatView floatView;
    private static FloatViewUtil instance;

    private FloatViewUtil() {
    }

    public static FloatViewUtil getInstance() {
        if (instance == null) {
            instance = new FloatViewUtil();
        }
        return instance;
    }

    public synchronized void showFloatView(Context context) {
        if (floatView != null) {
            floatView.destroy();
            floatView = null;
        }
        initFloatView(context, null);
    }


    private void initFloatView(Context context, FloatView.OnFloatBallClickListener mListener) {
        floatView = new FloatView(context, mListener);
        floatView.show();
    }

    public synchronized void hideFloatView(Context context) {
        if (floatView == null) {
            return;
        }
        floatView.destroy();
    }

}
