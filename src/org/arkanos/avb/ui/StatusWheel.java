/**
 * Copyright (C) 2014 Matheus Borges Teixeira
 * 
 * This is a part of Arkanos Vocabulary Builder (AVB)
 * AVB is an Android application to improve vocabulary on foreign languages.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

/**
 * Special view to draw graphics with statistics.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class StatusWheel extends View {
	/** Tag for debug outputs **/
	private static final String TAG = AVBApp.TAG + "StatusWheel";

	/** Gradient reflecting the correct knowledge distribution per language **/
	static private HashMap<String, SweepGradient> good;
	/** Gradient reflecting the incorrect knowledge distribution per language **/
	static private HashMap<String, SweepGradient> bad;
	/** Gradient reflecting the translation coverage distribution per language **/
	static private HashMap<String, SweepGradient> coverage;

	/** Summed statistics to define gradient proportions per language **/
	static private HashMap<String, Float> amounts;
	/** Status of the process calculating the gradients per language **/
	static private HashMap<String, Boolean> updating;

	/** Thickness reference for the disc band in points **/
	private static final int THICKNESS = 20;
	/** Thickness reference for the disc band in pixels **/
	private int thickness = 0;
	/** Diameter for the smallest disc in points **/
	private static final int MIN_SIZE = 60;
	/** Diameter for the smallest disc in pixels **/
	private int min_size = 0;

	/**
	 * Constructs the view and calculates references.
	 * 
	 * @param c defines the application context.
	 */
	public StatusWheel(Context c) {
		super(c);

		thickness = (int) (getResources().getDisplayMetrics().density * THICKNESS);
		min_size = (int) (getResources().getDisplayMetrics().density * MIN_SIZE);

		int needed_size = min_size + (LanguageSettings.getInstalledLanguages().size()) * thickness * 3 + thickness;

		this.setMinimumHeight(needed_size);
		this.setMinimumWidth(needed_size);
		this.updateData();
	}

	/**
	 * Starts all processes to fetch data and build gradients.
	 */
	private synchronized void updateData() {
		if (updating == null) {
			updating = new HashMap<String, Boolean>();
		}
		if (amounts == null) {
			amounts = new HashMap<String, Float>();
		}
		if (coverage == null) {
			coverage = new HashMap<String, SweepGradient>();
		}
		if (good == null) {
			good = new HashMap<String, SweepGradient>();
		}
		if (bad == null) {
			bad = new HashMap<String, SweepGradient>();
		}
		for (final String language : LanguageSettings.getInstalledLanguages()) {
			if (updating.get(language) == null || !updating.get(language)) {
				updating.put(language, true);
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						readData(language);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						updating.put(language, false);
						invalidate();
					}
				}.execute();
			}
		}
	}

	/**
	 * Reads trust, confidences and coverage building gradients for a language
	 * 
	 * @param language defines the language to be read.
	 */
	private void readData(String language) {
		LinkedList<Integer> li;
		LinkedList<Float> lf;
		if (!amounts.containsKey(language)) {
			Log.d(TAG, "Reading trusts for " + language);
			li = new LinkedList<Integer>();
			lf = new LinkedList<Float>();
			int total = BabelTower.fillTranslationTrustLists(language, li, lf);
			coverage.put(language, buildGradient(li, lf, 0xFFFFFFFF, 1f, WordnetImporter.WN_TOTAL));
			amounts.put(language, (float) total);
			Log.d(TAG, "Read trusts for " + language);
		}

		Log.d(TAG, "Reading known for " + language);
		li = new LinkedList<Integer>();
		lf = new LinkedList<Float>();
		BabelTower.fillTranslationKnownLists(language, li, lf);
		good.remove(language);
		good.put(language, buildGradient(li, lf, 0xFF00FF00, 0.5f, amounts.get(language)));
		Log.d(TAG, "Read known for " + language);

		Log.d(TAG, "Reading unknown for " + language);
		li = new LinkedList<Integer>();
		lf = new LinkedList<Float>();
		BabelTower.fillTranslationUnknownLists(language, li, lf);
		bad.remove(language);
		bad.put(language, buildGradient(li, lf, 0xFFFF0000, 0.5f, amounts.get(language)));
		Log.d(TAG, "Read unknown for " + language);
	}

	/**
	 * Builds a color SweepGradient for painting the discs.
	 * 
	 * @param li defines the amount of items of a particular value.
	 * @param lf defines the items which are unique values.
	 * @param color specifies the desired color to the gradient.
	 * @param correction specifies a controlling factor to generate better visual.
	 * @param max specifies a cap for the color.
	 * @return the gradient ready to be used for painting.
	 */
	private SweepGradient buildGradient(List<Integer> li, List<Float> lf, int color, float correction, float max) {
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
			total += value / max;
			colors[pos] = colors[0] | ((int) (255 * opacity) << 24) & 0xFF000000;
			// Log.d(TAG, (int) (255 * opacity) + "/1/" + positions[pos] + " value: " + value);
			++pos;
		}
		positions[pos] = total;
		colors[pos++] = colors[0] & 0x00FFFFFF;
		// Log.d(TAG, 0 + "/2/" + positions[pos]);
		positions[pos] = 1f;
		colors[pos] = colors[0] & 0x00FFFFFF;
		// Log.d(TAG, 0 + "/3/" + positions[pos]);
		return new SweepGradient(0f, 0f, colors, positions);
	}

	/**
	 * Composes the shape of a simple ring.
	 * 
	 * @param size defines the diameter of the ring.
	 * @param thickness defines the thickness of the rings' band.
	 * @return the composed shape.
	 */
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

	/**
	 * @see View#onDraw(Canvas)
	 */
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
