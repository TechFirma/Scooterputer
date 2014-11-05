/*
  Scooterputer 
  GUI Module - Android Cell Phone Application

  Copyright (C) 2012 - Tech Firma, LLC
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.

  Tech Firma, LLC
  Cincinnati, OH  45242

  info@techfirma.com

  ****************************

  Created August 9, 2010
  Revision 1.0
  Michael Bac
 
  Update History:

*/

package particle.scooterputer;

import particle.utilities.PaintUtils;
import particle.utilities.Size;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

public class Tachometer extends TextView {

	private float mTextSize = 54;
	private float mMeterTextSize = 34;
	private int mValueOffset = 0;
	private int mMax = 10000;
	private int mRPM = 0;

	public Tachometer( Context context ) {

		super( context );
	}

	public Tachometer( Context context, AttributeSet attrs ) {

		super( context, attrs );

		Initialize( context, attrs );
	}

	public Tachometer( Context context, AttributeSet attrs, int defStyle ) {

		super( context, attrs, defStyle );

		Initialize( context, attrs );
	}

	protected void Initialize( Context context, AttributeSet attrs ) {

		mTextSize = getTextSize( );

		TypedArray a = context.obtainStyledAttributes( attrs,	R.styleable.Tachometer );
		mValueOffset = a.getInt( R.styleable.Tachometer_valueOffset, 0 );
		mMeterTextSize = a.getDimension( R.styleable.Tachometer_meterTextSize, 12f );
	}

	public void setMaxRPM( int value ) {

		if( mMax == value )
			return;

		mMax = value;

		invalidate( );
	}

	public void setRPM( int value ) {

		if( mRPM == value )
			return;

		mRPM = value;

		invalidate( );
	}

