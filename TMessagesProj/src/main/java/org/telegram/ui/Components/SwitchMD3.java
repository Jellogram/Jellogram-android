package org.telegram.ui.Components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Property;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.animator.FactorAnimator;

public class SwitchMD3 extends View implements FactorAnimator.Target {

    private final BoolAnimator animatorChecked = new BoolAnimator(0, new Runnable() {
        @Override
        public void run() {
            // empty
        }
    }, CubicBezierInterpolator.EASE_OUT_QUINT, 200L, true);

    private static final Property<SwitchMD3, Float> PROGRESS_PROPERTY =
        new SwitchMD3.ProgressFloatProperty();

    private boolean isChecked;
    private float progress;
    private Paint paintTrack;
    private Paint paintThumb;
    private Path thumbPath;

    private int trackColorKey = Theme.key_md3SwitchTrack;
    private int trackCheckedColorKey = Theme.key_md3SwitchTrackChecked;
    private int thumbColorKey = Theme.key_md3SwitchThumb;
    private int thumbCheckedColorKey = Theme.key_md3SwitchThumbChecked;
    private int iconColorKey = Theme.key_md3SwitchIconChecked;

    private Theme.ResourcesProvider resourcesProvider;

    public SwitchMD3(Context context) {
        this(context, null);
    }

    public SwitchMD3(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        paintTrack = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintThumb = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPath = new Path();
    }

    public void setColors(int track, int trackChecked, int thumb, int thumbChecked, int icon) {
        trackColorKey = track;
        trackCheckedColorKey = trackChecked;
        thumbColorKey = thumb;
        thumbCheckedColorKey = thumbChecked;
        iconColorKey = icon;
    }

    public void setChecked(boolean checked, boolean animated) {
        if (isChecked != checked) {
            isChecked = checked;
            if (animated) {
                animateToCheckedState(checked);
            } else {
                progress = checked ? 1f : 0f;
                invalidate();
            }
        }
    }

    private void animateToCheckedState(boolean checked) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, PROGRESS_PROPERTY, checked ? 1f : 0f);
        animator.setDuration(200);
        animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        animator.start();
    }

    public boolean isChecked() {
        return isChecked;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        int trackWidth = AndroidUtilities.dp(32);
        int trackHeight = AndroidUtilities.dp(16);
        int thumbRadius = AndroidUtilities.dp(10);

        float cx = width / 2f;
        float cy = height / 2f;

        // Track
        paintTrack.setColor(getTrackColor());
        canvas.drawRoundRect(
            cx - trackWidth / 2f, cy - trackHeight / 2f,
            cx + trackWidth / 2f, cy + trackHeight / 2f,
            trackHeight / 2f, trackHeight / 2f,
            paintTrack
        );

        // Thumb position
        float thumbX = isChecked ? cx + trackWidth / 2f - thumbRadius - AndroidUtilities.dp(3) :
            cx - trackWidth / 2f + thumbRadius + AndroidUtilities.dp(3);
        float thumbY = cy;

        // Thumb
        paintThumb.setColor(getThumbColor());
        thumbPath.reset();
        thumbPath.addCircle(thumbX, thumbY, thumbRadius, Path.Direction.CW);
        canvas.drawPath(thumbPath, paintThumb);
    }

    private int getTrackColor() {
        if (progress == 0f) {
            return Theme.getColor(trackColorKey, resourcesProvider);
        } else if (progress == 1f) {
            return Theme.getColor(trackCheckedColorKey, resourcesProvider);
        } else {
            int color1 = Theme.getColor(trackColorKey, resourcesProvider);
            int color2 = Theme.getColor(trackCheckedColorKey, resourcesProvider);
            float ratio = progress;
            return Color.argb(
                (int) (Color.alpha(color1) + (Color.alpha(color2) - Color.alpha(color1)) * ratio),
                (int) (Color.red(color1) + (Color.red(color2) - Color.red(color1)) * ratio),
                (int) (Color.green(color1) + (Color.green(color2) - Color.green(color1)) * ratio),
                (int) (Color.blue(color1) + (Color.blue(color2) - Color.blue(color1)) * ratio)
            );
        }
    }

    private int getThumbColor() {
        int color1 = Theme.getColor(thumbColorKey, resourcesProvider);
        int color2 = Theme.getColor(thumbCheckedColorKey, resourcesProvider);
        float ratio = progress;
        return Color.argb(
            (int) (Color.alpha(color1) + (Color.alpha(color2) - Color.alpha(color1)) * ratio),
            (int) (Color.red(color1) + (Color.red(color2) - Color.red(color1)) * ratio),
            (int) (Color.green(color1) + (Color.green(color2) - Color.green(color1)) * ratio),
            (int) (Color.blue(color1) + (Color.blue(color2) - Color.blue(color1)) * ratio)
        );
    }

    @Override
    public void onFactorChanged(int id, float factor, float fraction, FactorAnimator callee) {
        progress = factor;
        invalidate();
    }

    @Override
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    public float getProgress() {
        return progress;
    }

    private static class ProgressFloatProperty extends Property<SwitchMD3, Float> {
        ProgressFloatProperty() {
            super(Float.class, "progress");
        }

        @Override
        public Float get(SwitchMD3 object) {
            return object.getProgress();
        }

        @Override
        public void set(SwitchMD3 object, Float value) {
            object.setProgress(value);
        }
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Switch");
        info.setCheckable(true);
        info.setChecked(isChecked);
    }
}