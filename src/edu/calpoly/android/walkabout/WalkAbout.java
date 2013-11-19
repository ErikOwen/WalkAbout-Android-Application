package edu.calpoly.android.walkabout;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Activity that contains an interactive Google Map fragment. Users can record
 * a traveled path, mark the map with information and take pictures that become
 * associated with the map.
 */
public class WalkAbout extends SherlockFragmentActivity implements android.location.LocationListener {

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
	
	private static String timestamp;
	
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
    	
    	this.m_arrPathPoints = new ArrayList<LatLng>();
    	
    	this.m_bRecording = false;
    	
    	this.m_arrPicturePoints = new ArrayList<Marker>();
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
		MenuItem pictureOption = menu.findItem(R.id.menu_takePicture);
		MenuItem gpsEnabledItem = menu.findItem(R.id.menu_enableGPS);
		MenuItem startStopItem = menu.findItem(R.id.menu_recording);
		
		if (this.m_locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			gpsEnabledItem.setVisible(false);
		}
		else {
			startStopItem.setVisible(false);
			Toast.makeText(this, "Thinks the location provider is not enabled", Toast.LENGTH_LONG).show();
		}
		
		if (!this.m_bRecording) {
			pictureOption.setVisible(false);
			startStopItem.setTitle(R.string.menuTitle_startRecording);
		}
		else {
			pictureOption.setVisible(true);
			startStopItem.setTitle(R.string.menuTitle_stopRecording);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case R.id.menu_recording:
			if (this.m_bRecording) {
				setRecordingState(!this.m_bRecording);
			}
			else {
				setRecordingState(!this.m_bRecording);
			}
			supportInvalidateOptionsMenu();
			Toast.makeText(this, "Record button hit.", Toast.LENGTH_SHORT).show();
		break;
		case R.id.menu_save:
			Toast.makeText(this, "Save button hit.", Toast.LENGTH_SHORT).show();
		break;
		case R.id.menu_load:
			Toast.makeText(this, "Load button hit.", Toast.LENGTH_SHORT).show();
		break;
		case R.id.menu_takePicture:
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Uri cameraUri = getOutputMediaFileUri(PICTURE_REQUEST_CODE);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
			startActivityForResult(cameraIntent, WalkAbout.PICTURE_REQUEST_CODE);
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
		if (requestCode == WalkAbout.PICTURE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, getResources().getString(R.string.pictureSuccess), Toast.LENGTH_SHORT).show();
				Location markerLocation = this.m_locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				LatLng markerCoordinates = new LatLng(markerLocation.getLatitude(), markerLocation.getLongitude());
				Marker picMarker = this.m_vwMap.addMarker(new MarkerOptions()
					.position(markerCoordinates)
					.title(this.timestamp));
				this.m_arrPicturePoints.add(picMarker);
			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, getResources().getString(R.string.pictureFail), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * Switch the application so it is or isn't recording the user's path on the map.
	 *  
	 * @param bRecording
	 * 						Whether or not to start recording.
	 */
	private void setRecordingState(boolean bRecording) {
		this.m_bRecording = bRecording;
		
		if (bRecording) {
			this.m_arrPathPoints.clear();
			this.m_arrPicturePoints.clear();
			this.m_vwMap.clear();
			
			this.m_pathLine = this.m_vwMap.addPolyline(new PolylineOptions());
			this.m_pathLine.setColor(Color.GREEN);
			
			this.onLocationChanged(this.m_locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
			this.m_locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, WalkAbout.MIN_TIME_CHANGE, WalkAbout.MIN_DISTANCE_CHANGE, this);
		}
		else {
			this.m_locManager.removeUpdates(this);
		}
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

	@Override
	public void onLocationChanged(Location location) {
		LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
		this.m_arrPathPoints.add(coordinates);
		this.m_pathLine.setPoints(this.m_arrPathPoints);
		CircleOptions circOpts = new CircleOptions();
		circOpts.center(coordinates);
		circOpts.radius(WalkAbout.CIRCLE_RADIUS);
		circOpts.fillColor(Color.CYAN);
		circOpts.strokeColor(Color.BLUE);
		this.m_vwMap.addCircle(circOpts);
		
		this.m_vwMap.animateCamera(CameraUpdateFactory.newLatLng(coordinates));
	}

	@Override
	public void onProviderDisabled(String provider) {
		setRecordingState(false);
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		//Functionality not needed for this project
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//Functionality not needed for this project
		
	}
	
	private static File getOutputMediaFile(int fileType) {
		File mediaFile = null;
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WalkAbout");
		
		if (!mediaStorageDir.exists()) {
			mediaStorageDir.mkdirs();
			if (!mediaStorageDir.exists()) {
				Log.w("edu.calpoly.android.walkabout", "Directory creation process failed.");
				return null;
			}
		}
		
		String dateString = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
		timestamp = dateString;
		
		if (fileType == PICTURE_REQUEST_CODE) {
			String pathString = mediaStorageDir.getPath() + File.separator + "IMG_" + dateString + ".jpg";
			pathString = pathString.replace(",", "");
			pathString = pathString.replace(" ", "");
			pathString = pathString.replace(":", "");
			Log.w("WalkAbout", "File path to image is: " + pathString);
			mediaFile = new File(pathString);
		}
		
		return mediaFile;
	}
	
	private static Uri getOutputMediaFileUri(int fileType) {
		File tempFile = getOutputMediaFile(fileType);
		
		return Uri.fromFile(tempFile);
	}
}