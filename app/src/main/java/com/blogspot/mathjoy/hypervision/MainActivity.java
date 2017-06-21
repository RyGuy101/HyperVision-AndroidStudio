package com.blogspot.mathjoy.hypervision;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static boolean doinDaDemo = false; //The Demo only works for the tablet version

    HyperView hp;
    public static MainActivity activity;
    int settingsWidth = -1;
    int demoStep = -1;

    public void startDemo(View v) {
        findViewById(R.id.demoButt).setVisibility(View.GONE);
        findViewById(R.id.demoText).setVisibility(View.VISIBLE);
        findViewById(R.id.nextButt).setVisibility(View.VISIBLE);
    }

    public void nextDemo(View v) {
        if (demoStep == -1) {
            ((TextView) findViewById(R.id.demoText)).setText("Put on the 3D glasses");//Html.fromHtml("This is the <b>Hypercube</b>")
            demoStep++;
        } else if (demoStep == 0) {
            ((TextView) findViewById(R.id.demoText)).setTextSize(24);
            ((TextView) findViewById(R.id.demoText)).setText("Drag your finger to rotate the hypercube");
            demoStep++;
        } else if (demoStep == 1) {
            ((TextView) findViewById(R.id.demoText)).setTextSize(25);
            ((TextView) findViewById(R.id.demoText)).setText("Use the buttons on the left to rotate in 3D or 4D");
            demoStep++;
        } else if (demoStep == 2) {
            ((TextView) findViewById(R.id.demoText)).setTextSize(30);
            ((TextView) findViewById(R.id.demoText)).setText("3D rotation looks normal");
            hp.autoRotate = true;
            hp.autoRotateAngle = 360;
            hp.autoRotateAngle = 360;
            if (hp.rotateDim != 3) {
                switchRotateDimension();
            }
            demoStep++;
        } else if (demoStep == 3) {
            ((TextView) findViewById(R.id.demoText)).setText("But 4D rotation looks crazy!");
            hp.autoRotate = true;
            hp.autoRotateAngle = 360;
            if (hp.rotateDim != 2) {
                switchRotateDimension();
            }
            demoStep++;
        } else if (demoStep == 4) {
            ((TextView) findViewById(R.id.demoText)).setText("Have fun at the BRAG Festival!");
            hp.stopTouch = System.currentTimeMillis();
            hp.demo = false;
            hp.autoRotate = false;
            demoStep++;
        } else {
            ((TextView) findViewById(R.id.demoText)).setText("HyperVision by Ryan Nemiroff");
            findViewById(R.id.endDemoButt).setVisibility(View.VISIBLE);
            findViewById(R.id.demoText).setVisibility(View.GONE);
            findViewById(R.id.nextButt).setVisibility(View.GONE);
            demoStep = -1;
        }
    }

    public void endDemo(View v) {
        ((TextView) findViewById(R.id.demoText)).setText("HyperVision by Ryan Nemiroff");
        findViewById(R.id.demoText).setVisibility(View.GONE);
        findViewById(R.id.nextButt).setVisibility(View.GONE);
        demoStep = -1;
        findViewById(R.id.endDemoButt).setVisibility(View.GONE);
        findViewById(R.id.demoButt).setVisibility(View.VISIBLE);
        hp.autoRotate = true;
        hp.autoRotateAngle = 360;
        if (hp.rotateDim != 3) {
            switchRotateDimension();
        }
        if (findViewById(R.id.showSettingsButt).getVisibility() == View.VISIBLE) {
            showSettings(null);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        create();
    }

    private void create() {
        if (getResources().getBoolean(R.bool.isTablet)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            if (doinDaDemo) {
                getSupportActionBar().hide();
                setContentView(R.layout.activity_main_demo);
            } else {
                setContentView(R.layout.activity_main);
                findViewById(R.id.angle3D).setEnabled(false);//This doesn't work in XML?!
            }
        } else {
            setContentView(R.layout.activity_main);
            findViewById(R.id.angle3D).setEnabled(false);//This doesn't work in XML?!
        }
        hp = (HyperView) findViewById(R.id.hyperView);
        final RadioGroup rotateDimRG = (RadioGroup) findViewById(R.id.rotateDimRG);
        rotateDimRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                rotateDimChanged(rotateDimRG, checkedId);
            }
        });

        final RadioGroup stereo = (RadioGroup) findViewById(R.id.stereoRG);
        stereo.setClickable(true);
        stereo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                findViewById(checkedId).setBackgroundResource(R.drawable.radio_selected);
                for (int i = 0; i < stereo.getChildCount(); i++) {
                    if (stereo.getChildAt(i) instanceof RadioButton && stereo.getChildAt(i).getId() != checkedId) {
                        stereo.getChildAt(i).setBackgroundResource(R.drawable.radio_not_selected);
                    }
                }
                if (checkedId != R.id.off3D)
                    (findViewById(R.id.angle3D)).setEnabled(true);
                if (checkedId == R.id.off3D) {
                    (findViewById(R.id.angle3D)).setEnabled(false);
                    hp.stereo3D = HyperView.OFF_3D;
                    hp.setup = true;
                } else if (checkedId == R.id.redCyan) {
                    hp.stereo3D = HyperView.RED_CYAN_3D;
                    hp.setup = true;
                } else if (checkedId == R.id.crossEye) {
                    hp.stereo3D = HyperView.CROSS_EYE_3D;
                    hp.setup = true;
                } else if (checkedId == R.id.parallel) {
                    hp.stereo3D = HyperView.PARALLEL_3D;
                    hp.setup = true;
                }
            }
        });

        ((SeekBar) findViewById(R.id.proj3D)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double prevDepth3D = hp.depth3D;
                hp.depth3D = progress / 1000.0 + 1;
                hp.sizeAdjust /= hp.depth3D / prevDepth3D;
            }
        });
        ((SeekBar) findViewById(R.id.proj4D)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hp.depth4D = progress / 1000.0 + 1;
            }
        });

        ((SeekBar) findViewById(R.id.angle3D)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hp.rotate3DMagnitude = progress / 10.0;
                if (hp.stereo3D == HyperView.CROSS_EYE_3D) {
                    hp.rotate3D = -hp.rotate3DMagnitude;
                } else if (hp.stereo3D != HyperView.OFF_3D) {
                    hp.rotate3D = hp.rotate3DMagnitude;
                } else {
                    hp.rotate3D = 0;
                }
                hp.rotate(new int[]{1, 3}, -hp.rotate3D / 2.0 - HyperView.rotate3DAdjust, HyperView.points);
                HyperView.rotate3DAdjust = -hp.rotate3D / 2.0;
            }
        });
        if (true) {
            final View rdLayout1 = findViewById(R.id.rdLayout1);
            ViewTreeObserver vto = rdLayout1.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    refreshLeftRDButton();
                }
            });
            final View rdLayout2 = findViewById(R.id.rdLayout2);
            ViewTreeObserver vto2 = rdLayout2.getViewTreeObserver();
            vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    refreshRightRDButton();
                }
            });
        }
        if (getResources().getBoolean(R.bool.isTablet)) {
            final LinearLayout settings = (LinearLayout) findViewById(R.id.settings);
            ViewTreeObserver vto = settings.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    LayoutParams params = settings.getLayoutParams();
                    int width = settings.getMeasuredWidth();
                    if (settingsWidth == -1) {
                        double settingsSize = doinDaDemo ? 0.75 : 0.666;
                        settingsWidth = (int) (width + (hp.getMeasuredWidth() - hp.getMeasuredHeight()) * settingsSize);
                    }
                    params.width = settingsWidth;
                    if (settings.getVisibility() == View.GONE) {
                        findViewById(R.id.showSettingsButt).setVisibility(View.VISIBLE);
                    } else if (hp.shrink) {
                        findViewById(R.id.outerHyperViewLayout).setLeft(hp.initialOuterLayoutLeft);
                        findViewById(R.id.innerHyperViewLayout).setRight(hp.initialInnerLayoutRight);
                        findViewById(R.id.buttonLayout).setRight(hp.initialButtonLayoutRight);
                        hp.setRight(hp.initialRight);
                    }
                    hp.setup = true;
                }
            });
        }
        activity = this;
    }

    public void refreshRightRDButton() {
        View rdLayout2 = findViewById(R.id.rdLayout2);
        int width = rdLayout2.getMeasuredWidth();
        rdLayout2.setLeft((int) (hp.shift + hp.panX - width / 2.0));
        rdLayout2.setRight(rdLayout2.getLeft() + width);
    }

    public void refreshLeftRDButton() {
        View rdLayout1 = findViewById(R.id.rdLayout1);
        int width = rdLayout1.getMeasuredWidth();
        rdLayout1.setLeft((int) (hp.panX - width / 2.0));
        rdLayout1.setRight(rdLayout1.getLeft() + width);
    }

    public void rotate3D4D(View v) {
        switchRotateDimension();
    }

    public void switchRotateDimension() {
        if (hp.rotateDim == 2) {
            rotateDimChanged((RadioGroup) findViewById(R.id.rotateDimRG), R.id.rotate4D);
        } else if (hp.rotateDim == 3) {
            rotateDimChanged((RadioGroup) findViewById(R.id.rotateDimRG), R.id.rotate3D);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getResources().getBoolean(R.bool.isTablet)) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                findViewById(R.id.settings).setVisibility(View.GONE);
                findViewById(R.id.rdLayout1).setVisibility(View.VISIBLE);
                if (hp.stereo3D == HyperView.CROSS_EYE_3D || hp.stereo3D == HyperView.PARALLEL_3D) {
                    findViewById(R.id.rdLayout2).setVisibility(View.VISIBLE);
                }
                if (hp.rotate3D == 2) {
                    ((TextView) findViewById(R.id.button3D1)).setText("3D");
                    ((TextView) findViewById(R.id.button3D2)).setText("3D");
                } else if (hp.rotate3D == 3) {
                    ((TextView) findViewById(R.id.button3D1)).setText("4D");
                    ((TextView) findViewById(R.id.button3D2)).setText("4D");
                }
                getSupportActionBar().hide();
                if (Build.VERSION.SDK_INT < 16) {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                } else {
                    View decorView = getWindow().getDecorView();
                    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(uiOptions);
                }
                hp.setup = true;
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                findViewById(R.id.settings).setVisibility(View.VISIBLE);
                findViewById(R.id.rdLayout1).setVisibility(View.GONE);
                findViewById(R.id.rdLayout2).setVisibility(View.GONE);
                getSupportActionBar().show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset) {
            hp.autoRotate = false;
            hp.stopTouch = System.currentTimeMillis();
            HyperView.points.clear();
            HyperView.originalPoints.clear();
            HyperView.lines.clear();
            ((SeekBar) findViewById(R.id.proj3D)).setProgress(125);
            ((SeekBar) findViewById(R.id.proj4D)).setProgress(500);
            ((SeekBar) findViewById(R.id.angle3D)).setProgress(70);
            rotateDimChanged((RadioGroup) findViewById(R.id.rotateDimRG), R.id.rotate3D);
            HyperView.rotate3DAdjust = 0;
            hp.initialSetup();
            hp.setup = true;
        } else if (item.getItemId() == R.id.autoRotate) {
            HyperView.autoRotate = true;
            hp.autoRotateAngle = 360;
        }
