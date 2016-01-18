package com.example.shiyam.myapplication;

import com.google.android.gms.maps.model.Marker;

public class CustomMarker {

	private String id;
	private Double latitude;
	private Double longitude;
	private Marker marker;

	public CustomMarker(String id, Double latitude, Double longitude) {

		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public CustomMarker() {
		this.id = "";
		this.latitude = 0.0;
		this.longitude = 0.0;
	}

	public String getCustomMarkerId() {
		return id;
	}

	public void setCustomMarkerId(String id) {
		this.id = id;
	}

	public Double getCustomMarkerLatitude() {
		return latitude;
	}

	public void setCustomMarkerLatitude(Double mLatitude) {
		this.latitude = mLatitude;
	}

	public Double getCustomMarkerLongitude() {
		return longitude;
	}

	public void setCustomMarkerLongitude(Double mLongitude) {
		this.longitude = mLongitude;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
}
