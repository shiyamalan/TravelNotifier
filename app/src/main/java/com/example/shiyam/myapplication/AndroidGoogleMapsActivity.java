package com.example.shiyam.myapplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.shiyam.myapplication.models.PlaceArrayAdapter;
import com.example.shiyam.myapplication.models.StaticData;
import com.example.shiyam.myapplication.network.connection.ConnectionDetector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class AndroidGoogleMapsActivity extends FragmentActivity implements
		GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks{

	// Google Map
	private GoogleMap googleMap;
	private HashMap<CustomMarker, Marker> markersHashMap;
	private Iterator<Entry<CustomMarker, Marker>> iter;
	private CameraUpdate cu;
	private CustomMarker customMarkerOne, customMarkerTwo;
	private static final String LOG_TAG = "MainActivity";
	private static final int GOOGLE_API_CLIENT_ID = 0;
	private AutoCompleteTextView mAutocompleteStartTextView;
	private AutoCompleteTextView mAutocompleteEndTextView;
	private GoogleApiClient mGoogleApiClient;
	private PlaceArrayAdapter mPlaceArrayAdapter;
	private View selectedView;

	private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
			new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_root);

		StaticData.current_context = getApplicationContext();
		try {


			boolean isInternetPresent = ConnectionDetector.isConnectingToInternet();
			if (!isInternetPresent) {
				// Alert Dialog Manager
//				AlertDialogManager alert = new AlertDialogManager();
				// Internet Connection is not present
//				alert.showAlertDialog(StaticData.current_context, "Internet Connection Error",
//						"Please connect to working Internet connection", false);
				// stop executing code by return
//				return;
			}
//
//			// creating GPS Class object
//			gps = new GPSTracker(this);
//
//			// check if GPS location can get
//			if (gps.canGetLocation()) {
//				Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
//			} else {
//				// Can't get user's current location
//				alert.showAlertDialog(StaticData.current_context, "GPS Status",
//						"Couldn't get location information. Please enable GPS",
//						false);
//				// stop executing code by return
//				return;
//			}
			// Loading map
			initilizeMap();
			initializeUiSettings();
			initializeMapLocationSettings();
			initializeMapTraffic();
			initializeMapType();
			initializeMapViewSettings();


			// Check if Internet present


		} catch (Exception e) {
			e.printStackTrace();

			Log.i(LOG_TAG, "On Create " + e.toString());
		}
	}
	private AdapterView.OnItemClickListener mAutocompleteClickListener
			= new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
			final String placeId = String.valueOf(item.placeId);
			Log.i(LOG_TAG, "Selected: " + item.description);
			PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
					.getPlaceById(mGoogleApiClient, placeId);
			placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
			Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
		}
	};

	private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
			= new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(PlaceBuffer places) {
			if (!places.getStatus().isSuccess()) {
				Log.e(LOG_TAG, "Place query did not complete. Error: " +
						places.getStatus().toString());
				return;
			}
			// Selecting the first object buffer.

			final Place place = places.get(0);
			CharSequence attributions = places.getAttributions();
			String place_name = place.getName().toString();
			double place_latitude = place.getLatLng().latitude;
			double place_longitude = place.getLatLng().longitude;


			if(selectedView !=null && selectedView.getId()
					== R.id.inputStart){
					setCustomMarkerOnePosition(place_name,
							place_latitude,place_longitude);
			}else if(selectedView !=null && selectedView.getId()
					== R.id.inputStart){
				setCustomMarkerTwoPosition(place_name,
						place_latitude,place_longitude);
			}
			startAnimation();
		}
	};

	@Override
	public void onConnected(Bundle bundle) {
		mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
		Log.i(LOG_TAG, "Google Places API connected.");

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(LOG_TAG, "Google Places API connection failed with error code: "
				+ connectionResult.getErrorCode());

		Toast.makeText(this,
				"Google Places API connection failed with error code:" +
						connectionResult.getErrorCode(),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnectionSuspended(int i) {
		mPlaceArrayAdapter.setGoogleApiClient(null);
		Log.e(LOG_TAG, "Google Places API connection suspended.");
	}
	@Override
	protected void onResume() {
		super.onResume();
		// initilizeMap();
	}

	private void initilizeMap() {

		googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getMap();

		// check if map is created successfully or not
		if (googleMap == null) {
			Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}

		(findViewById(R.id.mapFragment)).getViewTreeObserver().addOnGlobalLayoutListener(
				new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						// gets called after layout has been done but before
						// display
						// so we can get the height then hide the view
						if (android.os.Build.VERSION.SDK_INT >= 16) {
							(findViewById(R.id.mapFragment)).getViewTreeObserver().removeOnGlobalLayoutListener(this);
						} else {
							(findViewById(R.id.mapFragment)).getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
				});
	}

	void setCustomMarkerOnePosition(String placeName,double latitude,double longitude){
		customMarkerOne = new CustomMarker(placeName, latitude, longitude);
		addMarker(customMarkerOne);
	}

	void setCustomMarkerTwoPosition(String placeName,double latitude,double longitude) {
		customMarkerTwo = new CustomMarker(placeName, latitude, longitude);
		addMarker(customMarkerTwo);
	}

	public void startAnimation() {

		if(customMarkerOne != null)
			animateMarker(customMarkerOne, new LatLng(customMarkerOne.getCustomMarkerLatitude(),
				customMarkerOne.getCustomMarkerLongitude()));
		if(customMarkerTwo != null)
			animateMarker(customMarkerTwo, new LatLng(customMarkerTwo.getCustomMarkerLatitude(),
				customMarkerTwo.getCustomMarkerLongitude()));

		if(customMarkerOne != null && customMarkerTwo != null){
			zoomToMarkers();
		}
	}

	public void zoomToMarkers() {
		zoomAnimateLevelToFitMarkers(120);
	}

	public void animateBack(View v) {
		animateMarker(customMarkerOne, new LatLng(32.0675716, 27.7297251));
	}

	public void initializeUiSettings() {
		googleMap.getUiSettings().setCompassEnabled(true);
		googleMap.getUiSettings().setRotateGesturesEnabled(false);
		googleMap.getUiSettings().setTiltGesturesEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		mGoogleApiClient = new GoogleApiClient.Builder(StaticData.current_context)
				.addApi(Places.GEO_DATA_API)
				.enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
				.addConnectionCallbacks(this)
				.build();
		mAutocompleteStartTextView = (AutoCompleteTextView) findViewById(R.id
				.inputStart);

		mAutocompleteEndTextView = (AutoCompleteTextView) findViewById(R.id
				.inputEnd);

		mAutocompleteStartTextView.setOnItemClickListener(mAutocompleteClickListener);
		mAutocompleteStartTextView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				selectedView = mAutocompleteStartTextView;
				return false;
			}
		});
		mAutocompleteEndTextView.setOnItemClickListener(mAutocompleteClickListener);
		mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,
				BOUNDS_MOUNTAIN_VIEW, null);
		mAutocompleteStartTextView.setAdapter(mPlaceArrayAdapter);
		mAutocompleteEndTextView.setAdapter(mPlaceArrayAdapter);
		mAutocompleteEndTextView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				selectedView = mAutocompleteEndTextView;
				return false;
			}
		});
	}



	public void initializeMapLocationSettings() {

		//googleMap.setMyLocationEnabled(true);
	}

	public void initializeMapTraffic() {
		googleMap.setTrafficEnabled(true);
	}

	public void initializeMapType() {
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	}

	public void initializeMapViewSettings() {
		googleMap.setIndoorEnabled(true);
		googleMap.setBuildingsEnabled(false);
	}

	// this is method to help us set up a Marker that stores the Markers we want
	// to plot on the map
	public void setUpMarkersHashMap() {
		if (markersHashMap == null) {
			markersHashMap = new HashMap<CustomMarker, Marker>();
		}
	}

	// this is method to help us add a Marker into the hashmap that stores the
	// Markers
	public void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
		setUpMarkersHashMap();
		markersHashMap.put(customMarker, marker);
	}

	// this is method to help us find a Marker that is stored into the hashmap
	public Marker findMarker(CustomMarker customMarker) {
		if(markersHashMap != null) {
			iter = markersHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry mEntry = (Entry) iter.next();
				CustomMarker key = (CustomMarker) mEntry.getKey();
				if (customMarker.getCustomMarkerId().equals(key.getCustomMarkerId())) {
					Marker value = (Marker) mEntry.getValue();
					return value;
				}
			}
		}
		return null;
	}

	// this is method to help us add a Marker to the map
	public void addMarker(CustomMarker customMarker) {
		MarkerOptions markerOption = new MarkerOptions().position(
				new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude())).icon(
				BitmapDescriptorFactory.defaultMarker());

		Marker newMark = googleMap.addMarker(markerOption);
		addMarkerToHashMap(customMarker, newMark);
	}

	// this is method to help us remove a Marker
	public void removeMarker(CustomMarker customMarker) {
		if (markersHashMap != null) {
			if (findMarker(customMarker) != null) {
				findMarker(customMarker).remove();
				markersHashMap.remove(customMarker);
			}
		}
	}

	// this is method to help us fit the Markers into specific bounds for camera
	// position
	public void zoomAnimateLevelToFitMarkers(int padding) {
		LatLngBounds.Builder b = new LatLngBounds.Builder();
		iter = markersHashMap.entrySet().iterator();

		while (iter.hasNext()) {
			Entry mEntry = (Entry) iter.next();
			CustomMarker key = (CustomMarker) mEntry.getKey();
			LatLng ll = new LatLng(key.getCustomMarkerLatitude(), key.getCustomMarkerLongitude());
			b.include(ll);
		}
		LatLngBounds bounds = b.build();

		// Change the padding as per needed
		cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
		googleMap.animateCamera(cu);
	}

	// this is method to help us move a Marker.
	public void moveMarker(CustomMarker customMarker, LatLng latlng) {
		if (findMarker(customMarker) != null) {
			findMarker(customMarker).setPosition(latlng);
			customMarker.setCustomMarkerLatitude(latlng.latitude);
			customMarker.setCustomMarkerLongitude(latlng.longitude);
		}
	}

	// this is method to animate the Marker. There are flavours for all Android
	// versions
	public void animateMarker(CustomMarker customMarker, LatLng latlng) {
		if (findMarker(customMarker) != null) {

			LatLngInterpolator latlonInter = new LatLngInterpolator.LinearFixed();
			latlonInter.interpolate(20,
					new LatLng(customMarker.getCustomMarkerLatitude(), customMarker.getCustomMarkerLongitude()), latlng);

			customMarker.setCustomMarkerLatitude(latlng.latitude);
			customMarker.setCustomMarkerLongitude(latlng.longitude);

			if (android.os.Build.VERSION.SDK_INT >= 14) {
				MarkerAnimation.animateMarkerToICS(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
						customMarker.getCustomMarkerLongitude()), latlonInter);
			} else if (android.os.Build.VERSION.SDK_INT >= 11) {
				MarkerAnimation.animateMarkerToHC(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
						customMarker.getCustomMarkerLongitude()), latlonInter);
			} else {
				MarkerAnimation.animateMarkerToGB(findMarker(customMarker), new LatLng(customMarker.getCustomMarkerLatitude(),
						customMarker.getCustomMarkerLongitude()), latlonInter);
			}
		}
	}



}
