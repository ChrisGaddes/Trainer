package com.chrisgaddes.reaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;

public class ThirdActivity extends AppCompatActivity {
    private static final String TAG = "ThirdActivity";

    private DrawArrowsView mDrawArrowsView;
    private ImageView IV_peek_probMain_view;
    private ImageView IV_peek_parta;
    private ImageView IV_peek_partb;
    private ImageView IV_peek_parta_arrows;
    private ImageView IV_peek_partb_arrows;
    private ImageView IV_problem_part;

    private SquareImageView btn_peek_probMain;
    private SquareImageView btn_peek_parta;
    private SquareImageView btn_peek_partb;
    private SquareImageView btn_peek_parta_arrows;
    private SquareImageView btn_peek_partb_arrows;

    private String description;
    private String strDialogNextButton;
    private String part_letter;

    private AutoResizeTextView tv_current_statement;
    private AutoResizeTextView tv_prob_statement;
    private AutoResizeTextView tv_parta_statement;
    private AutoResizeTextView tv_partb_statement;

    private int problem_number;
    private int eventaction;

    private String str_toolbar_partCurrent_title;
    private String[] str_hint;
    private String[] str_part_statement;
    private String[] str_problem_statement;
    private String str_parta_statement[];
    private String str_partb_statement[];

    private FloatingActionButton fab_check_done;

    private TinyDB tinydb;

    private String previous_part_letter;
    private String str_parta_title;
    private String str_partb_title;
    private String str_toolbar_problem_title;

    private long pauseTime;
    private long resumeTime;
    private long totalForgroundTime;
    private long paused_timer_time;

    private ChronometerView rc;
    private String time_string_for_dialog;

    private Boolean enable_peek_a;
    private Boolean enable_peek_b;

    private Animation scaleIn;
    private Animation scaleOut;

    private Bitmap bm_A;
    private Bitmap bm_B;

    private Handler mHandler = new Handler();

    private AsyncTaskSaveViewToBitmap asyncSaveToBitmapPartA;
    private AsyncTaskSaveViewToBitmap asyncSaveToBitmapPartB;
    private MyCache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupWindowAnimations();
        setContentView(R.layout.activity_third);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        asyncSaveToBitmapPartA = new AsyncTaskSaveViewToBitmap();
        asyncSaveToBitmapPartB = new AsyncTaskSaveViewToBitmap();

        // initializes animations
        scaleIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_in);
        scaleOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_out);

        // initializes floating action button
        fab_check_done = (FloatingActionButton) findViewById(R.id.fab_check_done);

        // Sets database
        tinydb = new TinyDB(this);

        // initializes cache to store bitmaps in
        cache = new MyCache();
        cache.OpenOrCreateCache(this, "ArrowBitmaps");

        // Loads method to (re)startPart
        startPart();

        fab_check_done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean mrunCheckIfFinished = mDrawArrowsView.runCheckIfFinished();

                if (mrunCheckIfFinished) {
                    setDialogArrowsCorrect();
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), "Not Finished Yet...", Snackbar.LENGTH_LONG).setActionTextColor(ContextCompat.getColor(ThirdActivity.this, R.color.material_light_white)).setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            switch (event) {
                                case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                    btn_peek_probMain.setAlpha((float) 1.0);
                                    if (enable_peek_a) {
                                        btn_peek_parta.setAlpha((float) 1.0);
                                        btn_peek_parta_arrows.setAlpha((float) 1.0);
                                    }
                                    if (enable_peek_b) {
                                        btn_peek_partb.setAlpha((float) 1.0);
                                        btn_peek_partb_arrows.setAlpha((float) 1.0);
                                    }
                                    break;
                                case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                    btn_peek_probMain.setAlpha((float) 1.0);
                                    if (enable_peek_a) {
                                        btn_peek_parta.setAlpha((float) 1.0);
                                        btn_peek_parta_arrows.setAlpha((float) 1.0);
                                    }
                                    if (enable_peek_b) {
                                        btn_peek_partb.setAlpha((float) 1.0);
                                        btn_peek_partb_arrows.setAlpha((float) 1.0);
                                    }
                                    break;
                            }
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {
                            btn_peek_probMain.setAlpha((float) 0.0);
                            if (enable_peek_a) {
                                btn_peek_parta.setAlpha((float) 0.0);
                                btn_peek_parta_arrows.setAlpha((float) 0.0);
                            }
                            if (enable_peek_b) {
                                btn_peek_partb.setAlpha((float) 0.0);
                                btn_peek_partb_arrows.setAlpha((float) 0.0);
                            }
                        }
                    }).setAction("Hint", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialStyledDialog(ThirdActivity.this)
                                    .setTitle("Hint:")
                                    .setDescription(str_hint[0])
                                    .setIcon(R.drawable.ic_lightbulb_outline)
                                    .setStyle(Style.HEADER_WITH_ICON)
                                    .setScrollable(true)
                                    .setCancelable(true)
                                    .setPositive("Okay", new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                                }
                                            }
                                    ).show();
                        }
                    }).show();
                }
            }
        });

        btn_peek_probMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                eventaction = event.getAction();
                switch (eventaction) {
                    // TODO add if currently touched to

                    case MotionEvent.ACTION_DOWN:
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(str_toolbar_problem_title);
                        }
                        IV_peek_probMain_view.setAlpha((float) 1.0);
                        IV_problem_part.setAlpha((float) 0.0);
                        mDrawArrowsView.setAlpha((float) 0.0);
