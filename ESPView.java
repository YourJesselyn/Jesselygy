package com.xandroid.dx.touch;


import com.xandroid.dx.touch.MainActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.graphics.Matrix;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.SystemClock;
import com.xandroid.dx.touch.FloatingActivity;
import com.xandroid.dx.touch.Overlay;
import com.xandroid.dx.touch.PaintManager;
import com.xandroid.dx.touch.R;
import android.graphics.Typeface;
import static com.xandroid.dx.touch.Overlay.getConfig;
import androidx.annotation.RequiresApi;
import android.os.Build;
import androidx.core.content.res.ResourcesCompat;
import android.util.LruCache;
import java.net.IDN;
import android.view.Surface;
import android.util.Log;
import android.view.WindowManager;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.os.Handler;
import android.os.Looper;
import android.annotation.SuppressLint;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import androidx.core.util.Pair;
import android.view.Choreographer;
import android.graphics.Path;
import android.content.SharedPreferences;
import java.util.List;
import java.util.ArrayList;
import com.github.megatronking.stringfog.annotation.StringFogIgnore;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.BlurMaskFilter;

@StringFogIgnore()
//public class ESPView extends View {
public class ESPView extends View implements Runnable {
    
    private Paint mModeText, mStrokePaint,
    mCountPaint, mFilledPaint,mFilledPaint2, 
    mTextPaint,mNamePaint,mFpsPaint,mItemPaint,mLootBoxPaint,p;

    public Animation animation;

    
    private Rect eRect;
    private GradientDrawable eGradientDrawable;
    private int eColor[] = {Color.TRANSPARENT,Color.GREEN,Color.TRANSPARENT};

    private static LruCache<Integer, Bitmap> bitmapCache = new LruCache<>(10 * 1024 * 1024);
    Path path = new Path();
    
    private  static int itemPosition;

    private SimpleDateFormat formatter;
    String mode=MainActivity.xmode;
    private Date time;
    public static boolean showFps = false;
    private  static int setStroke;
    
    
    private volatile boolean mRunning = true;
    static long sleepTime;
    private Thread mThread;
    private float mFPS = 0.0f;
    private float mFPSCounter = 0.0f;
    private long mFPSTime = 0;
    
    
    public static void ChangeFps(int fps) {
        sleepTime = 1000 / fps;
    }
    
    /*
    static long sleepTime;
    private long lastDrawTime = 0;
    public static void ChangeFps(int fps) {
	if (fps <= 0) fps = 60;
	sleepTime = 1000 / fps;
    }*/
    
    public static void ChangeStrokeLine(int strokeline){
        setStroke = strokeline;
    }
  
    Bitmap bitmap, out, bitmap2, out2, OTHER, OTH1;

    Bitmap[] OTHER1 = new Bitmap[4];
    private static final int[] OTH1_NAME = {
	R.drawable.ic_clear_enemy,
	R.drawable.ic_clear_boot,
	R.drawable.ic_danger_enemy,
	R.drawable.ic_danger_boot
    };
    
    
    public ESPView(Context context) {
        super(context);
        DrawingPaints();
        setFocusableInTouchMode(false);
        setBackgroundColor(Color.TRANSPARENT);
	sleepTime = 1000 / 60;
	time = new Date();
        formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        mThread = new Thread(this);
        mThread.start();
    }
    
    /*
    public ESPView(Context context) {
	super(context);
	DrawingPaints();
	setFocusableInTouchMode(false);
	setBackgroundColor(Color.TRANSPARENT);

	sleepTime = 1000 / 60; // default 60 FPS
	time = new Date();
	formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }*/
    
    private Bitmap getBitmapFromCache(int resId) {
	return bitmapCache.get(resId);
    }

    private void addBitmapToCache(int resId, Bitmap bitmap) {
	if (getBitmapFromCache(resId) == null) {
	    bitmapCache.put(resId, bitmap);
	}
    }
    
 
    
