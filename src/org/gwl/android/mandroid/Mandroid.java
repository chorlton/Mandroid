package org.gwl.android.mandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class Mandroid extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        		
		View v = new MandroidView(this);
        setContentView(v);
    }
}