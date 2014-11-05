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

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;


public class BluetoothDevicePreference extends ListPreference {

	public BluetoothDevicePreference( Context context ) {

		super( context );
		
	}

	public BluetoothDevicePreference( Context context, AttributeSet attrs ) {

		super( context, attrs );
		
	}

	@Override
	protected View onCreateDialogView( ) {

		// Get a list of the bluetooth adapters
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter( );
		Set< BluetoothDevice > devices = mBluetoothAdapter.getBondedDevices( );

		int index = 0;
		String[ ] names = new String[ devices.size( ) ];
		
		if( devices.size( ) > 0 ) {
			
			// Loop through paired devices
			for( BluetoothDevice item : devices ) {
				
				names[ index++ ] = item.getName( );
			}
		} 

		setEntries( names );
		setEntryValues( names );
		
		return super.onCreateDialogView( );
	}
}
