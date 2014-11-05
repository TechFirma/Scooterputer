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

import java.util.TimerTask;

import particle.utilities.PaintUtils;
import particle.utilities.Size;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class Speedometer extends TextView {
	
	private float mTextSize = 164f;
	private float mMaxTextSize = 34f;
	private Typeface mDigitalFace;
	private String mUnits = "MPH";
	private int mSpeed = 0;
	private int mMaxSpeed = 0;
	private long mOdometer = 0;
	private RectF mMaxSpeedTouchRect;

	public Speedometer( Context context ) {
		
		super( context );
	}

	public Speedometer( Context context, AttributeSet attrs ) {
		
		super( context, attrs );

		Initialize( context, attrs );
	}

	public Speedometer( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );

		Initialize( context, attrs );
	}

	protected void Initialize( Context context, AttributeSet attrs ) {
		
		mTextSize = getTextSize( );
		mDigitalFace = Typeface.createFromAsset( context.getAssets( ),	"Fonts/Digital.ttf" );

		TypedArray a = context.obtainStyledAttributes( attrs,	R.styleable.Speedometer );
		mMaxTextSize = a.getDimension( R.styleable.Speedometer_maxTextSize, 12f );
	}

	public boolean IsKPH( ) {
		
		return mUnits == "KPH";
	}

	public void setUnits( boolean kph ) {
		
		if( !kph )
			mUnits = "MPH";
		else
			mUnits = "KPH";
	}

	public void setSpeed( int value ) {
		
		if( mSpeed == value )
			return;

		mSpeed = value;
		mMaxSpeed = Math.max( mSpeed, mMaxSpeed );

		invalidate( );
	}

	public long getOdometer( ) {
		
		return mOdometer;
	}

	public void setOdometer( long value ) {
		
		if( mOdometer == value )
			return;

		mOdometer = value;

		invalidate( );
	}

	public void resetMaxSpeed( ) {
		
		mMaxSpeed = 0;
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		
		Paint paint = getPaint( );
		paint.setARGB( 255, 0, 255, 0 );
		paint.setTypeface( mDigitalFace );
		paint.setTextSize( mTextSize );
		paint.setTextAlign( Align.RIGHT );

		String mph = String.format( "%d", mSpeed );

		float ascent = paint.ascent( );
		float top = getPaddingTop( ) - ascent - 20;
		float left = getPaddingLeft( );

		float mphWidth = paint.measureText( "00" );
		mphWidth += ( mphWidth * .1f );
		canvas.drawText( mph, left + mphWidth, top, paint );

		Size mphSize = new Size( );
		PaintUtils.getTextSize( paint, mph, mphSize );

		left += mphWidth;

		mMaxSpeedTouchRect = new RectF( left, getPaddingTop( ), 
		                                getRight( ) - getPaddingRight( ), getPaddingTop( ) - ascent );

		String maxMPH = String.valueOf( mMaxSpeed );

		paint.setARGB( 255, 0, 128, 0 );
		paint.setTypeface( null );
		paint.setTextSize( mMaxTextSize );

		Size unitsSize = new Size( );
		PaintUtils.getTextSize( paint, mUnits, unitsSize );

		ascent = paint.ascent( );

		left = getWidth( ) - getPaddingRight( );

		canvas.drawText( mUnits, left, top, paint );

		top += ascent - 5;
		left -= ( unitsSize.Width / 2 ) - paint.measureText( "0" );

		canvas.drawText( maxMPH, left, top, paint );

		String odometer = String.valueOf( mOdometer );

		top = getHeight( ) - PaintUtils.getTextHeight( paint, odometer );

		canvas.drawText( odometer, getWidth( ) - getPaddingRight( ), top, paint );
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		
		int action = event.getActionMasked( );

		if( mMaxSpeedTouchRect.contains( event.getX( ), event.getY( ) ) && 
			 ( action == MotionEvent.ACTION_DOWN ) ) {
			
			ScooterData.RESETTIMER.schedule( new ResetMaxMPHTask( this ), ScooterData.LONGPRESSTIME );
			
		} else if( action == MotionEvent.ACTION_UP ) {
			
			ScooterData.RESETTIMER.cancel( );
		}

		// TODO Auto-generated method stub
		return super.onTouchEvent( event );
	}

	protected class ResetMaxMPHTask extends TimerTask {
		
		private Speedometer mSpeedometer;

		public ResetMaxMPHTask( Speedometer speedo ) {
			
			super( );

			mSpeedometer = speedo;
		}

		@Override
		public void run( ) {
			
			mSpeedometer.resetMaxSpeed( );
		}
	}
}
