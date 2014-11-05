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
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class NumericPreference extends EditTextPreference {
	
	int mMaxLength = Integer.MAX_VALUE;

	public NumericPreference( Context context ) {
		
		super( context );
		// TODO Auto-generated constructor stub
	}

	public NumericPreference( Context context, AttributeSet attrs ) {
		
		super( context, attrs );

		Initialize( context, attrs );
	}

	public NumericPreference( Context context, AttributeSet attrs, int defStyle ) {
		
		super( context, attrs, defStyle );

		Initialize( context, attrs );
	}

	protected void Initialize( Context context, AttributeSet attrs ) {
		
		TypedArray a = context.obtainStyledAttributes( attrs,	R.styleable.NumericPreference );
		mMaxLength = a.getInt( R.styleable.NumericPreference_textLength, Integer.MAX_VALUE );
	}

	@Override
	protected void onAddEditTextToDialogView( View dialogView, EditText editText ) {
		
		editText.setKeyListener( DigitsKeyListener.getInstance( ) );
		editText.setFilters( new InputFilter[ ] { new InputFilter.LengthFilter(	mMaxLength ) } );

		super.onAddEditTextToDialogView( dialogView, editText );
	}
}