//                        tv_current_statement.setText(str_problem_statement[0]);

                        tv_current_statement.setAlpha((float) 0.0);
                        tv_prob_statement.setAlpha((float) 1.0);
                        tv_parta_statement.setAlpha((float) 0.0);
                        tv_partb_statement.setAlpha((float) 0.0);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            btn_peek_probMain.setElevation(dpToPx(8));
                        }

                        if (enable_peek_a) {
                            btn_peek_parta.startAnimation(scaleOut);
                            btn_peek_parta_arrows.startAnimation(scaleOut);
                        }

                        if (enable_peek_b) {
                            btn_peek_partb.startAnimation(scaleOut);
                            btn_peek_partb_arrows.startAnimation(scaleOut);
                        }

                        fab_check_done.hide();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(str_toolbar_partCurrent_title);
                        }
                        IV_peek_probMain_view.setAlpha((float) 0.0);
                        IV_problem_part.setAlpha((float) 1.0);

                        mDrawArrowsView.setAlpha((float) 1.0);
//                        tv_current_statement.setText(str_part_statement[0]);

                        tv_current_statement.setAlpha((float) 1.0);
                        tv_prob_statement.setAlpha((float) 0.0);
                        tv_parta_statement.setAlpha((float) 0.0);
                        tv_partb_statement.setAlpha((float) 0.0);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            btn_peek_probMain.setElevation(dpToPx(4));
                        }
                        if (enable_peek_a) {
                            btn_peek_parta.startAnimation(scaleIn);
                            btn_peek_parta_arrows.startAnimation(scaleIn);
                        }
                        if (enable_peek_b) {
                            btn_peek_partb.startAnimation(scaleIn);
                            btn_peek_partb_arrows.startAnimation(scaleIn);
                        }
                        fab_check_done.show();

                        break;
                }
                return true;
            }
        });

        // clicked on part a button
        btn_peek_parta.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                eventaction = event.getAction();
                if (enable_peek_a) {
                    switch (eventaction) {
                        case MotionEvent.ACTION_DOWN:
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(str_parta_title);
                            }

                            IV_peek_parta.setAlpha((float) 1.0);
                            IV_peek_parta_arrows.setAlpha((float) 1.0);
                            IV_problem_part.setAlpha((float) 0.0);
                            mDrawArrowsView.setAlpha((float) 0.0);
//                            tv_current_statement.setText(str_parta_statement[0]);

                            tv_current_statement.setAlpha((float) 0.0);
                            tv_prob_statement.setAlpha((float) 0.0);
                            tv_parta_statement.setAlpha((float) 1.0);
                            tv_partb_statement.setAlpha((float) 0.0);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                btn_peek_parta.setElevation(dpToPx(8));
                                btn_peek_parta_arrows.setElevation(dpToPx(8));
                            }
                            btn_peek_probMain.startAnimation(scaleOut);
                            btn_peek_partb.startAnimation(scaleOut);
                            btn_peek_partb_arrows.startAnimation(scaleOut);
                            fab_check_done.hide();
                            break;

                        case MotionEvent.ACTION_UP:
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(str_toolbar_partCurrent_title);
                            }
                            IV_peek_parta.setAlpha((float) 0.0);
                            IV_peek_parta_arrows.setAlpha((float) 0.0);
                            IV_problem_part.setAlpha((float) 1.0);
                            mDrawArrowsView.setAlpha((float) 1.0);
