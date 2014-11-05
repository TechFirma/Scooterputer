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

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {
	
	public static String SettingsName;
	public static String[ ] SettingKeys;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		
		super.onCreate( savedInstanceState );

		addPreferencesFromResource( R.xml.preferences );

		SettingsName = getPreferenceManager( ).getSharedPreferencesName( );

		// These should be in the same order as they need to be transmitted and
		// need to match what is in the preferences.xml file for the preference screen.
		SettingKeys = new String[ ] {
				getResources( ).getString( R.string.GPS ),
				getResources( ).getString( R.string.DistanceUnits ),
				getResources( ).getString( R.string.DistanceSensor ),
				getResources( ).getString( R.string.WheelDiameter ),
				getResources( ).getString( R.string.MaxRPM ),
				getResources( ).getString( R.string.IgnitionPPC ),
				getResources( ).getString( R.string.TemperatureUnits ),
				getResources( ).getString( R.string.TripOdometer ),
				getResources( ).getString( R.string.TimeFormat ),
				getResources( ).getString( R.string.Time ),
				getResources( ).getString( R.string.Odometer ),
				getResources( ).getString( R.string.SetDefaults ) };
	}
}