    @Override
    public void run() {
        while (mThread.isAlive() && !mThread.isInterrupted()) {
            try {
                long t1 = System.currentTimeMillis();
                postInvalidate();
                long td = System.currentTimeMillis() - t1;

                Thread.sleep(Math.max(Math.min(0, sleepTime - td), sleepTime));
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void stopRendering() {
	mRunning = false;
	if (mThread != null) {
	    mThread.interrupt();
	    try {
		mThread.join(100);
	    } catch (InterruptedException e) {
		Thread.currentThread().interrupt();
	    }
	    mThread = null;
	}
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
	int rotation = getDisplay().getRotation();
	if (canvas == null || rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
	    ClearCanvas(canvas);
	    return;
	}
	
	time.setTime(System.currentTimeMillis());
	ClearCanvas(canvas);
	Overlay.DrawOn(this, canvas);
	
    }

    /*
    @Override
    protected void onDraw(Canvas canvas) {
	if (canvas == null) return;

	int rotation = getDisplay().getRotation();
	if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
	    return;
	}

	long now = System.currentTimeMillis();

	// FPS LIMIT DARI sleepTime
	long delta = now - lastDrawTime;
	if (delta < sleepTime) {
	    postInvalidateDelayed(sleepTime - delta);
	    return;
	}

	lastDrawTime = now;
	time.setTime(now);

	Overlay.DrawOn(this, canvas);

	// render loop
	postInvalidateDelayed(sleepTime);
    }*/
    
    
    private void configurePaint(Paint paint) {
	if (getConfig("Enable_shadow_text")) {
	    paint.setShadowLayer(2, 0, 0, Color.parseColor("#000000"));
	} else {
	    paint.clearShadowLayer();
	}
    }
    
    private void configureAA(Paint paint) {
	if (getConfig("enable_anti_aliasing")) {
	    paint.setAntiAlias(true);
	} else {
	    paint.setAntiAlias(false);
	}
    }
    

   @SuppressLint("WrongConstant")
    public void DrawingPaints() {

        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(Color.rgb(0, 0, 0));
	mStrokePaint.setTextAlign(Paint.Align.CENTER);
	//configureAA(mStrokePaint);

        mFilledPaint = new Paint();
        mFilledPaint.setStyle(Paint.Style.FILL);
        mFilledPaint.setColor(Color.rgb(0, 0, 0));
	//configureAA(mFilledPaint);
	
	mFilledPaint2 = new Paint();
        mFilledPaint2.setStyle(Paint.Style.FILL);
        mFilledPaint2.setColor(Color.rgb(0, 0, 0));
        //configureAA(mFilledPaint2);

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        //configurePaint(mTextPaint);
        //configureAA(mTextPaint);
        
        mItemPaint = new Paint();
        //mItemPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mItemPaint.setTextAlign(Paint.Align.CENTER);
        //configurePaint(mItemPaint);
        //configureAA(mItemPaint);
	
	mCountPaint = new Paint();
        mCountPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCountPaint.setColor(Color.rgb(0, 0, 0));
        mCountPaint.setTextAlign(Paint.Align.CENTER);
	mCountPaint.setFakeBoldText(true);
        //configureAA(mCountPaint);

	p = new Paint();

        eRect = new Rect(-85,60,85,90);
        eGradientDrawable = new GradientDrawable();
        eGradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        eGradientDrawable.setColors(eColor);
        eGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        eGradientDrawable.setBounds(eRect);

	mModeText = new Paint();
        mModeText.setStyle(Paint.Style.FILL_AND_STROKE);
        mModeText.setColor(Color.rgb(0, 0, 0));
        mModeText.setTextAlign(Paint.Align.CENTER);
        //configurePaint(mModeText);
        //configureAA(mModeText);

        mFpsPaint  = new Paint();
        mFpsPaint.setStyle(Paint.Style.FILL);
        mFpsPaint.setColor(Color.rgb(0, 0, 0));
        mFpsPaint.setTextAlign(Paint.Align.CENTER);
        //configurePaint(mFpsPaint);
        //configureAA(mFpsPaint);

        mNamePaint = new Paint();
        mNamePaint.setStyle(Paint.Style.FILL);
        mNamePaint.setTextAlign(Paint.Align.CENTER);
        //configurePaint(mNamePaint);
        //configureAA(mNamePaint);
	
	final int bitmap_count_oth = OTHER1.length;

	for (int i = 0; i < bitmap_count_oth; i++) {

	    Bitmap cachedBitmap = getBitmapFromCache(OTH1_NAME[i]);
	    if (cachedBitmap == null) {

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), OTH1_NAME[i]);
		if (i == 4) {
		    bitmap = scale(bitmap, 500, 400);
		} else {
		    bitmap = scale(bitmap, 80, 80);
		}

		addBitmapToCache(OTH1_NAME[i], bitmap);
		OTHER1[i] = bitmap;
	    } else {

		OTHER1[i] = cachedBitmap;
	    }
	}
    }
    
    
    
    public void ClearCanvas(Canvas cvs) {
        cvs.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    public void DrawLine(Canvas cvs, int a, int r, int g,int b, float lineWidth, float fromX, float fromY, float toX, float toY) 
    {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        mStrokePaint.setStrokeWidth(lineWidth);
	configureAA(mStrokePaint);
        cvs.drawLine(fromX, fromY, toX, toY, mStrokePaint);
    }
    
    public void DrawLinePlayer(Canvas cvs, int a, int r, int g, int b, float lineWidth, float fromX, float fromY, float toX, float toY) {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        mStrokePaint.setStrokeWidth(lineWidth+setStroke/3);
        cvs.drawLine(fromX, fromY, toX, toY, mStrokePaint);
    }
    
    public void DrawOTH1(Canvas cvs, int image_number, float X, float Y) {
        cvs.drawBitmap(OTHER1[image_number], X, Y, p);
    }  

    public void DrawSkelton(Canvas cvs, int a, int r, int g, int b, float lineWidth, float fromX, float fromY, float toX, float toY) 
    {
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
        mStrokePaint.setStrokeWidth(lineWidth);
	configureAA(mStrokePaint);
        cvs.drawLine(fromX, fromY, toX, toY, mStrokePaint);
    }

    //darah kotak 
    public void DrawRect(Canvas cvs, int a, int r, int g, int b, float stroke, float x, float y, float width, float height) 
    {
        mStrokePaint.setStrokeWidth(stroke);
        mStrokePaint.setColor(Color.rgb(r, g, b));
        mStrokePaint.setAlpha(a);
	configureAA(mStrokePaint);
        cvs.drawRoundRect(new RectF(x, y, width, height), 2, 2, mStrokePaint);
    }

    public void DrawFilledRect(Canvas cvs, int a, int r, int g, int b, float x, float y, float width, float height) 
    {
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(a);
	configureAA(mFilledPaint);
        cvs.drawRoundRect(new RectF(x, y, width, height), 2, 2, mFilledPaint);
    }
    
    public void DrawRoundedRect(Canvas cvs, int a, int r, int g, int b,
			      float stroke, float x, float y, float width, float height, float radius) {
	mStrokePaint.setStrokeWidth(stroke);
	mStrokePaint.setColor(Color.rgb(r, g, b));
	mStrokePaint.setAlpha(a);
	configureAA(mStrokePaint);
	cvs.drawRoundRect(new RectF(x, y, width, height), radius, radius, mStrokePaint);
    }

    public void DrawFilledRoundedRect(Canvas cvs, int a, int r, int g, int b,
				    float x, float y, float width, float height, float radius) {
	mFilledPaint.setColor(Color.rgb(r, g, b));
	mFilledPaint.setAlpha(a);
	configureAA(mFilledPaint);
	cvs.drawRoundRect(new RectF(x, y, width, height), radius, radius, mFilledPaint);
    }
    
    //player
    public void DrawEnemyCount(Canvas cvs, int a, int r, int g, int b, int x, int y, int width, int height) {
        int colors[] = {Color.argb(a,r,g,b), Color.argb(a,r,g,b), Color.argb(a,r,g,b)};
        GradientDrawable mDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
        mDrawable.setShape(GradientDrawable.RECTANGLE);
        mDrawable.setCornerRadii(new float[] { 6, 6, 0, 0, 0, 0, 6, 6});
        mDrawable.setGradientRadius(2.0f * 60);
        Rect mRect = new Rect(x,y,width,height);
        mDrawable.setBounds(mRect);
        cvs.save();
        mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mDrawable.draw(cvs);
        cvs.restore();
    }
    //bot
    public void DrawEnemyCount2(Canvas cvs, int a, int r, int g, int b, int x, int y, int width, int height) {
        int colors[] = {Color.argb(a,r,g,b), Color.argb(a,r,g,b), Color.argb(a,r,g,b)};
        GradientDrawable mDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
        mDrawable.setShape(GradientDrawable.RECTANGLE);
        mDrawable.setCornerRadii(new float[] { 0, 0, 6, 6, 6, 6, 0, 0});
        mDrawable.setGradientRadius(2.0f * 60);
        Rect mRect = new Rect(x,y,width,height);
        mDrawable.setBounds(mRect);
        cvs.save();
        mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mDrawable.draw(cvs);
        cvs.restore();
    }

    public void EnemyCount(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size)
    {
        mCountPaint.setARGB(a, r, g, b);
        mCountPaint.setTextSize(size);
	configureAA(mCountPaint);
        cvs.drawText(txt, posX, posY, mCountPaint);
    }

    public void DebugText(String s) {
        System.out.println(s);
    }

    public void DrawTextCount(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size)
    {
        configurePaint(mCountPaint);
	configureAA(mCountPaint);
        mCountPaint.setARGB(a, r, g, b);
        mCountPaint.setTextSize(size);
        cvs.drawText(txt, posX, posY, mCountPaint);
    }

    /*
    public void DrawTextName(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) 
    {
	configurePaint(mFpsPaint);
	configureAA(mFpsPaint);
        mFpsPaint.setARGB(a, r, g, b);
	mFpsPaint.setTextSize(size);
	long currentTime = SystemClock.uptimeMillis();
        if (currentTime - mFPSTime >= 1000) {
            mFPS = mFPSCounter;
            mFPSCounter = 0;
            mFPSTime = currentTime;
        } else {
            mFPSCounter++;
        }
	String fpsText = "DXSR Running: " + mFPS + " f/s";
	cvs.drawText(fpsText, posX, posY, mFpsPaint);
    }*/
    
    public void DrawTextName(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) {
	if (!showFps) return;

	mFpsPaint.setARGB(a, r, g, b);
	mFpsPaint.setTextSize(size);

	long currentTime = SystemClock.uptimeMillis();
	if (currentTime - mFPSTime >= 1000) {
	    mFPS = mFPSCounter;
	    mFPSCounter = 0;
	    mFPSTime = currentTime;
	} else {
	    mFPSCounter++;
	}

	String fpsText = "DXSR Running: " + mFPS + " f/s";
	cvs.drawText(fpsText, posX, posY, mFpsPaint);
    }
  
    public void DrawName2(Canvas cvs, int a, int r, int g, int b, String nametxt, int teamid, float posX, float posY, float size) {
        String[] namesp = nametxt.split(":");
        char[] nameint = new char[namesp.length];
        for (int i = 0; i < namesp.length; i++)
            nameint[i] = (char) Integer.parseInt(namesp[i]);
        String realname = new String(nameint);
        configurePaint(mNamePaint);
	configureAA(mNamePaint);
        mNamePaint.setARGB(a, r, g, b);
        mNamePaint.setTextSize(size);
        cvs.drawText(teamid + ". " + realname, posX, posY, mNamePaint);
    }
    
    
    public void DrawName3(Canvas cvs, int a, int r, int g, int b, String nametxt, int teamid, float posX, float posY, float size) {
        String[] namesp = nametxt.split(":");
        char[] nameint = new char[namesp.length];
        for (int i = 0; i < namesp.length; i++)
            nameint[i] = (char) Integer.parseInt(namesp[i]);
        String realname = new String(nameint);
        mFpsPaint.setARGB(a, r, g, b);
        mFpsPaint.setTextSize(size);
	configurePaint(mFpsPaint);
	configureAA(mFpsPaint);
        cvs.drawText( teamid + ". " + realname, posX, posY, mFpsPaint);
    }

    public void DrawText(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size)
    {
        configurePaint(mTextPaint);
	configureAA(mTextPaint);
        mTextPaint.setARGB(a, r, g, b);
        mTextPaint.setTextSize(size);
        cvs.drawText(txt, posX, posY, mTextPaint);
    }

    public void DrawJess(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size)
    {
        mFpsPaint.setARGB(a, r, g, b);
        mFpsPaint.setTextSize(size);
	configurePaint(mFpsPaint);
	configureAA(mFpsPaint);
        cvs.drawText(txt, posX, posY, mFpsPaint);
    }

    public void DrawTextMode(Canvas cvs, int a, int r, int g, int b, String txt, float posX, float posY, float size) 
    {
        mModeText.setARGB(a,r, g, b);
        mModeText.setTextSize(size);
	configurePaint(mModeText);
	configureAA(mModeText);
        cvs.drawText(""+mode, posX, posY, mModeText);

    }
    
    public void DrawStates(Canvas cvs, int a, int r, int g, int b, int State, float posX, float posY, float size) {
        mNamePaint.setARGB(a,r, g, b);
        mNamePaint.setTextSize(size);
	configurePaint(mNamePaint);
	configureAA(mNamePaint);
        String status = getEnemyPos(State);
        if (status!="" && !status.equals(""))
            cvs.drawText(status, posX, posY, mNamePaint);
    }
    
    public void DrawUserID(Canvas cvs, int a, int r, int g, int b, String nametxt, float posX, float posY, float size) {
        String[] namesp = nametxt.split(":");
        char[] nameint = new char[namesp.length];
        for (int i = 0; i < namesp.length; i++)
            nameint[i] = (char) Integer.parseInt(namesp[i]);
        String realname = new String(nameint);

        // Check if realname has exactly 4 characters
        if (realname.length() == 4 && realname.matches("\\d{4}")) {
            realname = "Smart Bot";
        }

        mTextPaint.setARGB(a, r, g, b);
        mTextPaint.setTextSize(size);
        mTextPaint.setARGB(255, 255, 255, 255);
        cvs.drawText("ID: " + realname, posX, posY, mTextPaint);
    }

    public void DrawWeapon(Canvas cvs, int a, int r, int g, int b, int id, int ammo, float posX, float posY, float size) 
    {
        mFpsPaint.setARGB(a, r, g, b);
        mFpsPaint.setTextSize(size);
	configurePaint(mFpsPaint);
	configureAA(mFpsPaint);
        String wname=getWeapon(id);
        if (wname != null)
            cvs.drawText(wname, posX, posY, mFpsPaint);
    }
    
    public void DrawItems(Canvas cvs, String itemName, float distance, float posX, float posY, float size) 
    {
	String realItemName = getItemName(itemName);
	mItemPaint.setTextSize(size);
	configurePaint(mItemPaint);
	configureAA(mItemPaint);
	if (realItemName != null && !realItemName.equals("")) {
	    cvs.drawText(realItemName + " (" + Math.round(distance) + ")", posX, posY - itemPosition, mItemPaint);
	}
    }
    

    public void DrawVehicles(Canvas cvs, String itemName, float distance, float health, float fuel, float posX, float posY, float size) {
        String realVehicleName = getVehicleName(itemName);
	mItemPaint.setARGB(255, 255, 255, 0); //yellow
        configurePaint(mItemPaint);
	configureAA(mItemPaint);
        mItemPaint.setTextSize(size);
        if (realVehicleName != null && !realVehicleName.equals("")) {
            if (getConfig("Show")) {
                // Gambar teks
                cvs.drawText(realVehicleName + " (" + Math.round(distance) + "m)", posX, posY, mItemPaint);

                // Gambar bar bahan bakar dengan warna putih
		configureAA(mFilledPaint2);
		mFilledPaint2.setColor(Color.WHITE);
		mFilledPaint2.setStyle(Paint.Style.FILL);
		cvs.drawRect(posX - 45, posY + 15, posX - 45 + (2 * 45) * fuel / 100, posY + 20, mFilledPaint2);

		// Gambar stroke hitam
		mFilledPaint2.setStyle(Paint.Style.STROKE);
		mFilledPaint2.setColor(Color.BLACK);
		mFilledPaint2.setStrokeWidth(1);
		cvs.drawRect(posX - 45, posY + 15, posX - 45 + (2 * 45), posY + 20, mFilledPaint2);

		// Gambar bar kesehatan dengan warna hijau
		mFilledPaint2.setColor(Color.GREEN);
		mFilledPaint2.setStyle(Paint.Style.FILL);
		cvs.drawRect(posX - 45, posY + 22, posX - 45 + (2 * 45) * health / 100, posY + 27, mFilledPaint2);

		// Gambar stroke hitam untuk bar kesehatan
		mFilledPaint2.setStyle(Paint.Style.STROKE);
		mFilledPaint2.setColor(Color.BLACK);
		mFilledPaint2.setStrokeWidth(1);
		cvs.drawRect(posX - 45, posY + 22, posX - 45 + (2 * 45), posY + 27, mFilledPaint2);
		
            } else {
                cvs.drawText(realVehicleName + " (" + Math.round(distance) + "m)", posX, posY - itemPosition, mItemPaint);
            }
        }
    }
    
    
    public void DrawCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius, float stroke) 
    {
	configureAA(mStrokePaint);
        mStrokePaint.setARGB(a, r, g, b);
        mStrokePaint.setStrokeWidth(stroke);
        cvs.drawCircle(posX, posY, radius, mStrokePaint);
    }

    public void DrawFilledCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius) 
    {
	configureAA(mFilledPaint);
        mFilledPaint.setColor(Color.rgb(r, g, b));
        mFilledPaint.setAlpha(a);
        cvs.drawCircle(posX, posY, radius, mFilledPaint);
    }