//                            tv_current_statement.setText(str_part_statement[0]);

                            tv_current_statement.setAlpha((float) 1.0);
                            tv_prob_statement.setAlpha((float) 0.0);
                            tv_parta_statement.setAlpha((float) 0.0);
                            tv_partb_statement.setAlpha((float) 0.0);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                btn_peek_parta.setElevation(dpToPx(4));
                                btn_peek_parta_arrows.setElevation(dpToPx(4));
                            }

                            btn_peek_probMain.startAnimation(scaleIn);
                            if (enable_peek_b) {
                                btn_peek_partb.startAnimation(scaleIn);
                                btn_peek_partb_arrows.startAnimation(scaleIn);
                            }
                            fab_check_done.show();

                            break;
                    }
                }
                return true;
            }
        });

        // clicked on part b button
        btn_peek_partb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                eventaction = event.getAction();
                if (enable_peek_b) {
                    switch (eventaction) {
                        case MotionEvent.ACTION_DOWN:
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(str_partb_title);
                            }
                            IV_peek_partb.setAlpha((float) 1.0);
                            IV_peek_partb_arrows.setAlpha((float) 1.0);
                            IV_problem_part.setAlpha((float) 0.0);
                            mDrawArrowsView.setAlpha((float) 0.0);
//                            tv_current_statement.setText(str_partb_statement[0]);

                            tv_current_statement.setAlpha((float) 0.0);
                            tv_prob_statement.setAlpha((float) 0.0);
                            tv_parta_statement.setAlpha((float) 0.0);
                            tv_partb_statement.setAlpha((float) 1.0);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                btn_peek_partb.setElevation(dpToPx(8));
                                btn_peek_partb_arrows.setElevation(dpToPx(8));
                            }
                            btn_peek_probMain.startAnimation(scaleOut);
                            btn_peek_parta.startAnimation(scaleOut);
                            btn_peek_parta_arrows.startAnimation(scaleOut);
                            fab_check_done.hide();
                            break;

                        case MotionEvent.ACTION_UP:
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(str_toolbar_partCurrent_title);
                            }
                            IV_peek_partb.setAlpha((float) 0.0);
                            IV_peek_partb_arrows.setAlpha((float) 0.0);
                            IV_problem_part.setAlpha((float) 1.0);
                            mDrawArrowsView.setAlpha((float) 1.0);
