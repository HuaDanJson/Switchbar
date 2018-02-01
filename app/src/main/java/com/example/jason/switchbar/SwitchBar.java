package com.example.jason.switchbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class SwitchBar extends View {

    private static final String TAG = "SwitchBar";
    private static final long DEFAULT_DURATION = 500;
    private Paint mPaint;//主要画笔
    private TextPaint mTextPaint;//文字画笔
    private RectF mRectF;//圆角矩阵
    private float mOverlayRadius;//覆盖物半径
    private Path mClipPath;//裁剪区域
    private float[] mCurrentPosition;//遮盖物的坐标点
    boolean misLeft = true;//tab选中位置
    private boolean isAnimation;//是否正在切换条目中
    private float mTotalleft;//view的left
    private float mTotalTop;//view的top
    private float mTotalRight;//view的right
    private float mTotalBottom;//view的bottom
    private float mTotalHeight;//bottom－top
    private int mBaseLineY;//文字剧中线条
    private String[] mText = {"1P", "2P"};//tab 文字内容
    private OnClickListener mOnClickListener;
    private Bitmap oneP = BitmapFactory.decodeResource(getResources(), R.drawable.icon_onep);
    private Bitmap twoP = BitmapFactory.decodeResource(getResources(), R.drawable.icon_twop);
    PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
    private int colorRed = Color.rgb(0xff, 0x21, 0x10);
    private int colorPurple = Color.rgb(0x88, 0x88, 0xff);
    private RectF mLeftPicRectF;
    private RectF mRightPicRectF;

    private long mDuration = DEFAULT_DURATION;//自定义动画时长
    private final int mCommonPadding = 20;

    ValueAnimator mValueAnimator;

    public SwitchBar(Context context) {
        this(context, null);
    }

    public SwitchBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /*
    *
    * 设置tab文字
    * @param text 文字内容
    *
    * */
    public void setText(String[] text) {
        mText = text;
        invalidate();
    }

    /*
    *
    * 设置tab文字的size
    * @param size 文字大小
    *
    * */
    public void setTabTextSize(int size) {
        mTextPaint.setTextSize(size);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
        //基线中间点的y轴计算公式
        mBaseLineY = (int) (getHeight() / 2 - top / 2 - bottom / 2);
        invalidate();
    }

    /*
    *
    * 切换条目 动画默认500ms
    * @param isLeft true为左边的条目
    *
    * */
    public void switchButton(boolean isLeft) {
        switchB(isLeft, DEFAULT_DURATION);
    }

    public void switchButton(boolean isLeft, long duration) {
        switchB(isLeft, duration);
    }

    /*
    *
    * 添加tab切换监听
    *
    * */
    public void setOnTabClickListener(@Nullable OnClickListener listener) {
        mOnClickListener = listener;
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(48);
        mTextPaint.setTypeface(Typeface.SERIF);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width / 3;
        mTotalHeight = height - 10;
        mTotalleft = 5;
        mTotalTop = 5;
        mTotalRight = width - 5;
        mTotalBottom = height - 5;

        mRectF = new RectF(mTotalleft, mTotalTop, mTotalRight, mTotalBottom);

        RectF f = new RectF(mTotalleft + mTotalHeight / 2, mTotalTop, mTotalRight - mTotalHeight / 2, mTotalBottom);
        mOverlayRadius = (mTotalRight - mTotalleft) * 0.36F;
        mClipPath = new Path();
        mClipPath.setFillType(Path.FillType.WINDING);
        mClipPath.addRect(f, Path.Direction.CW);
        mClipPath.addCircle(mTotalleft + mTotalHeight / 2, mTotalTop + mTotalHeight / 2, mTotalHeight / 2, Path.Direction.CW);
        mClipPath.addCircle(mTotalRight - mTotalHeight / 2, mTotalTop + mTotalHeight / 2, mTotalHeight / 2, Path.Direction.CW);

        if (mCurrentPosition == null) {
            //保证位置不会再重新执行的时候赋值
            mCurrentPosition = new float[2];
            mCurrentPosition[0] = mTotalleft + mTotalHeight / 2 + mCommonPadding;
            mCurrentPosition[1] = mTotalBottom;
        }

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
        float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
        mBaseLineY = (int) (height / 2 - top / 2 - bottom / 2);

        float picWidth = height / 10 * 5 / 3 * 5;//图片高度为控件的3/5  图片宽高5：3
        float padding = (width / 2 - picWidth) / 2;//图片1/2居中后左右 padding
        mLeftPicRectF = new RectF(padding, height / 10 * 5 / 2, padding + picWidth, height / 10 * 8);  // 5-第二个参数 = 10-第三个参数,第二个参数+第三个参数=10
        mRightPicRectF = new RectF(width / 2 + padding, height / 10 * 5 / 2, width - padding, height / 10 * 8);

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(event.getX() > getWidth() / 2 ? 1 : 0, mText[event.getX() > getWidth() / 2 ? 1 : 0]);
                switchButton(event.getX() < getWidth() / 2, mDuration);
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);
        drawStroke(canvas);
        drawOverlay(canvas);
        canvas.restoreToCount(saved);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        canvas.drawBitmap(oneP, null, mLeftPicRectF, mPaint);
        canvas.drawBitmap(twoP, null, mRightPicRectF, mPaint);
//        canvas.drawText(mText[0], getWidth() / 4, mBaseLineY, mTextPaint);
//        canvas.drawText(mText[1], getWidth() / 4 * 3, mBaseLineY, mTextPaint);
    }

    private void drawOverlay(Canvas canvas) {
        mPaint.setXfermode(xfermode);
        mPaint.setColor(mCurrentPosition[0] > getWidth() / 2 ? colorPurple : colorRed);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], mOverlayRadius, mPaint);
        mPaint.setXfermode(null);

        mPaint.setStrokeWidth(1);
        canvas.save();
        canvas.clipPath(mClipPath);
        canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], mOverlayRadius, mPaint);
        canvas.restore();
    }


    private void drawStroke(Canvas canvas) {
        mPaint.setStrokeWidth(DensityUtil.dip2px(3));
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(mRectF, DensityUtil.dip2px(16), DensityUtil.dip2px(16), mPaint);
    }


    private void switchB(boolean isLeft, long duration) {
        if (misLeft == isLeft || isAnimation) {
            return;
        }
        Path overlayPath = new Path();

        RectF rectF = new RectF(mTotalleft + mTotalHeight / 2 + mCommonPadding, mTotalBottom - mOverlayRadius, mTotalRight - mTotalHeight / 2 - mCommonPadding, mTotalBottom + mOverlayRadius);

        if (isLeft) {
            overlayPath.addArc(rectF, 0, 180);//右到左
        } else {
            overlayPath.addArc(rectF, 180, -180);//左到右
        }
        PathMeasure pathMeasure = new PathMeasure(overlayPath, false);
        startPathAnim(pathMeasure, duration);
    }

    private void startPathAnim(final PathMeasure pathMeasure, long duration) {
        // 0 － getLength()
        mValueAnimator = ValueAnimator.ofFloat(0, pathMeasure.getLength());
        mValueAnimator.setDuration(duration);
        // 减速插值器
        mValueAnimator.setInterpolator(new DecelerateInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                // 获取当前点坐标封装到mCurrentPosition
                pathMeasure.getPosTan(value, mCurrentPosition, null);
                postInvalidate();
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                misLeft = !misLeft;
                isAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mValueAnimator.start();
    }

    public interface OnClickListener {
        void onClick(int position, String text);
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null) {
            mValueAnimator.removeAllListeners();
            mValueAnimator.end();
            mValueAnimator.cancel();
        }
        mValueAnimator = null;
    }
}