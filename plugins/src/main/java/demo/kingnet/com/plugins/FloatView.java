package demo.kingnet.com.plugins;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.InputStream;

public class FloatView extends LinearLayout {

    private static final int HANDLE_ALPHA = 500;//悬浮球透明
    private static final int HANDLE_ALPHA_ALL = 900;//悬浮球不透明
    private static final int HANDLE_SWITCH_PIC_HALF = 100;//悬浮球居中
    private static final int HANDLE_SWITCH_PIC_HALF_RIGHT = 150;//悬浮球靠右
    private static final int HANDLE_SWITCH_PIC_HALF_LEFT = 50;//悬浮球靠左
    private static final int HANDLE_FLOAT_ADSORB = 10;//手放开执行悬浮球吸边操作

    private Context mContext;
    private WindowManager.LayoutParams mWmParams;
    private WindowManager mWindowManager;

    private ImageView floatViewHome;//悬浮球控件
    private View floatView;
    //    private View spreadView;//展开的布局
    private boolean isSpread;//是否是展开的布局 true 展开  false 关闭

//    private Drawable tfloat;//悬浮球正常图

    private int screenWidth = 0;//屏幕宽度
    private int ballSize;//悬浮球大小

    private ValueAnimator mValueAnimator;//属性动画

    private OnFloatBallClickListener mListener;//监听悬浮球点击事件


    private FloatViewOnTouchListener homeListener = new FloatViewOnTouchListener();

    public FloatView(Context context) {
        super(context);
        init(context);
    }

    public FloatView(Context context, OnFloatBallClickListener mListener) {
        super(context);
        init(context);
        this.mListener = mListener;
    }


    public FloatView(Context context, AttributeSet attrs) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        // 更新浮动窗口位置参数 靠边
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        this.mWmParams = new WindowManager.LayoutParams();
        // 设置window type
        mWmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        // 设置图片格式，效果为背景透明
        mWmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        int[] locationXY = getInitFloatLocation();
        mWmParams.x = locationXY[0];
        mWmParams.y = locationXY[1];
        mWmParams.gravity = Gravity.TOP | Gravity.LEFT;
        ballSize = DisplayUtil.dip2px(mContext, 45);
        screenWidth = AppPhoneMgr.getInstance().getPhoneWidth(context);
        createView(context);
        mWindowManager.addView(this, mWmParams);
    }

    private int[] getInitFloatLocation() {
        String floatBallPosition = "0,300";
        String[] positions = floatBallPosition.split(",");
        int[] rs = new int[2];
        if (positions.length == 2) {
            rs[0] = NumberUtils.strToNumber(positions[0]);
            rs[1] = NumberUtils.strToNumber(positions[1]);
        } else {
            //悬浮球初始位置
            rs[0] = AppPhoneMgr.getInstance().getPhoneWidth(mContext) - ballSize;
            rs[1] = AppPhoneMgr.getInstance().getPhoneHeight(mContext) / 2;
        }
        return rs;
    }

    private View buildRootView(Context context) {
        FrameLayout layout = new FrameLayout(context);
        layout.setLayoutParams(new FrameLayout.LayoutParams(DisplayUtil.dip2px(mContext, 45), DisplayUtil.dip2px(mContext, 45)));
        floatViewHome = new ImageView(context);
        floatViewHome.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        layout.addView(floatViewHome);
        return layout;
    }

    private void createView(final Context context) {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        floatView = buildRootView(context);
        InputStream resourceAsStream = context.getClass().getClassLoader().getResourceAsStream("assets/" + "ghicon.png");
        Bitmap bitmap = BitmapFactory.decodeStream(resourceAsStream);
        floatViewHome.setImageBitmap(bitmap);
        floatViewHome.setOnTouchListener(homeListener);
        floatViewHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onFloatBallClicked();
                }
//                addDisplayView();
//                updateSpreadView();
            }
        });
        addCloseView();

        //加载展开的view
