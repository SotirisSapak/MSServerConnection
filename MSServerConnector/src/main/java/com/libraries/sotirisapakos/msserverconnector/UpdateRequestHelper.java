package com.libraries.sotirisapakos.msserverconnector;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;

/**
 * Class that implements an update background task to MS SQLServer database.
 * <p><b>Recommended use:</b> You may use this class for {@code UPDATE sql queries} and
 * {@code INSERT INTO sql queries}</p>
 * <p>To perform other sql queries like {@code drop table...} prefer
 * {@link Executor Executor} or other Helper classes </p>
 */
public abstract class UpdateRequestHelper {

    private final DatabaseHelper databaseHelper;

    /**
     * Result parameter used to inform developer the {@code executeUpdate()} method result.
     * {@code executeUpdate()} return 0 if nothing happened or >0 for the row count.
     * So, init {@code result} parameter to -1. {@code onPostExecuteFunction(int result)} will return
     * -1 if connection is null, or if Class has not found or <i>if an error occurred</i>
     */
    private int result = -1;

    private static String TAG = "UpdateRequestHelper";

    public UpdateRequestHelper(DatabaseHelper databaseHelper, String query){
        this.databaseHelper = databaseHelper;
        executeRequest(query);
    }
    public UpdateRequestHelper(DatabaseHelper databaseHelper, String query, String TAG){
        this.databaseHelper = databaseHelper;
        UpdateRequestHelper.TAG = TAG;
        executeRequest(query);
    }

    private void executeRequest(String query){
        new Executor(){

            @Override
            public void onPreExecute() {
                Log.d(TAG, "... onPreExecute() start running...");
                onPreExecuteFunction();
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
                    result = statement.executeUpdate(query);
                    conn.close();
                }catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", TAG +
                                " - Cannot perform executeUpdate() method");
                        e.printStackTrace();
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", TAG +
                                " - Cannot perform executeUpdate() method");
                        throwable.printStackTrace();
                    }
                }
            }

            @Override
            public void onPostExecute(ExecutorService executorService) {
                executorService.shutdown();
                onPostExecuteFunction(result);
            }
        }.execute();
    }

    protected abstract void onPreExecuteFunction();
    /**
     * {@code executeUpdate()} function return an int value based on the result.
     * <ul>
     *     <li>-1: error - custom library implementation</li>
     *     <li> 0: return nothing</li>
     *     <li>>0: rows count</li>
     * </ul>
     *
     * @param result result value from background {@code executeUpdate()} call.
     */
    protected abstract void onPostExecuteFunction(int result);
}
