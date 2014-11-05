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

import java.text.SimpleDateFormat;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

public class TimeDisplay extends TextView {
	
	protected boolean m24hour = false;

	public TimeDisplay( Context context ) {
		
		super( context );
		// TODO Auto-generated constructor stub
	}

	public TimeDisplay( Context context, AttributeSet attrs ) {
		
		super( context, attrs );
		// TODO Auto-generated constructor stub
	}

	public TimeDisplay( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );
		// TODO Auto-generated constructor stub
	}

	public void set24Hour( boolean value ) {
		
		if( m24hour == value )
			return;

		m24hour = value;

		invalidate( );
	}

	public void updateTimer( ) {
		
		SimpleDateFormat formatter = ( SimpleDateFormat ) DateFormat.getTimeFormat( getContext( ) );

		if( !m24hour )
			formatter.applyLocalizedPattern( "h:mm a" );
		else
			formatter.applyLocalizedPattern( "k:mm" );

		setText( formatter.format( System.currentTimeMillis( ) ) );
	}
}
