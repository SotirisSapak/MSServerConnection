package com.libraries.sotirisapakos.msserverconnector;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;

public abstract class SelectRequestHelper {

    private final DatabaseHelper databaseHelper;
    private static String TAG = "SelectRequestHelper";

    public SelectRequestHelper(DatabaseHelper databaseHelper, String query){
        this.databaseHelper = databaseHelper;
        executeRequest(query);
    }
    public SelectRequestHelper(DatabaseHelper databaseHelper, String query, String TAG){
        this.databaseHelper = databaseHelper;
        SelectRequestHelper.TAG = TAG;
        executeRequest(query);
    }

    private void executeRequest(String query){
        new Executor() {
            @Override
            public void onPreExecute() {
                Log.d(TAG, "... onPreExecute() start running...");
            }

            @Override
            public void doInBackground() {
                Log.d(TAG, "... doInBackground() start running...");
                try {
                    Class.forName(DatabaseHelper.DATABASE_LIBRARY_DRIVER);
                } catch (ClassNotFoundException e) {
                    Log.e("Exception found", TAG + " - cannot setup jdbc class");
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
                            onBackgroundFunctionality(resultSet);
                        } while (resultSet.next());
                    }
                    conn.close();
                }catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", TAG +
                                " - Cannot perform executeQuery() method");
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", TAG +
                                " - Cannot perform executeQuery() method");
                        throwable.printStackTrace();
                    }
                }
            }

            @Override
            public void onPostExecute(ExecutorService executor) {
                executor.shutdown();
                onFinishFunctionality();
            }
        }.execute();
    }

    /**
     * Implement your own functionality on background task.
     * <p>
     *     <b>IMPORTANT</b>: {@link ResultSet resultSet} is in do...while loop so do <b>NOT</b>
     *     add another loop inside {@code onBackgroundFunctionality()} method</p>
     * @param resultSet {@code executeQuery()} result from your {@code query}
     * @throws SQLException will probably use {@code resultSet.get...()} methods. These can throw
     * SQLException
     */
    protected abstract void onBackgroundFunctionality(ResultSet resultSet) throws SQLException;
    protected abstract void onFinishFunctionality();

}
