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
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class VoltageGauge extends TextView {
	
	private float mMin = 10.0f;
	private float mMax = 14.0f;
	private float mVoltage = 10.0f;
	private boolean mFlash = false;
	private int mDarkRed = Color.argb( 255, 128, 0, 0 );
	private int mLightRed = Color.argb( 255, 255, 0, 0 );
	private int mCurrentRed = Color.argb( 255, 128, 0, 0 );

	public VoltageGauge( Context context ) {
		
		super( context );
		// TODO Auto-generated constructor stub
	}

	public VoltageGauge( Context context, AttributeSet attrs ) {
		
		super( context, attrs );
		// TODO Auto-generated constructor stub
	}

	public VoltageGauge( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {

		// Leave room for the trip gauge at the bottom
		Paint paint = getPaint( );
		
		float ascent = paint.ascent( );
		float height = PaintUtils.getTextHeight( paint, "0" ) - ascent + .5f;

		int parentWidth = MeasureSpec.getSize( widthMeasureSpec );
		int parentHeight = MeasureSpec.getSize( heightMeasureSpec ) - ( int ) height;
		
		setMeasuredDimension( parentWidth, parentHeight );
}

	public void setVoltage( float value ) {
		
		if( mVoltage == value )
			return;

		mVoltage = value;

		invalidate( );
	}

	public void setVoltage( int percentage ) {
		
		float voltage = ( mMax - mMin ) * ( ( float ) percentage / 100 ) + mMin;

		if( mVoltage == voltage )
			return;

		mVoltage = voltage;

		invalidate( );
	}

	public void setMin( float value ) {
		
		mMin = value;
	}

	public void setMax( float value ) {
		
		mMax = value;
	}

	public void updateTimer( ) {
		
		if( !mFlash )
			return;

		if( mCurrentRed == mDarkRed )
			mCurrentRed = mLightRed;
		else
			mCurrentRed = mDarkRed;

		invalidate( );
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		
		String voltage = String.format( "%.1f", mVoltage ) + "v";
		// setText( voltage );

		super.onDraw( canvas );

		Paint paint = getPaint( );

		paint.setTypeface( getTypeface( ) );
		paint.setTextSize( getTextSize( ) );

		float ascent = paint.ascent( );
		float top = getPaddingTop( ) - ascent;

		canvas.drawText( voltage, 0, top, paint );

		float textHeight = PaintUtils.getTextHeight( paint, voltage ) + 15;
		float dividerHeight = ( getHeight( ) - 35 - textHeight ) / 3;
		float barWidth = ( getWidth( ) - 2 ) / 2; // leave 2 pixels of space between the bar and indicator
		top = textHeight;

		paint.setStyle( Style.FILL );
		paint.setARGB( 255, 210, 114, 0 );
		canvas.drawRect( 0, top, barWidth, top + dividerHeight, paint );

		top += dividerHeight;

		paint.setARGB( 255, 0, 128, 0 );
		canvas.drawRect( 0, top, barWidth, top + dividerHeight, paint );

		top += dividerHeight;

		paint.setColor( mCurrentRed );
		canvas.drawRect( 0, top, barWidth, top + dividerHeight, paint );

		float bottom = top + dividerHeight;

		float scale = ( ( float ) dividerHeight * 3 ) / ( mMax - mMin );
		float y = bottom - ( scale * ( mVoltage - mMin ) );
		float x = barWidth - 21;

		if( y > ( bottom - dividerHeight ) ) {
			
			mFlash = true;
			
		} else {
			
			mFlash = false;
			mCurrentRed = mDarkRed;
		}

		paint.setStyle( Style.FILL_AND_STROKE );

		Path path = new Path( );

		path.moveTo( x, y );
		path.lineTo( x + 36, y - 24 );
		path.lineTo( x + 36, y + 24 );
		path.lineTo( x, y );

		Path shadowPath = new Path( );
		path.offset( 0, 8, shadowPath );

		paint.setARGB( 50, 0, 0, 0 );
		canvas.drawPath( shadowPath, paint );

		paint.setARGB( 255, 255, 255, 0 );
		canvas.drawPath( path, paint );
	}
}