//                            tv_current_statement.setText(str_part_statement[0]);

                            tv_current_statement.setAlpha((float) 1.0);
                            tv_prob_statement.setAlpha((float) 0.0);
                            tv_parta_statement.setAlpha((float) 0.0);
                            tv_partb_statement.setAlpha((float) 0.0);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                btn_peek_partb.setElevation(dpToPx(4));
                                btn_peek_partb_arrows.setElevation(dpToPx(4));
                            }
                            btn_peek_probMain.startAnimation(scaleIn);
                            btn_peek_parta.startAnimation(scaleIn);
                            btn_peek_parta_arrows.startAnimation(scaleIn);
                            fab_check_done.show();

                            break;
                    }
                }
                return true;
            }
        });

        // starts showcase views

    }


    private void startPart() {

        // Loads problem information
        problem_number = tinydb.getInt("problem_number");
        part_letter = tinydb.getString("part_letter");
        tinydb.putString("prob" + problem_number + "_part_letter", part_letter);

        getEnablePeek();

        mDrawArrowsView = (DrawArrowsView) findViewById(R.id.idDrawArrowsView);
        mDrawArrowsView.setAlpha((float) 1.0);

        // load problem statement and part statement
        str_problem_statement = getResources().getStringArray(getResId("mainProblemStatement_" + "prob" + problem_number + "_part" + part_letter, R.array.class));
        str_part_statement = getResources().getStringArray(getResId("problemStatement_" + "prob" + problem_number + "_part" + part_letter, R.array.class));
        str_parta_statement = getResources().getStringArray(getResId("problemStatement_" + "prob" + problem_number + "_part" + "A", R.array.class));
        str_partb_statement = getResources().getStringArray(getResId("problemStatement_" + "prob" + problem_number + "_part" + "B", R.array.class));
        str_hint = getResources().getStringArray(getResId("hint_" + "prob" + problem_number + "_part" + part_letter, R.array.class));

        // converts to lower case
        part_letter = part_letter.toLowerCase();

        // Generates strings from problem information
        str_toolbar_partCurrent_title = "#" + problem_number + " - Part " + part_letter.toUpperCase();
        String str_partCurrent_file_name = "prob" + problem_number + "_part" + part_letter;
        String str_parta_file_name = "prob" + problem_number + "_part" + "a";
        String str_partb_file_name = "prob" + problem_number + "_part" + "b";

        String str_probCurrent_file_name = "prob" + problem_number;
        str_toolbar_problem_title = "Problem #" + problem_number;
        str_parta_title = "#" + problem_number + " - Part " + "A";
        str_partb_title = "#" + problem_number + " - Part " + "B";

        // Sets text for problem statement
        tv_current_statement = (AutoResizeTextView) this.findViewById(R.id.tv_current_statement);
        tv_current_statement.setText(str_part_statement[0]);
        // set max text size
        tv_current_statement.setMaxTextSize(56);
        tv_current_statement.setText(str_part_statement[0]);

        // Sets text for peek problem statement
        tv_prob_statement = (AutoResizeTextView) this.findViewById(R.id.tv_prob_statement);
        tv_prob_statement.setText(str_part_statement[0]);
        // set max text size
        tv_prob_statement.setMaxTextSize(56);
        tv_prob_statement.setText(str_problem_statement[0]);
        tv_prob_statement.setAlpha((float) 0.0);

        // Sets text for parta statement
        tv_parta_statement = (AutoResizeTextView) this.findViewById(R.id.tv_parta_statement);
        tv_parta_statement.setText(str_part_statement[0]);
        // set max text size
        tv_parta_statement.setMaxTextSize(56);
        tv_parta_statement.setText(str_parta_statement[0]);
        tv_parta_statement.setAlpha((float) 0.0);

        // Sets text for partb statement
        tv_partb_statement = (AutoResizeTextView) this.findViewById(R.id.tv_partb_statement);
        tv_partb_statement.setText(str_part_statement[0]);
        // set max text size
        tv_partb_statement.setMaxTextSize(56);
        tv_partb_statement.setText(str_partb_statement[0]);
        tv_partb_statement.setAlpha((float) 0.0);


        // Sets image for part
        IV_problem_part = (ImageView) findViewById(R.id.problem_part);
        Glide.with(this)
                .load(getResources().getIdentifier(str_partCurrent_file_name, "drawable", getPackageName()))
                .into(IV_problem_part);

        // Sets image for Main problem and sets "invisible"
        IV_peek_probMain_view = (ImageView) findViewById(R.id.peek_probCurrent_view);
        IV_peek_probMain_view.setAlpha((float) 0.0);
        btn_peek_probMain = (SquareImageView) findViewById(R.id.btn_peek_prob);
        btn_peek_probMain.setAlpha((float) 0.0);
        Glide.with(this)
                .load(getResources().getIdentifier(str_probCurrent_file_name, "drawable", getPackageName()))
                .load(getResources().getIdentifier(str_probCurrent_file_name, "drawable", getPackageName()))
                .into(IV_peek_probMain_view);
        Glide.with(this)
                .load(getResources().getIdentifier(str_probCurrent_file_name, "drawable", getPackageName()))
                .into(btn_peek_probMain);

        // Sets image for part a
        IV_peek_parta = (ImageView) findViewById(R.id.peek_parta);
        IV_peek_parta_arrows = (ImageView) findViewById(R.id.peek_parta_arrows);
        IV_peek_parta.setAlpha((float) 0.0);
        IV_peek_parta_arrows.setAlpha((float) 0.0);
        btn_peek_parta = (SquareImageView) findViewById(R.id.btn_peek_parta);
        btn_peek_parta_arrows = (SquareImageView) findViewById(R.id.btn_peek_parta_arrows);
        btn_peek_parta.setAlpha((float) 0.0);
        btn_peek_parta_arrows.setAlpha((float) 0.0);
        Glide.with(this)
                .load(getResources().getIdentifier(str_parta_file_name, "drawable", getPackageName()))
                .into(IV_peek_parta);
        Glide.with(this)
                .load(getResources().getIdentifier(str_parta_file_name, "drawable", getPackageName()))
                .into(btn_peek_parta);

        // Sets image for part b
        IV_peek_partb = (ImageView) findViewById(R.id.peek_partb);
        IV_peek_partb_arrows = (ImageView) findViewById(R.id.peek_partb_arrows);
        IV_peek_partb.setAlpha((float) 0.0);
        IV_peek_partb_arrows.setAlpha((float) 0.0);
        btn_peek_partb = (SquareImageView) findViewById(R.id.btn_peek_partb);
        btn_peek_partb_arrows = (SquareImageView) findViewById(R.id.btn_peek_partb_arrows);
        btn_peek_partb.setAlpha((float) 0.0);
        btn_peek_partb_arrows.setAlpha((float) 0.0);
        Glide.with(this)
                .load(getResources().getIdentifier(str_partb_file_name, "drawable", getPackageName()))
                .into(IV_peek_partb);
        Glide.with(this)
                .load(getResources().getIdentifier(str_partb_file_name, "drawable", getPackageName()))
                .into(btn_peek_partb);

        // Sets toolbar title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(str_toolbar_partCurrent_title);
        }
        // reloads peek images from files if activity destroyed and recreated
        insertPeekIfDestroyed();

        showPeekImages();

        tv_current_statement.resetTextSize();

