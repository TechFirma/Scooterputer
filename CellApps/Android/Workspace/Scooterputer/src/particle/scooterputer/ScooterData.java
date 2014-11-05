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

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import particle.utilities.DiagnosticLogger;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;

public class ScooterData extends Activity {

	public static final String TAG = "Scooterputer";
	private static final boolean D = true;

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothService mBluetoothService = null;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int RESET_TRIPA = 6;
	public static final int RESET_TRIPATIME = 7;
	public static final int RESET_TRIPB = 8;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int SETTINGS = 2;

	// Timer used to reset current values on long press
	public static final Timer RESETTIMER = new Timer( "ResetTimer" );
	public static final long LONGPRESSTIME = 1500;

	private TempGauge mTemp;
	private TimeDisplay mTime;
	private VoltageGauge mVoltage;
	private Speedometer mSpeedometer;
	private TripGauge mTripGauge;
	private Tachometer mTachometer;
	private Compass mCompass;
	private StatusDisplay mStatus;

	private boolean mPhoneGPS = true;
	private boolean mPhoneGPSActive = false;
	private float mTotalDistance = 0f;
	private float mTripA = 0f;
	private float mTripB = 0f;
	private Time mTripStartTime = new Time( );

	private int mGPSActivity = 0;
	private int mGPSMisses = 0;
	
	private static final int RECV_CONFIG = 0x5555;
	private boolean mSignalSettings = false;
	private boolean mSettingsReceived = false;
	private Map< String, ? > mSettingsCurrent = null;

