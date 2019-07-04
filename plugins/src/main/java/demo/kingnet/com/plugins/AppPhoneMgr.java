package demo.kingnet.com.plugins;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by liulb1 on 2018/11/19.
 * 主要功能:手机管理工具类
 */

public class AppPhoneMgr {

    private static AppPhoneMgr phoneUtil;

    public static AppPhoneMgr getInstance() {
        if (phoneUtil == null) {
            synchronized (AppPhoneMgr.class) {
                if (phoneUtil == null) {
                    phoneUtil = new AppPhoneMgr();
                }
            }
        }
        return phoneUtil;
    }

    /**
     * 获取手机宽度
     */
    @SuppressWarnings("deprecation")
    public int getPhoneWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point realSize = new Point();
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(realSize);
        }
        return wm.getDefaultDisplay().getWidth();
    }

    /**
     * 获取手机真实宽度
     * @param context
     * @return
     */
    public int getPhoneRealWidth(Context context){
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point realSize = new Point();
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(realSize);
            return realSize.x;
        }else{
            return size.x;
        }
    }

    /**
     * 获取手机虚拟导航的高度
     * @param context
     * @return
     */
    public int getNavigationBarHeight(Context context){
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 获取手机有虚拟导航的高度
     * @param context
     * @return
     */
    public int getHasNavigationWidth(Context context){
        Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if(ori == mConfiguration.ORIENTATION_LANDSCAPE ) {
            return getPhoneRealWidth(context) - getNavigationBarHeight(context);
        }else{
            return getPhoneRealWidth(context);
        }
    }

    /**
     * 获取手机高度
     *
     * @param context
     */
    @SuppressWarnings("deprecation")
    public int getPhoneHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }


}
