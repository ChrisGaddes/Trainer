package com.chrisgaddes.reaction;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DrawArrowsView extends ImageView {

    private static final String TAG = "ThirdActivity";

    final double pi = Math.PI;

    //double angles[] = {-pi, -3 * pi / 4, -pi / 2, -pi / 4, 0, pi / 4, pi / 2, 3 * pi / 4, pi};
    final double[] angles = {-pi, -5 * pi / 6, -2 * pi / 3, -pi / 2, -pi / 3, -pi / 6, 0, pi / 6, pi / 3, pi / 2, 2 * pi / 3, 5 * pi / 6, pi};

    // TODO: ** JAMES SENTELL: "Should I initialize these variables elsewhere?"
    // initialize variables

    /**
     * Chris:
     * What you are doing right here is declaring the member variables of the DrawArrowsView class.
     * What I usually do (not sure if this is "best practice" Java or just my habit) is to avoid
     * initializing variables here, but to just include the declarations (like you did).  Then, I
     * typically initialize the variables in the class constructor (public DrawArrowsView(Context
     * context, AttributeSet attrs)).  So, in my opinion, you are doing it right here.
     *
     * The one thing I would add is short comment sections on what each member variable is used for.
     * Hopefully, you will be able to tell what they do by their name, but if not comments help
     * (see examples below).
     */

    /**
     * Description for what paint_points does...
     */
    private final Paint paint_points;

    /**
     * Description for what paint_arrow does...
     */
    private final Paint paint_arrow;

    /**
     * Description for what paint_arrow_head_box does...
     */
    private final Paint paint_arrow_head_box;
    private final Paint paint_box;
    private final Paint paint_text;
    private final double len_arrow_shaft;
    private final double len_arrow_head;
    private final float dim_btn_radius;
    private final float dim_btn_radius_buffer;
    private long time_anim_arrow_dur;

    private int rectList_indice;
    private int rectListArrowHead_indice;
    private boolean clicked_on_arrow_head;

    //private ArrayList<Double> angleListCheck = new ArrayList<>();

    //TODO consider http://stackoverflow.com/questions/32324876/how-to-save-an-answer-in-a-riddle-game-without-creating-a-database

    // initialize ArrayLists for paths and points

    /**
     * Chris:
     * Per comment above at line 42, I would recommend initializing these in the constructor
     * (public DrawArrowsView(Context, context, AttributeSet attrs)
     */
    private ArrayList<Point> pointList = new ArrayList<>();
    List<List<Double>> angleListCheck = new ArrayList<>();
    private ArrayList<Rect> rectListButtons = new ArrayList<>();
    private ArrayList<Rect> rectListArrowHead = new ArrayList<>();
    private ArrayList<Path> pathList = new ArrayList<>();
    private ArrayList<Point> pointListArrowHead = new ArrayList<>();
    private ArrayList<Double> angleListArrowHead = new ArrayList<>();
    private ArrayList<Integer> linkList = new ArrayList<>();

    private Path path_arrow;

    private int btn_loc_x;
    private int btn_loc_y;
    private int loc_arrow_point_x;
    private int loc_arrow_point_y;

    private double len_arrow_shaft_current;
    private double angle;
    private double angle_difference;
    private double arrow_animated_fraction;

    private boolean clicked_on_button;
    private boolean inside_button;
    private boolean able_to_click;
    private boolean already_done;

    private boolean debuggingTextToggle;

    private long viewHeight;
    private long viewWidth;
    private Drawable mFocusedImage;
    private Drawable mGrayedImage;

    // TODO: JAMES SENTELL "Should I have another one of these with Just Contect context, or other things "

    /**
     * Chris: That's up to you and how you need to use it.  There is nothing wrong with just having
     * a constructor with the 2 parameters below.  The only reason I would make another constructor
     * would be if you needed one with different arguments.  I would add javadoc comments to all
     * your methods - see examples on methods below
     */

    /**
     * Description of what this Constructor does/is used for...
     * @param context What is the context argument used for?
     * @param attrs What is the attrs argument used for?
     */
    public DrawArrowsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // TODO replace depreciated "getDrawable" with something else
        mFocusedImage = context.getResources().getDrawable(R.drawable.fbd_2_bar);
        mGrayedImage = context.getResources().getDrawable(R.drawable.fbd_2_greyed);

        // create new paints
        paint_arrow = new Paint();
        path_arrow = new Path();
        paint_box = new Paint();
        paint_arrow_head_box = new Paint();
        paint_text = new Paint();
        paint_points = new Paint();

        // set values to false to begin
        clicked_on_button = false;
        inside_button = false;
        clicked_on_arrow_head = false;
        able_to_click = true;
        already_done = false;

        // sets dimensions of arrow, nodes, and touch areas
        len_arrow_shaft = dpToPx(62);
        len_arrow_head = dpToPx(19);
        dim_btn_radius = dpToPx(4);
        dim_btn_radius_buffer = dpToPx(19);

        // TODO JAMES SENTELL: "is it bad practice how I extracted this setArrowStyle method out here and don't have anything inside the parentheses? (pardon my lack of correct terminology)"

        /**
         * Chris: Actually this is very good.  Stuff inside the parenthesis are called "parameters"
         * or "arguments".  Nothing wrong with having a function with no parameters.  In fact, it is
         * MUCH better to separate out bits of functionality into re-usable functions rather than
         * cram everything into one function.
         */
        setArrowStyle();
    }

    /**
     *
     * @param context
     */
    private void setButtonPoints(Context context) {
        // this method sets the location of the points
        // TODO: import these from database
        // TODO: JAMES SENTELL: I used this variable "already_done" so that it only runs these once. this is probably a lousy way to accomplish this...
        /**
         * I think this is perfectly fine (have done similar things myself).  I would just add
         * comments so that you remember what you are doing when you come back to it after a while.
         */
        already_done = true;

        // Adds angles to the list of "correct" angles
        int btn = 0;
        angleListCheck.add(new ArrayList<Double>());
        angleListCheck.add(new ArrayList<Double>());
        angleListCheck.add(new ArrayList<Double>());
        angleListCheck.get(btn).add(0.0);
        angleListCheck.get(btn).add(-pi);
        angleListCheck.get(btn).add(pi / 2);
        angleListCheck.get(btn).add(-pi / 2);

        btn = 1;
        angleListCheck.add(new ArrayList<Double>());
        angleListCheck.get(btn).add(pi / 2);
        angleListCheck.get(btn).add(-pi / 2);

        btn = 2;
        angleListCheck.add(new ArrayList<Double>());
        angleListCheck.get(btn).add(pi / 6);
        angleListCheck.get(btn).add(-5 * pi / 6);

        // set node locations - touch "button" zones will be placed in boxes around these nodes
        PointF pointOne = new PointF((float) 18, (float) 31.5);
        PointF pointTwo = new PointF((float) 18, (float) 62.1);
        PointF pointThree = new PointF((float) 53.5, (float) 62.1);
        PointF pointFour = new PointF((float) 87.55, (float) 62.1);

// http://stackoverflow.com/questions/5022824/how-to-fill-a-two-dimensional-arraylist-in-java-with-integers

        // pointList.add(percentToPx(pointOne));
        pointList.add(percentToPx(pointTwo));
        pointList.add(percentToPx(pointThree));
        pointList.add(percentToPx(pointFour));

        // create Rects from pointList to create buttons at nodes
        for (Point g : pointList) {
            rectListButtons.add(new Rect(g.x - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), g.y - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), g.x + ((int) dim_btn_radius + (int) dim_btn_radius_buffer), g.y + ((int) dim_btn_radius + (int) dim_btn_radius_buffer)));
        }
    }

    /**
     * Describe the setArrowStyle function
     */
    private void setArrowStyle() {

        // gets shared preferences
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());

        // toggles debugging text - this value can be changed in menu in app
        debuggingTextToggle = SP.getBoolean("debuggingTextToggle", false);


        // toggles visibility of dots at nodes - this value can be changed in menu in app
        boolean nodeDotsToggle = SP.getBoolean("nodeDotsToggle", false);
        if (!nodeDotsToggle) {
            paint_points.setAlpha(0);
        }

        // sets style of arrows
        paint_arrow.setStyle(Paint.Style.FILL);
        paint_arrow.setStrokeWidth(dpToPx(6));
        paint_arrow.setColor(Color.RED);
        paint_arrow.setStyle(Paint.Style.STROKE);
        paint_arrow.setStrokeCap(Paint.Cap.ROUND);

        // toggles arrow animation - this value can be changed in menu in app
        boolean animationToggle = SP.getBoolean("animationToggle", false);
        if (animationToggle) {
            // arrow animations enabled
            time_anim_arrow_dur = 150;
        } else {
            // arrow animations disabled
            time_anim_arrow_dur = 0;
        }

        // sets style of boxes around arrow heads
        paint_arrow_head_box.setStyle(Paint.Style.FILL);
        paint_arrow_head_box.setStrokeWidth(dpToPx(2));
        paint_arrow_head_box.setStyle(Paint.Style.STROKE);
        paint_arrow_head_box.setPathEffect(new DashPathEffect(new float[]{10, 10, 10, 10}, 0));

        // toggles visibility of boxes around arrow heads - this value can be changed in menu in app
        boolean arrowBoxesToggle = SP.getBoolean("arrowBoxesToggle", false);
        if (arrowBoxesToggle) {
            paint_arrow_head_box.setColor(Color.GREEN);
        } else {
            paint_arrow_head_box.setColor(Color.TRANSPARENT);
        }

        // sets text size for debugging text
        paint_text.setTextSize(23f);

        // sets style of boxes around nodes
        paint_box.setStyle(Paint.Style.FILL);
        paint_box.setStrokeWidth(5f);
        paint_box.setStyle(Paint.Style.STROKE);
        paint_box.setPathEffect(new DashPathEffect(new float[]{10, 10, 10, 10}, 0));

        // toggles visibility of boxes around nodes
        boolean nodeBoxesToggle = SP.getBoolean("nodeBoxesToggle", false);
        if (nodeBoxesToggle) {
            paint_box.setColor(Color.GRAY);
            paint_box.setAlpha(80); // TODO remove hard coded alpha
        } else {
            paint_box.setColor(Color.TRANSPARENT);
            paint_box.setAlpha(0);
        }
    }

    /**
     *
     * @param xNew
     * @param yNew
     * @param xOld
     * @param yOld
     */
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // gets width of height of the view
        viewWidth = xNew;
        viewHeight = yNew;

        // TODO JAMES SENTELL: "I needed the size of the canvas in order to calculate the percentage the points were accross the screen, but it won't properly get the size until the view is drawn. So, I trigger setButtonPoints from here once the view is drawn and use the if statement below to only allow it to run once. This seems like a terrible was to do it but it works. I'd love advice on how to improve this if you think it matters"
        /**
         * This isn't bad, I don't think.  Just looking at the docs you might want to do this in the
         * override for "onLayout" instead, but I haven't tested it so I don't know for sure.
         */


        if (!already_done) {
            // TODO: JAMES SENTELL "could you explain 'context' to me sometime?"

            /**
             * Sure, we can talk about this some more, but "context" is an Android system thing.
             * Just basically contains info about what is currently active on the app and the
             * device.
             */
            setButtonPoints(getContext());
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO look into clippath. May be able to use it to isolate the members

        // TODO move some of this outside of onDraw for efficiency
        // TODO: JAMES SENTELL "I don't have a deep understanding on getClipBounds(). DO you? If so, do you have any advice on how to set the bounds of the image to the full size of the view? I have a func"

        /**
         * Not entirely sure, perhaps try the following (straight from stackoverflow - use at own
         * risk...)
         *
         * ImageView imageView = (ImageView)findViewById(R.id.imageview);
         * Drawable drawable = imageView.getDrawable();
         * Rect imageBounds = drawable.getBounds();
         */
        Rect imageBounds = canvas.getClipBounds();  // Adjust this for where you want it
        mGrayedImage.setBounds(imageBounds);
        mFocusedImage.setBounds(imageBounds);

        mGrayedImage.draw(canvas);
        mFocusedImage.draw(canvas);

        // draws rectangles around arrowheads
        for (Rect rect1 : rectListArrowHead) {
            canvas.drawRect(rect1, paint_arrow_head_box);
        }

        // draws rectangles around points
        for (Rect rect7 : rectListButtons) {
            canvas.drawRect(rect7, paint_box);
        }

        // draws black circle at points in ArrayList pointList
        for (Point ptLst_dots : pointList) {
            canvas.drawCircle(ptLst_dots.x, ptLst_dots.y, dim_btn_radius, paint_points);
        }

        // draws arrows from pathlist pthLst_arrows
        for (Path pthLst_arrows : pathList) {
            canvas.drawPath(pthLst_arrows, paint_arrow);
        }

        if (debuggingTextToggle) {
            canvas.drawText("inside_button = " + String.valueOf(inside_button), 20, 100, paint_text);
            canvas.drawText("rectList_indice = " + String.valueOf(rectList_indice), 20, 160, paint_text);
            canvas.drawText("rectListArrowHead_indice = " + String.valueOf(rectListArrowHead_indice), 20, 220, paint_text);
            canvas.drawText("linkList = " + String.valueOf(linkList), 20, 280, paint_text);

            canvas.drawText("rectListArrowHead.size = " + String.valueOf(rectListArrowHead.size()), 20, 460, paint_text);
            canvas.drawText("clicked_on_button = " + String.valueOf(clicked_on_button), 20, 580, paint_text);
            canvas.drawText("clicked_on_arrow_head = " + String.valueOf(clicked_on_button), 20, 640, paint_text);

            canvas.drawText("inside_button = " + String.valueOf(inside_button), 600, 100, paint_text);
            canvas.drawText("size of pointlist = " + String.valueOf(pointList.size()), 600, 160, paint_text);
            canvas.drawText("size of pathlist = " + String.valueOf(pathList.size()), 600, 220, paint_text);
            canvas.drawText("size of linklist = " + String.valueOf(linkList.size()), 600, 280, paint_text);
            canvas.drawText("size of pointListArrowHead = " + String.valueOf(pointListArrowHead.size()), 600, 340, paint_text);
            canvas.drawText("size of angleListArrowHead = " + String.valueOf(angleListArrowHead.size()), 600, 400, paint_text);
            canvas.drawText("size of rectListArrowHead = " + String.valueOf(rectListArrowHead.size()), 600, 460, paint_text);
            canvas.drawText("arrow_animated_fraction = " + String.valueOf(arrow_animated_fraction), 600, 520, paint_text);
            canvas.drawText("able_to_click = " + String.valueOf(able_to_click), 600, 580, paint_text);

            canvas.drawText("angleListCheck first row = " + String.valueOf(angleListCheck.get(0)), 20, 1340, paint_text);
            canvas.drawText("angleListCheck second row = " + String.valueOf(angleListCheck.get(1)), 20, 1400, paint_text);
            canvas.drawText("angleListCheck third row = " + String.valueOf(angleListCheck.get(2)), 20, 1460, paint_text);
            canvas.drawText("size of angleListCheck first row = " + String.valueOf(angleListCheck.get(0).size()), 20, 1520, paint_text);
            canvas.drawText("size of angleListCheck second row = " + String.valueOf(angleListCheck.get(1).size()), 20, 1580, paint_text);

            canvas.drawText("pointListArrowHead = " + String.valueOf(pointListArrowHead), 20, 1640, paint_text);
            canvas.drawText("angleListArrowHead = " + String.valueOf(angleListArrowHead), 20, 1700, paint_text);
            canvas.drawText("linkList = " + String.valueOf(linkList), 20, 1760, paint_text);
            canvas.drawText("rectListButtons = " + String.valueOf(rectListButtons), 20, 1820, paint_text);
        }
    }

    // triggers long press
    final Handler handler = new Handler();
    // TODO JAMES SENTELL : "when is it appropriate to start names of things with a lowercase 'm' ?"
    /**
     * Different people have different opinions here, but in my experience starting variables with a
     * lowercase 'm' indicates a member variable of a class.  It is useful when you are writing
     * an API or something that someone else is going to use and you have a lot of getter/setter
     * functions (functions that just solely set or get private member variables).  That allows you
     * to name the setter parameter more clearly for whoever is going to use the api without
     * worrying about name shadowing or confusion.
     *
     * i.e.
     *
     * // Assume inside some class...
     * private int mCoolNumber;
     *
     * ...
     *
     * public void setCoolNumber(int coolNumber) {
     *     this.mCoolNumber = coolNumber;
     *     // or
     *     mCoolNumber = coolNumber;
     * }
     *
     *
     */

    Runnable mLongPressed = new Runnable() {
        public void run() {
            Log.i("", "Long press!");
            Toast.makeText(getContext(), "Long Pressed!", Toast.LENGTH_SHORT).show();

            // TODO add moment on long press
            // remove arrow on long press
            path_arrow.reset();
            pointListArrowHead.remove(rectListArrowHead_indice);
            angleListArrowHead.remove(rectListArrowHead_indice);
            linkList.remove(rectListArrowHead_indice);
            rectListArrowHead.remove(rectListArrowHead_indice);
            pathList.remove(rectListArrowHead_indice);

            // set booleans to false state
            clicked_on_button = false;
            clicked_on_arrow_head = false;
            inside_button = false;
            invalidate();
        }
    };

    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();
        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventaction) {
            case MotionEvent.ACTION_DOWN:
                invalidate();
                // able_to_click is used to eliminate rapid clicks which can cause problems
                if (able_to_click) {

                    int k = 0;
                    for (Rect rect_tmp1 : rectListButtons) {
                        if (rect_tmp1.contains(X, Y)) {
                            // touch is inside button
                            clicked_on_button = true;
                            // touch is NOT on arrow head
                            clicked_on_arrow_head = false;
                            // starts long press timer
                            handler.postDelayed(mLongPressed, 500);
                            rectList_indice = k;
                            btn_loc_x = rect_tmp1.centerX();
                            btn_loc_y = rect_tmp1.centerY();
                            rectListArrowHead_indice = rectListArrowHead.size();
                        }
                        k++;
                    }

                    // check if arrow head has just been clicked on
                    k = 0;
                    if (!clicked_on_button) {
                        for (Rect rect_tmp2 : rectListArrowHead) {
                            if (rect_tmp2.contains(X, Y)) {
                                // TODO: I belive the next two lines can be removed. Check if they can safely
                                clicked_on_arrow_head = true;
                                clicked_on_button = false;
                                rectListArrowHead_indice = k;
                                btn_loc_x = rectListButtons.get(linkList.get(rectListArrowHead_indice)).centerX();
                                btn_loc_y = rectListButtons.get(linkList.get(rectListArrowHead_indice)).centerY();
                            }
                            k++;
                        }
                    }

                    if (clicked_on_button) {
                        path_arrow = new Path();
                        pathList.add(path_arrow);
                        path_arrow.reset();
                        loc_arrow_point_x = X;
                        loc_arrow_point_y = Y;
                        angle = Math.atan2(loc_arrow_point_y - btn_loc_y, loc_arrow_point_x - btn_loc_x);

                        pointListArrowHead.add(new Point(loc_arrow_point_x, loc_arrow_point_y));
                        angleListArrowHead.add(angle);
                        linkList.add(rectList_indice);
                        rectListArrowHead.add(new Rect(loc_arrow_point_x - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_y - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_x + ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_y + ((int) dim_btn_radius + (int) dim_btn_radius_buffer)));

                        drawArrow();
                        invalidate();

                    } else if (clicked_on_arrow_head) {
                        path_arrow = new Path();
                        pathList.set(rectListArrowHead_indice, path_arrow); // <-- Add this line.
                        path_arrow.reset();
                        loc_arrow_point_x = X;
                        loc_arrow_point_y = Y;

                        btn_loc_x = rectListButtons.get(linkList.get(rectListArrowHead_indice)).centerX();
                        btn_loc_y = rectListButtons.get(linkList.get(rectListArrowHead_indice)).centerY();

                        angle = Math.atan2(loc_arrow_point_y - btn_loc_y, loc_arrow_point_x - btn_loc_x);
                        drawArrow();
                        invalidate();
                    }
                } else {
                    // this is to prevent rapid clicks causing problems
                    Toast.makeText(getContext(), "Don't tap so quickly", Toast.LENGTH_SHORT).show();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (clicked_on_button || clicked_on_arrow_head) {

                    // checks if touch is inside button
                    if (rectListButtons.get(rectList_indice).contains(X, Y)) {
                        inside_button = true;
                    } else {
                        // cancels long press handler if touch is dragged outside box
                        handler.removeCallbacks(mLongPressed);
                        inside_button = false;
                    }

                    path_arrow.reset();
                    loc_arrow_point_x = X;
                    loc_arrow_point_y = Y;

                    angle = Math.atan2(loc_arrow_point_y - btn_loc_y, loc_arrow_point_x - btn_loc_x);
                    pointListArrowHead.set(rectListArrowHead_indice, new Point(loc_arrow_point_x, loc_arrow_point_y));
                    angleListArrowHead.set(rectListArrowHead_indice, angle);
                    rectListArrowHead.set(rectListArrowHead_indice, new Rect(loc_arrow_point_x - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_y - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_x + ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_y + ((int) dim_btn_radius + (int) dim_btn_radius_buffer)));

                    drawArrow();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (clicked_on_button || clicked_on_arrow_head) {

                    // TODO move this into loop right below. Seems redundant
                    // checks if release is inside button
                    if (rectListButtons.get(linkList.get(rectListArrowHead_indice)).contains(X, Y)) {
                        inside_button = true;
                    }

                    // break if released inside button
                    if (inside_button) {
                        path_arrow.reset();

                        // cancel long press handler
                        handler.removeCallbacks(mLongPressed);

                        // remove arrow
                        pointListArrowHead.remove(rectListArrowHead_indice);
                        angleListArrowHead.remove(rectListArrowHead_indice);
                        linkList.remove(rectListArrowHead_indice);
                        rectListArrowHead.remove(rectListArrowHead_indice);
                        pathList.remove(rectListArrowHead_indice);

                        // reset booleans to false state
                        clicked_on_button = false;
                        clicked_on_arrow_head = false;
                        inside_button = false;
                        invalidate();
                        break;
                    }

                    path_arrow.reset();
                    loc_arrow_point_x = X;
                    loc_arrow_point_y = Y;

                    // calculates the angle of arrow at release
                    final double angle_start = Math.atan2(loc_arrow_point_y - btn_loc_y, loc_arrow_point_x - btn_loc_x);


                    // snaps arrow to pi/6 (30 degree) increments
                    double angle_dist = Math.abs(angles[0] - angle_start);
                    int idx = 0;
                    for (int c = 1; c < angles.length; c++) {
                        double tmp_angle_dist = Math.abs(angles[c] - angle_start);
                        if (tmp_angle_dist < angle_dist) {
                            idx = c;
                            angle_dist = tmp_angle_dist;
                        }
                    }
                    // calculates angle between released angle and the nearest 30 degree increment
                    angle_difference = angles[idx] - angle_start;

                    // breaks if arrows are being stacked
                    for (Point point5 : pointListArrowHead) {
                        int x_tmp = (int) (len_arrow_shaft * Math.cos(angles[idx]) + btn_loc_x);
                        int y_tmp = (int) (len_arrow_shaft * Math.sin(angles[idx]) + btn_loc_y);
                        if (point5.equals(x_tmp, y_tmp)) {
                            Toast.makeText(getContext(), "You can't stack arrows", Toast.LENGTH_SHORT).show();
                            path_arrow.reset();

                            // cancel long press handler
                            handler.removeCallbacks(mLongPressed);

                            // remove arrow from all arraylists
                            pointListArrowHead.remove(rectListArrowHead_indice);
                            angleListArrowHead.remove(rectListArrowHead_indice);
                            linkList.remove(rectListArrowHead_indice);
                            rectListArrowHead.remove(rectListArrowHead_indice);
                            pathList.remove(rectListArrowHead_indice);

                            // reset booleans to false state
                            clicked_on_button = false;
                            clicked_on_arrow_head = false;
                            inside_button = false;
                            invalidate();
                            return true; // breaks out of case switch loop
                        }
                    }

                    // calculates the length of the arrow shaft upon release
                    double len_arrow_shaft_start = Math.hypot((loc_arrow_point_x - btn_loc_x), (loc_arrow_point_y - btn_loc_y));

                    // animates decrease in length and angle
                    ValueAnimator animator = ValueAnimator.ofFloat((float) len_arrow_shaft_start, (float) len_arrow_shaft);
                    animator.setDuration(time_anim_arrow_dur);
                    animator.setInterpolator(new OvershootInterpolator());
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            arrow_animated_fraction = animation.getAnimatedFraction();
                            len_arrow_shaft_current = (Float) animation.getAnimatedValue();
                            angle = angle_start + arrow_animated_fraction * angle_difference;
                            loc_arrow_point_y = (int) (len_arrow_shaft_current * Math.sin(angle) + btn_loc_y);
                            loc_arrow_point_x = (int) (len_arrow_shaft_current * Math.cos(angle) + btn_loc_x);
                            path_arrow.reset();

                            // replace arrow position with new value each iteration
                            pointListArrowHead.set(rectListArrowHead_indice, new Point(loc_arrow_point_x, loc_arrow_point_y));
                            angleListArrowHead.set(rectListArrowHead_indice, angle);
                            rectListArrowHead.set(rectListArrowHead_indice, new Rect(loc_arrow_point_x - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_y - ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_x + ((int) dim_btn_radius + (int) dim_btn_radius_buffer), loc_arrow_point_y + ((int) dim_btn_radius + (int) dim_btn_radius_buffer)));

                            // prevents user from clicking while the arrow is animating. THis eliminates a bug where "ghost arrows" are created when user click rapidly
                            able_to_click = false;

                            // stops loop at end of animation
                            if (arrow_animated_fraction == 1) {
                                clicked_on_button = false;
                                clicked_on_arrow_head = false;

                                // allow user to click again
                                able_to_click = true;

                                // TODO
                                // checks if arrow placed is in a correct location
                                for (Double ang1 : angleListCheck.get(linkList.get(rectListArrowHead_indice))) {

                                    //rectListButtons.get(linkList.get(rectListArrowHead_indice)

                                    if (ang1.equals(angle)) {

                                        // convert angle to degrees
                                        double angle_degrees;
                                        if (ang1 == 0.0) {
                                            angle_degrees = Math.round(Math.toDegrees(ang1));
                                        } else {
                                            angle_degrees = Math.round(Math.toDegrees(-ang1));
                                        }

                                        // TODO: JAMES SENTELL: "I can't get a snackbar to work here. Any idea why? I assume it has somehting to do with 'this' "
                                        // Snackbar.make(this, "Correct angle! " + angle_degrees + "\u00B0", Snackbar.LENGTH_SHORT).show();
                                        Toast.makeText(getContext(), "in list", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            drawArrow();
                            invalidate(); // TODO: Change to invalidate("just the arrow drawn") for efficiency
                        }
                    });

                    // starts animator to snap arrow into position
                    animator.start();
                    invalidate();
                    // prints angle snapped to in degrees to snackbar
                    // All angles are inverted, so this if statement shows 0.0 instead of -0.0
                    double angle_degrees;
                    if (angles[idx] == 0.0) {
                        angle_degrees = Math.round(Math.toDegrees(angles[idx]));
                    } else {
                        angle_degrees = Math.round(Math.toDegrees(-angles[idx]));
                    }

                    //TODO add popup or snackbar that says where arrow was placed
                    Snackbar.make(this, "Created force at " + angle_degrees + "\u00B0", Snackbar.LENGTH_SHORT).show();
                    inside_button = false;
                }
                break;
        }
        return true;
    }

    private void drawArrow() {
        // sets angle of arrow head
        double angle_arrow_head_left = 4 * pi / 3 - angle;
        double angle_arrow_head_right = -pi / 3 - angle;

        // calculates location of points for both sides of arrow head
        float loc_arrow_head_left_x = (float) ((float) len_arrow_head * Math.sin(angle_arrow_head_left) + loc_arrow_point_x);
        float loc_arrow_head_left_y = (float) ((float) len_arrow_head * Math.cos(angle_arrow_head_left) + loc_arrow_point_y);
        float loc_arrow_head_right_x = (float) ((float) len_arrow_head * Math.sin(angle_arrow_head_right) + loc_arrow_point_x);
        float loc_arrow_head_right_y = (float) ((float) len_arrow_head * Math.cos(angle_arrow_head_right) + loc_arrow_point_y);

        // draws arrow shaft
        path_arrow.moveTo(btn_loc_x, btn_loc_y);
        path_arrow.lineTo(loc_arrow_point_x, loc_arrow_point_y);
        path_arrow.moveTo(loc_arrow_point_x, loc_arrow_point_y);

        // draws arrow head
        path_arrow.moveTo(loc_arrow_head_left_x, loc_arrow_head_left_y);
        path_arrow.lineTo(loc_arrow_point_x, loc_arrow_point_y);
        path_arrow.lineTo(loc_arrow_head_right_x, loc_arrow_head_right_y);
    }

    // converts dp to pixels
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    // converts screen percentage to pixels
    public Point percentToPx(PointF per) {
        return new Point(Math.round(per.x * viewWidth / 100), Math.round((per.y * viewHeight / 100)));
    }

    // TODO: remove this before release if unused
    // converts pixels to dp
    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    // This method keeps the aspect ration of the view constant. This is necessary since I am placing points on the canvas by percentage of the way across the view
    // code from https://github.com/jesperborgstrup/buzzingandroid/blob/master/src/com/buzzingandroid/ui/ViewAspectRatioMeasurer.java
    // The aspect ratio to be respected by the measurer
    private static final double VIEW_ASPECT_RATIO = .75; // Do not change this or you will have to re place all the points at the corect location!!
    private ViewAspectRatioMeasurer varm = new ViewAspectRatioMeasurer(VIEW_ASPECT_RATIO);

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        varm.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(varm.getMeasuredWidth(), varm.getMeasuredHeight());
    }

//    @Override
//    public boolean equals(Object obj) {
//        return !super.equals(obj);
//    }
//
//    @Override
//    public int hashCode() {
//        return getName().hashCode();
//    }

}