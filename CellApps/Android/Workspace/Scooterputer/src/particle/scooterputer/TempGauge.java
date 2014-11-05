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
import android.util.AttributeSet;
import android.widget.TextView;

public class TempGauge extends TextView {
	
	protected int mTemp = 0;
	protected String mUnits = "°F";

	public TempGauge( Context context ) {
		
		super( context );
		// TODO Auto-generated constructor stub
	}

	public TempGauge( Context context, AttributeSet attrs ) {
		
		super( context, attrs );
		// TODO Auto-generated constructor stub
	}

	public TempGauge( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );
		// TODO Auto-generated constructor stub
	}

	public boolean IsCelcius( ) {
		
		return mUnits == "°C";
	}

	public void setUnits( boolean celcius ) {
		
		if( !celcius )
			mUnits = "°F";
		else
			mUnits = "°C";
	}

	public void setTemp( int value ) {
		
		if( mTemp == value )
			return;

		mTemp = value;

		String text = String.valueOf( value );
		text += mUnits;
		setText( text );
	}
}
