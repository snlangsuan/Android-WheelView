package me.decimo.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * Created by Sak-Ka-RIN on 7/5/2558.
 */

public class WheelView extends View implements View.OnTouchListener {
    private Context context;
    private int attrDrawableWheel, attrDrawablePointer;
    private boolean attrIsClockwise = true;
    private int attrRotateDegree = 45;
    private float attrScalePointer = 0.5f;
    private int attrDelayEndTime = 0;
    private Bitmap wheelBitmap, pointerBitmap;
    private int pointerX = 0;
    private int pointerY = 0;

    private AnimateDrawable mWheelDrawable;
    private Drawable wheelMovingDot;
    private rotateAnimation anWheel;
    private int spin_round = 6;
    private int duration = 4700;

    private AnimateDrawable mPointerDrawable;
    private Drawable pointerMovingDot;
    private rotateAnimation anPointer;

    private SoundPool soundEffect = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    int soundId, streamId;

    private OnSpinListener spinListener;
    private OnTouchSpinListener touchSpinListener;
    private boolean isSpining = false;

    private final String TAG = "WHEELVIEW";

    private Handler handler;

    public WheelView(Context context) {
        super(context);
        this.context = context;
        this.soundId = soundEffect.load(context, R.raw.wheel_spin, 1);

        this.setOnTouchListener(this);
        this.handler = new Handler();
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WheelView, 0, 0);

        try {
            this.attrDrawableWheel = a.getResourceId(R.styleable.WheelView_drawableWheel, R.drawable.default_wheel);
            this.attrDrawablePointer = a.getResourceId(R.styleable.WheelView_drawablePointer, R.drawable.default_pointer);
            this.attrIsClockwise = a.getBoolean(R.styleable.WheelView_isClockwise, true);
            this.attrRotateDegree = a.getInt(R.styleable.WheelView_rotateDegree, 45);
            this.attrScalePointer = a.getFloat(R.styleable.WheelView_scalePointer, 0.5f);
            this.attrDelayEndTime = a.getInt(R.styleable.WheelView_delayEndTime, 0);
        } finally {
            a.recycle();
        }

        if ( this.attrScalePointer > 1 ) {
            this.attrScalePointer = 1;
        }

        if ( this.attrScalePointer < 0) {
            this.attrScalePointer = 0;
        }

        if ( this.attrDelayEndTime < 0) {
            this.attrDelayEndTime = 0;
        }

        if ( !this.attrIsClockwise ) {
            this.attrRotateDegree = this.attrRotateDegree * -1;
        }
        