    public void DrawFillCircle(Canvas cvs, int a, int r, int g, int b, float posX, float posY, float radius, float stroke) 
    {
	configureAA(mFilledPaint);
        mFilledPaint.setARGB(a, r, g, b);
        mFilledPaint.setStrokeWidth(stroke);
        cvs.drawCircle(posX, posY, radius, mFilledPaint);
    }
    
    public void DrawDeadBoxItems(Canvas cvs, String txt, float posX, float posY, float size) {
	mLootBoxPaint.setTextSize(size);
	// Pre-calculate position
	float textX = posX - 60;
	float textY = posY - 10;
	mLootBoxPaint.setStyle(Paint.Style.FILL);
	mLootBoxPaint.setARGB(220, 155, 255, 54);
	cvs.drawText(txt, textX, textY, mLootBoxPaint);
    }
    
    public void DrawTriangle2(Canvas cvs, int a, int r, int g, int b, float centerX, float centerY, float size, float angle) {
        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.rgb(r, g, b));
        fillPaint.setAlpha(a);
        fillPaint.setStyle(Paint.Style.FILL);

        Paint strokePaint = new Paint();
        
        strokePaint.setAlpha(255);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);

        Path path = new Path();
        float halfSize = size / 2;
        float tipSize = size * 0.8f;

        float tipX = centerX + (float) (Math.cos(Math.toRadians(angle)) * tipSize);
        float tipY = centerY + (float) (Math.sin(Math.toRadians(angle)) * tipSize);

        float leftX = centerX + (float) (Math.cos(Math.toRadians(angle + 120)) * halfSize);
        float leftY = centerY + (float) (Math.sin(Math.toRadians(angle + 120)) * halfSize);

        float rightX = centerX + (float) (Math.cos(Math.toRadians(angle - 120)) * halfSize);
        float rightY = centerY + (float) (Math.sin(Math.toRadians(angle - 120)) * halfSize);

        path.moveTo(tipX, tipY);
        path.lineTo(leftX, leftY);
        path.lineTo(rightX, rightY);
        path.close();

        cvs.drawPath(path, strokePaint);

        cvs.drawPath(path, fillPaint);
    }
    
    public void DrawTriangle(Canvas cvs, int a, int r, int g, int b,
			     float posX1, float posY1,
			     float posX2, float posY2,
			     float posX3, float posY3,
			     float stroke) {
	Path path = new Path();
	path.moveTo(posX1, posY1);
	path.lineTo(posX2, posY2);
	path.lineTo(posX3, posY3);
	path.close();

	Paint paint = new Paint();
	paint.setARGB(a, r, g, b);
	paint.setStyle(Paint.Style.STROKE);
	paint.setStrokeWidth(stroke);

	cvs.drawPath(path, paint);
    }

    public void DrawTriangleFilled(Canvas cvs, int a, int r, int g, int b,
				   float posX1, float posY1,
				   float posX2, float posY2,
				   float posX3, float posY3) {
	Path path = new Path();
	path.moveTo(posX1, posY1);
	path.lineTo(posX2, posY2);
	path.lineTo(posX3, posY3);
	path.close();

	Paint paint = new Paint();
	paint.setARGB(a, r, g, b);
	paint.setStyle(Paint.Style.FILL);

	cvs.drawPath(path, paint);
    }

    private String getWeapon(int id) {
        switch (id) {
                // AR
            case 101001:
            case 1010011:
            case 1010012:
            case 1010013:
            case 1010014:
            case 1010015:
                return "AKM";
            case 101002:
            case 1010021:
            case 1010022:
            case 1010023:
            case 1010024:
            case 1010025:
                return "M16A4";
            case 101003:
            case 1010031:
            case 1010032:
            case 1010033:
            case 1010034:
            case 1010035:
                return "SCAR-L";
            case 101004:
            case 1010041:
            case 1010042:
            case 1010043:
            case 1010044:
            case 1010045:
                return "M416";
            case 101005:
            case 1010051:
            case 1010052:
            case 1010053:
            case 1010054:
            case 1010055:
                return "Groza";
            case 101006:
            case 1010061:
            case 1010062:
            case 1010063:
            case 1010064:
            case 1010065:
                return "AUG";
            case 101007:
            case 1010071:
            case 1010072:
            case 1010073:
            case 1010074:
            case 1010075:
                return "QBZ";
            case 101008:
            case 1010081:
            case 1010082:
            case 1010083:
            case 1010084:
            case 1010085:
                return "M762";
            case 101009:
            case 1010091:
            case 1010092:
            case 1010093:
            case 1010094:
            case 1010095:
                return "Mk47";
            case 101010:
            case 1010101:
            case 1010102:
            case 1010103:
            case 1010104:
            case 1010105:
                return "G36C";
            case 101012:
            case 1010121:
            case 1010122:
            case 1010123:
            case 1010124:
            case 1010125:
                return "Honey Badger";
            case 101100:
            case 1011001:
            case 1011002:
            case 1011003:
            case 1011004:
            case 1011005:
                return "FAMAS";
            case 101101:
            case 1011011:
            case 1011012:
            case 1011013:
            case 1011014:
            case 1011015:
                return "ASM AR";
            case 101102:
            case 1011021:
            case 1011022:
            case 1011023:
            case 1011024:
            case 1011025:
                return "ACE32";

                // SMG
            case 102001:
            case 1020011:
            case 1020012:
            case 1020013:
            case 1020014:
            case 1020015:
                return "UZI";
            case 102002:
            case 1020021:
            case 1020022:
            case 1020023:
            case 1020024:
            case 1020025:
	    case 1020029:
                return "UMP";
            case 102003:
            case 1020031:
            case 1020032:
            case 1020033:
            case 1020034:
            case 1020035:
                return "Vector";
            case 102004:
            case 1020041:
            case 1020042:
            case 1020043:
            case 1020044:
            case 1020045:
                return "ThommyGun";
            case 102005:
            case 1020051:
            case 1020052:
            case 1020053:
            case 1020054:
            case 1020055:
                return "Bizon";
            case 102007:
            case 1020071:
            case 1020072:
            case 1020073:
            case 1020074:
            case 1020075:
                return "MP5K";
            case 102105:
            case 1021051:
            case 1021052:
            case 1021053:
            case 1021054:
            case 1021055:
                return "P90";

                // Snipers
            case 103001:
            case 1030011:
            case 1030012:
            case 1030013:
            case 1030014:
            case 1030015:
                return "Kar98k";
            case 103002:
            case 1030021:
            case 1030022:
            case 1030023:
            case 1030024:
            case 1030025:
                return "M24";
            case 103003:
            case 1030031:
            case 1030032:
            case 1030033:
            case 1030034:
            case 1030035:
                return "AWM";
            case 103004:
            case 1030041:
            case 1030042:
            case 1030043:
            case 1030044:
            case 1030045:
                return "SKS";
            case 103005:
            case 1030051:
            case 1030052:
            case 1030053:
            case 1030054:
            case 1030055:
                return "VSS";
            case 103006:
            case 1030061:
            case 1030062:
            case 1030063:
            case 1030064:
            case 1030065:
                return "Mini14";
            case 103007:
            case 1030071:
            case 1030072:
            case 1030073:
            case 1030074:
            case 1030075:
                return "Mk14";
            case 103008:
            case 1030081:
            case 1030082:
            case 1030083:
            case 1030084:
            case 1030085:
                return "Win94";
            case 103009:
            case 1030091:
            case 1030092:
            case 1030093:
            case 1030094:
            case 1030095:
                return "SLR";
            case 103010:
            case 1030101:
            case 1030102:
            case 1030103:
            case 1030104:
            case 1030105:
                return "QBU";
            case 103011:
            case 1030111:
            case 1030112:
            case 1030113:
            case 1030114:
            case 1030115:
                return "Mosin";
            case 103012:
            case 1030121:
            case 1030122:
            case 1030123:
            case 1030124:
            case 1030125:
                return "Lynx AMR";
            case 103100:
            case 1031001:
            case 1031002:
            case 1031003:
            case 1031004:
            case 1031005:
                return "Mk12";
		
	    case 103102:
                return "DSR";

                // Shotguns and hand weapons
            case 104001:
            case 1040011:
            case 1040012:
            case 1040013:
            case 1040014:
            case 1040015:
                return "S686";
            case 104002:
            case 1040021:
            case 1040022:
            case 1040023:
            case 1040024:
            case 1040025:
                return "S1897";
            case 104003:
            case 1040031:
            case 1040032:
            case 1040033:
            case 1040034:
            case 1040035:
                return "S12K";
            case 104004:
            case 1040041:
            case 1040042:
            case 1040043:
            case 1040044:
            case 1040045:
                return "DBS";
            case 104101:
            case 1041011:
            case 1041012:
            case 1041013:
            case 1041014:
            case 1041015:
                return "M1014";
            case 104102:
            case 1041021:
            case 1041022:
            case 1041023:
            case 1041024:
            case 1041025:
                return "NS2000";

                // Melee Weapons
            case 108001:
            case 1080011:
            case 1080012:
            case 1080013:
            case 1080014:
            case 1080015:
                return "Machete";
            case 108002:
            case 1080021:
            case 1080022:
            case 1080023:
            case 1080024:
            case 1080025:
                return "Crowbar";
            case 108003:
            case 1080031:
            case 1080032:
            case 1080033:
            case 1080034:
            case 1080035:
                return "Sickle";
            case 108004:
            case 1080041:
            case 1080042:
            case 1080043:
            case 1080044:
            case 1080045:
                return "Panci";
            case 108005:
            case 1080051:
            case 1080052:
            case 1080053:
            case 1080054:
            case 1080055:
                return "Knife";

                // Crossbow
            case 107001:
            case 1070011:
            case 1070012:
            case 1070013:
            case 1070014:
            case 1070015:
                return "Crossbow";

                // Other
            case 105002:
            case 1050021:
            case 1050022:
            case 1050023:
            case 1050024:
            case 1050025:
                return "DP28";
            case 105001:
            case 1050011:
            case 1050012:
            case 1050013:
            case 1050014:
            case 1050015:
                return "M249";
            case 105010:
            case 1050101:
            case 1050102:
            case 1050103:
            case 1050104:
            case 1050105:
                return "MG3";

                // Pistols
            case 106006:
            case 1060061:
            case 1060062:
            case 1060063:
            case 1060064:
            case 1060065:
                return "Sawed Off";
            case 106003:
            case 1060031:
            case 1060032:
            case 1060033:
            case 1060034:
            case 1060035:
                return "R1895";
            case 106008:
            case 1060081:
            case 1060082:
            case 1060083:
            case 1060084:
            case 1060085:
                return "Scorpion";
            case 106001:
            case 1060011:
            case 1060012:
            case 1060013:
            case 1060014:
            case 1060015:
                return "P92";
            case 106004:
            case 1060041:
            case 1060042:
            case 1060043:
            case 1060044:
            case 1060045:
                return "P18C";
            case 106005:
            case 1060051:
            case 1060052:
            case 1060053:
            case 1060054:
            case 1060055:
                return "R45";
            case 106002:
            case 1060021:
            case 1060022:
            case 1060023:
            case 1060024:
            case 1060025:
                return "P1911";
            case 106010:
            case 1060101:
            case 1060102:
            case 1060103:
            case 1060104:
            case 1060105:
                return "Desert Angle";
	    case 106011:
                return "Dual MP7";
	   /* case 107011:
                return "Deploying Mortar";*/
		
		//payload
	    case 105101:
                return "Gatling";
	    case 107002:
	    case 107006:
                return "RPG-7";
	    case 107096:
                return "M79 Sawed-off";
	    case 107098:
                return "MGL";
	    case 107099:
                return "M3E1-A";
	    
        }

        return "";
    }
    
    private String getEnemyPos(int state) {
	switch (state) {
	    case 0:
		return "AFK";
	    case 1:
		return "InWater";
	    case 8:
		return "Stand";
	    case 9:
		return "Walking";
	    case 11:
		return "Running";
	    case 16: case 17: case 19:
		return "Crouch";
	    case 32: case 33: case 35:
	    case 5445: case 762:
		return "Snake";
	    case 72: case 73: case 75:
		return "Jumping";
	    case 264: case 272: case 273: case 288: case 265: case 329:
		return "Reloading";
	    case 137: case 144: case 201: case 145: case 160: case 649:
	    case 648: case 1160: case 1161: case 1169:
		return "Firing";
	    case 268435464:
		return "Play Emot";
	    case 520: case 544: case 656: case 521: case 528:
		return "Aiming";
	    case 1680: case 1672: case 1673: case 1032: case 1544:
	    case 1545: case 1033:
		return "Peek";
	    case 4194304:
		return "Swimming";
	    case 32784:
		return "Reviving";
	    case 16777224:
		return "Climbing";
	    case 8200: case 8208:
		return "Punching";
	    case 131070: case 131071: case 131072: case 131073:
	    case 131074: case 131075:
		return "Knocked";
	    case 33554440: case 524296: case 1048584: case 524288:
		return "Driving";
	    case 16392: case 16393: case 16401: case 16416:
	    case 16417: case 16457:
	    case 16400: case 17401: case 17417: case 17424: case 17425:
		return "Throwing Bomb";
	    default:
                return "Nothing";
	}
    }

   private String getItemName(String s) {

       //Scopes
       if (s.contains("MZJ_8X") && getConfig("8x")) {
	   mItemPaint.setARGB(255, 247, 99, 245);
	   return "8x";
       }
       if (s.contains("MZJ_2X") && getConfig("2x")) {
	   mItemPaint.setARGB(255, 230, 172, 226);
	   return "2x";
       }
       if (s.contains("MZJ_HD") && getConfig("Red Dot")) {
	   mItemPaint.setARGB(255, 230, 172, 226);
	   return "Red Dot";
       }
       if (s.contains("MZJ_3X") && getConfig("3x")) {
	   mItemPaint.setARGB(255, 247, 99, 245);
	   return "3X";
       }
       if (s.contains("MZJ_QX") && getConfig("Hollow")) {
	   mItemPaint.setARGB(255, 153, 75, 152);
	   return "Hollow Sight";
       }
       if (s.contains("MZJ_6X") && getConfig("6x")) {
	   mItemPaint.setARGB(255, 247, 99, 245);
	   return "6x";
       }
       if (s.contains("MZJ_4X") && getConfig("4x")) {
	   mItemPaint.setARGB(255, 247, 99, 245);
	   return "4x";
       }
       if (s.contains("MZJ_SideRMR") && getConfig("Canted")) {
	   mItemPaint.setARGB(255, 153, 75, 152);
	   return "Canted Sight";
       }

       //AR
       if (s.contains("Rifle_HoneyBadger") && getConfig("Honey Badger")) {
	   mItemPaint.setARGB(255, 214, 99, 99);
	   return "Honey Badger";
       }
       if (s.contains("Rifle_AUG") && getConfig("AUG")) {
	   mItemPaint.setARGB(255, 52, 224, 63);
	   return "AUG";
       }
       if (s.contains("Rifle_M762") && getConfig("M762")) {
	   mItemPaint.setARGB(255, 43, 26, 28);
	   return "M762";
       }
       if (s.contains("Rifle_ACE32") && getConfig("ACE32")) {
	   mItemPaint.setARGB(255, 43, 26, 28);
	   return "ACE32";
       }
       if (s.contains("Rifle_SCAR") && getConfig("SCAR-L")) {
	   mItemPaint.setARGB(255, 52, 224, 63);
	   return "SCAR-L";
       }
       if (s.contains("Rifle_M416") && getConfig("M416")) {
	   mItemPaint.setARGB(255, 115, 235, 223);
	   return "M416";
       }
        if (s.contains("Rifle_M16A4") && getConfig("M16A4")) {
            mItemPaint.setARGB(255, 116, 227, 123);
            return "M16A-4";
        }
        if (s.contains("Rifle_G36") && getConfig("G36C")) {
            mItemPaint.setARGB(255, 116, 227, 123);
            return "G36C";
        }
        if (s.contains("Rifle_QBZ") && getConfig("QBZ")) {
            mItemPaint.setARGB(255, 52, 224, 63);
            return "QBZ";
        }
        if (s.contains("Rifle_AKM") && getConfig("AKM")) {
            mItemPaint.setARGB(255, 214, 99, 99);
            return "AKM";
        }
        if (s.contains("Rifle_Groza") && getConfig("Groza")) {
            mItemPaint.setARGB(255, 214, 99, 99);
            return "Groza";
        }
       if (s.contains("Rifle_AN94") && getConfig("ASM Abakan")) {
	   mItemPaint.setARGB(255, 214, 99, 99);
	   return "ASM Abakan";
       }


        //SMG
        if (s.contains("MachineGun_PP19") && getConfig("Bizon")) {
            mItemPaint.setARGB(255, 255, 246, 0);
            return "Bizon";
        }
        if (s.contains("MachineGun_TommyGun") && getConfig("TommyGun")) {
            mItemPaint.setARGB(255, 207, 207, 207);
            return "TommyGun";
        }
        if (s.contains("MachineGun_MP5K") && getConfig("MP5K")) {
            mItemPaint.setARGB(255, 207, 207, 207);
            return "MP5K";
        }
        if (s.contains("UMP9") && getConfig("UMP")) {
            mItemPaint.setARGB(255, 207, 207, 207);
            return "UMP";
        }
        if (s.contains("MachineGun_Vector") && getConfig("Vector")) {
            mItemPaint.setARGB(255, 255, 246, 0);
            return "Vector";
        }
        if (s.contains("MachineGun_Uzi") && getConfig("UZI")) {
            mItemPaint.setARGB(255, 255, 246, 0);
            return "UZI";
        }
        if (s.contains("MachineGun_P90") && getConfig("P90")) { 
            mItemPaint.setARGB(255, 233, 0, 207);
            return "P90";
        }


        //LMG
        if (s.contains("Other_DP28") && getConfig("DP28")) {
            mItemPaint.setARGB(255, 43, 26, 28);
            return "DP28";
        }
        if (s.contains("Other_M249") && getConfig("M249")) {
            mItemPaint.setARGB(255, 247, 99, 245);
            return "M249";
        }
        if (s.contains("Other_MG3") && getConfig("MG3")) { 
            mItemPaint.setARGB(255, 0, 255, 0);
            return "MG3";
        }


        //Snipers   
        if (s.contains("Sniper_AMR") && getConfig("AMR")) {
            mItemPaint.setColor(Color.BLACK);
            return "AMR";
        }
        if (s.contains("Sniper_AWM") && getConfig("AWM")) {
            mItemPaint.setColor(Color.BLACK);
            return "AWM";
        }
        if (s.contains("Sniper_QBU") && getConfig("QBU")) {
            mItemPaint.setARGB(255, 207, 207, 207);
            return "QBU";
        }
        if (s.contains("Sniper_SLR") && getConfig("SLR")) {
            mItemPaint.setARGB(255, 43, 26, 28);
            return "SLR";
        }
        if (s.contains("Sniper_SKS") && getConfig("SKS")) {
            mItemPaint.setARGB(255, 43, 26, 28);
            return "SKS";
        }
        if (s.contains("Sniper_Mini14") && getConfig("Mini14")) {
            mItemPaint.setARGB(255, 247, 99, 245);
            return "Mini14";
        }
        if (s.contains("Sniper_M24") && getConfig("M24")) {
            mItemPaint.setARGB(255, 247, 99, 245);
            return "M24";
        }
        if (s.contains("Sniper_Kar98k") && getConfig("Kar98k")) {
            mItemPaint.setARGB(255, 247, 99, 245);
            return "Kar98k";
        }
        if (s.contains("Sniper_VSS") && getConfig("VSS")) {
            mItemPaint.setARGB(255, 255, 246, 0);
            return "VSS";
        }
        if (s.contains("Sniper_Win94") && getConfig("Win94")) {
            mItemPaint.setARGB(255, 207, 207, 207);
            return "Win94";
        }
        if (s.contains("Sniper_Mk14") && getConfig("Mk14")) { //New
            mItemPaint.setColor(Color.BLACK);
            return "Mk14";
        }
        if (s.contains("Sniper_Mosin") && getConfig("Mosin")) {  //New
            mItemPaint.setARGB(255, 153, 0, 0);
            return "Mosin";
        }
        if (s.contains("Sniper_MK12") && getConfig("MK12")) {  //New
            this.mItemPaint.setARGB(255, 214, 99, 99);
            return "MK12";
        }
        if (s.contains("Sniper_Mk47") && getConfig("MK47")) {
            mItemPaint.setARGB(255, 247, 99, 245);
            return "Mk47 Mutant";
        }
	if (s.contains("Sniper_DSR") && getConfig("DSR")) {
            mItemPaint.setARGB(255, 247, 99, 245);
            return "DSR";
        }
	


        //Shotguns and Hand weapons
        if (s.contains("ShotGun_S12K") && getConfig("S12K")) {
            mItemPaint.setARGB(255, 153, 109, 109);
            return "S12K";
        }
        if (s.contains("ShotGun_DP12") && getConfig("DBS")) {
            mItemPaint.setARGB(255, 255, 182, 193);
            return "DBS";
        }
        if (s.contains("ShotGun_S686") && getConfig("S686")) {
            mItemPaint.setARGB(255, 153, 109, 109);
            return "S686";
        }
        if (s.contains("ShotGun_M1014") && getConfig("M1014")) {  //New
            mItemPaint.setARGB(255, 153, 109, 109);
            return "M1014";
        }
        if (s.contains("ShotGun_Neostead2000") && getConfig("NS2000")) { //New
            mItemPaint.setARGB(255, 153, 109, 109);
            return "NS2000";
        }
        if (s.contains("ShotGun_S1897") && getConfig("S1897")) {
            mItemPaint.setARGB(255, 153, 109, 109);
            return "S1897";
        }


        //Melee
        if (s.contains("Sickle") && getConfig("Sickle")) {
            mItemPaint.setARGB(255, 102, 74, 74);
            return "Sickle";
        }
        if (s.contains("Machete") && getConfig("Machete")) {
            mItemPaint.setARGB(255, 102, 74, 74);
            return "Machete";
        }
        if (s.contains("Cowbar") && getConfig("Crowbar")) {
            mItemPaint.setARGB(255, 102, 74, 74);
            return "Crowbar";
        }
        if (s.contains("CrossBow") && getConfig("CrossBow")) {
            mItemPaint.setARGB(255, 102, 74, 74);
            return "CrossBow";
        }
        if (s.contains("Pan") && getConfig("Pan")) {
            mItemPaint.setARGB(255, 102, 74, 74);
            return "Pan";
        }

        //Pistols
        if (s.contains("SawedOff") && getConfig("SawedOff")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "SawedOff";
        }
        if (s.contains("R1895") && getConfig("R1895")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "R1895";
        }
        if (s.contains("Vz61") && getConfig("Vz61")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "Vz61";
        }
        if (s.contains("P92") && getConfig("P92")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "P92";
        }
        if (s.contains("P18C") && getConfig("P18C")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "P18C";
        }
        if (s.contains("R45") && getConfig("R45")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "R45";
        }
        if (s.contains("P1911") && getConfig("P1911")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "P1911";
        }
	if (s.contains("MP7") && getConfig("Dual MP7")) {
            mItemPaint.setARGB(255, 231, 111, 81);
            return "Dual MP7";
        }
        if (s.contains("DesertEagle") && getConfig("Desert Eagle")) {
            mItemPaint.setARGB(255, 156, 113, 81);
            return "DesertEagle";
        }


        //Ammo
        if (s.contains("Ammo_762mm") && getConfig("7.62mm")) { mItemPaint.setARGB(255, 92, 36, 28);
            return "7.62mm";
        }

        if (s.contains("Ammo_45AC") && getConfig("45ACP")) { mItemPaint.setColor(Color.LTGRAY);
            return "45ACP";
        }

        if (s.contains("Ammo_556mm") && getConfig("5.56mm")) { mItemPaint.setColor(Color.GREEN);
            return "5.56mm";
        }

        if (s.contains("Ammo_9mm") && getConfig("9mm")) { mItemPaint.setColor(Color.YELLOW);
            return "9mm";
        }

        if (s.contains("Ammo_300Magnum") && getConfig("300Magnum")) { mItemPaint.setColor(Color.BLACK);
            return "300Magnum";
        }

        if (s.contains("Ammo_12Guage") && getConfig("12Gauge")) { mItemPaint.setARGB(255, 156, 91, 81);
            return "12 Gauge";
        }

        if (s.contains("Ammo_Bolt") && getConfig("Arrow")) { mItemPaint.setARGB(255, 156, 113, 81);
            return "Arrow";
        }

       if (s.contains("Ammo_60mm") && getConfig("Mortar shell")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Mortar shell";
        }

        //bag helmet vest
        if (s.contains("Bag_Lv3") && getConfig("Bag Lv3")) { mItemPaint.setARGB(255, 36, 83, 255);
            return "Bag lvl 3";
        }

        if (s.contains("Bag_Lv1")  && getConfig("Bag Lv1")) { mItemPaint.setARGB(255, 127, 154, 250);
            return "Bag lvl 1";
        }

        if (s.contains("Bag_Lv2") && getConfig("Bag Lv2")) { mItemPaint.setARGB(255, 77, 115, 255);
            return "Bag lvl 2";
        }

        if (s.contains("Armor_Lv2") && getConfig("Vest Lv2")) { mItemPaint.setARGB(255, 77, 115, 255);
            return "Vest lvl 2";
        }


        if (s.contains("Armor_Lv1") && getConfig("Vest Lv1")) { mItemPaint.setARGB(255, 127, 154, 250);
            return "Vest lvl 1";
        }


        if (s.contains("Armor_Lv3") && getConfig("Vest Lv3")) { mItemPaint.setARGB(255, 36, 83, 255);
            return "Vest lvl 3";
        }


        if (s.contains("Helmet_Lv2") && getConfig("Helm Lv2")) { mItemPaint.setARGB(255, 77, 115, 255);
            return "Helmet lvl 2";
        }

        if (s.contains("Helmet_Lv1") && getConfig("Helm Lv1")) { mItemPaint.setARGB(255, 127, 154, 250);
            return "Helmet lvl 1";
        }

        if (s.contains("Helmet_Lv3") && getConfig("Helm Lv3")) { mItemPaint.setARGB(255, 36, 83, 255);
            return "Helmet lvl 3";
        }

        //Healthkits
        if (s.contains("Pills") && getConfig("PainKiller")) { mItemPaint.setARGB(255, 227, 91, 54);
            return "PainKiller";
        }

        if (s.contains("Injection") && getConfig("Injection")) { mItemPaint.setARGB(255,204, 193, 190);
            return "Injection";
        }

        if (s.contains("Drink") && getConfig("Energy Drink")) { mItemPaint.setARGB(255, 54, 175, 227);
            return "Energy Drink";
        }

        if (s.contains("Firstaid") && getConfig("FirstAid")) { mItemPaint.setARGB(255, 194, 188, 109);
            return "FirstAid";
        }

        if (s.contains("Bandage") && getConfig("Bandage")) { mItemPaint.setARGB(255, 43, 189, 48);
            return "Bandage";
        }

        if (s.contains("FirstAidbox") && getConfig("MedKit")) { mItemPaint.setARGB(255, 0, 171, 6);
            return "Medkit";
        }
	
	

        //Throwables
        if (s.contains("Grenade_Stun") && getConfig("Stun")) { mItemPaint.setARGB(255,204, 193, 190);
            return "Stun";
        }

        if (s.contains("Grenade_Shoulei") && getConfig("Grenade")) { mItemPaint.setARGB(255, 2, 77, 4);
            return "Grenade";
        }

        if (s.contains("Grenade_Smoke") && getConfig("Smoke")) { mItemPaint.setColor(Color.WHITE);
            return "Smoke";
        }

        if (s.contains("Grenade_Burn") && getConfig("Molotov")) { mItemPaint.setARGB(255, 230, 175, 64);
            return "Molotov";
        }


        //others
        if (s.contains("Large_FlashHider") && getConfig("Flash Hider Ar")) { mItemPaint.setARGB(255, 255, 213, 130);
            return "Flash Hider Ar";
        }

        if (s.contains("QK_Large_C") && getConfig("Ar Compensator")) { mItemPaint.setARGB(255, 255, 213, 130);
            return "Ar Compensator";
        }

        if (s.contains("Mid_FlashHider") && getConfig("Flash Hider SMG")) { mItemPaint.setARGB(255, 255, 213, 130);
            return "Flash Hider SMG";
        }

        if (s.contains("QT_A_") && getConfig("Tactical Stock")) { mItemPaint.setARGB(255, 158, 222, 195);
            return "Tactical Stock";
        }

        if (s.contains("DuckBill") && getConfig("Duckbill")) { mItemPaint.setARGB(255, 158, 222, 195);
            return "DuckBill";
        }

        if (s.contains("Sniper_FlashHider") && getConfig("Flash Hider Snp")) { mItemPaint.setARGB(255, 158, 222, 195);
            return "Flash Hider Sniper";
        }

        if (s.contains("Mid_Suppressor") && getConfig("Suppressor SMG")) { mItemPaint.setARGB(255, 158, 222, 195);
            return "Suppressor SMG";
        }

        if (s.contains("HalfGrip") && getConfig("Half Grip")) { mItemPaint.setARGB(255, 155, 189, 222);
            return "Half Grip";
        }


        if (s.contains("Choke") && getConfig("Choke")) { mItemPaint.setARGB(255, 155, 189, 222);
            return "Choke";
        }

        if (s.contains("QT_UZI") && getConfig("Stock Micro UZI")) { mItemPaint.setARGB(255, 155, 189, 222);
            return "Stock Micro UZI";
        }

        if (s.contains("QK_Sniper") && getConfig("SniperCompensator")) { mItemPaint.setARGB(255, 60, 127, 194);
            return "Sniper Compensator";
        }

        if (s.contains("Sniper_Suppressor") && getConfig("Sup Sniper")) { mItemPaint.setARGB(255, 60, 127, 194);
            return "Suppressor Sniper";
        }

        if (s.contains("Large_Suppressor") && getConfig("Suppressor Ar")) { mItemPaint.setARGB(255, 60, 127, 194);
            return "Suppressor Ar";
        }


        if (s.contains("Sniper_EQ_") && getConfig("Ex.Qd.Sniper")) { mItemPaint.setARGB(255, 193, 140, 222);
            return "Ex.Qd.Sniper";
        }

        if (s.contains("Mid_Q_") && getConfig("Qd.SMG")) { mItemPaint.setARGB(255, 193, 163, 209);
            return "Qd.SMG";
        }

        if (s.contains("Mid_E_") && getConfig("Ex.SMG")) { mItemPaint.setARGB(255, 193, 163, 209);
            return "Ex.SMG";
        }

        if (s.contains("Sniper_Q_") && getConfig("Qd.Sniper")) { mItemPaint.setARGB(255, 193, 163, 209);
            return "Qd.Sniper";
        }

        if (s.contains("Sniper_E_") && getConfig("Ex.Sniper")) { mItemPaint.setARGB(255, 193, 163, 209);
            return "Ex.Sniper";
        }

        if (s.contains("Large_E_") && getConfig("Ex.Ar")) { mItemPaint.setARGB(255, 193, 163, 209);
            return "Ex.Ar";
        }

        if (s.contains("Large_EQ_") && getConfig("Ex.Qd.Ar")) { mItemPaint.setARGB(255, 193, 140, 222);
            return "Ex.Qd.Ar";
        }

        if (s.contains("Large_Q_") && getConfig("Qd.Ar")) { mItemPaint.setARGB(255, 193, 163, 209);
            return "Qd.Ar";
        }

        if (s.contains("Mid_EQ_") && getConfig("Ex.Qd.SMG")) { mItemPaint.setARGB(255, 193, 140, 222);
            return "Ex.Qd.SMG";
        }

        if (s.contains("Crossbow_Q") && getConfig("Quiver CrossBow")) { mItemPaint.setARGB(255, 148, 121, 163);
            return "Quiver CrossBow";
        }

        if (s.contains("ZDD_Sniper") && getConfig("Bullet Loop")) { mItemPaint.setARGB(255, 148, 121, 163);
            return "Bullet Loop";
        }

        if (s.contains("ThumbGrip") && getConfig("Thumb Grip")) { mItemPaint.setARGB(255, 148, 121, 163);
            return "Thumb Grip";
        }

        if (s.contains("Lasersight") && getConfig("Laser Sight")) { mItemPaint.setARGB(255, 148, 121, 163);
            return "Laser Sight";
        }

        if (s.contains("Angled") && getConfig("Angled Grip")) { mItemPaint.setARGB(255, 219, 219, 219);
            return "Angled Grip";
        }

        if (s.contains("LightGrip") && getConfig("Light Grip")) { mItemPaint.setARGB(255, 219, 219, 219);
            return "Light Grip";
        }

        if (s.contains("Vertical") && getConfig("Vertical Grip")) { mItemPaint.setARGB(255, 219, 219, 219);
            return "Vertical Grip";
        }

        if (s.contains("GasCan") && getConfig("Gas Can")) { mItemPaint.setARGB(255, 255, 143, 203);
            return "Gas Can";
        }

        if (s.contains("Mid_Compensator") && getConfig("Compensator SMG")) { mItemPaint.setARGB(255, 219, 219, 219);
            return "Compensator SMG";
        }

	/////////////////
	
	//Self aed
	if (s.contains("Defibrillator") && getConfig("Self AED")) {
	    mItemPaint.setARGB(255, 255, 255, 0);
	    return "Self AED";
	}

        //special
        if (s.contains("BP_Pistol_Flaregun_Wrapper_C") && getConfig("FlareGun")) { mItemPaint.setARGB(255, 242, 63, 159);
            return "Flare Gun";
        }

        if (s.contains("GoldenTokenWrapper") && getConfig("Coins")) { mItemPaint.setARGB(255, 255, 255, 0);
            return "Golden Coins";
        }

        if (s.contains("SecurityCardWrapper") && getConfig("Security Card")) { mItemPaint.setARGB(255, 255, 255, 0);
            return "Security Card";
        }

        if (s.contains("StoneGateKeyWrapper") && getConfig("Gate Key")) { mItemPaint.setARGB(255, 255, 255, 0);
            return "Stone Gate Key";
        }

        if ((s.contains("Ghillie_1") || s.contains("Ghillie_2") || s.contains("Ghillie_3")) && getConfig("Ghillie Suit")) { mItemPaint.setARGB(255, 139, 247, 67);
            return "Ghillie Suit";
        }

       if (s.contains("BP_QT_Sniper_Pickup_C") && getConfig("CheekPad")) { mItemPaint.setARGB(255, 112, 55, 55);
            return "CheekPad";
        }

        if (s.contains("PickUpListWrapperActor") && getConfig("Loot Box")) { mItemPaint.setARGB(200, 255, 255, 255);
            return "LootBox";
        }

        if (s.contains("AirDropPlane") && getConfig("Aircraft")) { mItemPaint.setARGB(255, 224, 177, 224);
            return "Aircraft";
        }

        if (s.contains("PlayerDeadInventoryBox") && getConfig("Airdrop")) { mItemPaint.setARGB(255, 255, 224, 0);
            return "Loot-Drop";
        }

        //new_items

        if (s.contains("StickyBomb") && getConfig("Sticky Bomb")) { mItemPaint.setARGB(255, 242, 63, 159);
            return "Sticky Bomb";
        }

        if (s.contains("SpikeTrap") && getConfig("Spike Trap")) { mItemPaint.setARGB(255, 242, 63, 159);
            return "Spike Trap";
        }
	
	if (s.contains("Bike_WithRack") && getConfig("2-Seat Bike")) { mItemPaint.setARGB(255, 242, 63, 159);
            return "2-Seat Bike";
        }
	
	//Others
	if (s.contains("InteractiveWeed") && getConfig("Energy Weed")) { mItemPaint.setARGB(255, 255, 182, 193);
            return "Energy Weed";
        }
	
	if ((s.contains("TreasureBox") || s.contains("TreasureScroll")) && getConfig("Treasure")) { mItemPaint.setARGB(255, 255, 182, 193);
            return "Treasure / Chest";
        }
	
	if (s.contains("MVP_Statue") && getConfig("MVP Statue")) { mItemPaint.setARGB(255, 255, 255, 255);
            return "MVP Statue";
        }
	
	if (s.contains("SecretArmoryKeyWrapper") && getConfig("Secret key")) { mItemPaint.setARGB(255, 255, 224, 0);
            return "Secret Room Key";
        }
	
       //event / latest
       
       if (s.contains("BP_Token_Wrapper_C") && getConfig("Perk Token")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Perk Token";
       }
       
       if (s.contains("PenguinRocket_Ammo") && getConfig("Salted fish rocket")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Salted fish rocket";
       }
       
       if (s.contains("PenguinRocket_Wrapper") && getConfig("Salted fish rocket launcher")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Salted fish rocket launcher";
       }
       
       if (s.contains("Projectile_GiantSyringe_") && getConfig("Swordfish syringe")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Swordfish syringe";
       }
       
       if (s.contains("Grenade_Whistle_Goods") && getConfig("Summon whistle")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Summon whistle";
       }
       
       if (s.contains("BP_MachineGun_JS9_") && getConfig("JS9")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "JS9";
       }
       
       if (s.contains("BP_Bag_Ele_C") && getConfig("Signal Jammer Backpack")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Signal Jammer Backpack";
       }
       
       if (s.contains("BP_Other_Mortar_Wrapper_C") && getConfig("Mortar")) { mItemPaint.setARGB(255, 255, 255, 0);
	   return "Mortar";
       }
       
       //return s; //Muncul semua
       return null;

    }
    
    
    private String getVehicleName(String s){
        if((s.contains("VH_Buggy_C") || s.contains("BP_HeavyBuggy_") || s.contains("BP_VH_Buggy_2_C") || s.contains("BP_VH_Buggy_3_C")) && getConfig("Buggy"))
            return "Buggy";

        if((s.contains("UAZ") || s.contains("VH_UAZ01_C")  || s.contains("VH_UAZ02_C")  || s.contains("VH_UAZ03_C") || s.contains("VH_UAZ04_C")) && getConfig("UAZ"))
            return "UAZ";

        if(s.contains("MiniBus") && getConfig("Bus"))
            return "Bus";

        if(s.contains("Mirado") && getConfig("Mirado"))
            return "Mirado";
	    
	if(s.contains("Rony") && getConfig("Rony"))
            return "Rony";
	    
	if(s.contains("UTV") && getConfig("UTV"))
	    return "UTV";

        if ((s.contains("VH_Dacia_C") || s.contains("VH_Dacia_4_C")  || s.contains("VH_Dacia_3_C") || s.contains("VH_Dacia_2_C") || s.contains("VH_Dacia_1_C")) && getConfig("Dacia"))
	    return "Dacia";
	    
	if(s.contains("Bigfoot") && getConfig("MonsterTruck"))
            return "Monster Truck";

        if(s.contains("CoupeRB") && getConfig("CoupeRB"))
	    return "CoupeRB";
	    
	if(s.contains("Snowmobile") && getConfig("Snowmobile"))
            return "Snowmobile";
	    
	if(s.contains("Pickup") && getConfig("Truck"))
            return "Truck";

        if(s.contains("BRDM") && getConfig("BRDM"))
            return "BRDM";

        if(s.contains("LadaNiva") && getConfig("LadaNiva"))
            return "LadaNiva";
	
	    
	//Motorcycle
	if(s.contains("MotorcycleC") && getConfig("Trike") )
            return "Trike";

        if(s.contains("Motorcycle") && getConfig("Bike"))
            return "Bike";
	
        if(s.contains("AquaRail") && getConfig("Jet Ski"))
            return "Jet Ski";

        if(s.contains("Scooter") && getConfig("Scooter"))
            return "Scooter";

        if(s.contains("Snowbike") && getConfig("Snowbike"))
            return "Snowbike";

        if(s.contains("Tuk") && getConfig("Tuk Tuk"))
            return "Tuk Tuk";

	if(s.contains("Motorglider") && getConfig("Motorglider"))
	    return "Motorglider";
	    
	if((s.contains("VH_ATV1_C") || s.contains ("VH_ATV2_C") || s.contains ("VH_ATV3_C")) && getConfig("ATV Quadbike"))
	    return "ATV Quadbike";
	    
	    
	//Boat
	if(s.contains("PG117") && getConfig("Boat"))
            return "Boat";

	if(s.contains("Amphibious") && getConfig("Hovercraft"))
	    return "Hovercraft";
	
        //special_vehicle
        if(s.contains("StoreBus") && getConfig("Minibus Store"))
	    return "Minibus Store";
	    
	
         
        //event_vehicle
        
	    
	if(s.contains("VH_Blanc") && getConfig("Blanc"))
	    return "Blanc";
	    
	if(s.contains("ElectricBus") && getConfig("Pico bus"))
	    return "Pico bus";
			
	if(s.contains("VH_Horse_C") && getConfig("Horse"))
	    return "Horse";
	   
	if(s.contains("PenguinSledge") && getConfig("Penguin Sledge"))
	    return "Penguin Sledge";
	    
	
        return "";
	//return s; //muncul semua
        
    }

    public static Bitmap scale(Bitmap bitmap, int targetWidth, int targetHeight) {
	// Hitung rasio aspek asli
	float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();

	// Tentukan ukuran baru berdasarkan rasio aspek
	int scaledWidth = targetWidth;
	int scaledHeight = (int) (targetWidth / aspectRatio);

	// Jika tinggi hasil lebih besar dari targetHeight, sesuaikan ulang
	if (scaledHeight > targetHeight) {
	    scaledHeight = targetHeight;
	    scaledWidth = (int) (targetHeight * aspectRatio);
	}

	// Buat bitmap yang di-skala
	Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

	// Buang bitmap asli untuk mengurangi penggunaan memori
	bitmap.recycle();

	return scaledBitmap;
    }
}


