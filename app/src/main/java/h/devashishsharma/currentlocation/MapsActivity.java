package h.devashishsharma.currentlocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
    LatLng latLng;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }
        setContentView(R.layout.activity_maps);
        View parentLayout = findViewById(android.R.id.content);
        final Snackbar snackbar = Snackbar.make(parentLayout, "Click on Marker to Send Coordinate to MainActivity", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //LocationButton Alignment
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 350);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mygoogleMap = googleMap;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mygoogleMap.setMyLocationEnabled(true);
                mygoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mygoogleMap.getUiSettings().setZoomControlsEnabled(true);
                mygoogleMap.getUiSettings().setZoomGesturesEnabled(true);
                mygoogleMap.getUiSettings().setCompassEnabled(true);
                mygoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mygoogleMap.getUiSettings().setRotateGesturesEnabled(true);
                mygoogleMap.getUiSettings().setMapToolbarEnabled(true);
                mygoogleMap.getUiSettings().setTiltGesturesEnabled(true);
                mygoogleMap.getUiSettings().setMapToolbarEnabled(true);
                mygoogleMap.getUiSettings().setScrollGesturesEnabled(true);
                mygoogleMap.getUiSettings().setAllGesturesEnabled(true);
                mygoogleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
                mygoogleMap.setPadding(12, 22, 12, 12);
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
                    marker.setSnippet(String.valueOf(position.latitude + " , " + position.longitude));
                    marker.showInfoWindow();
                    Toast.makeText(MapsActivity.this, "" + filterAddress.getAddressLine(0), Toast.LENGTH_SHORT).show();
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
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                return false;
            }
        });
        mygoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MapsActivity.this, "" + latLng, Toast.LENGTH_SHORT).show();
            }
        });
        mygoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                try {
                    currentUserLocationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    currentUserLocationMarker.setPosition(latLng);
                    currentUserLocationMarker.setSnippet(String.valueOf(latLng.latitude + " ," + latLng.longitude));
                    currentUserLocationMarker.showInfoWindow();
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {

            case R.id.item1:
                builder.setMessage("This application is developed by DEVASHISH SHARMA, under the supervision of 'Fenestec Technologies Pvt. Ltd.'\n" +
                        "Any Copy or Modification in content of this application may illegal." + "\n\n" + "Thank You\nDevashish Sharma")
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_insert_comment_black_24dp)
                        .setTitle("Disclaimer")
                        .setPositiveButton("OK Got It !!!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getApplicationContext(), "Your have readed Disclaimer",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
                break;
            case R.id.item2:
                builder.setMessage("Do you want to close application now!!!")
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_close_black_24dp)
                        .setTitle("!!! Close Application !!!")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Toast.makeText(getApplicationContext(), "Thank you For using application",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(), "Welcome Back",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
                break;
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}