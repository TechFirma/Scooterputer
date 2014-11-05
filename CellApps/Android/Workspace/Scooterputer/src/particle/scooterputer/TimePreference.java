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
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {
	
	TimePicker mTime;

	public TimePreference( Context context, AttributeSet attrs ) {
		
		super( context, attrs );
	}

	public TimePreference( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );
	}

	@Override
	protected View onCreateDialogView( ) {
		
		mTime = new TimePicker( getContext( ) );

		SharedPreferences prefs = getPreferenceManager( ).getSharedPreferences( );
		String format = prefs.getString(	getContext( ).getString( R.string.TimeFormat ), "0" );
		boolean is24Hour = format.compareTo( "1" ) == 0 ? true : false;

		mTime.setIs24HourView( is24Hour );

		return mTime;
	}

	@Override
	protected void onDialogClosed( boolean positiveResult ) {
		
		// TODO Auto-generated method stub
		super.onDialogClosed( positiveResult );

		if( !positiveResult )
			return;

		SharedPreferences prefs = getPreferenceManager( ).getSharedPreferences( );
		SharedPreferences.Editor editor = prefs.edit( );

		editor.putString(	getKey( ), String.format( "%d:%d", mTime.getCurrentHour( ),	mTime.getCurrentMinute( ) ) );
		editor.commit( );
	}
}