//
    }

    private void showIntro1() {
        new MaterialIntroView.Builder(this)
//                .enableDotAnimation(true)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .setInfoText("Read the problem statement for Part A carefully and tap in the circle to continue.\n\n COMMON MISTAKE: Just because the pins (the small black circles) are NOT included in the free-body diagram does NOT mean there are no forces applied at points A and B.")
                .setTarget(findViewById(R.id.tv_current_statement))
                .setUsageId("IntroThirdAct1_" + tinydb.getString("ID_IntroView"))
//                .setConfiguration(matIntroConfig)
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {
                        showIntro3();
                    }
                })
                .show();
    }



    private void showIntro3() {
        new MaterialIntroView.Builder(this)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .enableFadeAnimation(true)
                .setInfoText("Before you start drawing, click on the help button here for a quick tutorial.")
                .setTarget(findViewById(R.id.action_help))
                .setUsageId("IntroThirdAct3_" + tinydb.getString("ID_IntroView"))
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {
                        showIntro4();
                        findViewById(R.id.action_help).performClick();

                    }
                })
                .show();
    }

    private void showIntro4() {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .enableIcon(false)
                .setDelayMillis(1000)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .enableFadeAnimation(true)
                .setInfoText("Press and hold here to reference the main problem statement.")
                .setTarget(findViewById(R.id.btn_peek_prob))
                .setUsageId("IntroThirdAct4_" + tinydb.getString("ID_IntroView"))
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {

                        btn_peek_probMain.performClick();

                        showIntro5();
                        //initiate the button
//                        btn_peek_probMain.performClick();
//                        btn_peek_probMain.setPressed(true);
//                        btn_peek_probMain.invalidate();
//                        // delay completion till animation completes
//                        btn_peek_probMain.postDelayed(new Runnable() {  //delay button
//                            public void run() {
//                                btn_peek_probMain.setPressed(false);
//                                btn_peek_probMain.invalidate();
//                                //any other associated action
//                            }
//                        }, 800);  // .8secs delay time
                    }
                })
                .show();
    }

    private void showIntro5() {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .setDelayMillis(2000)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .enableFadeAnimation(true)
                .setInfoText("Click here to remove all arrows and startover. (You probably haven't drawn any arrows yet, but this may be useful later.)")
                .setTarget(findViewById(R.id.action_startover))
                .setUsageId("IntroThirdAct5_" + tinydb.getString("ID_IntroView"))
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {
                        findViewById(R.id.action_startover).performClick();
                        showIntro6();

                    }
                })
                .show();
    }

    private void showIntro6() {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .setDelayMillis(500)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .enableFadeAnimation(true)
                .setInfoText("Finally, click on the red check button to see if you have drawn the forces and moments correctly. A popup bar with a HINT button will appear if you're having trouble.")
                .setTarget(findViewById(R.id.fab_check_done))
                .setUsageId("IntroThirdAct6_" + tinydb.getString("ID_IntroView"))
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {
                        fab_check_done.performClick();
                        showIntro7();

                    }
                })
                .show();
    }

    private void showIntro7() {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .setDelayMillis(2000)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .enableFadeAnimation(true)
                .setInfoText("That should do it for now. Click here and start drawing forces. Good luck!")
                .setTarget(findViewById(R.id.problem_part))
                .setUsageId("IntroThirdAct7_" + tinydb.getString("ID_IntroView"))
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {
                    }
                })
                .show();
    }

    private void insertPeekIfDestroyed() {
        // reloads peek images from files
        if (enable_peek_a) {
            if (btn_peek_parta_arrows.getDrawable() == null) {
                Log.d(TAG, "Peek A is null");
                String db_title_A = "prob" + problem_number + "_part" + "A" + "_arrows";
                bm_A = cache.OpenBitmap(db_title_A);

                IV_peek_parta_arrows.setImageBitmap(bm_A);
                btn_peek_parta_arrows.setImageBitmap(bm_A);
            }
        }

        if (enable_peek_b) {
            if (btn_peek_partb_arrows.getDrawable() == null) {
                Log.d(TAG, "Peek B is null");
                String db_title_B = "prob" + problem_number + "_part" + "B" + "_arrows";
                bm_B = cache.OpenBitmap(db_title_B);

                IV_peek_partb_arrows.setImageBitmap(bm_B);
                btn_peek_partb_arrows.setImageBitmap(bm_B);
            }
        }
    }

    private void getEnablePeek() {
        // sets enable peek booleans
        if (tinydb.getString("part_letter").equals("A")) {
            enable_peek_a = false;
            enable_peek_b = false;
        } else if (tinydb.getString("part_letter").equals("B")) {
            enable_peek_a = true;
            enable_peek_b = false;
        } else if (tinydb.getString("part_letter").equals("C")) {
            enable_peek_a = true;
            enable_peek_b = true;
        } else {
            enable_peek_a = false;
            enable_peek_b = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // pauses timer
        paused_timer_time = SystemClock.elapsedRealtime();

        if (rc != null) {
            rc.stop();
        }

        pauseTime = System.currentTimeMillis();
        totalForgroundTime = tinydb.getLong("TotalForegroundTime", 0) + (pauseTime - resumeTime);
        tinydb.putLong("TotalForegroundTime", totalForgroundTime);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // resumes timer
        if (rc != null) {
            rc.setPauseTimeOffset(SystemClock.elapsedRealtime() - paused_timer_time);
            rc.run();
        }

        resumeTime = System.currentTimeMillis();
        Log.d(TAG, "onResume run");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG + "onDestroy", "Destroyed");

        // TODO remove this unbindDrawables if safe
        unbindDrawables(findViewById(R.id.relativeLayout_Main));
    }

    private void setupWindowAnimations() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Explode explode = new Explode();
            explode.setDuration(250);

            //exclude toolbar
            explode.excludeTarget(R.id.toolbar, true);
            //exclude status bar
            explode.excludeTarget(android.R.id.statusBarBackground, true);
            //exclude navigation bar
            explode.excludeTarget(android.R.id.navigationBarBackground, true);
        }
    }

    private void setDialogArrowsCorrect() {

        // marks problem as complete
        tinydb.putBoolean("prob" + problem_number + "_completed", true);

        // stop counter
        rc.stop();

        long timer_time = rc.getCurrentTime();
        long minutes = timer_time / 60;
        long seconds = timer_time - (60 * minutes);
        if (minutes > 0) {
            time_string_for_dialog = minutes + " minutes, " + seconds + " seconds!";
        } else if (minutes <= 0 && seconds == 1) {
            time_string_for_dialog = seconds + " second... Inconceivable!";
        } else if (minutes <= 0 && seconds != 1) {
            time_string_for_dialog = seconds + " seconds!";
        }

        switch (part_letter.toUpperCase()) {
            case "A":
                previous_part_letter = tinydb.getString("part_letter");
                tinydb.putString("part_letter", "B");
                tinydb.putString("prob" + problem_number + "_part_letter", "B");
                asyncSaveToBitmapPartA.execute();
                showDialogCorrectPartA();
                break;
            case "B":
                previous_part_letter = tinydb.getString("part_letter");
                tinydb.putString("part_letter", "C");
                tinydb.putString("prob" + problem_number + "_part_letter", "C");
                asyncSaveToBitmapPartB.execute();
                showDialogCorrectPartB();
                break;
            case "C":
                tinydb.putString("prob" + problem_number + "_part_letter", "Done");

                if (problem_number == 3) {
                    tinydb.putBoolean("survey_allowed", true);

                    // gets total app time usage
                    pauseTime = System.currentTimeMillis();
                    totalForgroundTime = tinydb.getLong("TotalForegroundTime", 0) + (pauseTime - resumeTime);
                    tinydb.putLong("TotalForegroundTime", totalForgroundTime);

                    // puts final time the app was used into this variable
                    tinydb.putLong("time_for_survey", totalForgroundTime);

                    // sets finish time value to Clipboard
                    String stringYouExtracted = Long.toString(tinydb.getLong("time_for_survey", 0));
                    setClipboard(stringYouExtracted);

                    // TODO delete these two as needed
                    previous_part_letter = tinydb.getString("part_letter");
                    tinydb.putString("part_letter", "A");

                    // show Dialog correct which includes link to survey
                    showDialogCorrectSurvey();

                } else {
                    showDialogCorrectPartC();
                    break;
                }
        }

        // animate buttons out
        btn_peek_probMain.startAnimation(scaleOut);
        btn_peek_parta.startAnimation(scaleOut);
        btn_peek_parta_arrows.startAnimation(scaleOut);
        btn_peek_partb.startAnimation(scaleOut);
        btn_peek_partb_arrows.startAnimation(scaleOut);
        fab_check_done.hide();
    }

    private void showDialogCorrectPartA() {
        description = "All forces placed correctly! You finished in " + time_string_for_dialog;
        strDialogNextButton = "Next Part";
        new MaterialStyledDialog(this)
                .setTitle("Correct!")
                .setDescription(description)
                .setIcon(R.drawable.ic_check)
                .setStyle(Style.HEADER_WITH_ICON)
                .setScrollable(true)
                .setCancelable(false)
                .setPositive(strDialogNextButton, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        mDrawArrowsView.setAlpha((float) 0.0);
                        fab_check_done.show();

                        // resets DrawArrowsView
                        mDrawArrowsView.resetForNextPart();
                        mDrawArrowsView.loadArrowCheckLocations();

                        startPart();
                            }
                        }
                ).show();
    }

    private void showDialogCorrectPartB() {
        description = "All forces placed correctly! You finished in " + time_string_for_dialog;
        strDialogNextButton = "Next Part";
        new MaterialStyledDialog(this)
                .setTitle("Correct!")
                .setDescription(description)
                .setIcon(R.drawable.ic_check)
                .setStyle(Style.HEADER_WITH_ICON)
                .setScrollable(true)
                .setCancelable(false)
                .setPositive(strDialogNextButton, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        mDrawArrowsView.setAlpha((float) 0.0);
                        fab_check_done.show();

                        // resets DrawArrowsView
                        mDrawArrowsView.resetForNextPart();
                        mDrawArrowsView.loadArrowCheckLocations();

                        startPart();
                            }
                        }
                ).show();
    }

    private void showDialogCorrectPartC() {
        description = "All forces placed correctly! You finished in " + time_string_for_dialog + " Problem " + problem_number + " is now complete.";
        strDialogNextButton = "Main Menu";
        new MaterialStyledDialog(this)
                .setTitle("Correct! Problem " + problem_number + " finished!")
                .setDescription(description)
                .setIcon(R.drawable.ic_check)
                .setStyle(Style.HEADER_WITH_ICON)
                .setScrollable(true)
                .setCancelable(false)
                .setPositive("Next Problem", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        mDrawArrowsView.setAlpha((float) 0.0);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ThirdActivity.this);
                        // TODO put logic in here so it knows how many problems there are

                        // put code here that redirects them to survey if they finish all 3 problems
                        if (problem_number < 3) {
//                            tinydb.putBoolean("prob" + problem_number + "_completed", true);
                            int new_problem_number = problem_number + 1;
                            tinydb.putInt("problem_number", new_problem_number);
                            tinydb.putString("part_letter", "A");
                            tinydb.putString("prob" + new_problem_number + "_part_letter", "A");
                        }
                        Intent mainIntent = new Intent(ThirdActivity.this, SecondActivity.class);
                        startActivity(mainIntent, options.toBundle());
                        finish();
                    }
                })
                .setNegative("Main Menu", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        mDrawArrowsView.setAlpha((float) 0.0);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ThirdActivity.this);
