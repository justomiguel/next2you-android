package com.globant.next2you.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globant.next2you.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class FragmentMap extends BaseFragment {
	private GoogleMap map;
	private SupportMapFragment mMapFragment;
	private static int EARTH_RADIUS = 6371000;
	
    public FragmentMap() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);   
        
        mMapFragment = new SupportMapFragment() {
            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                map = mMapFragment.getMap();
                if (map != null) {
                	try {
            			MapsInitializer.initialize(getActivity());
            		} catch (GooglePlayServicesNotAvailableException e) {
            			log(e);
            		}
                    LatLng pos = new LatLng(43, 25);
            		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pos, 14);
            		if(map != null) {
            			map.moveCamera(cameraUpdate);
            		}
                    
            		Resources res = getActivity().getResources();
            		int radius = (int) res.getDimension(R.dimen.map_my_location_radius);
            		drawCircle(pos, radius);
            		map.addMarker(
                            new MarkerOptions().position(pos)
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.marker_my_position)));
            		
                }
            }
        };
        
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_map_root, mMapFragment).commit();

        
        return rootView;
    }
    
    
    public Polygon drawCircle(LatLng center, int radius) {
        // Clear the map to remove the previous circle
        map.clear();
        // Generate the points
        List<LatLng> points = new ArrayList<LatLng>();
        int totalPonts = 120; // number of corners of the pseudo-circle
        for (int i = 0; i < totalPonts; i++) {
            points.add(getPoint(center, radius, i*2*Math.PI/totalPonts));
        }
        // Create and return the polygon
        Resources res = getActivity().getResources();
        return map.addPolygon(new PolygonOptions().addAll(points).fillColor(res.getColor(R.color.marker_radius_purple)).strokeWidth(0));
    }
    
    private LatLng getPoint(LatLng center, int radius, double angle) {
        // Get the coordinates of a circle point at the given angle
        double east = radius * Math.cos(angle);
        double north = radius * Math.sin(angle);

        double cLat = center.latitude;
        double cLng = center.longitude;
        double latRadius = EARTH_RADIUS * Math.cos(cLat / 180 * Math.PI);

        double newLat = cLat + (north / EARTH_RADIUS / Math.PI * 180);
        double newLng = cLng + (east / latRadius / Math.PI * 180);

        return new LatLng(newLat, newLng);
    }

	@Override
	protected String getLogTag() {
		return "FragmentMap";
	}

	
}