        this.soundId = soundEffect.load(context, R.raw.wheel_spin, 1);
        this.handler = new Handler();
    }

    public void setDrawableWheel(int resId) {
        this.attrDrawableWheel = resId;
    }

    public int getDrawableWheel() {
        return this.attrDrawableWheel;
    }

    public void setDrawablePointer(int resId) {
        this.attrDrawablePointer = resId;
    }

    public int getDrawablePointer() {
        return this.attrDrawablePointer;
    }

    public void setClockwise(boolean isClockwise) {
        this.attrIsClockwise = isClockwise;
    }

    public boolean getClockwise() {
        return this.attrIsClockwise;
    }

    public void setRotateDegree(int degree) {

        if ( !this.attrIsClockwise ) {
            this.attrRotateDegree = degree * -1;
        } else {
            this.attrRotateDegree = degree;
        }
    }

    public int getRotateDegree() {
        return this.attrRotateDegree;
    }

    public void setScalePointer(float scale) {

        this.attrScalePointer = scale;

        if ( this.attrScalePointer > 1 ) {
            this.attrScalePointer = 1;
        }

        if ( this.attrScalePointer < 0) {
            this.attrScalePointer = 0;
        }
    }

    public float getScalePointer() {
        return this.attrScalePointer;
    }

    public void setDelayEndTime(int time) {
        this.attrDelayEndTime = time;

        if ( this.attrDelayEndTime < 0) {
            this.attrDelayEndTime = 0;
        }
    }

    public int getDelayEndTime() {
        return this.attrDelayEndTime;
    }

    public void setOnSpinListener(OnSpinListener listener) {
        this.spinListener = listener;
    }

    public void setOnTouchSpinListener(OnTouchSpinListener listener) {
        this.touchSpinListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWheelDrawable.draw(canvas);
        mPointerDrawable.draw(canvas);

        if ( touchSpinListener != null ) {
            this.setOnTouchListener(this);
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        //Log.i("WheelView", "view width => " + parentWidth);
        wheelBitmap = BitmapFactory.decodeResource(context.getResources(), attrDrawableWheel);
        wheelBitmap = Bitmap.createScaledBitmap(wheelBitmap, parentWidth, parentWidth, false);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap tmp = BitmapFactory.decodeResource(context.getResources(), attrDrawablePointer, options);
        int pointerHeight = options.outHeight;
        int pointerWidth = options.outWidth;
        int newPointerWidth = (int)( parentWidth * attrScalePointer );
        int newPointerHeight = (int)( ( (float)pointerHeight/(float) pointerWidth ) * newPointerWidth );

        pointerX = ( parentWidth / 2 ) - ( newPointerWidth / 2 );
        pointerY = ( parentWidth / 2 ) - ( newPointerHeight / 2 );
        //Log.i("WheelView", "pointer old => " + pointerWidth + " x " + pointerHeight + " new => " + newPointerWidth + " x " + newPointerHeight + " top => " + pointerY + " left => " + pointerX);
        options = null;
        tmp = null;
        pointerBitmap = BitmapFactory.decodeResource(context.getResources(), attrDrawablePointer);
        pointerBitmap = Bitmap.createScaledBitmap(pointerBitmap, newPointerWidth, newPointerHeight, false);
        this.setMeasuredDimension(parentWidth, parentWidth);

        this.wheelMovingDot = context.getResources().getDrawable(attrDrawableWheel);
        this.wheelMovingDot.setBounds(0, 0, wheelBitmap.getWidth(), wheelBitmap.getHeight());
        rotate(0, 0, 50);

        this.pointerMovingDot = context.getResources().getDrawable(attrDrawablePointer);
        this.pointerMovingDot.setBounds(pointerX, pointerY, (pointerBitmap.getWidth() + pointerX), (pointerBitmap.getHeight() + pointerY));
        meter(0, 0, 50);
    }

    private void rotate(float startAngle, float endAngle, int duration) {
        anWheel = new rotateAnimation(startAngle, endAngle, Animation.ABSOLUTE, ( wheelBitmap.getWidth() / 2 ), Animation.ABSOLUTE, ( wheelBitmap.getHeight() / 2 ));
        anWheel.setInterpolator(new DecelerateInterpolator());
        anWheel.setDuration(duration);
        anWheel.setFillEnabled(true);
        anWheel.setFillAfter(true);
        anWheel.setRepeatCount(0);
        anWheel.initialize(0, 0, wheelBitmap.getWidth(), wheelBitmap.getHeight());
        setFocusable(true);
        mWheelDrawable = new AnimateDrawable(wheelMovingDot, anWheel);
        anWheel.startNow();

    }

    private void meter(int round, float endAngle, int duration) {
        if ( round <= 0 ) {
            round = 1;
        }

        anPointer = new rotateAnimation(0, endAngle, Animation.ABSOLUTE, ( ( pointerBitmap.getWidth() / 2 ) + pointerX ), Animation.ABSOLUTE, ( ( pointerBitmap.getHeight() / 2 ) + pointerY ));
        anPointer.setInterpolator(new LinearInterpolator());
        anPointer.setDuration((int) (duration / (round * 1.2)));
        anPointer.setFillEnabled(true);
        anPointer.setFillAfter(true);
        anPointer.setRepeatCount(round - 1);
        anPointer.initialize(0, 0, pointerBitmap.getWidth(), pointerBitmap.getHeight());
        mPointerDrawable = new AnimateDrawable(pointerMovingDot, anPointer);
        anPointer.startNow();
    }

    public void startSpin(float startAngle, float endAngle) {
        if ( !isSpining ) {
            isSpining = true;
            final float lastAngle = endAngle;

            if (attrIsClockwise) {
                endAngle = (360 * this.spin_round) + endAngle;
            } else {
                endAngle = ((360 * this.spin_round) - endAngle) * -1;
            }
            rotate(startAngle, endAngle, this.duration);
            meter(this.spin_round, attrRotateDegree, this.duration);

            anPointer.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    meter(0, 0, 0);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            anWheel.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    streamId = soundEffect.play(soundId, 1f, 1f, 1, 0, 1.0f);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    meter(0, 0, 0);
                    soundEffect.stop(streamId);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if ( spinListener != null ) {
                                spinListener.onSpin(lastAngle);
                            }
                            isSpining = false;
                        }
                    }, attrDelayEndTime);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            Log.e(TAG, "IS SPINING");
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
            float eventX = event.getX() - 20;
            float eventY = event.getY() - 20;
            if ( ( eventX> pointerX && eventX < ( pointerX + pointerBitmap.getWidth() ) ) && ( eventY > pointerY && eventY < ( pointerY + pointerBitmap.getHeight() )) ) {
                this.touchSpinListener.onTouchSpin(event);
            }
            return true;
        }
        return false;
    }

    private boolean tabOnBitmap(AnimateDrawable bitmap, MotionEvent event) {

        return false;
    }

    private static class rotateAnimation extends RotateAnimation {

        public rotateAnimation(float fromDegrees, float toDegrees,
                               int pivotXType, float pivotXValue, int pivotYType,
                               float pivotYValue) {
            super(fromDegrees, toDegrees, pivotXType, pivotXValue, pivotYType,
                    pivotYValue);
            // TODO Auto-generated constructor stub
        }

        public rotateAnimation(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void cancel() {
            Log.e("Cancel ", "Cacel ");
            super.cancel();
        }

    }

    public interface OnSpinListener {
        public void onSpin(float lastAngle);
    }

    public interface OnTouchSpinListener {
        public void onTouchSpin(MotionEvent event);
    }
}