//                        part_letter = tinydb.getString("part_letter");

                        // TODO put logic in here so it knows how many problems there are instead of hard coding in 3
                        if (problem_number < 3) {
                            tinydb.putBoolean("prob" + problem_number + "_completed", true);
                            Intent mainIntent = new Intent(ThirdActivity.this, MainActivity.class);
                            startActivity(mainIntent, options.toBundle());
                            finish();
                        }
                    }
                })
                .show();
    }

    private void showDialogCorrectSurvey() {
        description = "All forces placed correctly! You finished in " + time_string_for_dialog + "Problem " + problem_number + " is now complete.";
        strDialogNextButton = "Main Menu";
        new MaterialStyledDialog(this)
                .setTitle("Correct! Problem " + problem_number + " finished!")
                .setDescription(description)
                .setIcon(R.drawable.ic_check)
                .setStyle(Style.HEADER_WITH_ICON)
                .setScrollable(true)
                .setCancelable(false)
                .setPositive("Take 1 min Survey", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        showSurveyPrompt();
//                        String url = "https://goo.gl/forms/0wl3LGhqtNYC4oyA2";

//                        finish();

                    }
                })
                .setNegative("Main Menu", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        mDrawArrowsView.setAlpha((float) 0.0);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ThirdActivity.this);
                        part_letter = tinydb.getString("part_letter");

                        // TODO put logic in here so it knows how many problems there are instead of hard coding in 3
