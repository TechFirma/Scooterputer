package particle.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import particle.scooterputer.ScooterData;

import android.os.Environment;
import android.util.Log;

public class DiagnosticLogger 
{
	private final String FILENAME = "Scooterputer.log";
	private BufferedWriter mLogFile = null;
	private File mDirectory = null;
	
	public DiagnosticLogger( )
	{
		mDirectory = Environment.getExternalStorageDirectory( );
		
		if( !mDirectory.canWrite( ) )
			mDirectory = Environment.getRootDirectory( );
	}
	
	public void setDirectory( String dir )
	{
		if( mDirectory.getAbsolutePath( ).matches( dir ) )
			return;
		
		boolean wasOpen = false;
		
		if( mLogFile != null )
		{
			wasOpen = true;
			close( );
		}
		
		File file = new File( mDirectory, FILENAME );
		file.delete( );

		mDirectory = new File( dir );
		
		if( wasOpen )
			open( );
	}
	
	public void open( )
	{
		if( mLogFile != null )
			return;
		
		try
		{
			if( !mDirectory.exists( ) )
				mDirectory.mkdirs( );
			
			File file = new File( mDirectory, FILENAME );
			FileWriter writer = new FileWriter( file, true );

			mLogFile = new BufferedWriter( writer );
		}
		catch( IOException e )
		{
			Log.e( ScooterData.TAG, "Could not write file " + e.getMessage( ) );
		}
	}
	
	public void log( String msg )
	{
		if( mLogFile == null )
			return;
		
		try
		{
			mLogFile.write( msg );
			mLogFile.newLine( );
		} 
		catch( IOException e ) 
		{
			Log.e( ScooterData.TAG, "Could not write file " + e.getMessage( ) );
		}
	}
	
	public void flush( )
	{
		if( mLogFile == null )
			return;
		
		try
		{
			mLogFile.flush( );
		} 
		catch( IOException e ) 
		{
			Log.e( ScooterData.TAG, "Could not write file " + e.getMessage( ) );
		}
	}
	
	public void close( )
	{
		if( mLogFile == null )
			return;
		
		try
		{
			mLogFile.close( );
			mLogFile = null;
		} 
		catch( IOException e ) 
		{
			Log.e( ScooterData.TAG, "Could not write file " + e.getMessage( ) );
		}
	}
}
