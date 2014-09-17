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
	public static final String ERROR_RADIUS_VALUE = "Radius must be percentage";
	private static final int DEGREE_360 = 360;
	private static String[] PIE_COLORS 	= null;
	private static int iColorListSize 	= 0;
	
	
	private Paint paintPieFill;
	private Paint paintPieBorder;
	private Paint paintText;
	private ArrayList<PieChartData> alPieCharData = new ArrayList<PieChartData>();

	private int iDisplayWidth, iDisplayHeight;
	private int iSelectedIndex 	= -1;
	private int iCenterPoint 	= 0;
	private int iShift			= 0;
	private int iDataSize		= 0;
	private int iPaddingLeft    = 0;
	private int iPaddingTop     = 0;
	private int iR              = 0;
	
	private RectF rectPie 			= null;
	private RectF rectLegendIcon[] 			= null;

	private float fDensity 		= 0.0f;
	private float fStartAngle 	= 0.0f;
	private float fEndAngle 	= 0.0f;
	private float fLegendLeft         = 0.0f;
	private float fLegendIconSize     = 0.0f;
	private float fMargin   = 0.0f;
	private boolean bIsAlignCenter = true;
	private boolean bIsShowLegend = false;

	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		PIE_COLORS = getResources().getStringArray(R.array.colors);
		iColorListSize = PIE_COLORS.length;
			
		fnGetDisplayMetrics(context);
		iShift 	      = (int) fnGetRealPxFromDp(20);
		iPaddingLeft  = (int) fnGetRealPxFromDp(5);
		iPaddingTop   = (int) fnGetRealPxFromDp(15);
		fMargin       = fnGetRealPxFromDp(15);
		fLegendIconSize     = fnGetRealPxFromDp(10);
		
		
		
		
		paintText = new Paint();
		paintText.setTextSize(getResources().getDimension(R.dimen.legend_font_size));
		paintText.setColor(Color.WHITE);
		
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
			
			final PieChartData tmpData = alPieCharData.get(i);
			fEndAngle = tmpData.fPercentage;
            
			if (bIsShowLegend) {
			    // draw rectangle
			    canvas.drawRect(rectLegendIcon[i], paintPieFill);

			    // draw text
			    canvas.drawText(tmpData.strTitle + " " + fEndAngle + "%",
	                    fLegendLeft+fnGetRealPxFromDp(15),
	                    iDisplayHeight-fLegendIconSize*(6-i)- fMargin*(6-i),
	                    paintText);

			}


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

			canvas.drawArc(rectPie, fStartAngle, fEndAngle, true, paintPieFill);

			// if the part of pie was selected then draw a border
			if (iSelectedIndex == i) {
				canvas.drawArc(rectPie, fStartAngle, fEndAngle, true, paintPieBorder);
				canvas.restore();
			}
			fStartAngle = fStartAngle + fEndAngle;
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (iDisplayWidth == 0) {
		    // get screen size
		    iDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
		    iDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);
		} else {
		    // do not get size again
		    setMeasuredDimension(getMeasuredWidth(), iDisplayHeight);
		    return;
		}
		
		if (iDisplayWidth>iDisplayHeight) {
			iDisplayWidth = iDisplayHeight;
		}

		/*
		 *  determine the rectangle size
		 */
		iCenterPoint = iDisplayWidth / 2;
		this.iR = iCenterPoint * this.iR / 100;
		iDisplayHeight = iR * 2 + iPaddingTop+iPaddingTop;
		if (rectPie == null) {
		    int iLeft = iCenterPoint - iR;
		    int iTop  = iPaddingTop;
		    int iRight = iCenterPoint + iR;
		    int iBottom = iR * 2 +iPaddingTop;

		    if (!bIsAlignCenter) {
		        iLeft = iPaddingLeft;
		        iRight = iR * 2 + iPaddingLeft;
		        iCenterPoint = iR;
		    }

		    rectPie = new RectF(iLeft,
		            iTop,
		            iRight,
		            iBottom);

		}

		fLegendLeft  = iCenterPoint + iR + fLegendIconSize;
		float fRight    = iCenterPoint + iR + fLegendIconSize + fLegendIconSize;
		for (int i = iDataSize-1; i >= 0; i--) {
		    rectLegendIcon[i].set(fLegendLeft,
		            iDisplayHeight-fLegendIconSize-fLegendIconSize*(6-i)- fMargin*(6-i),
		            fRight,
		            iDisplayHeight-fLegendIconSize*(6-i)- fMargin*(6-i));
		}
		setMeasuredDimension(iDisplayWidth, iDisplayHeight);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// get degree of the touch point
		double dx = Math.atan2(event.getY() - iR, event.getX() - iCenterPoint);
		float fDegree = (float) (dx / (2 * Math.PI) * DEGREE_360);
		fDegree = (fDegree + DEGREE_360) % DEGREE_360;

		// get the percent of the selected degree
		float fSelectedPercent = fDegree * 100 / DEGREE_360;

		// check which pie was selected
		float fTotalPercent = 0;
		for (int i = 0; i < iDataSize; i++) {
			fTotalPercent += alPieCharData.get(i).fPercentage;
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

	public void setSelectedIndex(int iSelectedIndex) {
	    this.iSelectedIndex = iSelectedIndex;
	    this.invalidate();
	}

	public void setAdapter(ArrayList<PieChartData> alPercentage) throws Exception {
		this.alPieCharData = alPercentage;
		iDataSize         = alPercentage.size();
		rectLegendIcon    = new RectF[iDataSize];
		
		for (int i = 0; i < iDataSize; i++) {
		    rectLegendIcon[i] = new RectF();
		}
        
		float fSum = 0;
		for (int i = 0; i < iDataSize; i++) {
			fSum+=alPercentage.get(i).fPercentage;
		}
		
		if (fSum!=100){
			Log.e(TAG,ERROR_NOT_EQUAL_TO_100);
			iDataSize = 0;
			throw new Exception(ERROR_NOT_EQUAL_TO_100);
		}
		
	}
	
	public void setRadius(int iR) throws Exception{
	    if (iR>100 || iR<0){
            Log.e(TAG,ERROR_RADIUS_VALUE);
            throw new Exception(ERROR_RADIUS_VALUE);
        }

	    this.iR = iR;
	}
	
	public void setIsCenter(boolean bIsAlignCenter) {
	    this.bIsAlignCenter = bIsAlignCenter;
	}
	
	public void setShowLegend(boolean bIsShowLegend) {
	    this.bIsShowLegend = bIsShowLegend;
	}

}
