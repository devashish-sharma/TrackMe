package h.devashishsharma.currentlocation;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginActivity extends AppCompatActivity {
    Button login, save;
    String passwordd;
    EditText ip_address, db_name, password;
    String[] ipdb_detail;
    private String sql_username = "TrackMe";
    String ipaddress, dbname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        save = findViewById(R.id.save);
        ip_address = findViewById(R.id.ipaddress);
        db_name = findViewById(R.id.dbname);
        password = findViewById(R.id.password);
        password.setBackgroundColor(Color.GRAY);
        loadfile();
        save.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                save.setText("Save Data");
                save.setBackgroundColor(Color.RED);
                save.setTextColor(Color.WHITE);
                login.setEnabled(false);
                login.setBackgroundColor(Color.GRAY);
                ip_address.setBackgroundColor(Color.WHITE);
                db_name.setBackgroundColor(Color.WHITE);
                ip_address.setEnabled(true);
                db_name.setEnabled(true);
                password.setEnabled(false);
                password.setBackgroundColor(Color.GRAY);
                return true;
            }
        });
    }

    @SuppressLint({"ResourceAsColor", "NewApi"})
    public void savefile(View view) {
        save.setText("Update Configurations");

        login.setEnabled(true);
        login.setBackground(getResources().getDrawable(R.drawable.btn_effect));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            save.setBackground(getResources().getDrawable(R.drawable.btn_effect));
        }
        password.setEnabled(true);
        password.setBackgroundColor(Color.WHITE);
        ip_address.setEnabled(false);
        db_name.setEnabled(false);
        ip_address.setBackgroundColor(Color.GRAY);
        db_name.setBackgroundColor(Color.GRAY);
        ipaddress = ip_address.getText().toString();
        dbname = db_name.getText().toString();
        if (ipaddress.isEmpty()) {
            ip_address.setError("IP Address Required");
        }
        if (dbname.isEmpty()) {
            db_name.setError("Enter Database name");
        }
        if (ipaddress.isEmpty() || dbname.isEmpty()) {
            password.setEnabled(false);
            password.setBackgroundColor(Color.GRAY);
        }
        try {
            FileOutputStream fOut = openFileOutput("mysdFile.txt", Context.MODE_PRIVATE);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(ipaddress + "\n");
            myOutWriter.append(dbname);
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getApplicationContext(), "Database Name and IP-Address saved'", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Press Long Tap on UPDATE CONFIGURATIONS", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("file", "File output stream releted exception " + e.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void loadfile() {
        try {
            FileInputStream fIn = openFileInput("mysdFile.txt");
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            while ((i = fIn.read()) != -1) {
                stringBuilder.append((char) i);
            }
            ipdb_detail = stringBuilder.toString().split("\n");
            ip_address.setText(ipdb_detail[0]);
            db_name.setText(ipdb_detail[1]);
            if (ip_address != null || db_name != null) {
                ip_address.setEnabled(false);
                db_name.setEnabled(false);
                ip_address.setBackgroundColor(Color.GRAY);
                db_name.setBackgroundColor(Color.GRAY);
                save.setText("Update Configurations");
                password.setEnabled(true);
                password.setBackgroundColor(Color.WHITE);
            }
            fIn.close();
            Log.d("file", "Data readed from file");
            Toast.makeText(getApplicationContext(), "Done reading SD 'mysdfile.txt'", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Please Save The IP and DB name first", Toast.LENGTH_SHORT).show();
            Log.d("file", "File Input stream occur exception " + e.getMessage());
        }
    }

    public void LoginNow(View view) {
        try {
            MyLoginAsync myLoginAsync = new MyLoginAsync();
            myLoginAsync.execute("");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("file", "Login button occur exception " + e.getMessage());
        }
    }

    class MyLoginAsync extends AsyncTask<String, String, String> {
        String msg = "";
        Boolean isSuccess = false;
        ProgressBar progressBar = findViewById(R.id.progressbar);

        @Override
        protected void onPreExecute() {
            login.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected void onPostExecute(String r) {
            try {
                progressBar.setVisibility(View.GONE);
                login.setVisibility(View.VISIBLE);
                if (isSuccess) {
                    save.setEnabled(false);
                    password.setEnabled(false);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userpass", passwordd);
                    intent.putExtra("ip", ipdb_detail[0]);
                    intent.putExtra("db", ipdb_detail[1]);
                    startActivity(intent);
                    finish();
                    Toast.makeText(LoginActivity.this, "Login Successfull", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Please Check your Internet Connection or \n IP and DB not found", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("file", "Exception releted to postexecute method " + e.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                passwordd = password.getText().toString();
                if (passwordd.isEmpty()) {
                    password.setError("Enter correct password");
                }
                String ConnURL = "jdbc:jtds:sqlserver://" + ipdb_detail[0] + ":1433;" + "databaseName=" + ipdb_detail[1] + ";user=" + sql_username + ";password="
                        + passwordd + ";";
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection con = DriverManager.getConnection(ConnURL);
                if (con == null) {
                    msg = "Check Your Internet Access!";
                    Log.d("here", "Connection is Null");
                } else {
                    Log.d("here", ConnURL);
                    String query = "SELECT * FROM [FTUser] WHERE [password] = '" + passwordd.toString() + "'  ";
                    ResultSet resultSet = null;
                    Statement statement = con.createStatement();
                    resultSet = statement.executeQuery(query);
                    if (resultSet.next()) {
                        msg = "Login successful";
                        isSuccess = true;
                        con.close();
                    } else {
                        msg = "Invalid Credentials!";
                        isSuccess = false;
                    }
                    con.close();
                    Log.d("file", "Connection Success ");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("file", "SQL exception occur in Login Activity " + e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("file", "Class not found exception " + e);
            } catch (Exception e) {
                Log.d("file", "Main Exception " + e.getMessage());
            }
            return msg;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
            case R.id.item1:
                builder.setMessage("This application is developed by DEVASHISH SHARMA.This Application is developed under suervision of 'Fenestec Technologies Pvt. Ltd.'\n" +
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
        }
        return super.onOptionsItemSelected(item);
    }
}