package com.libraries.sotirisapakos.msserverconnector;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;

public abstract class CustomRequestHelper {

    private final DatabaseHelper databaseHelper;
    private static String TAG = "CustomRequestHelper";

    private int result = 0;

    public CustomRequestHelper(DatabaseHelper databaseHelper, String query){
        this.databaseHelper = databaseHelper;
        executeRequest(query);
    }
    public CustomRequestHelper(DatabaseHelper databaseHelper, String query, String TAG){
        this.databaseHelper = databaseHelper;
        CustomRequestHelper.TAG = TAG;
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
                    result = -1;
                }
                Connection conn = null;
                try {
                    DriverManager.setLoginTimeout(4);
                    conn = DriverManager.getConnection(databaseHelper.getUrl(),
                            databaseHelper.getUsername(), databaseHelper.getPassword());
                    Statement statement = conn.createStatement();
                    boolean statementResult = statement.execute(query);
                    onBackgroundFunctionality(statementResult, statement);
                    conn.close();
                    result = 1;
                }catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", TAG +
                                " - Cannot perform execute() method");
                        e.printStackTrace();
                        result = -1;
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", TAG +
                                " - Cannot perform execute() method");
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
     * @return {@link CustomRequestHelper#result} parameter
     */
    public int getResult() {
        return result;
    }

    /**
     * <h3>Implement your custom server request.</h3>
     * <p>Info: Already executed {@code statement.execute(query)} function.</p>
     * @param statementResult boolean return value of {@code execute()} function.
     * @param statement {@link Statement statement} interface
     * @see Statement#execute(String) statement.execute()
     */
    protected abstract void onBackgroundFunctionality(boolean statementResult, Statement statement);
    /**
     * This function will execute when background task has finished.
     * If you want to check if task has executed successfully call {@code this.getResult()}
     * method.
     */
    protected abstract void onFinishFunctionality();

}
