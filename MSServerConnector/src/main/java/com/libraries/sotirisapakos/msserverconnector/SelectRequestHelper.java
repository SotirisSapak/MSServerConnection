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

    /**
     * Internal parameter to inform user if request throw any exception.
     * <p>Values:</p>
     * <ul>
     *     <li>1: Success</li>
     *     <li>0: Default (Not executed)</li>
     *     <li>-1: Error</li>
     * </ul>
     */
    private int result = 0;

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
                    result = -1;
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
                    result = 1;
                }catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", TAG +
                                " - Cannot perform executeQuery() method");
                        e.printStackTrace();
                        result = -1;
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", TAG +
                                " - Cannot perform executeQuery() method");
                        throwable.printStackTrace();
                        result = -1;
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
     * Get request result from execution
     * call this as {@code this.getResult()} at {@code onFinishFunctionality()} method.
     * @return {@link SelectRequestHelper#result} parameter
     */
    public int getResult() {
        return result;
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

    /**
     * This function will execute when background task has finished.
     * If you want to check if task has executed successfully call {@code this.getResult()}
     * method.
     */
    protected abstract void onFinishFunctionality();

}
