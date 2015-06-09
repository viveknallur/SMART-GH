package com.group17.smart_gh;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends ActionBarActivity{
	
	public final static String EXTRA_MESSAGE = "com.group17.smart_gh";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.typesOfRoutes, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/** Called when the user clicks the Search button */
	public void searchRoute(View view) throws Exception {
		
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		
	    // Do something in response to button
		EditText editText = (EditText) findViewById(R.id.editText1);
		String to = editText.getText().toString();
		Log.i("SearchRoute", to);
		
		editText = (EditText) findViewById(R.id.editText2);
		String from = editText.getText().toString();
		Log.i("SearchRoute", from);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		String routeSelected=spinner.getSelectedItem().toString();

		Log.i("SearchRoute",routeSelected);
		
		// hardcoded test.
		String temp = "http://172.16.160.130:8989/route/"
				//From
				+ "?point=53.340662%2C-6.243925"
				//To
				+ "&point=53.338305%2C-6.237595"
				//vehicle can be car, bike or foot.
				+ "&vehicle=foot"
				//weighting can be:least_noise,least_air_pollution,fastest or shortest.
				+ "&weighting=least_air_pollution"
				+ "&locale=en-US";
		
		requestRoute test = new requestRoute(temp);
		String route = test.sendRoute();
		
		Log.i("", route);
		
		requestRoute test2 = new requestRoute(53.340662, -6.243925, 53.338305, -6.237595, routeSelected, "foot");
		String route2 = test2.sendRoute();	
		Log.i("", route2);
		
		if(route.equals(route2)) Log.i("", "Strings are equal!");
		
		intent.putExtra(EXTRA_MESSAGE, route);
		startActivity(intent);
	}
}
