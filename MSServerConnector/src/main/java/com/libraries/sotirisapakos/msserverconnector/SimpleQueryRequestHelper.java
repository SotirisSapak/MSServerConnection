package com.libraries.sotirisapakos.msserverconnector;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class that implements a background task to MS SQLServer database.
 * <p><b>Recommended use:</b> You may use this class for {@code select sql queries}</p>
 * <p>To perform other sql queries like {@code insert into...} or
 * {@code drop table...} prefer {@link Executor Executor} class to implement your own custom
 * background task.</p>
 */
public abstract class SimpleQueryRequestHelper{

    private final DatabaseHelper databaseHelper;

    public SimpleQueryRequestHelper(DatabaseHelper databaseHelper, String query){
        this.databaseHelper = databaseHelper;
        executeRequest(query);
    }
    private void executeRequest(String query){
        new Executor() {
            @Override
            public void onPreExecute() {
                Log.d("SimpleQueryRequest", "... onPreExecute() start running...");
                onPreExecuteFunction();
            }

            @Override
            public void doInBackground() {
                Log.d("SimpleQueryRequest", "... doInBackground() start running...");
                try {
                    Class.forName(DatabaseHelper.DATABASE_LIBRARY_DRIVER);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Connection conn = null;
                try {
                    DriverManager.setLoginTimeout(4);
                    conn = DriverManager.getConnection(databaseHelper.getUrl(),
                            databaseHelper.getUsername(), databaseHelper.getPassword());
                    Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    if (!resultSet.next()) {
                        // do nothing
                        conn.close();
                    } else {
                        do {
                            doInBackgroundFunction(resultSet);
                        } while (resultSet.next());
                    }
                    conn.close();
                }catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", "Cannot get data from database");
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", "Cannot get data from database");
                        throwable.printStackTrace();
                    }
                }
            }

            @Override
            public void onPostExecute() {
                onPostExecuteFunction();
            }
        }.execute();
    }

    public abstract void onPreExecuteFunction();
    public abstract void doInBackgroundFunction(ResultSet resultSet) throws SQLException;
    public abstract void onPostExecuteFunction();

}
