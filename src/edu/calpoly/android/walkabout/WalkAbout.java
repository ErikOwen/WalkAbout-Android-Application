package edu.calpoly.android.walkabout;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

/**
 * Activity that contains an interactive Google Map fragment. Users can record
 * a traveled path, mark the map with information and take pictures that become
 * associated with the map.
 */
public class WalkAbout extends SherlockFragmentActivity {

	/** The interactive Google Map fragment. */
	private GoogleMap m_vwMap;
	
	/** The list of locations, each having a latitude and longitude. */
	private ArrayList<LatLng> m_arrPathPoints;
	
	/** The list of markers, each having a latitude, longitude and title. */
	private ArrayList<Marker> m_arrPicturePoints;
	
	/** The continuous set of lines drawn between points on the map. */
	private Polyline m_pathLine;
	
	/** The Location Manager for the map. Used to obtain location status, etc. */
	private LocationManager m_locManager;
	
	/** Whether or not recording is currently in progress. */
	private boolean m_bRecording;

	/** The radius of a Circle drawn on the map, in meters. */
	private static final int CIRCLE_RADIUS = 30;
	
	/** Constants for the LocationManager. */
	private static final int MIN_TIME_CHANGE = 3000;
	private static final int MIN_DISTANCE_CHANGE = 3;
	
	/** Request codes for starting new Activities. */
	private static final int ENABLE_GPS_REQUEST_CODE = 1;
	private static final int PICTURE_REQUEST_CODE = 2;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLocationData();
        initLayout();
    }
    
    /**
     * Initializes all Location-related data.
     */
    private void initLocationData() {
    	this.m_locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    
    /**
     * Initializes all other data for the application.
     */
	private void initLayout() {
		setContentView(R.layout.map_layout);
		
		FragmentManager manager = getSupportFragmentManager();
		this.m_vwMap = ((SupportMapFragment) manager.findFragmentById(R.id.map)).getMap();
		if(this.m_vwMap != null) {
			UiSettings settings = this.m_vwMap.getUiSettings();
			settings.setZoomControlsEnabled(true);
			settings.setCompassEnabled(true);
			this.m_vwMap.setMyLocationEnabled(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.m_locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			MenuItem gpsEnabledItem = menu.findItem(R.id.menu_enableGPS);
			gpsEnabledItem.setVisible(false);
		}
		else {
			MenuItem startStopItem = menu.findItem(R.id.menu_recording);
			startStopItem.setVisible(false);
			Toast.makeText(this, "Thinks the location provider is not enabled", Toast.LENGTH_LONG).show();
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case R.id.menu_recording:
			Toast.makeText(this, "Record button hit.", Toast.LENGTH_SHORT).show();
		break;
		case R.id.menu_save:
			Toast.makeText(this, "Save button hit.", Toast.LENGTH_SHORT).show();
		break;
		case R.id.menu_load:
			Toast.makeText(this, "Load button hit.", Toast.LENGTH_SHORT).show();
		break;
		case R.id.menu_takePicture:
			Toast.makeText(this, "Take Picture button hit.", Toast.LENGTH_SHORT).show();
		break;
		case R.id.menu_enableGPS:
			Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(settingsIntent, WalkAbout.ENABLE_GPS_REQUEST_CODE);
			Toast.makeText(this, "Enable GPS button hit.", Toast.LENGTH_SHORT).show();
		break;
		}
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WalkAbout.ENABLE_GPS_REQUEST_CODE) {
	        supportInvalidateOptionsMenu();
	    }
	}
	
	/**
	 * Switch the application so it is or isn't recording the user's path on the map.
	 *  
	 * @param bRecording
	 * 						Whether or not to start recording.
	 */
	private void setRecordingState(boolean bRecording) {
		// TODO
	}
	
	/**
	 * Writes important map data to a private application file.
	 */
	private void saveRecording() {
		// TODO
	}
	
	/**
	 * Retrieves specific map data that was previously written to a private application file
	 * and initializes both the lists and the map with the new data.
	 */
	private void loadRecording() {
		// TODO
	}
}