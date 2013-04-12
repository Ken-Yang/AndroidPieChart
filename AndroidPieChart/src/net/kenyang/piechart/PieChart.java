/*
 * Copyright 2013 Ken Yang
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.kenyang.piechart;

import java.util.ArrayList;

import net.kenyang.androidpiechart.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PieChart extends View {
	
	public interface OnSelectedLisenter{
		public abstract void onSelected(int iSelectedIndex);
	}
	
	private OnSelectedLisenter onSelectedListener = null;

	private static final String TAG = PieChart.class.getName();
	public static final String ERROR_NOT_EQUAL_TO_100 = "NOT_EQUAL_TO_100";
	private static final int DEGREE_360 = 360;
	private static String[] PIE_COLORS 	= null;
	private static int iColorListSize 	= 0;
	
	
	private Paint paintPieFill;
	private Paint paintPieBorder;
	private ArrayList<Float> alPercentage = new ArrayList<Float>();

	private int iDisplayWidth, iDisplayHeight;
	private int iSelectedIndex 	= -1;
	private int iCenterWidth 	= 0;
	private int iShift			= 0;
	private int iMargin 		= 0;  // margin to left and right, used for get Radius
	private int iDataSize		= 0;
	
	private RectF r 			= null;

	private float fDensity 		= 0.0f;
	private float fStartAngle 	= 0.0f;
	private float fEndAngle 	= 0.0f;

	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		PIE_COLORS = getResources().getStringArray(R.array.colors);
		iColorListSize = PIE_COLORS.length;
			
		fnGetDisplayMetrics(context);
		iShift 	= (int) fnGetRealPxFromDp(30);
		iMargin = (int) fnGetRealPxFromDp(40);
		
		// used for paint circle
		paintPieFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieFill.setStyle(Paint.Style.FILL);

		// used for paint border
		paintPieBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintPieBorder.setStyle(Paint.Style.STROKE);
		paintPieBorder.setStrokeWidth(fnGetRealPxFromDp(3));
		paintPieBorder.setColor(Color.WHITE);
		Log.i(TAG, "PieChart init");

	}
	
	// set listener
	public void setOnSelectedListener(OnSelectedLisenter listener){
		this.onSelectedListener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.i(TAG, "onDraw");
		for (int i = 0; i < iDataSize; i++) {
			
			// check whether the data size larger than color list size
			if (i>=iColorListSize){
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i%iColorListSize]));
			}else{
				paintPieFill.setColor(Color.parseColor(PIE_COLORS[i]));
			}
			
			fEndAngle = alPercentage.get(i);

			// convert percentage to angle
			fEndAngle = fEndAngle / 100 * DEGREE_360;

			// if the part of pie was selected then change the coordinate
			if (iSelectedIndex == i) {
				canvas.save(Canvas.MATRIX_SAVE_FLAG);
				float fAngle = fStartAngle + fEndAngle / 2;
				double dxRadius = Math.toRadians((fAngle + DEGREE_360) % DEGREE_360);
				float fY = (float) Math.sin(dxRadius);
				float fX = (float) Math.cos(dxRadius);
				canvas.translate(fX * iShift, fY * iShift);
			}

			canvas.drawArc(r, fStartAngle, fEndAngle, true, paintPieFill);
			
			// if the part of pie was selected then draw a border
			if (iSelectedIndex == i) {
				canvas.drawArc(r, fStartAngle, fEndAngle, true, paintPieBorder);
				canvas.restore();
			}
			fStartAngle = fStartAngle + fEndAngle;
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// get screen size
		iDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
		iDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		if (iDisplayWidth>iDisplayHeight){
			iDisplayWidth = iDisplayHeight;
		}

		/*
		 *  determine the rectangle size
		 */
		iCenterWidth = iDisplayWidth / 2; 
		int iR = iCenterWidth-iMargin;
		if (r == null) {
			r = new RectF(iCenterWidth-iR,  // top
					iCenterWidth-iR,  		// left
					iCenterWidth+iR,  		// rights
					iCenterWidth+iR); 		// bottom
		}
		setMeasuredDimension(iDisplayWidth, iDisplayWidth);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// get degree of the touch point
		double dx = Math.atan2(event.getY() - iCenterWidth, event.getX() - iCenterWidth);
		float fDegree = (float) (dx / (2 * Math.PI) * DEGREE_360);
		fDegree = (fDegree + DEGREE_360) % DEGREE_360;

		// get the percent of the selected degree
		float fSelectedPercent = fDegree * 100 / DEGREE_360;

		// check which pie was selected
		float fTotalPercent = 0;
		for (int i = 0; i < iDataSize; i++) {
			fTotalPercent += alPercentage.get(i);
			if (fTotalPercent > fSelectedPercent) {
				iSelectedIndex = i;
				break;
			}
		}
		if (onSelectedListener != null){
			onSelectedListener.onSelected(iSelectedIndex);
		}
		invalidate();
		return super.onTouchEvent(event);
	}
	
	private void fnGetDisplayMetrics(Context cxt){
		final DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
		fDensity = dm.density;
	}
	
	private float fnGetRealPxFromDp(float fDp){
		return (fDensity!=1.0f) ? fDensity*fDp : fDp;
	}

	public void setAdapter(ArrayList<Float> alPercentage) throws Exception {
		this.alPercentage = alPercentage;
		iDataSize = alPercentage.size();
		float fSum = 0;
		for (int i = 0; i < iDataSize; i++) {
			fSum+=alPercentage.get(i);
		}
		if (fSum!=100){
			Log.e(TAG,ERROR_NOT_EQUAL_TO_100);
			iDataSize = 0;
			throw new Exception(ERROR_NOT_EQUAL_TO_100);
		}
		
	}

}
