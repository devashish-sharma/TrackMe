package h.devashishsharma.currentlocation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class CustomerFragment extends Fragment {
    View view;
    ListView listView;
    private MyAppAdapter myAppAdapter;
    ArrayList<CustomerItem> customerItems;
    String sql_username = "TrackMe";

    public CustomerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_customer, container, false);
        String[] arr = {"dev", "Deva", "devu"};

        listView = (ListView) view.findViewById(R.id.customerlist);

        customerItems = new ArrayList<CustomerItem>();
        SELECTAsync selectAsync = new SELECTAsync();
        selectAsync.execute("");
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                } catch (Exception e) {
                    Log.d("here", "Check " + e.getMessage());
                }
            }
        });
        return view;
    }

    private class SELECTAsync extends AsyncTask<String, String, String> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getContext(), "Loading Data...", "Please wait for moment and Do not exit Application...", true);
        }

        @Override
        protected void onPostExecute(String s) {
            myAppAdapter = new MyAppAdapter(customerItems, getContext());
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setAdapter(myAppAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String Customername = adapterView.getItemAtPosition(i).toString();
                    Toast.makeText(getContext(), "" + Customername, Toast.LENGTH_SHORT).show();
                }
            });
            progress.dismiss();
        }

        @Override
        public String doInBackground(String... strings) {
            try {
                String userpass = getActivity().getIntent().getExtras().getString("userpass");
                String ipaddress = getActivity().getIntent().getStringExtra("ip");
                String databasename = getActivity().getIntent().getStringExtra("db");
                String ConnURL = "jdbc:jtds:sqlserver://" + ipaddress + ":1433;" + "databaseName=" + databasename + ";user=" + sql_username + ";password="
                        + userpass + ";";
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection con = DriverManager.getConnection(ConnURL);
                if (con != null) {
                    ResultSet resultSet = null;
                    String query = "SELECT [CustomerID],[Title],[GeoLocation],[ContactNumber],[Email],[InvAddressLine1],[InvAddressLine2],[Country],[State],[City],[Pincode] FROM [FT_V_Customer]";
                    Statement statement = con.createStatement();
                    resultSet = statement.executeQuery(query);
                    while (resultSet.next()) {
                        String address1 = resultSet.getString("InvAddressLine1");
                        String address2 = resultSet.getString("InvAddressLine2");
                        String country = resultSet.getString("Country");
                        String state = resultSet.getString("State");
                        String city = resultSet.getString("City");
                        String pincode = resultSet.getString("Pincode");
                        String fulladdress = address1 + " " + address2 + " " + city + ", " + state + ", " + country + ", " + pincode;
                        customerItems.add(new CustomerItem(
                                resultSet.getString("CustomerID"),
                                resultSet.getString("Title"),
                                resultSet.getString("GeoLocation"),
                                fulladdress,
                                resultSet.getString("ContactNumber"),
                                resultSet.getString("Email"))
                        );
                    }
                    Log.d("here", "Connection Success ");
                    con.close();
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

    public class MyAppAdapter extends BaseAdapter {
        public class ViewHolder {
            TextView id, name, lat, address, contact, email;
        }

        public List<CustomerItem> customerList;

        public Context context;
        ArrayList<CustomerItem> arraylist;

        private MyAppAdapter(List<CustomerItem> apps, Context context) {
            this.customerList = apps;
            this.context = context;
            arraylist = new ArrayList<CustomerItem>();
            arraylist.addAll(customerList);
        }

        @Override
        public int getCount() {
            return customerList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            ViewHolder viewHolder = null;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.item_customer, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.id = (TextView) rowView.findViewById(R.id.id);
                viewHolder.name = (TextView) rowView.findViewById(R.id.name);
                viewHolder.lat = (TextView) rowView.findViewById(R.id.latlong1);
                viewHolder.address = (TextView) rowView.findViewById(R.id.address);
                viewHolder.contact = (TextView) rowView.findViewById(R.id.contact);
                viewHolder.email = (TextView) rowView.findViewById(R.id.email);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.id.setText(customerList.get(position).getCusid());
            viewHolder.name.setText(customerList.get(position).getTitle());
            viewHolder.lat.setText(customerList.get(position).getLatlongs());
            viewHolder.address.setText(customerList.get(position).getAddress());
            viewHolder.contact.setText(customerList.get(position).getContact());
            viewHolder.email.setText(customerList.get(position).getEmail());
            return rowView;
        }
    }
}