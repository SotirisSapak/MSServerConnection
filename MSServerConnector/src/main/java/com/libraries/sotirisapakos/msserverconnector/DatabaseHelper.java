package com.libraries.sotirisapakos.msserverconnector;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import java.sql.Connection;
import java.util.Locale;

/**
 * This class hold connection data and set the Microsoft Server connection Url based on
 * user input parameters.
 */
public class DatabaseHelper {

    // default port for Microsoft SQL Server
    private static final String DEFAULT_MS_SERVER_PORT = "1433";
    // logcat value
    private static final String LOG_CAT = "DatabaseHelper";
    // library static driver value
    public static final String DATABASE_LIBRARY_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

    // connection fields
    private String ip;
    private String port;
    private String db_name;
    private String username;
    private String password;

    private final Activity activity;
    private String url;
    private final Connection connection = null;

    public DatabaseHelper(Activity activity){ this.activity = activity; }

    /**
     * Private method to setup:
     * <li>ThreadPolicy</li>
     * <li>Connection Url</li>
     */
    private void initDatabaseConnection(){
        url = String.format(Locale.ENGLISH,
                "jdbc:jtds:sqlserver://%s:%s/%s", ip, port, db_name);
        ActivityCompat.requestPermissions(activity, new String[]
                {Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    /**
     * Call this method after creating {@link DatabaseHelper} instance to set connection parameters.
     * Although, is optional because you can set parameters via setters.
     */
    public void setConnectionFields(@NonNull String ip,
                                    String port,
                                    @NonNull String db_name,
                                    String username,
                                    String password){
        this.ip = ip;
        if(port == null || port.trim().equalsIgnoreCase(""))
            this.port = DEFAULT_MS_SERVER_PORT;
        else
            this.port = port;
        this.db_name = db_name;
        this.username = username;
        this.password = password;
        initDatabaseConnection();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setPort(String port) {
        if(port.trim().equalsIgnoreCase("")) this.port = DEFAULT_MS_SERVER_PORT;
        else this.port = port;
    }

    public String getIp() {
        return ip;
    }
    public String getPort() {
        return port;
    }
    public String getDb_name() {
        return db_name;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getUrl() {
        return url;
    }
    public Connection getConnection() {
        return connection;
    }
}