//                        if (problem_number < 3) {
                            Intent mainIntent = new Intent(ThirdActivity.this, MainActivity.class);
                            startActivity(mainIntent, options.toBundle());
                            finish();
//                        }
                    }
                })
                .show();
    }

    private void showSurveyPrompt() {

        pauseTime = System.currentTimeMillis();
        totalForgroundTime = tinydb.getLong("TotalForegroundTime", 0) + (pauseTime - resumeTime);
        tinydb.putLong("TotalForegroundTime", totalForgroundTime);

        // sets finish time value to Clipboard
        String stringYouExtracted = Long.toString(Math.round(totalForgroundTime / 1000));
        setClipboard(stringYouExtracted);


        Toast.makeText(ThirdActivity.this, "Copied time: " + stringYouExtracted + " seconds to clipboard ", Toast.LENGTH_LONG).show();

        new MaterialStyledDialog(ThirdActivity.this)
                .setTitle("Usage time: " + stringYouExtracted + " seconds")
                .setDescription("The length of time you have used this app, " + stringYouExtracted + " seconds, has been copied to your clipboard. Please paste it into the survey as directed.")
                .setIcon(R.drawable.ic_assignment_light)
                .setStyle(Style.HEADER_WITH_ICON)
                .setScrollable(true)
//                .setCancelable(true)
                .setPositive("Take Survey", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        String url = "https://goo.gl/forms/20nMeq7L0KCilwym2";
                        Intent openSurveyUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(openSurveyUrl);
                        finish();
                    }
                })
                .show();
    }

    private void helpDialog() {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ThirdActivity.this);
        Intent intent = new Intent(ThirdActivity.this, HelpActivity.class);
        startActivity(intent, options.toBundle());

    }

    private int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.third_menu, menu);

        startTimer(menu);
        showIntro1();
        return super.onCreateOptionsMenu(menu);
    }

    private void startTimer(Menu menu) {

        rc = (ChronometerView) menu
                .findItem(R.id.timer)
                .getActionView();
        rc.setPauseTimeOffset(tinydb.getLong("TotalForegroundTime", 0));
        rc.setOverallDuration(2 * 600);
        rc.setWarningDuration(90);
        rc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        rc.reset();
        rc.run();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivity(i);
                return true;

            case R.id.action_help:
                // User chose the "Help" action, shows help popup
                helpDialog();
                return true;

            case R.id.action_startover:
                // User chose the "Start over" action, resets all arrows
                mDrawArrowsView.setAllToUnused();
                mDrawArrowsView.resetAllValues();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private Bitmap getBitmapFromView(View view) {
        // defines a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // binds a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);

        // saves arrows from DrawArrowsView to bitmap
        view.draw(canvas);

        // returns the bitmap
        return returnedBitmap;
    }

    // converts dp to pixels
    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private class AsyncTaskSaveViewToBitmap extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            Bitmap bm = getBitmapFromView(mDrawArrowsView);

            String db_title_A = "prob" + problem_number + "_part" + "A" + "_arrows";
            String db_title_B = "prob" + problem_number + "_part" + "B" + "_arrows";

            // part_letter: A
            if (previous_part_letter.toUpperCase().equals("A")) {
                // saves Bitmap of finished Part A to file
                cache.SaveBitmap(db_title_A, bm);
                bm_A = bm;
            }

            // part_letter: B
            if (previous_part_letter.toUpperCase().equals("B")) {
                // loads Bitmap of finished Part A
                cache.SaveBitmap(db_title_B, bm);
                bm_A = cache.OpenBitmap(db_title_A);
                bm_B = bm;
            }

            // part_letter: C
            if (previous_part_letter.toUpperCase().equals("A")) {
                // loads Bitmap of finished Part A & B
//                cache.SaveBitmap(db_title_B, bm);
                bm_A = cache.OpenBitmap(db_title_A);
                bm_B = cache.OpenBitmap(db_title_B);
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            if (previous_part_letter.toUpperCase().equals("A")) {
                IV_peek_parta_arrows.setImageBitmap(bm_A);
                btn_peek_parta_arrows.setImageBitmap(bm_A);
            }

            if (previous_part_letter.toUpperCase().equals("B")) {
                IV_peek_parta_arrows.setImageBitmap(bm_A);
                btn_peek_parta_arrows.setImageBitmap(bm_A);
                IV_peek_partb_arrows.setImageBitmap(bm_B);
                btn_peek_partb_arrows.setImageBitmap(bm_B);
            }
        }
    }

    private void showPeekImages() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                getEnablePeek();

                if (tinydb.getString("part_letter").equals("A")) {
                    btn_peek_probMain.startAnimation(scaleIn);
                    btn_peek_probMain.setAlpha((float) 1.0);
                } else if (!tinydb.getString("part_letter").equals("A")) {
                    // shows appropriate peek buttons
                    btn_peek_probMain.startAnimation(scaleIn);
                    btn_peek_probMain.setAlpha((float) 1.0);

                    if (enable_peek_a) {
                        btn_peek_parta.startAnimation(scaleIn);
                        btn_peek_parta_arrows.startAnimation(scaleIn);
                        btn_peek_parta.setAlpha((float) 1.0);
                        btn_peek_parta_arrows.setAlpha((float) 1.0);
                    } else {
                        btn_peek_parta.setAlpha((float) 0.0);
                        btn_peek_parta_arrows.setAlpha((float) 0.0);
                    }

                    if (enable_peek_b) {
                        btn_peek_parta.startAnimation(scaleIn);
                        btn_peek_parta_arrows.startAnimation(scaleIn);
                        btn_peek_parta.setAlpha((float) 1.0);
                        btn_peek_parta_arrows.setAlpha((float) 1.0);

                        btn_peek_partb.startAnimation(scaleIn);
                        btn_peek_partb_arrows.startAnimation(scaleIn);
                        btn_peek_partb.setAlpha((float) 1.0);
                        btn_peek_partb_arrows.setAlpha((float) 1.0);
                    } else {
                        btn_peek_partb.setAlpha((float) 0.0);
                        btn_peek_partb_arrows.setAlpha((float) 0.0);
                    }
                }
            }
        }, 750);
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    private class MyCache {

        private String DiretoryName;

        private void OpenOrCreateCache(Context _context, String _directoryName) {
            File file = new File(_context.getCacheDir().getAbsolutePath() + "/" + _directoryName);
            if (!file.exists()) {
                file.mkdir();
            }
            DiretoryName = file.getAbsolutePath();
        }

        private void SaveBitmap(String fileName, Bitmap bmp) {
            try {
                File file = new File(DiretoryName + "/" + fileName);
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream Filestream = new FileOutputStream(DiretoryName + "/" + fileName);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Filestream.write(byteArray);
                Filestream.close();
                bmp = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Bitmap OpenBitmap(String name) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                File file = new File(DiretoryName + "/" + name);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(DiretoryName + "/" + name, options);
                    return bitmap;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void setClipboard(String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ThirdActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ThirdActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Text : ", text);
            clipboard.setPrimaryClip(clip);
        }
    }


}

