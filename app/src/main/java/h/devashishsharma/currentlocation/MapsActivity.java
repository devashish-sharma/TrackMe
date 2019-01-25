package h.devashishsharma.currentlocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mygoogleMap;
    private GoogleApiClient googleApiClient;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;
    private LatLng latLng;
    View mapView;
    View locationButton;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }
        setContentView(R.layout.activity_maps);
//        ActionBar actionBar=getSupportActionBar();
//        actionBar.hide();
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        AutocompleteFilter filter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                .build();
        autocompleteFragment.setFilter(filter);
        autocompleteFragment.setHint("Search Your Location");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mygoogleMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(String.valueOf(place.getAddress()))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
                mygoogleMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mygoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12.0f));
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this, "Error in PlaceAutoCompleteFragment " + status, Toast.LENGTH_SHORT).show();
            }
        });

        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, "Click on Marker to Send Coordinate to MainActivity", Snackbar.LENGTH_LONG).show();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        final Switch aSwitch = findViewById(R.id.onoff);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                if (ischecked) {
                    try {
                        Toast.makeText(MapsActivity.this, "Night Mode Enabled", Toast.LENGTH_SHORT).show();
                        ((SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map)).getMapAsync(MapsActivity.this);
                        mygoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.mapstyle_night));
                    } catch (Exception e) {
                        Log.d("myDrag", "Night Mode " + e.getMessage());
                    }
                } else {
                    try {
                        Toast.makeText(MapsActivity.this, "Night Mode Disabled", Toast.LENGTH_SHORT).show();
                        mygoogleMap.setMapStyle(null);
                    } catch (Exception e) {
                        Log.d("myDrag", "Day Mode " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mygoogleMap = googleMap;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(true);
                googleMap.getUiSettings().setTiltGesturesEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(true);
                googleMap.getUiSettings().setScrollGesturesEnabled(true);
                googleMap.getUiSettings().setAllGesturesEnabled(true);
                googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
                googleMap.setTrafficEnabled(true);
                if (mapView != null &&
                        mapView.findViewById(Integer.parseInt("1")) != null) {
                    // Get the button view
                    locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                    // and next place it, on bottom right (as Google Maps app)
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                            locationButton.getLayoutParams();
                    // position on right bottom
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    layoutParams.setMargins(0, 0, 30, 350);
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        mygoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.e("mapDrag", "DragStart : " + marker.getPosition());
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                Toast.makeText(MapsActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDrag(final Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                try {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    Log.e("mapDrag", "DragStart : " + marker.getPosition());
                    LatLng position = marker.getPosition();
                    Address filterAddress;
                    Geocoder geoCoder = new Geocoder(
                            getBaseContext(), Locale.getDefault());
                    List<Address> addresses = geoCoder.getFromLocation(
                            position.latitude,
                            position.longitude, 1);
                    filterAddress = addresses.get(0);
                    marker.setTitle(filterAddress.getAddressLine(0));
                    marker.setSnippet(String.valueOf(position.latitude + " , " + position.longitude));
                    marker.showInfoWindow();
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, " " + filterAddress.getAddressLine(0), Snackbar.LENGTH_SHORT).show();
                    Log.d("myDrag", String.valueOf(position) + filterAddress.getAddressLine(0));
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });
        mygoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                return false;
            }
        });
        mygoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                locationButton.setBackgroundColor(Color.TRANSPARENT);
                Toast.makeText(MapsActivity.this, "" + latLng, Toast.LENGTH_SHORT).show();
            }
        });
        mygoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("!!! Instruction !!!")
                        .setMessage("Please Enable Location Services at High Accuracy and Internet Connection if your location not Fetching." +
                                "\n\nThank You\nFenestec Technologies Pvt. Ltd.")
                        .setCancelable(false)
                        .setPositiveButton("Got It!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(MapsActivity.this, "Thank You ", Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        mygoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onMyLocationButtonClick() {
                try {
                    locationButton.setDefaultFocusHighlightEnabled(true);
                    locationButton.setBackgroundColor(Color.BLUE);
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        mygoogleMap.setMyLocationEnabled(true);
                    }
                    Address filterAddress;
                    Geocoder geoCoder = new Geocoder(
                            getBaseContext(), Locale.getDefault());
                    List<Address> addresses = geoCoder.getFromLocation(
                            latLng.latitude,
                            latLng.longitude, 1);
                    filterAddress = addresses.get(0);
                    currentUserLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    currentUserLocationMarker.setPosition(latLng);
                    currentUserLocationMarker.setTitle("You are Here");
                    currentUserLocationMarker.setSnippet(String.valueOf(latLng.latitude + " ," + latLng.longitude));
                    currentUserLocationMarker.showInfoWindow();
                    View parentLayout = findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "" + filterAddress.getAddressLine(0), Snackbar.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("here", " " + e.getMessage());
                }
                return false;
            }
        });
    }

    public boolean checkUserLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient();//create new client map
                        }
                        mygoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(final Location location) {
        Location lastlocation = location;
        latLng = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
        if (currentUserLocationMarker != null) {
            currentUserLocationMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("You are Here");
        markerOptions.snippet(latLng.latitude + "," + latLng.longitude);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Log.d("myDrag", Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()));
        currentUserLocationMarker = mygoogleMap.addMarker(markerOptions);
        mygoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mygoogleMap.animateCamera(CameraUpdateFactory.zoomBy(12));
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
        mygoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker3) {
                try {
                    LatLng mypos = marker3.getPosition();
                    Geocoder geoCoder = new Geocoder(
                            getBaseContext(), Locale.getDefault());
                    List<Address> addresses = geoCoder.getFromLocation(
                            mypos.latitude,
                            mypos.longitude, 1);
                    String locationlat = String.valueOf(mypos.latitude);
                    String locationlong = String.valueOf(mypos.longitude);
                    Intent intent = new Intent("LAT_LONG")
                            .putExtra("lats", locationlat)
                            .putExtra("longs", locationlong)
                            .putExtra("address", addresses.get(0).getAddressLine(0));
                    LocalBroadcastManager.getInstance(MapsActivity.this).sendBroadcast(intent);
                    onBackPressed();
                    Toast.makeText(MapsActivity.this, "Location Updates Send to MainActivity " + locationlat + " " + locationlong, Toast.LENGTH_SHORT).show();
                    Log.d("myDrag", String.valueOf(locationlat + " , " + locationlong));

                } catch (Exception e) {
                    Log.d("here", "Main Exception " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "Connection suspended", Toast.LENGTH_LONG).show();
        googleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Connection failed: " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }
}