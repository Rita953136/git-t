
package fcu.app.schoolApp;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class LuckyWheelView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    public interface OnSpinUpdateListener {
        void onCurrentPrizeUpdate(String prize);
    }
    private OnSpinUpdateListener updateListener;

    public void setOnSpinUpdateListener(OnSpinUpdateListener listener) {
        this.updateListener = listener;
    }

    private SurfaceHolder holder;
    private Thread thread;
    private boolean isRunning = false;
    private long lastClickTime = 0;
    private boolean shouldDecelerate = false;

    private Paint paint = new Paint();
    private int[] colors = {
            Color.parseColor("#FF5722"), Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"),
            Color.parseColor("#3F51B5"), Color.parseColor("#03A9F4"), Color.parseColor("#4CAF50"),
            Color.parseColor("#FFC107"), Color.parseColor("#FF9800")
    };

    private String[] prizes = {
    };

    private float angle = 0f;
    private float speed = 0f;

    public LuckyWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            angle += speed;
            angle %= 360;

            if (speed > 0) {
                if (!shouldDecelerate && System.currentTimeMillis() - lastClickTime > 2000) {
                    shouldDecelerate = true;
                }

                if (shouldDecelerate) {
                    speed -= 0.5f;
                }

                if (updateListener != null) {
                    float sweep = 360f / prizes.length;
                    float currentAngle = (630 - (angle % 360)) % 360;
                    int sector = (int)(currentAngle / sweep);
                    String currentPrize = prizes[sector];
                    post(() -> updateListener.onCurrentPrizeUpdate(currentPrize));
                }

                if (speed < 0.5f && shouldDecelerate) {
                    speed = 0f;
                    if (updateListener != null) {
                        post(() -> updateListener.onCurrentPrizeUpdate(null));
                    }
                }
            }

            long end = System.currentTimeMillis();
            try {
                Thread.sleep(Math.max(0, 16 - (end - start)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void draw() {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        // 清除畫面避免破圖
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int w = getWidth(), h = getHeight();
        int radius = Math.min(w, h) / 2 - 40;
        int centerX = w / 2;
        int centerY = h / 2;
        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        canvas.drawCircle(centerX, centerY, radius + 10, paint);

        float sweepAngle = 360f / prizes.length;
        float startAngle = angle;

        paint.setAntiAlias(true);
        paint.setTextSize(70);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < prizes.length; i++) {
            paint.setColor(colors[i % colors.length]);
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint);

            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(
                    centerX,
                    centerY,
                    (float)(centerX + radius * Math.cos(Math.toRadians(startAngle))),
                    (float)(centerY + radius * Math.sin(Math.toRadians(startAngle))),
                    paint
            );
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.WHITE);
            float textAngle = (float) Math.toRadians(startAngle + sweepAngle / 2);
            float textX = (float) (centerX + (radius * 0.65f) * Math.cos(textAngle));
            float textY = (float) (centerY + (radius * 0.65f) * Math.sin(textAngle));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(prizes[i], textX, textY, paint);

            startAngle += sweepAngle;
        }

        drawCenterWithArrow(canvas, centerX, centerY);
        holder.unlockCanvasAndPost(canvas);
    }

    private void drawCenterWithArrow(Canvas canvas, int cx, int cy) {
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle(cx, cy, 110, paint);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx, cy, 90, paint);

        Path triangle = new Path();
        triangle.moveTo(cx, cy - 150);
        triangle.lineTo(cx - 20, cy - 90);
        triangle.lineTo(cx + 20, cy - 90);
        triangle.close();

        paint.setColor(Color.DKGRAY);
        canvas.drawPath(triangle, paint);
    }

    public void startSpin() {
        speed += 30f;
        if (speed > 40f) speed = 40f;
        lastClickTime = System.currentTimeMillis();
        shouldDecelerate = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            float dx = event.getX() - cx;
            float dy = event.getY() - cy;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < 120) {
                startSpin();
                return true;
            }
        }
        return true;
    }

    public void setPrizes(String[] newPrizes) {
        this.prizes = newPrizes;
        invalidate();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
}


