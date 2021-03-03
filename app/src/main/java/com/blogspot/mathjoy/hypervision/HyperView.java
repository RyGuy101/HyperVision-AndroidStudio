package com.blogspot.mathjoy.hypervision;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class HyperView extends View implements OnTouchListener {
    double shift = 0;
    double frameRate = 60.0;
    Paint pointPaint = new Paint();
    Paint linePaint = new Paint();
    Paint pointPaint2 = new Paint();
    Paint linePaint2 = new Paint();
    // Paint filterPaint = new Paint();
    static ArrayList<Point> originalPoints = new ArrayList<>();
    static ArrayList<Point> points = new ArrayList<>();
    static ArrayList<Line> lines = new ArrayList<>();
    int dimension = 4;
    int numPoints = (int) Math.pow(2, dimension);
    double ppi;
    double size;
    double sizeAdjust;
    double panX;
    double panY;
    double prevX;
    double prevY;
    double currentX;
    double currentY;
    double velocityX;
    double velocityY;
    static boolean autoRotate = true;
    boolean demo = false;
    static long stopTouch;
    double autoRotateAngle = 360;
    boolean touching = false;
    int down = 0;
    public int rotateDim = 2;
    public static final int OFF_3D = 0;
    public static final int RED_CYAN_3D = 1;
    public static final int CROSS_EYE_3D = 2;
    public static final int PARALLEL_3D = 3;
    int stereo3D = MainActivity.doinDaDemo ? RED_CYAN_3D : OFF_3D;
    double eyeSeparation = 2.0;
    double leftEyePosX = 0;
    double rightEyePosX = 0;
    double eyePosZ = 13.5135;
    double camera4dPosW = 2;
    double pointRadius = 0.05;

    boolean setup = true;
    Path path = new Path();
    private Bitmap bitmap;
    Canvas myCanvas;
    private double dpi;
    boolean expand = false;
    boolean shrink = false;
    boolean respondToTouch = true;
    GestureDetector gestureDetector;

    public HyperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
        initialSetup();
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void initialSetup() {
        dpi = getResources().getDisplayMetrics().xdpi;
//        filterPaint.setColorFilter(new ColorMatrixColorFilter(new float[]{
//                15.9375f, 0, 0, 0, 0,
//                0, 2.008f, 0, 0, 0,
//                0, 0, 2.008f, 0, 0,
//                0, 0, 0, 1, 0,
//        }));
//        setLayerType(LAYER_TYPE_HARDWARE, filterPaint);

        if (points.size() == 0) {
            for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                String binaryNum = String.format("%" + Integer.toString(dimension) + "s", Integer.toBinaryString(pointIndex)).replace(' ', '0');
                double[] coords = new double[dimension];
                for (int coordDimension = 0; coordDimension < dimension; coordDimension++) {
                    char binaryDigit = binaryNum.charAt(coordDimension);
                    if (binaryDigit == '0') {
                        coords[coordDimension] = -1;
                    } else if (binaryDigit == '1') {
                        coords[coordDimension] = 1;
                    } else {
                        throw new RuntimeException("Error when converting int to binary string.");
                    }
                }
                originalPoints.add(new Point(coords));
            }
            for (Point p : originalPoints) {
                points.add(p.clone());
            }

            for (int lineDimension = 0; lineDimension < dimension; lineDimension++) {
                int groupSize = (int) Math.pow(2, lineDimension);
                for (int pointIndex = 0; pointIndex < numPoints; pointIndex += groupSize * 2) {
                    for (int groupIndex = 0; groupIndex < groupSize; groupIndex++) {
                        lines.add(new Line(pointIndex + groupIndex, pointIndex + groupIndex + groupSize));
                    }
                }
            }
        }
        velocityX = 0;
        velocityY = 0;
    }

    @Override
    protected void onDraw(Canvas c) {
        long startTime = System.currentTimeMillis();
        if (expand) {
            respondToTouch = false;
            expand();
            refreshSize();
            MainActivity.activity.refreshLeftRDButton();
            MainActivity.activity.refreshRightRDButton();
        } else if (shrink) {
            respondToTouch = false;
            shrink();
            refreshSize();
            MainActivity.activity.refreshLeftRDButton();
            MainActivity.activity.refreshRightRDButton();
        }
        if (setup) {
            setup();
            setup = false;
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(c.getWidth(), c.getHeight(), Bitmap.Config.ARGB_8888);
            myCanvas = new Canvas(bitmap);
        }
        if (myCanvas != null) {
            drawBackground();
            if (startTime - stopTouch >= 20000 && !autoRotate && !touching && MainActivity.doinDaDemo) {
                autoRotate = true;
                autoRotateAngle = 360;
                MainActivity.activity.endDemo(null);
            }
            if (autoRotate) {
                if (autoRotateAngle >= 360) {
                    autoRotateAngle = 0;
                    points.clear();
                    for (Point p : originalPoints) {
                        points.add(p.clone());
                    }
//                    if (MainActivity.activity.demoStep == 3) {
//                        rotate(0, 2, 15, points);
//                    } else
                    if (MainActivity.activity.demoStep == 4) {
                        rotate(1, 2, 15, points);
                    }
                }
                if (autoRotateAngle != 0) {
                    rotate(0, 2, -25, points);
                }
                autoRotateAngle += 0.6;
                if (MainActivity.activity.demoStep != 3)
                    rotate(0, 3, 0.6, points);
                if (MainActivity.activity.demoStep != 4)
                    rotate(1, 2, 0.6, points);
//                if (MainActivity.activity.demoStep == 4) {
                rotate(0, 2, 25, points);
//                }
            } else {
                if (touching) {
                    velocityX = down * ((-currentX - -prevX) / size) * 60;
                    velocityY = down * ((-currentY - -prevY) / size) * 60;
                }
                rotate(0, rotateDim, velocityX, points);
                rotate(1, rotateDim, velocityY, points);
                prevX = currentX;
                prevY = currentY;
                down = 1;
            }
            if (stereo3D != OFF_3D) {
                for (Point p : points) {
                    p.updateDisplayInfo4dLeft(leftEyePosX, eyePosZ, camera4dPosW, pointRadius);
                }
                for (Line l : lines) {
                    drawLineLeft(l, points, false, linePaint);
                }
                for (Point p : points) {
                    drawPointLeft(p, false, pointPaint);
                }
                for (Point p : points) {
                    p.updateDisplayInfo4dRight(rightEyePosX, eyePosZ, camera4dPosW, pointRadius);
                }
                for (Line l : lines) {
                    drawLineRight(l, points, true, linePaint2);
                }
                for (Point p : points) {
                    drawPointRight(p, true, pointPaint2);
                }
            } else {
                for (Point p : points) {
                    p.updateDisplayInfo4dLeft(0, eyePosZ, camera4dPosW, pointRadius);
                }
                for (Line l : lines) {
                    drawLineLeft(l, points, false, linePaint);
                }
                for (Point p : points) {
                    drawPointLeft(p, false, pointPaint);
                }
            }
            long timeTook = System.currentTimeMillis() - startTime;
            if (timeTook < 1000.0 / frameRate) {
                try {
                    Thread.sleep((long) (1000.0 / frameRate - timeTook));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                c.drawBitmap(bitmap, 0, 0, null);
            }
        }
        invalidate();
    }

    private void setup() {
        refreshSize();

        if (stereo3D == RED_CYAN_3D) {
//            pointPaint.setColor(Color.argb(255, 255, 0, 0));
//            linePaint.setColor(Color.argb(255, 255, 0, 0));
//            pointPaint2.setColor(Color.argb(127, 0, 255, 255));
//            linePaint2.setColor(Color.argb(127, 0, 255, 255));
            pointPaint.setColor(Color.argb(255, 255, 0, 0));
            linePaint.setColor(Color.argb(255, 255, 0, 0));
            pointPaint2.setColor(Color.argb(255, 0, 255, 255));
            linePaint2.setColor(Color.argb(255, 0, 255, 255));
            pointPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            linePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            pointPaint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            linePaint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        } else {
            pointPaint.setColor(Color.argb(255, 255, 63, 63));
            linePaint.setColor(Color.argb(255, 191, 191, 191));
            pointPaint2.setColor(Color.argb(255, 255, 63, 63));
            linePaint2.setColor(Color.argb(255, 191, 191, 191));
            pointPaint.setXfermode(null);
            linePaint.setXfermode(null);
            pointPaint2.setXfermode(null);
            linePaint2.setXfermode(null);
        }

        MainActivity.activity.refreshLeftRDButton();
        MainActivity.activity.refreshRightRDButton();
    }


    public void refreshSize() {
        if (stereo3D == OFF_3D || stereo3D == RED_CYAN_3D) {
            if (getWidth() < getHeight()) {
                size = getWidth() / 3.75;
            } else {
                size = getHeight() / 3.75;
            }
            panX = getWidth() / 2.0;
            shift = 0;
        } else if (stereo3D == CROSS_EYE_3D || stereo3D == PARALLEL_3D) {
            if (getWidth() / 2.0 < getHeight()) {
                size = getWidth() / 7.5;
                shift = getWidth() / 2.0;
            } else {
                size = getHeight() / 3.75;
                shift = getHeight();
            }
            panX = (getWidth() - shift) / 2.0;
        }
        panY = getHeight() / 2.0;
        sizeAdjust = 1 / (eyePosZ / (eyePosZ - 1));
        updateEyePosX();
    }

    public void updateEyePosX() {
        leftEyePosX = -eyeSeparation / 2.0;
        rightEyePosX = eyeSeparation / 2.0;
//        leftEyePosX = -inchesToUnits(eyeSeparation / 2.0);
//        rightEyePosX = inchesToUnits(eyeSeparation / 2.0);
        if (stereo3D == CROSS_EYE_3D) {
            leftEyePosX *= -1;
            rightEyePosX *= -1;
        }
    }

    private void drawLineLeft(Line l, List<Point> points, boolean doShift, Paint paint) {
        drawLine(points.get(l.getStartIndex()).getDisplayXLeft(), points.get(l.getStartIndex()).getDisplayYLeft(), points.get(l.getStartIndex()).getDisplayPointRadiusLeft(), points.get(l.getEndIndex()).getDisplayXLeft(), points.get(l.getEndIndex()).getDisplayYLeft(), points.get(l.getEndIndex()).getDisplayPointRadiusLeft(), doShift, paint);
    }

    private void drawLineRight(Line l, List<Point> points, boolean doShift, Paint paint) {
        drawLine(points.get(l.getStartIndex()).getDisplayXRight(), points.get(l.getStartIndex()).getDisplayYRight(), points.get(l.getStartIndex()).getDisplayPointRadiusRight(), points.get(l.getEndIndex()).getDisplayXRight(), points.get(l.getEndIndex()).getDisplayYRight(), points.get(l.getEndIndex()).getDisplayPointRadiusRight(), doShift, paint);
    }

    private void drawLine(double displayX1, double displayY1, double displayPointRadius1, double displayX2, double displayY2, double displayPointRadius2, boolean doShift, Paint paint) {
        double displayHalfWidth1 = displayPointRadius1 / 2.0;
        double displayHalfWidth2 = displayPointRadius2 / 2.0;

        double theta = 90;
        if (displayX2 - displayX1 != 0) {
            theta = Math.atan((displayY2 - displayY1) / (displayX2 - displayX1));
        }
        float sinTheta = (float) Math.sin(theta);
        float cosTheta = (float) Math.cos(theta);

        double x1a = displayX1 + displayHalfWidth1 * sinTheta;
        double y1a = displayY1 - displayHalfWidth1 * cosTheta;
        double x1b = displayX1 - displayHalfWidth1 * sinTheta;
        double y1b = displayY1 + displayHalfWidth1 * cosTheta;
        double x2a = displayX2 - displayHalfWidth2 * sinTheta;
        double y2a = displayY2 + displayHalfWidth2 * cosTheta;
        double x2b = displayX2 + displayHalfWidth2 * sinTheta;
        double y2b = displayY2 - displayHalfWidth2 * cosTheta;

        double shift = doShift ? this.shift : 0;

        path.reset();
        path.moveTo((float) (panX + x1a * size * sizeAdjust + shift), (float) (panY + y1a * size * sizeAdjust));
        path.lineTo((float) (panX + x1b * size * sizeAdjust + shift), (float) (panY + y1b * size * sizeAdjust));
        path.lineTo((float) (panX + x2a * size * sizeAdjust + shift), (float) (panY + y2a * size * sizeAdjust));
        path.lineTo((float) (panX + x2b * size * sizeAdjust + shift), (float) (panY + y2b * size * sizeAdjust));
        myCanvas.drawPath(path, paint);
    }

    private void drawBackground() {
        myCanvas.drawColor(Color.BLACK);
    }

    private void drawPointLeft(Point p, boolean doShift, Paint paint) {
        drawPoint(p.getDisplayXLeft(), p.getDisplayYLeft(), p.getDisplayPointRadiusLeft(), doShift, paint);
    }

    private void drawPointRight(Point p, boolean doShift, Paint paint) {
        drawPoint(p.getDisplayXRight(), p.getDisplayYRight(), p.getDisplayPointRadiusRight(), doShift, paint);
    }

    private void drawPoint(double displayX, double displayY, double displayPointRadius, boolean doShift, Paint paint) {
        double shift = doShift ? this.shift : 0;
        myCanvas.drawCircle((float) (panX + displayX * size * sizeAdjust + shift), (float) (panY + displayY * size * sizeAdjust), (float) (displayPointRadius * size * sizeAdjust), paint);
    }

    public void rotate(int firstAxis, int secondAxis, double degrees, List<Point> points) { //The two axes define a plane
        if (firstAxis >= dimension || secondAxis >= dimension) {
            throw new IllegalArgumentException("Whoa! There aren't " + (Math.max(firstAxis, secondAxis) + 1) + "axes in " + dimension + "dimensions!");
        }

        double sinTheta = Math.sin(Math.toRadians(degrees));
        double cosTheta = Math.cos(Math.toRadians(degrees));
        for (Point p : points) {
            double firstCoord = p.getCoord(firstAxis);
            double secondCoord = p.getCoord(secondAxis);
            p.setCoord(firstAxis, firstCoord * cosTheta - secondCoord * sinTheta);
            p.setCoord(secondAxis, secondCoord * cosTheta + firstCoord * sinTheta);
        }
    }

    public double inchesToUnits(double inches) {
        return inches * ppi / (size * sizeAdjust);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (respondToTouch) {
            gestureDetector.onTouchEvent(event);
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                down = 0;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                touching = false;
                stopTouch = System.currentTimeMillis();
            } else {
                touching = true;
                if (!demo) {
                    autoRotate = false;
                }
            }
            currentX = event.getX();
            currentY = event.getY();
            return true;
        }
        return false;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            MainActivity.activity.switchRotateDimension();
            return true;
        }
    }

    boolean startExpand = true;
    long startExpandTime;
    int duration = 250;
    int distance;
    int initialOuterLayoutLeft;
    int initialOuterLayoutRight;
    int initialInnerLayoutRight;
    int initialButtonLayoutRight;
    int initialButtonWidth;
    int initialRight;

    private void expand() {
        if (startExpand) {
            distance = MainActivity.activity.settingsWidth;
            initialOuterLayoutLeft = MainActivity.activity.findViewById(R.id.outerHyperViewLayout).getLeft();
            initialOuterLayoutRight = MainActivity.activity.findViewById(R.id.outerHyperViewLayout).getRight();
            initialInnerLayoutRight = MainActivity.activity.findViewById(R.id.innerHyperViewLayout).getRight();
            initialButtonLayoutRight = MainActivity.activity.findViewById(R.id.buttonLayout).getRight();
            initialButtonWidth = MainActivity.activity.findViewById(R.id.button3D1).getLayoutParams().width;
            initialRight = getRight();
            startExpand = false;
            startExpandTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - startExpandTime < duration) {
            int currentDistance = (int) ((((double) (System.currentTimeMillis() - startExpandTime)) / duration) * distance);
            MainActivity.activity.findViewById(R.id.outerHyperViewLayout).setLeft(initialOuterLayoutLeft - currentDistance);
            MainActivity.activity.findViewById(R.id.outerHyperViewLayout).setRight(initialOuterLayoutRight + currentDistance);
            MainActivity.activity.findViewById(R.id.innerHyperViewLayout).setRight(initialInnerLayoutRight + currentDistance);
            MainActivity.activity.findViewById(R.id.buttonLayout).setRight(initialButtonLayoutRight + currentDistance);
            setRight(initialRight + currentDistance);
        } else {
            stopExpand();
        }
    }

    public void stopExpand() {
        startExpand = true;
        expand = false;
        respondToTouch = true;
    }

    boolean startShrink = true;
    long startShrinkTime;

    private void shrink() {
        if (startShrink) {
            distance = MainActivity.activity.settingsWidth;
            initialButtonWidth = MainActivity.activity.findViewById(R.id.button3D1).getLayoutParams().width;
            startShrink = false;
            startShrinkTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - startShrinkTime < duration) {
            int currentDistance = (int) ((((double) (System.currentTimeMillis() - startShrinkTime)) / duration) * distance);
            MainActivity.activity.findViewById(R.id.outerHyperViewLayout).setLeft(initialOuterLayoutLeft + currentDistance);
            setRight(initialRight - currentDistance);
        } else {
            MainActivity.activity.findViewById(R.id.outerHyperViewLayout).setLeft(initialOuterLayoutLeft + distance);
            setRight(initialRight - distance);
            stopShrink();
        }
    }

    public void stopShrink() {
        startShrink = true;
        shrink = false;
        respondToTouch = true;
    }
}
