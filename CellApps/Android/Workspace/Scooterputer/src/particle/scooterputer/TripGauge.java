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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class TripGauge extends TextView {

	private boolean mTripAandB = false;
	private long mTripATime =0;
	private float mTripAOdometer = 0;
	private float mTripBOdometer = 0;
	private RectF mLeftRect;
	private RectF mRightRect;
	private Handler mHandler;

	public TripGauge( Context context ) {

		super( context );
		// TODO Auto-generated constructor stub
	}

	public TripGauge( Context context, AttributeSet attrs ) {

		super( context, attrs );
		// TODO Auto-generated constructor stub
	}

	public TripGauge( Context context, AttributeSet attrs, int defStyle ) {

		super( context, attrs, defStyle );
		// TODO Auto-generated constructor stub
	}

	public void setHandler( Handler handler ) {

		mHandler = handler;
	}

	public void SetTripAandB( boolean value ) {
		
		if( mTripAandB == value ) 
			return;
		
		mTripAandB = value;
		
		invalidate( );
	}
	
	public void setTripATime( long value ) {

		if( mTripATime == value )
			return;

		mTripATime = value;

		invalidate( );
	}

	public void setTripAOdometer( float value ) {

		if( mTripAOdometer == value )
			return;

		mTripAOdometer = value;

		invalidate( );
	}

	public void setTripBOdometer( float value ) {

		if( mTripBOdometer == value )
			return;

		mTripBOdometer = value;

		invalidate( );
	}

	public void resetTripA( ) {

		mTripAOdometer = 0;

		mHandler.obtainMessage( ScooterData.RESET_TRIPA, 0, 0 ).sendToTarget( );
	}
	
	public void resetTripATime( ) {
		
		mTripATime = 0;

		mHandler.obtainMessage( ScooterData.RESET_TRIPATIME, 0, 0 ).sendToTarget( );
	}

	public void resetTripB( ) {

		mTripBOdometer = 0;

		mHandler.obtainMessage( ScooterData.RESET_TRIPB, 0, 0 ).sendToTarget( );
	}
	
	@Override
	protected void onDraw( Canvas canvas ) {

		// TODO Auto-generated method stub
		// super.onDraw( canvas );

		Paint paint = getPaint( );

		paint.setColor( getCurrentTextColor( ) );

		float ascent = paint.ascent( );
		float top = getPaddingTop( ) - ascent;
		float left = getPaddingLeft( );

		String text;
		if( mTripAandB ) {
		
			text = String.format( "%.1f", mTripBOdometer );
			
		}	else {
			long hours = mTripATime / 3600000;
			
			text = String.format( "%02d:%02d", hours, ( mTripATime - ( hours * 3600000 ) ) / 60000 );
		}
		
		canvas.drawText( text, left, top, paint );
		float height = PaintUtils.getTextHeight( paint, text );
		mLeftRect = new RectF( left, top + ascent, 
		                       left + paint.measureText( text ) + 30, top + height );
		
		text = String.format( "%.1f", mTripAOdometer );
		canvas.drawText( text, getWidth( ) - paint.measureText( text ) - 30, top, paint );

		height = PaintUtils.getTextHeight( paint, text );
		
		mRightRect = new RectF( getWidth( ) - paint.measureText( text ) - 30, top + ascent, 
		                        getRight( ), top + height );
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ) {

		int action = event.getActionMasked( );

		if( action == MotionEvent.ACTION_DOWN ) {
			if ( mLeftRect.contains(  event.getX( ), event.getY( ) ) ) {
				
				if( mTripAandB )
					ScooterData.RESETTIMER.schedule( new ResetTripATask( this ), ScooterData.LONGPRESSTIME );
				else
					ScooterData.RESETTIMER.schedule( new ResetTripATimeTask( this ), ScooterData.LONGPRESSTIME );
					
			}	else if( mRightRect.contains( event.getX( ), event.getY( ) ) ) {
				
				if( mTripAandB )
					ScooterData.RESETTIMER.schedule( new ResetTripBTask( this ), ScooterData.LONGPRESSTIME );
				else
					ScooterData.RESETTIMER.schedule( new ResetTripATask( this ), ScooterData.LONGPRESSTIME );
			}
			
		} else if( action == MotionEvent.ACTION_UP ) {
			
			ScooterData.RESETTIMER.cancel( );
		}

		// TODO Auto-generated method stub
		return super.onTouchEvent( event );
	}

	protected class ResetTripATask extends TimerTask {

		private TripGauge mTrip;

		public ResetTripATask( TripGauge trip ) {

			super( );

			mTrip = trip;
		}

		@Override
		public void run( ) {

			mTrip.resetTripA( );
		}
	}

	protected class ResetTripATimeTask extends TimerTask {

		private TripGauge mTrip;

		public ResetTripATimeTask( TripGauge trip ) {

			super( );

			mTrip = trip;
		}

		@Override
		public void run( ) {

			mTrip.resetTripATime( );
		}
	}

	protected class ResetTripBTask extends TimerTask {

		private TripGauge mTrip;

		public ResetTripBTask( TripGauge trip ) {

			super( );

			mTrip = trip;
		}

		@Override
		public void run( ) {

			mTrip.resetTripB( );
		}
	}

}
