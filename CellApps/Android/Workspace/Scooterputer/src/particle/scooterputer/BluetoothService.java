/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class BluetoothService {
	
	// Debugging
	private static final String TAG = "BluetoothService";
	private static final boolean D = true;

	// Name for the SDP record when creating server socket
	public static String NAME = "Scooterputer";

	// Unique UUID for this application
	private static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );

	// Member fields
	private Context mContext;
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; 			
	public static final int STATE_CONNECTING = 1; 	
	public static final int STATE_CONNECTED = 2; 	

	// Constants identifying information in the data
	protected static byte STX = 2;
	protected static byte ETX = 3;
	protected static int RECV = 0x5555;
	protected static int IDLE = 0x0000;
	protected static byte settingsChar = 'Z';
	protected static byte tripAChar = 'A';
	protected static byte tripBChar = 'B';
	protected static byte tripATimeChar = 'R';
	protected static byte GPSChar = 'G';

	// device

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothService( Context context, Handler handler ) {
		
		mContext = context;
		mAdapter = BluetoothAdapter.getDefaultAdapter( );
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState( int state ) {
		
		if( D )
			Log.d( TAG, "setState( ) " + mState + " -> " + state );

		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage( ScooterData.MESSAGE_STATE_CHANGE, state, -1 ).sendToTarget( );
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState( ) {
		
		return mState;
	}

	public void signalSettings( ) {
		
		write( settingsChar );
	}

	public void resetTripA( ) {
		
		write( tripAChar );
	}
	
	public void resetTripATime( ) {
		
		write( tripATimeChar );
	}

	public void resetTripB( ) {
		
		write( tripBChar );
	}
	
	public void writeSettings( Map< String, ? > oldSettings ) {
		
		int offset = 0;
		byte[ ] pkt = new byte[ ( ( Settings.SettingKeys.length + 2 ) * 2 ) + 2 ]; // 2 props take 2 ints + start and end bytes

		SharedPreferences prefs = mContext.getSharedPreferences(	Settings.SettingsName, Context.MODE_PRIVATE );
		Map< String, ? > settings = prefs.getAll( );

		pkt[ offset++ ] = STX;
		
		for( int i = 0; i < Settings.SettingKeys.length; ++i ) {
			
			String key = Settings.SettingKeys[ i ];
			String sValue = ( String ) settings.get( key );
			
			if( sValue != oldSettings.get( key ) ) {
				
				if( key.compareTo( "Time" ) == 0 ) {
					
					// Time goes across as Minutes first followed by the hours
					String[ ] fields = sValue.split( ":" );
					encodeInt( pkt, offset, Integer.parseInt( fields[ 1 ] ) );
					offset += 2;
					encodeInt( pkt, offset, Integer.parseInt( fields[ 0 ] ) );
					
				} else if( key.compareTo( "Odometer" ) == 0 ) {
					
					// Odometer goes across 100s first then 1000s
					int value = Integer.parseInt( sValue );
					
					// Check to see if the distance units is in kilometers and if so convert to mph
					// since that is how the sensor board expects it right now.
					if( settings.get( R.string.DistanceUnits ) == "1" )
						value = Math.round( ( float ) value * 0.621371192f );		

					encodeInt( pkt, offset, value % 1000 );
					offset += 2;
					encodeInt( pkt, offset, value / 1000 );
				} else {
					
					encodeInt( pkt, offset, Integer.parseInt( sValue ) );
				}
			} else {
				
				encodeInt( pkt, offset, -1 );

				if( ( key.compareTo( "Time" ) == 0 ) || ( key.compareTo( "Odometer" ) == 0 ) ) {
					
					offset += 2;
					encodeInt( pkt, offset, -1 );
				}
			}

			offset += 2;
		}

		pkt[ offset++ ] = ETX;

		if( mConnectedThread != null )
			mConnectedThread.write( pkt );
	}

	private void encodeInt( byte[ ] array, int offset, int value ) {
		
		Integer highByte = value >> 8;
		Integer lowByte = value & 0xff;
		array[ offset++ ] = highByte.byteValue( );
		array[ offset++ ] = lowByte.byteValue( );
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connect( BluetoothDevice device ) {
		
		if( D )
			Log.d( TAG, "connect to: " + device );

		// Cancel any thread attempting to make a connection
		if( ( mState == STATE_CONNECTING ) && ( mConnectThread != null ) ) {
			
			mConnectThread.cancel( );
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if( mConnectedThread != null ) {
			
			mConnectedThread.cancel( );
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread( device );
		mConnectThread.start( );

		setState( STATE_CONNECTING );
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected( BluetoothSocket socket,	BluetoothDevice device ) {
		
		if( D )
			Log.d( TAG, "connected" );

		// Cancel the thread that completed the connection
		if( mConnectThread != null ) {
			
			mConnectThread.cancel( );
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if( mConnectedThread != null ) {
			
			mConnectedThread.cancel( );
			mConnectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread( socket );
		mConnectedThread.start( );

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage( ScooterData.MESSAGE_DEVICE_NAME );

		Bundle bundle = new Bundle( );
		bundle.putString( ScooterData.DEVICE_NAME, device.getName( ) );

		msg.setData( bundle );
		mHandler.sendMessage( msg );

		setState( STATE_CONNECTED );
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop( ) {
		
		if( D )
			Log.d( TAG, "stop" );

		if( mConnectThread != null ) {
			
			mConnectThread.cancel( );
			mConnectThread = null;
		}
		if( mConnectedThread != null ) {
			
			mConnectedThread.cancel( );
			mConnectedThread = null;
		}

		setState( STATE_NONE );
	}

	public void write( byte out ) {
		
		write( new byte[ ] { out } );
	}
	
	public void write( long out ) {
		
		for( int i = 0 ; i < 4; ++i ) {
			write( ( byte )( out >> ( i * 4 ) ) );
		}
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write( byte[ ] out ) {
		
		// Create temporary object
		ConnectedThread r;

		// Synchronize a copy of the ConnectedThread
		synchronized( this ) {
			
			if( mState != STATE_CONNECTED )
				return;

			r = mConnectedThread;
		}

		// Perform the write unsynchronized
		r.write( out );
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed( ) {
		
		setState( STATE_NONE );

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage( ScooterData.MESSAGE_TOAST );

		Bundle bundle = new Bundle( );
		bundle.putString( ScooterData.TOAST, "Unable to connect device" );

		msg.setData( bundle );
		mHandler.sendMessage( msg );
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost( ) {
		
		setState( STATE_NONE );

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage( ScooterData.MESSAGE_TOAST );

		Bundle bundle = new Bundle( );
		bundle.putString( ScooterData.TOAST, "Device connection was lost" );

		msg.setData( bundle );
		mHandler.sendMessage( msg );
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private boolean mmCanceled = false;

		public ConnectThread( BluetoothDevice device ) {
			
			mmDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				
				tmp = device.createRfcommSocketToServiceRecord( MY_UUID );
				
			} catch( IOException e ) {
				
				Log.e( TAG, "create() failed", e );
			}

			mmSocket = tmp;
		}

		public void run( ) {
			
			Log.i( TAG, "BEGIN mConnectThread" );

			setName( "ConnectThread" );

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery( );

			// Make a connection to the BluetoothSocket
			try {
				
				if( mmSocket == null )
					throw new IOException( );
					
				// This is a blocking call and will only return on a successful connection or an exception
				mmSocket.connect( );
				
			} catch( IOException e ) {
				
				connectionFailed( );
				
				// Close the socket
				try {
					
					mmSocket.close( );
					
				} catch( IOException e2 ) {
					
					Log.e( TAG,	"unable to close() socket during connection failure",	e2 );
				}
				
				if( mmCanceled )
					return;
				
				// Sleep for a few second and try to connect again
				try {
					
					sleep( 1000 );
					
				} catch( InterruptedException e1 ) {
					
					Log.e( TAG,	"unable to sleep in ConnectThread during connection failure",	e1 );
				}
				
				BluetoothService.this.connect( mmDevice );
				
				// Start the service over to restart listening mode
				// BluetoothService.this.start( );

				return;
			}

			// Reset the ConnectThread because we're done
			synchronized( BluetoothService.this ) {
				
				mConnectThread = null;
			}

			// Start the connected thread
			connected( mmSocket, mmDevice );
		}

		public void cancel( ) {
			
			try {
				mmCanceled = true;
				
				mmSocket.close( );
				
			} catch( IOException e ) {
				
				Log.e( TAG, "close( ) of connect socket failed", e );
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private boolean mmCanceled = false;

		public ConnectedThread( BluetoothSocket socket ) {
			
			Log.d( TAG, "create ConnectedThread" );

			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				
				tmpIn = socket.getInputStream( );
				tmpOut = socket.getOutputStream( );
				
			} catch( IOException e ) {
				
				Log.e( TAG, "temp sockets not created", e );
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run( ) {
			
			Log.i( TAG, "BEGIN mConnectedThread" );

			int BUFFER_LENGTH = 512;
			int PKT_LENGTH = 60;
			byte[ ] buffer = new byte[ BUFFER_LENGTH ];
			byte[ ] msg = new byte[ BUFFER_LENGTH ];
			int offset = 0;
			int startOffset = 0;

			// Keep listening to the InputStream while connected
			while( !mmCanceled ) {
				try {
					
					//if( mmInStream.available( ) <= 0 )
					//	continue;
					
					// Read from the InputStream
					offset += mmInStream.read( buffer, offset, BUFFER_LENGTH - offset - 1 );

					// Collect at least 2 pkts worth of information before processing
					// This guarantees that we have at least 1 complete pkt worth of information
					if( offset < ( PKT_LENGTH * 2 ) )
						continue;

					int maxPkts = PKT_LENGTH * 4;

					// Keep no more than 4 pkts worth of info before dumping the older information
					if( offset > maxPkts )
						startOffset = offset - maxPkts;

					int i = startOffset;
					while( i < offset - PKT_LENGTH ) {
						
						// Look for beginning and ending of a pkt
						if( ( buffer[ i ] == STX )	&& ( buffer[ i + PKT_LENGTH - 1 ] == ETX ) ) {
							
							// Don't copy the start and stop bytes
							System.arraycopy( buffer, i + 1, msg, 0, PKT_LENGTH - 2 );

							// Send the obtained bytes to the UI Activity
							mHandler.obtainMessage( ScooterData.MESSAGE_READ, PKT_LENGTH, -1, msg ).sendToTarget( );

							i += PKT_LENGTH;

							break;
						}

						++i;
					}

					startOffset = 0;
					offset -= i;
					System.arraycopy( buffer, i, buffer, 0, offset );
					
				} catch( IOException e ) {
					
					Log.e( TAG, "disconnected", e );

					connectionLost( );
					
					// Try restarting the connection
					if( !mmCanceled && ( mmSocket != null ) )
						BluetoothService.this.connect( mmSocket.getRemoteDevice( ) );
					
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write( byte[ ] buffer ) {
			
			try {
				
				mmOutStream.write( buffer );

				// Share the sent message back to the UI Activity
				mHandler.obtainMessage( ScooterData.MESSAGE_WRITE, -1, -1, buffer ).sendToTarget( );
				
			} catch( IOException e ) {
				
				Log.e( TAG, "Exception during write", e );
			}
		}

		public void cancel( ) {
			
			try {
				mmCanceled = true;
				
				mmSocket.close( );
				
			} catch( IOException e ) {
				
				Log.e( TAG, "close() of connect socket failed", e );
			}
		}
	}
}
