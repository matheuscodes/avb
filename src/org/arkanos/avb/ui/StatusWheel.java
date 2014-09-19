package org.arkanos.avb.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.arkanos.avb.AVBApp;
import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.LanguageSettings;
import org.arkanos.avb.data.WordnetImporter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.shapes.PathShape;
import android.util.Log;
import android.view.View;

public class StatusWheel extends View {
	private static final String TAG = AVBApp.TAG + "StatusWheel";

	private HashMap<String, SweepGradient> good;
	private HashMap<String, SweepGradient> bad;
	private HashMap<String, SweepGradient> coverage;

	private static final int THICKNESS = 20;
	private int thickness = 0;
	private static final int MIN_SIZE = 60;
	private int min_size = 0;

	public StatusWheel(Context c) {
		super(c);

		thickness = (int) (getResources().getDisplayMetrics().density * THICKNESS);
		min_size = (int) (getResources().getDisplayMetrics().density * MIN_SIZE);

		int needed_size = min_size + (LanguageSettings.getInstalledLanguages().size()) * thickness * 3 + thickness;

		this.setMinimumHeight(needed_size);
		this.setMinimumWidth(needed_size);
		this.updateData();
	}

	private void updateData() {
		good = new HashMap<String, SweepGradient>();
		bad = new HashMap<String, SweepGradient>();
		coverage = new HashMap<String, SweepGradient>();
		for (String language : LanguageSettings.getInstalledLanguages()) {
			readData(language);
		}
	}

	private void readData(String language) {
		Log.d(TAG, "Reading trusts for " + language);
		LinkedList<Integer> li = new LinkedList<Integer>();
		LinkedList<Float> lf = new LinkedList<Float>();
		BabelTower.fillTranslationTrustLists(language, li, lf);
		coverage.put(language, buildGradient(li, lf, 0xFFFFFFFF, 1f));
		Log.d(TAG, "Read trusts for " + language);

		Log.d(TAG, "Reading known for " + language);
		li = new LinkedList<Integer>();
		lf = new LinkedList<Float>();
		BabelTower.fillTranslationKnownLists(language, li, lf);
		good.put(language, buildGradient(li, lf, 0xFF00FF00, 0.5f));
		Log.d(TAG, "Read known for " + language);

		Log.d(TAG, "Reading unknown for " + language);
		li = new LinkedList<Integer>();
		lf = new LinkedList<Float>();
		BabelTower.fillTranslationUnknownLists(language, li, lf);
		bad.put(language, buildGradient(li, lf, 0xFFFF0000, 0.5f));
		Log.d(TAG, "Read unknown for " + language);
	}

	private SweepGradient buildGradient(List<Integer> li, List<Float> lf, int color, float correction) {
		float[] positions = new float[lf.size() + 2];
		int[] colors = new int[lf.size() + 2];
		int pos = 0;
		float total = 0;
		colors[pos] = color;
		for (int value : li) {
			float opacity = Math.abs(lf.remove(0)) / correction;
			if (opacity > 1f) {
				opacity = 1f;
			}
			positions[pos] = total;
			total += value / (WordnetImporter.WN_TOTAL * correction);
			colors[pos] = colors[0] | ((int) (255 * opacity) << 24) & 0xFF000000;
			// Log.d(TAG, (int) (255 * opacity) + "/1/" + positions[pos] + " value: " + value);
			++pos;
		}
		positions[pos] = total;
		colors[pos++] = colors[0] & 0x00FFFFFF;
		positions[pos] = 1f;
		colors[pos] = colors[0] & 0x00FFFFFF;
		// Log.d(TAG, 0 + "/3/" + positions[pos]);
		return new SweepGradient(0f, 0f, colors, positions);
	}

	private static PathShape buildRing(int size, int thickness) {
		Path p = new Path();
		p.addArc(new RectF(-size / 2, -size / 2, size / 2, size / 2), -90, 360);
		p.moveTo(0, size / 2 - thickness);
		p.addArc(new RectF(-(size / 2 - thickness), -(size / 2 - thickness), (size / 2 - thickness), (size / 2 - thickness)), -90, -360);
		p.close();

		PathShape rs = new PathShape(p, size, size);
		rs.resize(size, size);
		return rs;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();

		canvas.save();
		canvas.rotate(90);
		canvas.translate(canvas.getClipBounds().exactCenterX(), canvas.getClipBounds().exactCenterY());

		int size = min_size + 2 * thickness;
		for (String l : LanguageSettings.getInstalledLanguages()) {

			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setShader(coverage.get(l));
			buildRing(size, thickness).draw(canvas, paint);
			paint.reset();

			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setShader(good.get(l));
			buildRing(size, thickness / 2).draw(canvas, paint);
			paint.reset();

			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setShader(bad.get(l));
			buildRing(size - thickness, thickness / 2).draw(canvas, paint);
			paint.reset();

			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(getResources().getColor(R.color.stats_text));
			paint.setTextSize(thickness / 2);
			Path line = new Path();
			line.addArc(new RectF(-size / 2 - thickness / 2, -size / 2 - thickness / 2, size / 2 + thickness / 2, size / 2 + thickness / 2), 0, -90);
			canvas.drawTextOnPath(LanguageSettings.prettyName(l, this.getContext()), line, 0, 0, paint);

			paint.reset();

			size += 2 * thickness + 3 * thickness / 2;
		}

		canvas.restore();
	}
}
