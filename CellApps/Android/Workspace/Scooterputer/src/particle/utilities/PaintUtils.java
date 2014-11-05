package particle.utilities;

import android.graphics.Paint;
import android.graphics.Rect;

public class PaintUtils
{
	public static int getTextHeight( Paint paint, String text )
	{
		Rect bounds = new Rect( );
		paint.getTextBounds( text, 0, text.length( ), bounds );
		
		return bounds.height( );
	}
	
	public static void getTextSize( Paint paint, String text, Size size )
	{
		Rect bounds = new Rect( );
		paint.getTextBounds( text, 0, text.length( ), bounds );

		size.Width = bounds.width( );
		size.Height = bounds.height( );
	}
}
