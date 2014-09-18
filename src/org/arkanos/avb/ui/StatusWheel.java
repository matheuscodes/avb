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

		readData(LanguageSettings.SWEDISH);
		readData(LanguageSettings.GERMAN);
	}

	private void readData(String language) {
		Log.d(TAG, "Reading trusts for " + language);
		LinkedList<Integer> li = new LinkedList<Integer>();
		LinkedList<Float> lf = new LinkedList<Float>();
		BabelTower.fillTranslationTrustLists(language, li, lf);
		coverage.put(language, buildGradient(li, lf, 0xFFFFFFFF));
		Log.d(TAG, "Read trusts for " + language);

		Log.d(TAG, "Reading known for " + language);
		li = new LinkedList<Integer>();
		lf = new LinkedList<Float>();
		BabelTower.fillTranslationKnownLists(language, li, lf);
		good.put(language, buildGradient(li, lf, 0xFF00FF00));
		Log.d(TAG, "Read known for " + language);

		Log.d(TAG, "Reading unknown for " + language);
		li = new LinkedList<Integer>();
		lf = new LinkedList<Float>();
		BabelTower.fillTranslationUnknownLists(language, li, lf);
		bad.put(language, buildGradient(li, lf, 0xFFFF0000));
		Log.d(TAG, "Read unknown for " + language);
	}

	private SweepGradient buildGradient(List<Integer> li, List<Float> lf, int color) {
		float[] positions = new float[lf.size() + 2];
		int[] colors = new int[lf.size() + 2];
		int pos = 0;
		float total = 0;
		colors[pos] = color;
		for (int value : li) {
			float opacity = lf.remove(0);// TODO maybe removeFirst
			if (opacity > 1f) {
				opacity = 1f;
			}
			positions[pos] = total;
			total += value / ((float) WordnetImporter.WN_TOTAL);
			colors[pos] = colors[0] | ((int) (255 * opacity) << 24) & 0xFF000000;
			Log.d(TAG, (int) (255 * opacity) + "/1/" + positions[pos] + " value: " + value);
			++pos;
		}
		positions[pos] = total;
		colors[pos++] = colors[0] & 0x00FFFFFF;
		positions[pos] = 1f;
		colors[pos] = colors[0] & 0x00FFFFFF;
		Log.d(TAG, 0 + "/3/" + positions[pos]);
		return new SweepGradient(0f, 0f, colors, positions);
	}

	private void createFakeData(String language) {
		float help1 = 0;
		float help2 = 1;
		int i = 2 + (int) (Math.random() * 5);
		float[] positions = new float[i + 2];
		int[] colors = new int[i + 2];
		for (int j = 0; j < i; j++) {
			positions[j] = help1;
			int color = 0x00FF0000;
			color |= ((int) (255 * help2) << 24) & 0xFF000000;
			colors[j] = color;
			help1 += Math.random() * (0.1f - help1);
			if (help1 < 0)
				help1 = 0;
			help2 -= Math.random() * help2;
			// Log.d(TAG, (colors[j] & 0x00FFFFFF) + "," + positions[j]);
		}
		positions[i] = help1 + 0.01f;
		colors[i] = 0x00FF0000;
		positions[i + 1] = 1f;
		colors[i + 1] = 0x00FF0000;

		bad.put(language, new SweepGradient(0f, 0f, colors, positions));

		help1 = 0;
		help2 = 1;
		i = 2 + (int) (Math.random() * 10);
		positions = new float[i + 2];
		colors = new int[i + 2];
		for (int j = 0; j < i; j++) {
			positions[j] = help1;
			int color = 0x0000FF00;
			color |= ((int) (255 * help2) << 24) & 0xFF000000;
			colors[j] = color;
			help1 += Math.random() * (0.3f - help1);
			if (help1 < 0)
				help1 = 0;
			help2 -= Math.random() * help2;
			// Log.d(TAG, (colors[j] & 0x00FFFFFF) + "-" + positions[j]);
		}
		positions[i] = help1 + 0.01f;
		colors[i] = 0x0000FF00;
		positions[i + 1] = 1f;
		colors[i + 1] = 0x0000FF00;

		good.put(language, new SweepGradient(0f, 0f, colors, positions));

		help1 = 0.3f;
		help2 = 1;
		i = 2 + (int) (Math.random() * 10);
		positions = new float[i + 3];
		colors = new int[i + 3];
		positions[0] = 0f;
		colors[0] = 0xFFFFFFFF;
		for (int j = 1; j < i; j++) {
			positions[j] = help1;
			int color = 0x00FFFFFF;
			color |= ((int) (255 * help2) << 24) & 0xFF000000;
			colors[j] = color;
			help1 += Math.random() * (0.99f - help1);
			if (help1 < 0)
				help1 = 0;
			help2 -= Math.random() * help2;
			// Log.d(TAG, (colors[j] & 0x00FFFFFF) + "/" + positions[j]);
		}
		positions[i + 1] = help1 + 0.01f;
		colors[i + 1] = 0x00FFFFFF;
		positions[i + 2] = 1f;
		colors[i + 2] = 0x00FFFFFF;

		coverage.put(language, new SweepGradient(0f, 0f, colors, positions));
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