//        spreadView = LayoutInflater.from(mContext).inflate(ResourceUtils.getLayoutId("ky_float_view"), null);
//        spreadView.findViewById(ResourceUtils.getViewId("float_spread_iv")).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addCloseView();
//            }
//        });
//        spreadView.findViewById(ResourceUtils.getViewId("float_spread_iv_right")).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                addCloseView();
//            }
//        });

        //注册有虚拟导航的 监听虚拟导航打开关闭
        registerNavigationBarObserver();
    }


    /**
     * 添加收起的view
     */
    private void addCloseView() {
        //先移除所有的view
        removeAllViews();
        isSpread = false;
        addView(floatView);
        //5 秒后吸边操作
        if (mWmParams.x + ballSize / 2 > screenWidth / 2) {
            mWmParams.x = screenWidth - ballSize;
            mHandler.sendEmptyMessageDelayed(HANDLE_SWITCH_PIC_HALF_RIGHT, 5000);
            mHandler.sendEmptyMessageDelayed(HANDLE_ALPHA, 5000);
        } else {
            mWmParams.x = 0;
            mHandler.sendEmptyMessageDelayed(HANDLE_SWITCH_PIC_HALF_LEFT, 5000);
            mHandler.sendEmptyMessageDelayed(HANDLE_ALPHA, 5000);
        }
    }

    //添加展开的view
    private void addDisplayView() {
        //移除所有的延时操作
        mHandler.removeCallbacksAndMessages(null);
        //先移除所有的view
        removeAllViews();
        isSpread = true;
//        addView(spreadView);
    }

    /**
     * 收起
     */
    public void spreadView() {
        if (isSpread) {
            //如果当前的view是 展开的 则收起
            addCloseView();
        }
    }

    /**
     * 注册虚拟导航显示和隐藏
     */
    private void registerNavigationBarObserver() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor
                    ("navigationbar_is_min"), true, mNavigationBarObserver);
        } else {
            mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor
                    ("navigationbar_is_min"), true, mNavigationBarObserver);
        }
    }

    /**
     * 取消注册
     */
    private void unRegisterNavigationBarObserver() {
        mContext.getContentResolver().unregisterContentObserver(mNavigationBarObserver);
    }

    private ContentObserver mNavigationBarObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            int navigationBarIsMin = 0;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                navigationBarIsMin = Settings.System.getInt(mContext.getContentResolver(),
                        "navigationbar_is_min", 0);
            } else {
                navigationBarIsMin = Settings.Global.getInt(mContext.getContentResolver(),
                        "navigationbar_is_min", 0);
            }
            if (navigationBarIsMin == 1) {
                //导航键隐藏了
                screenWidth = AppPhoneMgr.getInstance().getPhoneRealWidth(mContext);
                //if (bool_hide != 0) {
                if (null != mWmParams && mWindowManager != null) {
                    mWmParams.x = screenWidth - ballSize;
                    mWindowManager.updateViewLayout(FloatView.this, mWmParams);
                }
                //}
            } else {
                //导航键显示了
                screenWidth = AppPhoneMgr.getInstance().getHasNavigationWidth(mContext);
            }
        }
    };

    /**
     * 控制是否移动 移动过程不响应点击事件
     */
    boolean isMoved = false;

    class FloatViewOnTouchListener implements OnTouchListener {

        int lastX, lastY;
        int paramX, paramY;

        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    paramX = mWmParams.x;
                    paramY = mWmParams.y;
                    isMoved = false;
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.sendEmptyMessage(HANDLE_ALPHA_ALL);
                    mHandler.sendEmptyMessage(HANDLE_SWITCH_PIC_HALF);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getRawX();
                    int y = (int) event.getRawY();
                    int dx = x - lastX;
                    int dy = y - lastY;
                    mWmParams.x = paramX + dx;
                    mWmParams.y = paramY + dy;
                    //更新悬浮球位置
                    mWindowManager.updateViewLayout(FloatView.this, mWmParams);

                    //判断是否是移动
                    if (mWmParams.x - paramX > 5 || mWmParams.y - paramY > 5)
                        isMoved = true;

                    break;
                case MotionEvent.ACTION_UP:
                    //不操作悬浮球吼 3秒变暗
                    //mHandler.sendEmptyMessageDelayed(HANDLE_ALPHA, 5000);
                    //if (isMoved) {
                    mHandler.sendEmptyMessage(HANDLE_FLOAT_ADSORB);
                    //}
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    //home.performClick();
                    break;
            }

            return isMoved;
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_ALPHA:
                    floatViewHome.setAlpha(0.5f);
                    break;
                case HANDLE_ALPHA_ALL:
                    floatViewHome.setAlpha(1.0f);
                    break;
                case HANDLE_SWITCH_PIC_HALF_RIGHT:
