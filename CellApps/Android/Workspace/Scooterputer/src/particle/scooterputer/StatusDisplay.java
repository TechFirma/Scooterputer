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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class StatusDisplay extends ImageView {

	public static final int STATUS_DISCONNECTED = 0;
	public static final int STATUS_CONNECTING = 1;
	public static final int STATUS_CONNECTED = 2;
	public static final int STATUS_COMMUNICATING = 3;
	
	private int mBTStatus = STATUS_DISCONNECTED;
	private int mGPSStatus = STATUS_DISCONNECTED;
	
	private Bitmap mBTBitmapGray;
	private Bitmap mGPSBitmapGray;
	private Bitmap mBTBitmapYellow;
	private Bitmap mGPSBitmapYellow;
	private Bitmap mBTBitmapBlue;
	private Bitmap mGPSBitmapGreen;
	
	private boolean mBTColor = true;
	private boolean mGPSColor = true;
	
	public StatusDisplay( Context context ) {
		
		super( context );

		InitializeBitmaps( );
	}

	public StatusDisplay( Context context, AttributeSet attrs ) {
		
		super( context, attrs );

		InitializeBitmaps( );
	}

	public StatusDisplay( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );

		InitializeBitmaps( );
	}

	private void InitializeBitmaps( ) {
		
		mBTBitmapGray = BitmapFactory.decodeResource( getResources( ), R.drawable.ic_bluetooth_gray );
		mBTBitmapYellow = BitmapFactory.decodeResource( getResources( ), R.drawable.ic_bluetooth_yellow );
		mBTBitmapBlue = BitmapFactory.decodeResource( getResources( ), R.drawable.ic_bluetooth_blue );
		
		mGPSBitmapGray = BitmapFactory.decodeResource( getResources( ), R.drawable.ic_gps_gray );
		mGPSBitmapYellow = BitmapFactory.decodeResource( getResources( ), R.drawable.ic_gps_yellow );
		mGPSBitmapGreen = BitmapFactory.decodeResource( getResources( ), R.drawable.ic_gps_green );
	}
	
	public void setBluetoothStatus( int status )
	{
		mBTStatus = status;
	}
	
	public void setGPSStatus( int status )
	{
		mGPSStatus = status;
	}
	
	public void updateTimer( ) {
		
		mBTColor = !mBTColor;
		mGPSColor = !mGPSColor;

		invalidate( );
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		
		// TODO Auto-generated method stub
		super.onDraw( canvas );

		Bitmap BTBitmap = mBTBitmapGray;
		
		if( mBTStatus == STATUS_COMMUNICATING )
			BTBitmap = mBTBitmapBlue;
		else if( ( ( mBTStatus == STATUS_CONNECTING ) && mBTColor ) || ( mBTStatus == STATUS_CONNECTED ) )
			BTBitmap = mBTBitmapYellow;
			
		Bitmap GPSBitmap = mGPSBitmapGray;

		if( mGPSStatus == STATUS_COMMUNICATING )
			GPSBitmap = mGPSBitmapGreen;
		else if( ( ( mGPSStatus == STATUS_CONNECTING ) && mGPSColor ) || ( mGPSStatus == STATUS_CONNECTED ) ) 
			GPSBitmap = mGPSBitmapYellow;

		int height = getHeight( );
		int width = height * 2;	// The height dictates the width as the bitmaps are square
		int middle = height;
		
		Bitmap bm = Bitmap.createBitmap( width, height, BTBitmap.getConfig( ) );
		Canvas bmCanvas = new Canvas( bm );
		
		bmCanvas.drawBitmap( BTBitmap, new Rect( 0, 0, BTBitmap.getWidth( ), BTBitmap.getHeight( ) ), new Rect( 0, 0, middle, height ), null ); 
		bmCanvas.drawBitmap( GPSBitmap, new Rect( 0, 0, GPSBitmap.getWidth( ), GPSBitmap.getHeight( ) ), new Rect( middle, 0, width, height ), null ); 
		                     
		setImageBitmap( bm );
	}
}
