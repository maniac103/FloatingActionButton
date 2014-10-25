package com.faizmalkani.floatingactionbutton;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

public class FloatingActionButton extends View {

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable mIconDrawable;
    private int mRadius;
    private int mShadowRadius;
    private int mShadowOffsetX, mShadowOffsetY;
    private int mColor;
    private int mPressedColor;
    private boolean mHidden = false;
    /** The FAB button's Y position when it is displayed. */
    private float mYDisplayed = -1;
    /** The FAB button's Y position when it is hidden. */
    private float mYHidden = -1;
    
    public FloatingActionButton(Context context) {
        this(context, null);
    }
    
    public FloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }
    
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
        mColor = a.getColor(R.styleable.FloatingActionButton_fab_color, Color.WHITE);
        mPressedColor = a.getColor(R.styleable.FloatingActionButton_fab_pressedColor, darkenColor(mColor));
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(mColor);
        float radius, dx, dy;
        radius = a.getDimension(R.styleable.FloatingActionButton_fab_shadowRadius,
                getResources().getDimension(R.dimen.fab_default_shadow_radius));
        dx = a.getDimension(R.styleable.FloatingActionButton_fab_shadowDx, 0.0f);
        dy = a.getDimension(R.styleable.FloatingActionButton_fab_shadowDy,
                getResources().getDimension(R.dimen.fab_default_shadow_dy));
        int color = a.getInteger(R.styleable.FloatingActionButton_fab_shadowColor, Color.argb(100, 0, 0, 0));
        mButtonPaint.setShadowLayer(radius, dx, dy, color);

        setDrawable(a.getDrawable(R.styleable.FloatingActionButton_fab_drawable));

        mRadius = a.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_radius,
                getResources().getDimensionPixelSize(R.dimen.fab_radius));

        a.recycle();

        mShadowRadius = (int) Math.ceil(radius);
        mShadowOffsetX = Math.round(dx);
        mShadowOffsetY = Math.round(dy);

        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
        WindowManager mWindowManager = (WindowManager)
        context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mYHidden = size.y;
    }
    
    public void setColor(int color) {
        mColor = color;
        mButtonPaint.setColor(mColor);
        invalidate();
    }
    
    public void setPressedColor(int color) {
        mPressedColor = color;
        invalidate();
    }

    public void setDrawable(Drawable drawable) {
        mIconDrawable = drawable;
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
        invalidate();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = 2 * mRadius + 2 * mShadowRadius;
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getWidth() / 2 - mShadowOffsetX - (getPaddingRight() - getPaddingLeft()) / 2;
        float centerY = getHeight() / 2 - mShadowOffsetY - (getPaddingBottom() - getPaddingTop()) / 2;
        canvas.drawCircle(centerX, centerY, mRadius, mButtonPaint);
        if (mIconDrawable != null) {
            canvas.save();
            canvas.translate(centerX - mIconDrawable.getIntrinsicWidth() / 2,
                    centerY - mIconDrawable.getIntrinsicHeight() / 2);
            mIconDrawable.draw(canvas);
            canvas.restore();
        }
    }
    
    @Override protected void onLayout (boolean changed, int left, int top, int right, int bottom)
    {
        // Perform the default behavior
        super.onLayout(changed, left, top, right, bottom);
        
        // Store the FAB button's displayed Y position if we are not already aware of it
        if (mYDisplayed == -1)
        {
            mYDisplayed = this.getY();
        }
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int color = event.getAction() == MotionEvent.ACTION_UP ? mColor : mPressedColor;
        mButtonPaint.setColor(color);
        invalidate();
        return super.onTouchEvent(event);
    }
    
    public void hide(boolean hide) {
        // If the hidden state is being updated
        if (mHidden != hide) {
            
            // Store the new hidden state
            mHidden = hide;
            
            // Animate the FAB to it's new Y position
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "Y", mHidden ? mYHidden : mYDisplayed);
            animator.setInterpolator(mInterpolator);
            animator.start();
        }
    }
    
    public void listenTo(AbsListView listView) {
        if (null != listView) {
            listView.setOnScrollListener(new DirectionScrollListener(this));
        }
    }
    
    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