	@Override
	protected void onDraw( Canvas canvas ) {

		float markerDiff = 12;

		// We draw based off a max of 10k. Set the scale according to the max
		// from there.
		float scale = mMax / 10000f;

		Paint paint = getPaint( );
		paint.setTextSize( mMeterTextSize );

		float halfNumberOffset = paint.measureText( "0" ) / 2; 
		float bottom = getBottom( ) - getPaddingBottom( );
		float top = bottom - 45;
		float left = getPaddingLeft( ) + halfNumberOffset;
		float right = getWidth( ) - getPaddingRight( ) - 45;
		
		// Calculate incSegment based on the vertical portion of the gauge to make sure
		// the entire gauge fits. "top" here is actually the top of the bottom set of tick marks 
		float incSegment = ( top - getPaddingTop( ) ) / 11.5f;		// 11.5 segments in this section

		// Add any left over space to the left side of the gauge
        float zeroLocation = Math.max( left, right - ( ( incSegment * 7.5f ) ) );
		left = zeroLocation;

		// Draw the RPM text
		paint.setARGB( 255, 0, 255, 0 );
		paint.setTextSize( mTextSize );

		String rpm = String.valueOf( mRPM );

		Size rpmSize = new Size( );
		PaintUtils.getTextSize( paint, rpm, rpmSize );

		float ascent = paint.ascent( );
		float textTop = getPaddingTop( ) - ascent + ( ( getHeight( ) - ( rpmSize.Height * 2 ) ) / 2 ) + mValueOffset;

		canvas.drawText( rpm, left + 30, textTop, paint );

		textTop -= ascent - 5;

		paint.setARGB( 255, 0, 128, 0 );

		canvas.drawText( "RPM", left + 30 + Math.max( 0, rpmSize.Width - paint.measureText( "RPM" ) ), textTop, paint );

		// Start drawing the scale
		paint.setARGB( 255, 255, 255, 255 );
		paint.setTextSize( mMeterTextSize );

		canvas.drawText( "0", 
		                 left - halfNumberOffset, top - PaintUtils.getTextHeight( paint, "0" ) + 8, paint );

		canvas.drawLine( left, top, left, bottom, paint );
		top += markerDiff;
		left += incSegment;
		canvas.drawLine( left, top, left, bottom, paint );
		left += incSegment;
		canvas.drawLine( left, top - markerDiff, left, bottom, paint );
		left += incSegment;
		canvas.drawLine( left, top, left, bottom, paint );
		top -= markerDiff;
		left += incSegment;

		String marker = String.valueOf( ( int ) ( 2 * scale ) );

		canvas.drawText( marker, left - ( paint.measureText( marker ) / 2 ),
							  top - PaintUtils.getTextHeight( paint, marker ) + 8, paint );
		canvas.drawLine( left, top, left, bottom, paint );
		top += markerDiff;
		left += incSegment;
		canvas.drawLine( left, top, left, bottom, paint );
		left += incSegment;
		canvas.drawLine( left, top - markerDiff, left, bottom, paint );
		left += incSegment;
		canvas.drawLine( left, top, left, bottom, paint );
		top -= markerDiff;
		left += incSegment / 2;
		right = left + 45;

		canvas.drawLine( left, top, right - 14, bottom - 14, paint );

		right += incSegment / 2;
		left += markerDiff;
		top -= incSegment / 2;
		canvas.drawLine( left, top, right, top, paint );
		top -= incSegment;
		canvas.drawLine( left - markerDiff, top, right, top, paint );
		top -= incSegment;
		canvas.drawLine( left, top, right, top, paint );
		left -= markerDiff;
		top -= incSegment;

		marker = String.valueOf( ( int ) ( 6 * scale ) );

		canvas.drawText( marker, left - paint.measureText( marker ) - 8, 
		                 top	+ ( PaintUtils.getTextHeight( paint, marker ) / 2 ), paint );
		canvas.drawLine( left, top, right, top, paint );
		left += markerDiff;
		top -= incSegment;
		canvas.drawLine( left, top, right, top, paint );
		top -= incSegment;
		canvas.drawLine( left - markerDiff, top, right, top, paint );
		top -= incSegment;
		canvas.drawLine( left, top, right, top, paint );
		left -= markerDiff;
		top -= incSegment;

		marker = String.valueOf( ( int ) ( 8 * scale ) );

		canvas.drawText( marker, left - paint.measureText( marker ) - 8, 
		                 top + ( PaintUtils.getTextHeight( paint, marker ) / 2 ), paint );
		canvas.drawLine( left, top, right, top, paint );
		left += markerDiff;
		top -= incSegment;
		canvas.drawLine( left, top, right, top, paint );
		top -= incSegment;
		canvas.drawLine( left - markerDiff, top, right, top, paint );
		top -= incSegment;
		canvas.drawLine( left, top, right, top, paint );
		left -= markerDiff;
		top -= incSegment;

		marker = String.valueOf( ( int ) ( 10 * scale ) );

		canvas.drawText( marker, left - paint.measureText( marker ) - 8,
		                 top	+ PaintUtils.getTextHeight( paint, marker ), paint );
		canvas.drawLine( left, top, right, top, paint );

		bottom = getBottom( ) - getPaddingBottom( );
		top = bottom - 45 + ( markerDiff / 2 );
		left = zeroLocation;
		right = getWidth( ) - getPaddingRight( );

		Path path = new Path( );

		float segment = 500f * scale;
		float numSegments = mRPM / segment;

		if( mRPM <= ( 3750 * scale ) ) {
			
			RectF rect = new RectF( left, top, left + ( incSegment * numSegments ), bottom );

			path.addRect( rect, Direction.CCW );
			
		} else {
			
			RectF rect = new RectF( left, top, left + ( incSegment * 7 ), bottom );
			rect.right += incSegment / 2;
			left = rect.right;

			path.addRect( rect, Direction.CCW );
		}

		path.moveTo( left, top );

		float width = right - left;
		float height = bottom - top;

		if( mRPM > ( 3750 * scale ) ) {
			
			float arc = 90;

			if( mRPM <= ( 4250 * scale ) )
				arc = ( mRPM - ( 3750 * scale ) ) / ( segment / 90f );

			RectF small = new RectF( left - markerDiff / 2, top - markerDiff,	left + markerDiff / 2, top );
			path.arcTo( small, 90, -arc );

			RectF large = new RectF( left - width, top - height - markerDiff,	right, bottom );
			path.arcTo( large, 0 + ( 90 - arc ), arc );
		}

		if( mRPM > ( 4250 * scale ) ) {
			
			numSegments = ( mRPM - ( 4250 * scale ) ) / segment;

			left += markerDiff / 2;
			top -= markerDiff / 2;
			bottom = top;

			path.moveTo( left, top );

			if( mRPM < ( 6000 * scale ) ) {
				
				RectF rect = new RectF( left,	top - ( incSegment * numSegments ), right, bottom );

				path.addRect( rect, Direction.CCW );
				
			} else {
				
				top -= ( incSegment / 2 );
				RectF rect = new RectF( left, top - ( incSegment * 3 ), right,	bottom );

				path.addRect( rect, Direction.CCW );
			}
		}

		paint.setARGB( 255, 0, 128, 0 );
		paint.setStyle( Style.FILL );
		canvas.drawPath( path, paint );

		if( mRPM > ( 6000 * scale ) ) {
			
			numSegments = ( mRPM - ( 6000 * scale ) ) / segment;

			top -= ( incSegment * 3 );
			bottom = top;

			RectF rect;
			if( mRPM < ( 8000 * scale ) ) {
				
				rect = new RectF( left, top - ( incSegment * numSegments ),	right, bottom );
				
			} else {
				
				rect = new RectF( left, top - ( incSegment * 4 ), right, bottom );
			}

			paint.setARGB( 255, 255, 255, 0 );
			canvas.drawRect( rect, paint );

			if( mRPM > ( 8000 * scale ) ) {
				
				numSegments = ( mRPM - ( 8000 * scale ) ) / segment;

				top -= ( incSegment * 4 );
				bottom = top;

				if( mRPM < ( 10000 * scale ) ) {
					
					rect = new RectF( left, top - ( incSegment * numSegments ),	right, bottom );
					
				} else {
					
					rect = new RectF( left, top - ( incSegment * 4 ), right, bottom );
				}

				paint.setARGB( 255, 255, 0, 0 );
				canvas.drawRect( rect, paint );
			}
		}
	}
}
