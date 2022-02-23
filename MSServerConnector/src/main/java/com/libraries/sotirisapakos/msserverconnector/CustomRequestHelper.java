package com.libraries.sotirisapakos.msserverconnector;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;

public abstract class CustomRequestHelper {

    private DatabaseHelper databaseHelper;
    private static String TAG = "CustomRequestHelper";

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
                }catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", TAG +
                                " - Cannot perform execute() method");
                        e.printStackTrace();
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", TAG +
                                " - Cannot perform execute() method");
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
     * <h3>Implement your custom server request.</h3>
     * <p>Info: Already executed {@code statement.execute(query)} function.</p>
     * @param statementResult boolean return value of {@code execute()} function.
     * @param statement {@link Statement statement} interface
     * @see Statement#execute(String) statement.execute()
     */
    protected abstract void onBackgroundFunctionality(boolean statementResult, Statement statement);
    protected abstract void onFinishFunctionality();

}
