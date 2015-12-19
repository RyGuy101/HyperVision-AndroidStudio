package com.example.hypervision;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	HyperView hp;
	public static MainActivity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getResources().getBoolean(R.bool.isTablet)) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		setContentView(R.layout.activity_main);
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
				((RadioButton) findViewById(checkedId)).setBackgroundResource(R.drawable.light_gray);
				for (int i = 0; i < stereo.getChildCount(); i++) {
					try {
						if (stereo.getChildAt(i).getId() != checkedId) {
							stereo.getChildAt(i).setBackgroundResource(R.drawable.transparent);
						}
					} catch (Exception e) {
					}
				}
				((SeekBar) findViewById(R.id.angle3D)).setClickable(true); //TODO Make this work like it should.
				if (checkedId == R.id.off3D) {
					hp.stereo3D = HyperView.OFF_3D;
					hp.setup = true;
					((SeekBar) findViewById(R.id.angle3D)).setClickable(false); //TODO Make this work like it should.
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
				hp.rotate(new int[] { 1, 3 }, -hp.rotate3D / 2.0 - HyperView.rotate3DAdjust);
				HyperView.rotate3DAdjust = -hp.rotate3D / 2.0;
			}
		});
		if (!getResources().getBoolean(R.bool.isTablet)) {
			final View rdLayout1 = findViewById(R.id.rdLayout1);
			ViewTreeObserver vto = rdLayout1.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					int width = rdLayout1.getMeasuredWidth();
					rdLayout1.setLeft((int) (hp.panX - width / 2.0));
					rdLayout1.setRight(rdLayout1.getLeft() + width);
				}
			});
			final View rdLayout2 = findViewById(R.id.rdLayout2);
			ViewTreeObserver vto2 = rdLayout2.getViewTreeObserver();
			vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					int width = rdLayout2.getMeasuredWidth();
					rdLayout2.setLeft((int) (hp.shift + hp.panX - width / 2.0));
					rdLayout2.setRight(rdLayout2.getLeft() + width);
				}
			});
		} else {
			final LinearLayout settings = (LinearLayout) findViewById(R.id.settings);
			ViewTreeObserver vto = settings.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					LayoutParams params = settings.getLayoutParams();
					int width = settings.getMeasuredWidth();
					params.width = width + hp.getMeasuredWidth() - hp.getMeasuredHeight();
					hp.setRight(width + hp.getMeasuredHeight());
				}
			});
		}
		activity = this;
	}

	public void setupLayout() {
		if (!getResources().getBoolean(R.bool.isTablet)) {
			//			View rdLayout1 = findViewById(R.id.rdLayout1);
			//			int width1 = rdLayout1.getMeasuredWidth();
			//			rdLayout1.setLeft((int) (hp.panX - width1 / 2.0));
			//			rdLayout1.setRight(rdLayout1.getLeft() + width1);
			//			View rdLayout2 = findViewById(R.id.rdLayout2);
			//			int width2 = rdLayout2.getMeasuredWidth();
			//			rdLayout2.setLeft((int) (hp.shift + hp.panX - width2 / 2.0));
			//			rdLayout2.setRight(rdLayout2.getLeft() + width2);
		}
	}

	//	public void rotate3D(View v) {
	//		hp.rotateDim = 3;
	//	}
	//
	//	public void rotate4D(View v) {
	//		hp.rotateDim = 2;
	//	}

	public void rotate3D4D(View v) {
		if (hp.rotateDim == 3) {
			rotateDimChanged((RadioGroup) findViewById(R.id.rotateDimRG), R.id.rotate4D);
		} else if (hp.rotateDim == 2) {
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
				if (hp.rotate3D == 3) {
					((TextView) findViewById(R.id.button3D1)).setText("3D");
					((TextView) findViewById(R.id.button3D2)).setText("3D");
				} else if (hp.rotate3D == 2) {
					((TextView) findViewById(R.id.button3D1)).setText("4D");
					((TextView) findViewById(R.id.button3D2)).setText("4D");
				}
				getActionBar().hide();
				hp.setup = true;
			} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				findViewById(R.id.settings).setVisibility(View.VISIBLE);
				findViewById(R.id.rdLayout1).setVisibility(View.GONE);
				findViewById(R.id.rdLayout2).setVisibility(View.GONE);
				getActionBar().show();
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
		return true;
	}

	private void rotateDimChanged(final RadioGroup rotateDimRG, int checkedId) {
		((RadioButton) findViewById(checkedId)).setBackgroundResource(R.drawable.light_gray);
		for (int i = 0; i < rotateDimRG.getChildCount(); i++) {
			try {
				RadioButton temp = (RadioButton) rotateDimRG.getChildAt(i);
				if (rotateDimRG.getChildAt(i).getId() != checkedId) {
					rotateDimRG.getChildAt(i).setBackgroundResource(R.drawable.transparent);
				}
			} catch (Exception e) {
			}
		}
		if (checkedId == R.id.rotate3D) {
			hp.rotateDim = 3;
			if (!getResources().getBoolean(R.bool.isTablet)) {
				((TextView) findViewById(R.id.button3D1)).setText("3D");
				((TextView) findViewById(R.id.button3D2)).setText("3D");
			}
			rotateDimRG.check(checkedId);
		} else if (checkedId == R.id.rotate4D) {
			hp.rotateDim = 2;
			if (!getResources().getBoolean(R.bool.isTablet)) {
				((TextView) findViewById(R.id.button3D1)).setText("4D");
				((TextView) findViewById(R.id.button3D2)).setText("4D");
			}
			rotateDimRG.check(checkedId);
		}
	}
}
