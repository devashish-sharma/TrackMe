package h.devashishsharma.currentlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    ProgressBar progressBar;
    Connection con = null;
    PreparedStatement statement;
    String selectedcustomerid;
    String loc;
    ArrayList<Customer> itemList;
    Spinner spinner;
    private String sql_username = "TrackMe";
    String sql_customername;
    String databasename, ipaddress, userpass;
    Button updatelocation;
    View view;
    String ConnURL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        SELECTAsync selectAsync = new SELECTAsync();
        selectAsync.execute(" ");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, new IntentFilter("LAT_LONG"));
        final Button getuserlist = view.findViewById(R.id.btn1);
        updatelocation = view.findViewById(R.id.btn2);
        progressBar = view.findViewById(R.id.pb);
        itemList = new ArrayList<Customer>();
        spinner = view.findViewById(R.id.spinner);
        final Button showmap = view.findViewById(R.id.showmap);
        showmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!enabled) {
                    Toast.makeText(getContext(), "Please Enable Location Services", Toast.LENGTH_SHORT).show();
                    intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                } else {
                    intent = new Intent(getContext(), MapsActivity.class);
                    startActivity(intent);
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Customer swt = (Customer) adapterView.getItemAtPosition(i);
                selectedcustomerid = swt.customerID;
                sql_customername = swt.customerName;
                Toast.makeText(getContext(), "" + sql_customername, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getContext(), "Select Any of them", Toast.LENGTH_SHORT).show();
            }
        });

        getuserlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ArrayAdapter<Customer> adapter = new ArrayAdapter<Customer>(getContext(), R.layout.spinner_item, itemList);
                    adapter.setDropDownViewResource(R.layout.spinner_item);
                    spinner.setAdapter(adapter);
                    Toast.makeText(getContext(), "Data Retrieved Successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("here", "Exception occur in Get User List Button " + e.getMessage());
                }
            }
        });
        updatelocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedcustomerid == null) {
                    Toast.makeText(getContext(), "Customer ID is NULL ", Toast.LENGTH_LONG).show();
                    Snackbar.make(view, "!!! Select Customer First !!!", Snackbar.LENGTH_LONG).show();
                } else {
                    new UPDATEAsync().execute();
                    Snackbar.make(view, "Data is Successfully Updated of user " + sql_customername, Snackbar.LENGTH_SHORT).show();
                    Log.d("here", "Data is updated successfully for " + sql_customername);
                }
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String receivedlat = intent.getStringExtra("lats");
                String receivedlong = intent.getStringExtra("longs");
                String receivedaddress = intent.getStringExtra("address");
                TextView textView = (TextView) view.findViewById(R.id.getlatlong);
                loc = receivedlat + "," + receivedlong;
                textView.setText(receivedlat + " " + receivedlong + "\n" + receivedaddress);
                if (textView != null) {
                    updatelocation.setEnabled(true);
                    updatelocation.setBackground(getResources().getDrawable(R.drawable.btn_effect));
                } else {
                    updatelocation.setEnabled(false);
                    updatelocation.setBackgroundColor(Color.GRAY);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    class SELECTAsync extends AsyncTask<String, String, String> {
        @Override
        public String doInBackground(String... strings) {
            try {
                userpass = getActivity().getIntent().getExtras().getString("userpass");
                ipaddress = getActivity().getIntent().getStringExtra("ip");
                databasename = getActivity().getIntent().getStringExtra("db");
                Log.d("here", userpass + " " + ipaddress + " " + databasename);
                ConnURL = "jdbc:jtds:sqlserver://" + ipaddress + ":1433;" + "databaseName=" + databasename + ";user=" + sql_username + ";password="
                        + userpass + ";";
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                con = DriverManager.getConnection(ConnURL);
                if (con != null) {
                    ResultSet resultSet = null;
                    String query = "SELECT [CustomerID],[Title] FROM [FTCustomer]";
                    statement = con.prepareStatement(query);
                    resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        String key = resultSet.getString("CustomerID");
                        String value = resultSet.getString("Title");
                        itemList.add(new Customer(key, value));
                        Log.d("here", ConnURL);
                        Log.d("here", key + " " + value + " \n ");
                    }
                    con.close();
                    Log.d("here", "Connection Success ");
                } else {
                    Log.d("here", "Connection Failed ");
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("here", "Class not found in SELECT statement " + e.getMessage());

            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("here", "SQL Exception occur in SELECT query " + e.getMessage() + " " + e.getSQLState() + " " + e.getErrorCode());
            }
            return null;
        }
    }

    class UPDATEAsync extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                ConnURL = "jdbc:jtds:sqlserver://" + ipaddress + ":1433;" + "databaseName=" + databasename + ";user=" + sql_username + ";password="
                        + userpass + ";";
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                con = DriverManager.getConnection(ConnURL);
                if (con != null) {
                    String updatequery = "UPDATE [FTCustomer] SET [GeoLocation]='" + loc + "' WHERE [CustomerID]='" + selectedcustomerid + "'";
                    statement = con.prepareStatement(updatequery);
                    statement.executeUpdate();
                    Log.d("here", ConnURL);
                    Log.d("here", "Data is successfully Updated--: " + statement);
                } else {
                    Log.d("here", "Connection is null here");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("here", "SQL exception occur in update query");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("here", "Class not found in UPDATE statement " + e.getMessage());
            }
            return null;
        }
    }

    private static class Customer {
        public String customerID;
        public String customerName;

        public Customer(String cusID, String cusName) {
            this.customerID = cusID;
            this.customerName = cusName;
        }

        @Override
        public String toString() {
            return customerName;
        }
    }
}