//                    floatViewHome.setImageDrawable(drawable_left);
                    break;
                case HANDLE_SWITCH_PIC_HALF_LEFT:
//                    floatViewHome.setImageDrawable(drawable_right);
                    break;
                case HANDLE_SWITCH_PIC_HALF:
//                    floatViewHome.setImageDrawable(tfloat);
                    break;
                case HANDLE_FLOAT_ADSORB:
                    adsorbView();
                    break;
            }
        }
    };

    /**
     * 吸附到左右两边
     */
    private void adsorbView() {
        if (null != mWindowManager && null != mWmParams) {
            final int start = mWmParams.x;
            int end = 0;
            if (start + ballSize / 2 > screenWidth / 2) {
                //吸附右边
                end = screenWidth - ballSize;
            } else {
                //吸附左边
                end = 0;
            }

            if (start == end) {
                if (start == 0) {
                    mHandler.sendEmptyMessageDelayed(HANDLE_SWITCH_PIC_HALF_LEFT, 5000);
                    mHandler.sendEmptyMessageDelayed(HANDLE_ALPHA, 5000);
                } else {
                    mHandler.sendEmptyMessageDelayed(HANDLE_SWITCH_PIC_HALF_RIGHT, 5000);
                    mHandler.sendEmptyMessageDelayed(HANDLE_ALPHA, 5000);
                }
                return;
            }
            mValueAnimator = ValueAnimator.ofInt(start, end);
            mValueAnimator.setDuration(600);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (null != mWindowManager && null != mWmParams) {
                        mWmParams.x = (int) animation.getAnimatedValue();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (isAttachedToWindow()) {
                                mWindowManager.updateViewLayout(FloatView.this, mWmParams);
                            }
                        } else {
                            mWindowManager.updateViewLayout(FloatView.this, mWmParams);
                        }
                    }
                }
            });

            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mWmParams.x < screenWidth / 2) {
                        mHandler.sendEmptyMessageDelayed(HANDLE_SWITCH_PIC_HALF_LEFT, 5000);
                        mHandler.sendEmptyMessageDelayed(HANDLE_ALPHA, 5000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(HANDLE_SWITCH_PIC_HALF_RIGHT, 5000);
                        mHandler.sendEmptyMessageDelayed(HANDLE_ALPHA, 5000);
                    }
                }
            });
            mValueAnimator.start();
        }
    }

    /**
     * 显示悬浮窗
     */
    public void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
            mWmParams.alpha = 1f;
            mWindowManager.updateViewLayout(this, mWmParams);
        }
    }

    /**
     * 去除悬浮球
     */
    public void destroy() {
        //保存上次悬浮球的位置
        if (null != mValueAnimator) {
            mValueAnimator.cancel();
        }
        int x = 0;
        if (mWmParams.x < 0) {
            x = 0;
        } else if (mWmParams.x + ballSize > screenWidth) {
            mWmParams.x = screenWidth - ballSize;
        } else {
            x = mWmParams.x;
        }
        removeFloatView();
        unRegisterNavigationBarObserver();
        stopHandler();

    }

    private void removeFloatView() {
        try {
            if (mWindowManager != null) {
                mWindowManager.removeView(this);
            }
        } catch (Throwable ex) {
        }
    }

    public void stopHandler() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public interface OnFloatBallClickListener {
        void onFloatBallClicked();
    }


    //根据悬浮球在左边还是右边显示展开的箭头方向
    private void updateSpreadView() {
//        if (mWmParams.x< AppPhoneMgr.getInstance().getPhoneWidth(mContext)/2){
//            spreadView.findViewById(ResourceUtils.getViewId("float_spread_iv")).setVisibility(View.VISIBLE);
//            spreadView.findViewById(ResourceUtils.getViewId("float_spread_iv_right")).setVisibility(View.GONE);
//        }else{
//            spreadView.findViewById(ResourceUtils.getViewId("float_spread_iv_right")).setVisibility(View.VISIBLE);
//            spreadView.findViewById(ResourceUtils.getViewId("float_spread_iv")).setVisibility(View.GONE);
//        }
    }
}
