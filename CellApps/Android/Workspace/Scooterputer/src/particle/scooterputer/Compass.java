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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;

public class Compass extends TextView {
	
	private float N = 0;
	private float NE = 45;
	private float E = 90;
	private float SE = 135;
	private float S = 180;
	private float SW = 225;
	private float W = 270;
	private float NW = 315;

	private float mHeading = 0.0f;

	public Compass( Context context ) {
		
		super( context );
		// TODO Auto-generated constructor stub
	}

	public Compass( Context context, AttributeSet attrs ) {
		
		super( context, attrs );
		// TODO Auto-generated constructor stub
	}

	public Compass( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );
		// TODO Auto-generated constructor stub
	}

	public void setHeading( float value ) {
		
		if( mHeading == value )
			return;

		mHeading = value;

		invalidate( );
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		
		// TODO Auto-generated method stub
		// super.onDraw( canvas );

		Paint paint = getPaint( );

		paint.setTextAlign( Align.CENTER );
		paint.setARGB( 255, 255, 255, 0 );
		paint.setStrokeWidth( 5 );

		float ascent = paint.ascent( );
		float textSize = getTextSize( );
		float left = getPaddingLeft( );
		float right = getWidth( );

		canvas.drawLine( left, 0, left, 18, paint );
		canvas.drawLine( right, 0, right, 18, paint );

		paint.setARGB( 255, 0, 128, 0 );

		canvas.drawLine( left, 19, right, 19, paint );

		paint.setStrokeWidth( 3 );

		// 120 degrees total in 5 degree increments, 60 degrees left & right of
		// the heading at center
		float start = 0;
		if( ( mHeading % 5f ) != 0 )
			start = 5 - ( mHeading % 5f );

		float heading = ( mHeading - 60f ) + start;
		if( heading < 0 )
			heading += 360;

		float yText = 42 - ascent;
		float tickHeight = 24;
		float xTick = ( right - left ) / 24;

		for( float x = start + left; x < right; x += xTick ) {
			
			paint.setARGB( 255, 0, 128, 0 );
			tickHeight = heading % 10 == 0 ? 30 : 24;

			if( ( heading == N ) || ( heading == NE ) || ( heading == E ) || 
				 ( heading == SE ) || ( heading == S ) || ( heading == SW ) || 
				 ( heading == W )	|| ( heading == NW ) ) {
				
				tickHeight = 36;
				paint.setARGB( 255, 0, 255, 0 );
			}

			canvas.drawLine( x, 20, x, tickHeight, paint );

			if( ( x - left >= ( xTick * 8 ) - 1 ) && ( x - left <= ( xTick * 16 ) + 1 ) ) {
				
				paint.setTextSize( textSize + 23 );
				ascent = paint.ascent( );
				yText = 33 - ascent;
				
			} else {
				
				paint.setTextSize( textSize );
				ascent = paint.ascent( );
				yText = 42 - ascent;
			}

			paint.setARGB( 255, 170, 144, 0 );

			if( heading == N )
				canvas.drawText( "N", x, yText, paint );

			if( heading == NE )
				canvas.drawText( "NE", x, yText, paint );

			if( heading == E )
				canvas.drawText( "E", x, yText, paint );

			if( heading == SE )
				canvas.drawText( "SE", x, yText, paint );

			if( heading == S )
				canvas.drawText( "S", x, yText, paint );

			if( heading == SW )
				canvas.drawText( "SW", x, yText, paint );

			if( heading == W )
				canvas.drawText( "W", x, yText, paint );

			if( heading == NW )
				canvas.drawText( "NW", x, yText, paint );

			heading += 5;
			if( heading == 360 )
				heading = 0;
		}

		paint.setStyle( Style.FILL_AND_STROKE );

		left = ( ( right - left ) / 2 ) + getPaddingLeft( );

		Path path = new Path( );

		path.moveTo( left, 23 );
		path.lineTo( left - 12, 0 );
		path.lineTo( left + 12, 0 );
		path.lineTo( left, 23 );

		Path shadowPath = new Path( );
		path.offset( 5, 0, shadowPath );

		paint.setARGB( 100, 0, 0, 0 );
		canvas.drawPath( shadowPath, paint );

		paint.setARGB( 255, 255, 255, 0 );
		canvas.drawPath( path, paint );
	}
}