//        else if (item.getItemId() == R.id.manual) {
//            Intent intent = new Intent(this, ManualActivity.class);
//            startActivity(intent);
//        }
        return true;
    }

    private void rotateDimChanged(final RadioGroup rotateDimRG, int checkedId) {
        hp.velocityX = 0;
        hp.velocityY = 0;
//        hp.down = 0;
        findViewById(checkedId).setBackgroundResource(R.drawable.radio_selected);
        for (int i = 0; i < rotateDimRG.getChildCount(); i++) {
            if (rotateDimRG.getChildAt(i) instanceof RadioButton && rotateDimRG.getChildAt(i).getId() != checkedId) {
                rotateDimRG.getChildAt(i).setBackgroundResource(R.drawable.radio_not_selected);
            }
        }
        if (checkedId == R.id.rotate3D) {
            hp.rotateDim = 2;
            ((TextView) findViewById(R.id.button3D1)).setText("3D");
            ((TextView) findViewById(R.id.button3D2)).setText("3D");
            rotateDimRG.check(checkedId);
        } else if (checkedId == R.id.rotate4D) {
            hp.rotateDim = 3;
            ((TextView) findViewById(R.id.button3D1)).setText("4D");
            ((TextView) findViewById(R.id.button3D2)).setText("4D");
            rotateDimRG.check(checkedId);
        }
    }

    public void hideSettings(View v) {
        findViewById(R.id.hideSettingsButt).setClickable(false);
        LinearLayout settings = (LinearLayout) findViewById(R.id.settings);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.out_left);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hp.stopExpand();
                findViewById(R.id.settings).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        settings.startAnimation(anim);
        hp.expand = true;
    }


    public void showSettings(View v) {
        hp.initialOuterLayoutLeft = findViewById(R.id.outerHyperViewLayout).getLeft();
        hp.initialInnerLayoutRight = findViewById(R.id.innerHyperViewLayout).getRight();
        hp.initialButtonLayoutRight = findViewById(R.id.buttonLayout).getRight();
        hp.initialRight = hp.getRight();
        hp.shrink = true;
        LinearLayout settings = (LinearLayout) findViewById(R.id.settings);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.in_left);
        settings.startAnimation(anim);
        findViewById(R.id.settings).setVisibility(View.VISIBLE);
        findViewById(R.id.showSettingsButt).setVisibility(View.GONE);
        findViewById(R.id.hideSettingsButt).setClickable(true);
    }
}
