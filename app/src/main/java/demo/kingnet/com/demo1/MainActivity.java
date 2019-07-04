package demo.kingnet.com.demo1;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import dalvik.system.DexClassLoader;
import demo.kingnet.com.pluginslibrary.IDelegate;

public class MainActivity extends AppCompatActivity {

    private String dexPath;
    private String fileName = "plugins-debug.apk";
    private String cacheDir;
    private DexClassLoader dexClassLoader;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        this.dexPath = Utils.copyFiles(newBase, fileName);
        this.cacheDir = Utils.getCacheDir(newBase).getAbsolutePath();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dexClassLoader = new DexClassLoader(dexPath, cacheDir, null, getClassLoader());
        try {
            Class<?> mClass = dexClassLoader.loadClass("demo.kingnet.com.plugins.Plugins");
            IDelegate delegate = (IDelegate) mClass.newInstance();
            delegate.init(getApplication());
        } catch (Exception ex) {
            Log.e("=====",ex.toString());
            ex.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