	public DiagnosticLogger mLogger = new DiagnosticLogger( );

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState ) {

		super.onCreate( savedInstanceState );

		PreferenceManager.setDefaultValues( this, R.xml.preferences, false );

		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter( );

		requestWindowFeature( Window.FEATURE_NO_TITLE );
		getWindow( ).setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

		setContentView( R.layout.main );

		mTemp = ( TempGauge ) findViewById( R.id.TempGauge );
		mTime = ( TimeDisplay ) findViewById( R.id.TimeDisplay );
		mVoltage = ( VoltageGauge ) findViewById( R.id.VoltageGauge );
		mSpeedometer = ( Speedometer ) findViewById( R.id.Speedometer );
		mTripGauge = ( TripGauge ) findViewById( R.id.TripGauge );
		mTachometer = ( Tachometer ) findViewById( R.id.Tachometer );
		mCompass = ( Compass ) findViewById( R.id.Compass );
		mStatus = ( StatusDisplay ) findViewById( R.id.StatusDisplay );

		mTripStartTime.set( 0 );
	}

	@Override
	protected void onStart( ) {

		super.onStart( );

		initialize( );

		if( !mBluetoothAdapter.isEnabled( ) ) {

			Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
			startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT );

		} else {

			initializeBluetooth( );
		}

		mHandler.removeCallbacks( mUpdateTimeTask );
		mHandler.postDelayed( mUpdateTimeTask, 100 );
	}

	public void initializeBluetooth( ) {

		if( mBluetoothService != null )
			return;

		Set< BluetoothDevice > devices = mBluetoothAdapter.getBondedDevices( );
		BluetoothDevice device = null;

		if( devices.size( ) > 0 ) {
			
			// Loop through paired devices
			for( BluetoothDevice item : devices ) {
				if( item.getName( ).equals( BluetoothService.NAME ) ) {
					device = item;
					break;
				}
			}
		} else {
			mLogger.log( "No bluetooth devices found!" );
		}

		if( device != null ) {
			mBluetoothService = new BluetoothService( this, mHandler );
			mBluetoothService.connect( device );
		}
		else {
			mLogger.log( "Couldn't find bluetooth device named Scooterputer!" );
		}
	}

	protected void initialize( ) {

		// Update the current settings first to make sure we're using the latest settings.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
		mSettingsCurrent = prefs.getAll( );

		String sVersion = ( String ) mSettingsCurrent.get( getResources( ).getString( R.string.Version ) );
		if( ( sVersion == null ) || ( sVersion.compareToIgnoreCase( "Version 1.0.3" ) != 0 ) )
		{
			// Clear out the preferences
			Editor editor = prefs.edit( ).clear( );
			editor.putString( getResources( ).getString( R.string.Version ), "Version 1.0.3" );
			editor.commit( );
			
			mSettingsCurrent = prefs.getAll( );
		}
		
		mLogger.close( );

		Boolean logging = ( Boolean ) mSettingsCurrent.get( getResources( ).getString( R.string.EnableLogging ) );

		if( ( logging != null ) && logging ) {
			
			mLogger.setDirectory( ( String ) mSettingsCurrent.get( getResources( ).getString( R.string.LogDirectory ) ) );
			mLogger.open( );
			
			Date dt = new Date( );
			
			mLogger.log( "Logging started at " + dt.toString( ) );
		}
		
		mTripGauge.setHandler( mHandler );

		BluetoothService.NAME = ( String ) mSettingsCurrent.get( getResources( ).getString( R.string.BluetoothDevice ) );
		
		if( BluetoothService.NAME == null )
			BluetoothService.NAME = ( String ) getResources( ).getString( R.string.Scooterputer );

		String sValue = ( String ) mSettingsCurrent.get( getResources( ).getString( R.string.DistanceUnits ) );
		if( sValue != null )
			mSpeedometer.setUnits( sValue.matches( getResources( ).getStringArray( R.array.DistanceUnitValues )[ 1 ] ) );
		
		sValue = ( String ) mSettingsCurrent.get( getResources( ).getString( R.string.TripOdometer ) );
		if( sValue != null )
			mTripGauge.SetTripAandB( sValue.matches( getResources( ).getStringArray( R.array.TripOdometerValues )[ 1 ] )  );

		sValue = ( String ) mSettingsCurrent.get( getResources( ).getString( R.string.TemperatureUnits ) );
		if( sValue != null )
			mTemp.setUnits( sValue.matches( getResources( ).getStringArray( R.array.TemperatureUnitValues )[ 1 ] ) );
		
		if( mPhoneGPS ) {
			
			if( !mPhoneGPSActive ) {
				
				mLogger.log( "Starting GPS listener" );
		
				LocationManager locationManager = ( LocationManager ) getSystemService( Context.LOCATION_SERVICE );
		
				// Register the listener with the Location Manager to receive location updates
				locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 500, 0.25f, mLocationListener );
				
				mStatus.setGPSStatus( StatusDisplay.STATUS_CONNECTING );
				
				mPhoneGPSActive = true;
			}
		}
		else {
			
			stopLocationListener( );
		}
	}
	
	LocationListener mLocationListener = new LocationListener( ) {

		private double mPrevLat = 0;
		private double mPrevLon = 0;

		public void onLocationChanged( Location location ) {

			mLogger.log( "GPS Location Changed" );

			mStatus.setGPSStatus( StatusDisplay.STATUS_COMMUNICATING );

			boolean kph = mSpeedometer.IsKPH( );
			if( location.hasSpeed( ) ) {
				
				int speed = 0;

				if( kph )
					speed = ( int ) ( location.getSpeed( ) * 3.6f ); // Convert to KPH
				else
					speed = ( int ) ( location.getSpeed( ) * 2.2369362920544f ); // Convert to MPH

				mSpeedometer.setSpeed( speed );	

				mLogger.log( "GPS Speed = " + speed );
			}

			if( location.hasBearing( ) ) {
				
				mCompass.setHeading( location.getBearing( ) );

				mLogger.log( "GPS Bearing = " + location.getBearing( ) );
			}

			double lat = location.getLatitude( );
			double lon = location.getLongitude( );
			float distance = 0;

			if( ( mPrevLat != 0 ) && ( mPrevLon != 0 ) ) {
				
				float[ ] results = new float[ 1 ];
				Location.distanceBetween( mPrevLat, mPrevLon, lat, lon, results );

				if( kph )
					distance = results[ 0 ] * 0.001f; // Convert from meters to kilometers
				else
					distance = results[ 0 ] * 0.000621371192f; // Convert from meters to miles

				mTotalDistance += distance;
				mSpeedometer.setOdometer( ( long ) mTotalDistance );

				mTripA += distance;
				mTripGauge.setTripAOdometer( mTripA );

				mTripB += distance;
				mTripGauge.setTripBOdometer( mTripB );
			}

			mPrevLat = lat;
			mPrevLon = lon;
		}

		public void onStatusChanged( String provider, int status, Bundle extras ) {

			if( status == LocationProvider.AVAILABLE )
				mStatus.setGPSStatus( StatusDisplay.STATUS_CONNECTED );
			else
				mStatus.setGPSStatus( StatusDisplay.STATUS_DISCONNECTED );
		}

		public void onProviderEnabled( String provider ) {

		}

		public void onProviderDisabled( String provider ) {

		}
	};

	public void stopLocationListener( ) {
		
		if( !mPhoneGPSActive )
			return;
		
		LocationManager locationManager = ( LocationManager ) getSystemService( Context.LOCATION_SERVICE );
		locationManager.removeUpdates( mLocationListener );

		mStatus.setGPSStatus( StatusDisplay.STATUS_DISCONNECTED );
		
		mPhoneGPSActive = false;
	}

	@Override
	protected void onStop( ) {

		super.onStop( );

		mLogger.flush( );
	}

	@Override
	protected void onDestroy( ) {

		super.onDestroy( );

		if( mBluetoothService != null ) {
			
			mBluetoothService.stop( );
		}
		
		stopLocationListener( );
		mLogger.close( );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {

		MenuInflater inflater = getMenuInflater( );
		inflater.inflate( R.menu.options, menu );

		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {

		// Handle item selection
		switch( item.getItemId( ) ) {
			
			case R.id.settings:
				mSignalSettings = true;

				// Make sure we save the current settings so we can see what changed
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
				mSettingsCurrent = prefs.getAll( );

				Intent settingsActivity = new Intent( getBaseContext( ), Settings.class );

				startActivityForResult( settingsActivity, SETTINGS );

				return true;
				
			default:
				return super.onOptionsItemSelected( item );
		}
	}

	@Override
	public void onOptionsMenuClosed( Menu menu ) {

		// TODO Auto-generated method stub
		super.onOptionsMenuClosed( menu );
	}

	private Runnable mUpdateTimeTask = new Runnable( ) {

		public void run( ) {

			mTime.updateTimer( );
			mVoltage.updateTimer( );
			mStatus.updateTimer( );

			if( mSignalSettings && ( mBluetoothService != null ) )
				mBluetoothService.signalSettings( );

			if( mPhoneGPS ) {
				
				if( ( mTripStartTime.toMillis( false ) == 0 ) && ( mTotalDistance > 0 ) )
					mTripStartTime.setToNow( );
					
				if( mTripStartTime.toMillis( false ) > 0 ) {
					Time now = new Time( );
					now.setToNow( );
					
					mTripGauge.setTripATime( now.toMillis(  false  ) - mTripStartTime.toMillis( false ) );
				}
			}
			
			mHandler.postDelayed( this, 500 );
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler( ) {

		@Override
		public void handleMessage( Message msg ) {

			switch( msg.what ) {
				
				case MESSAGE_STATE_CHANGE:
					if( D )
						Log.i( TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1 );

					switch( msg.arg1 ) {
						case BluetoothService.STATE_CONNECTED:
							mLogger.log( "Bluetooth connected!" );

							// From this point forward don't use the phone GPS since we have a 
							// connection to the sensor board
							stopLocationListener( );
							mPhoneGPS = false;
							mStatus.setBluetoothStatus( StatusDisplay.STATUS_CONNECTED );
						break;
						
						case BluetoothService.STATE_CONNECTING:
							mLogger.log( "Bluetooth connecting" );
							
							mStatus.setBluetoothStatus( StatusDisplay.STATUS_CONNECTING );
							break;
							
						case BluetoothService.STATE_NONE:
							mStatus.setBluetoothStatus( StatusDisplay.STATUS_DISCONNECTED );
							break;
					}
					break;
					
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					// mConnectedDeviceName = msg.getData( ).getString( DEVICE_NAME );
					break;

				case MESSAGE_TOAST:
					mLogger.log( msg.getData( ).getString( TOAST ) );

					break;
					
				case MESSAGE_WRITE:
					break;
					
				case MESSAGE_READ:
					mStatus.setBluetoothStatus( StatusDisplay.STATUS_COMMUNICATING );
					mStatus.setGPSStatus( StatusDisplay.STATUS_CONNECTING );
					
					byte[ ] readBuf = ( byte[ ] ) msg.obj;

					mTachometer.setRPM( decodeInt( readBuf, 0 ) );
					
					int speed = decodeInt( readBuf, 2 );
					
					if( mSpeedometer.IsKPH( ) )
						speed = Math.round( ( float ) speed * 1.609344f );
					
					mSpeedometer.setSpeed( speed );

					mSpeedometer.setOdometer( decodeLong( readBuf, 4 ) );

					mTripGauge.setTripAOdometer( ( float ) decodeLong( readBuf, 8 ) / 10f );
					mTripGauge.setTripBOdometer( ( float ) decodeLong( readBuf, 12 ) / 10f );

					int hours = decodeInt( readBuf, 16 );
					int minutes = decodeInt( readBuf, 18 );

					mTripGauge.setTripATime( hours * 3600000 + minutes * 60000  );

					// Skip reading the time

					mTemp.setTemp( decodeInt( readBuf, 26 ) );

					mVoltage.setVoltage( ( float ) decodeInt( readBuf, 28 ) / 10f );

					int activity = decodeInt( readBuf, 30 );
					if( mGPSActivity != activity )
					{
						mGPSActivity = activity;
						mGPSMisses = 0;
						mStatus.setGPSStatus( StatusDisplay.STATUS_COMMUNICATING );
					}
					else if( ++mGPSMisses > 10 )
					{
						mStatus.setGPSStatus( StatusDisplay.STATUS_CONNECTED );
					}
										
					mCompass.setHeading( ( float ) decodeLong( readBuf, 32 ) / 100 );

					// Skip Backlight
					//decodeInt( readBuf, 36 );
					
					// Sync Config
					int config = decodeInt( readBuf, 38 );

					if( !mSettingsReceived && ( config != RECV_CONFIG ) ) {
						
						mSettingsReceived = true;

						SharedPreferences prefs = getSharedPreferences( Settings.SettingsName, MODE_PRIVATE );
						SharedPreferences.Editor editor = prefs.edit( );

						editor.putString( getString( R.string.GPS ), String.valueOf( decodeInt( readBuf, 40 ) ) );
						int distanceUnits = decodeInt( readBuf, 42 );
						mSpeedometer.setUnits( distanceUnits == 1 );
						editor.putString( getString( R.string.DistanceUnits ), String.valueOf( distanceUnits ) );
						editor.putString( getString( R.string.DistanceSensor ), String.valueOf( decodeInt( readBuf, 44 ) ) );
						editor.putString( getString( R.string.WheelDiameter ), String.valueOf( decodeInt( readBuf, 46 ) ) );

						int index = decodeInt( readBuf, 48 );
						String[ ] RPMOptions = getResources( ).getStringArray( R.array.MaxRPMOptions );
						String RPMOption = RPMOptions[ 0 ];
						
						if( index < RPMOptions.length )
							RPMOption = RPMOptions[ index ];
						
						int maxRPM = Integer.parseInt( RPMOption.replaceAll( ",", "" ) );
						mTachometer.setMaxRPM( maxRPM );
						editor.putString( getString( R.string.MaxRPM ), String.valueOf( index ) );

						editor.putString( getString( R.string.IgnitionPPC ), String.valueOf( decodeInt( readBuf, 50 ) ) );

						int tempUnits = decodeInt( readBuf, 52 );
						mTemp.setUnits( tempUnits == 1 );
						editor.putString( getString( R.string.TemperatureUnits ), String.valueOf( tempUnits ) );

						int tripFormat = decodeInt( readBuf, 54 );
						mTripGauge.SetTripAandB( tripFormat == 0 ? false : true );

						editor.putString( getString( R.string.TripOdometer ), String.valueOf( tripFormat ) );

						int timeFormat = decodeInt( readBuf, 56 );
						mTime.set24Hour( timeFormat == 0 ? false : true );
						editor.putString( getString( R.string.TimeFormat ), String.valueOf( timeFormat ) );

						editor.commit( );
					}
					break;
					
				case RESET_TRIPA:
					if( mBluetoothService != null )
						mBluetoothService.resetTripA( );

					mTripA = 0f;
					mTripGauge.invalidate( );
					break;
					
				case RESET_TRIPATIME:
					if( mBluetoothService != null )
						mBluetoothService.resetTripATime( );

					mTripStartTime.setToNow( );
					mTripGauge.invalidate( );
					break;
					
				case RESET_TRIPB:
					if( mBluetoothService != null )
						mBluetoothService.resetTripB( );

					mTripB = 0f;
					mTripGauge.invalidate( );

					break;
			}
		}

		private int decodeInt( byte[ ] array, int offset ) {

			int value = ( ( ( int ) array[ offset ] ) << 8 )
							+ ( array[ offset + 1 ] & 0xff );

			return value;
		}

		private long decodeLong( byte[ ] array, int offset ) {

			long value = ( ( ( long ) decodeInt( array, offset ) ) << 16 )
								+ ( decodeInt( array, offset + 2 ) & 0xffff );

			return value;
		}
	};

	public void onActivityResult( int requestCode, int resultCode, Intent data ) {

		switch( requestCode ) {
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if( resultCode == Activity.RESULT_OK ) {
					
					// Bluetooth is now enabled, so set up a chat session
					initializeBluetooth( );
					
				} else {
					
					// User did not enable Bluetooth or an error occured
					finish( );
				}

				break;
			case SETTINGS:
				// Push the setting to the sensor board
				mSignalSettings = false;
				mSettingsReceived = false;
				
				// Check to see if the bluetooth device name setting changed and connect to the new device
				String key = getResources( ).getString( R.string.BluetoothDevice );
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
				String bluetoothName = prefs.getString( key, BluetoothService.NAME );
				
				if( bluetoothName.compareTo( BluetoothService.NAME ) != 0 ) {

					if( mBluetoothService != null ) {
						mBluetoothService.stop( );
						mBluetoothService = null;
					}
					
					BluetoothService.NAME = bluetoothName;

					initializeBluetooth( );
				}
				
				if( mBluetoothService != null )
					mBluetoothService.writeSettings( mSettingsCurrent );

				break;
		}
	}
}
