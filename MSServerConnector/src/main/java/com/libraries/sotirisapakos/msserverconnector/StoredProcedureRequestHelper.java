package com.libraries.sotirisapakos.msserverconnector;

import android.util.Log;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * <h2>BETA VERSION</h2>
 * <p>This is a beta implementation of {@link StoredProcedureRequestHelper} class</p>
 * <h3>Recommended: Use this class with
 * {@link StoredProcedureRequestHelper#setFullLog(boolean) setFullLog(true)} to see if something is not
 * operating correctly</h3>
 */
public abstract class StoredProcedureRequestHelper {

    private String TAG = "StoreProcedureRequestHelper";

    /**
     * Will use {@link DatabaseHelper databaseHelper} to get:
     * <ul>
     *     <li>{@link DatabaseHelper#getUrl() Url}</li>
     *     <li>{@link DatabaseHelper#getUsername() Username}</li>
     *     <li>{@link DatabaseHelper#getPassword() Password}</li>
     * </ul>
     */
    private final DatabaseHelper databaseHelper;
    /**
     * Any storeProcedure has a title
     */
    private String procedureName; // dbo.something
    /**
     * A {@link Map} object to hold the storeProcedure parameters.
     * A typical storeProcedure should require some parameters.
     * If a store procedure does not have parameters then use {@code SelectRequestHelper} class instead.
     * <p>Example:</p>
     * <p>{@code procedureParameters.put("parameter_name#1", new Date();}</p>
     * <p>{@code procedureParameters.put("parameter_name#2", x);  // x value is an int param}</p>
     * @see SelectRequestHelper
     */
    private Map<String, Object> procedureParameters;
    /**
     * Some storeProcedure may have output parameters. This {@link Map} object will hold the
     * configuration parameters as below:
     * <p>Example: </p>
     * <P>// Must reference second {@link Map} parameter with {@link java.sql.Types} values</P>
     * <p>{@code outParameterStructure.put(index, Types.INTEGER);}</p>
     */
    private Map<String, Integer> outParameterStructure;
    /**
     * Custom timeout seconds value from constructor or setter
     * <p>See: </p>
     * <ul>
     *     <li>{@link #StoredProcedureRequestHelper(DatabaseHelper, String, Map, Map, String)}</li>
     *     <li>{@link #setRequestTimeoutSeconds(int)}</li>
     * </ul>
     */
    private int requestTimeoutSeconds = DefaultValues.DEFAULT_REQUEST_TIMEOUT_SECONDS;
    /**
     * Internal parameter to check if user will wait any {@code SELECT *} functionality inside
     * storeProcedure.
     * <p>{@code Default value: false}</p>
     */
    private boolean awaitResults = false;
    /**
     * Internal parameter to check if user will wait any output parameter inside
     * storeProcedure.
     * <p>{@code Default value: false}</p>
     */
    private boolean hasOutParams = false;
    /**
     * Internal parameter to show more log data if parameter is true. If false show some basic log.
     */
    private boolean fullLog = false;
    /**
     * Internal parameter to inform user if stored procedure throws any exception.
     * <p>Values:</p>
     * <ul>
     *     <li>1: Success</li>
     *     <li>0: Default (Not executed)</li>
     *     <li>-1: Error</li>
     * </ul>
     */
    private int procedureValue = 0;

    /**
     * Default constructor.
     * @param databaseHelper {@link DatabaseHelper} with initialized values
     */
    public StoredProcedureRequestHelper(DatabaseHelper databaseHelper) { this.databaseHelper = databaseHelper;}

    /**
     * Default constructor with TAG value
     * @param databaseHelper {@link DatabaseHelper} with initialized values
     * @param TAG procedure {@link android.util.Log} TAG value
     */
    public StoredProcedureRequestHelper(DatabaseHelper databaseHelper, String TAG) {
        this.databaseHelper = databaseHelper;
        this.TAG = TAG;
    }

    /**
     * A customized constructor for setting the storeProcedure name and parameters to execute
     * @param databaseHelper {@link DatabaseHelper} with initialized values
     * @param procedureName StoreProcedure call name {@code dbo.something}
     * @param procedureParameters {@link Map} object with parameters
     * @param TAG procedure {@link android.util.Log} TAG value
     */
    public StoredProcedureRequestHelper
            (
                    DatabaseHelper databaseHelper,
                    String procedureName,
                    Map<String, Object> procedureParameters,
                    String TAG
            ){
        this.databaseHelper = databaseHelper;
        this.procedureName = procedureName;
        this.procedureParameters = procedureParameters;
        // if given tag is empty then choose default value instead of an empty string
        if(TAG.trim().equals("")) this.TAG = "StoreProcedureRequestHelper";
        else this.TAG = TAG;
    }
    /**
     * A customized constructor for setting the storeProcedure name, parameters and output parameters
     * structure to execute
     * @param databaseHelper {@link DatabaseHelper} with initialized values
     * @param procedureName StoreProcedure call name {@code dbo.something}
     * @param procedureParameters {@link Map} object with parameters
     * @param outParameterStructure {@link Map} object with output parameters structure
     * @param TAG procedure {@link android.util.Log} TAG value
     */
    public StoredProcedureRequestHelper
            (
                    DatabaseHelper databaseHelper,
                    String procedureName,
                    Map<String, Object> procedureParameters,
                    Map<String, Integer> outParameterStructure,
                    String TAG
            ){
        this.databaseHelper = databaseHelper;
        this.procedureName = procedureName;
        this.procedureParameters = procedureParameters;
        this.outParameterStructure = outParameterStructure;
        hasOutParams = true;
        // if given tag is empty then choose default value instead of an empty string
        if(TAG.trim().equals("")) this.TAG = "StoreProcedureRequestHelper";
        else this.TAG = TAG;
    }
    /**
     * A customized constructor for setting the storeProcedure name, parameters, output parameters and
     * custom request timeout. Structure to execute
     * @param databaseHelper {@link DatabaseHelper} with initialized values
     * @param procedureName StoreProcedure call name {@code dbo.something}
     * @param procedureParameters {@link Map} object with parameters
     * @param outParameterStructure {@link Map} object with output parameters structure
     * @param TAG procedure {@link android.util.Log} TAG value
     * @param requestTimeoutSeconds {@link StoredProcedureRequestHelper#requestTimeoutSeconds}
     */
    public StoredProcedureRequestHelper
            (
                    DatabaseHelper databaseHelper,
                    String procedureName,
                    Map<String, Object> procedureParameters,
                    Map<String, Integer> outParameterStructure,
                    String TAG,
                    int requestTimeoutSeconds
            ){
        this.databaseHelper = databaseHelper;
        this.procedureName = procedureName;
        this.procedureParameters = procedureParameters;
        this.outParameterStructure = outParameterStructure;
        hasOutParams = true;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
        // if given tag is empty then choose default value instead of an empty string
        if(TAG.trim().equals("")) this.TAG = "StoreProcedureRequestHelper";
        else this.TAG = TAG;
    }

    /**
     * Function to set dynamically the stored procedure name
     * @param procedureName stored procedure name (example: dbo.example)
     */
    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    /**
     * Function to change store procedure functionality.
     * <p>Call functionality to get output parameters</p>
     * @param hasOutParams {@link Boolean} if store procedure has output parameters
     */
    public void setHasOutParams(boolean hasOutParams) {
        this.hasOutParams = hasOutParams;
    }

    /**
     * Function to change store procedure functionality.
     * <p>Call functionality to enable {@link java.sql.ResultSet} object in abstract method</p>
     * @param awaitResults {@link Boolean} if store procedure has {@code SELECT} query
     */
    public void setAwaitResults(boolean awaitResults) {
        this.awaitResults = awaitResults;
    }

    /**
     * Function to change store procedure functionality. Log info.
     * @param fullLog {@code true} if want full log, {@code false} if want basic log.
     */
    public void setFullLog(boolean fullLog) {
        this.fullLog = fullLog;
    }

    /**
     * Apart from constructor, can change your TAG with this function.
     * @param TAG procedure {@link android.util.Log} TAG value
     */
    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    /**
     * Function to set custom request timeout in seconds.
     * @param requestTimeoutSeconds time in seconds (4 -> 4 seconds timeout)
     */
    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    /**
     * Set procedure parameters.
     * @param procedureParameters {@link Map} object with parameters
     */
    public void setProcedureParameters(Map<String, Object> procedureParameters) {
        this.procedureParameters = procedureParameters;
    }

    /**
     * Set output parameter structure. Also, must call {@code setHasOutParams(true) to enable functionality}
     * @param outParameterStructure @link Map} object with output parameters structure
     */
    public void setOutParameterStructure(Map<String, Integer> outParameterStructure) {
        this.outParameterStructure = outParameterStructure;
    }

    /**
     * <p>Setup callable statement url within inner functions.</p>
     * <p>Example: </p>
     * In database, you have a procedure with name: {@code dbo.example} and has 3 parameters
     * <p>Url generated: {@code {call dbo.example(?,?,?)}}</p>
     * @return generated {@link String URL}
     */
    private String getCallableStatementUrl(){
        StringBuilder builder = new StringBuilder();
        // start with basic call...
        // {call dbo.example(
        builder.append("{").
                append("call ").
                append(procedureName).  // of course, procedure name will be a dynamic value!
                append("(");

        // if user want out parameters then these must be calculated as '?' in parameters
        int outParamsSize = 0;
        if(outParameterStructure != null) outParamsSize = outParameterStructure.size();
        for (int i = 0; i < procedureParameters.size() + outParamsSize; i++) {
            // add after '(' the '?,'
            builder.append("?,");
        }
        // Example: if has 3 params then the current builder string will be:
        // {call dbo.example(?,?,?,

        // remove last comma generated.
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")}");
        // final url at above example: {call dbo.example(?,?,?)}
        return builder.toString();
    }

    /**
     * Get procedure value. Call this in {@code handleOnTaskFinished()} method.
     * @return procedure result
     */
    public int getProcedureValue() {
        return procedureValue;
    }

    /**
     * User input verification
     * @return {@link Boolean boolean} value if everything is ok to execute database request.
     */
    private boolean verification(){
        // store procedure name...
        if(procedureName == null || procedureName.trim().equals("")) {
            Log.e(TAG, "Procedure name is empty or null...");
            return false;
        }
        // parameters
        if(procedureParameters == null || procedureParameters.isEmpty()){
            Log.e(TAG, "Use this interface to call store procedures with parameters.");
            Log.w(TAG, "if your procedure has no params use SelectRequestHelper class instead.");
            return false;
        }
        // output parameters
        if(hasOutParams){
           // if user apply request for output params then check if parameters structure has given
           if(outParameterStructure == null || outParameterStructure.isEmpty()){
               Log.e(TAG, "Have selected output parameters functionality " +
                       "but no parameters structure was found.");
            return false;
           }
        }
        return true;
    }

    public void execute(){
        // verification first...
        if(verification()){
            // after verification, start building execution components...
            // separate functionalities...
            if(hasOutParams){
                executeWithOutputParameters();
            }else{
                // has no output parameters
                executeSimple();
            }
        }
    }

    /**
     * Execute store procedure with no output parameters
     */
    private void executeSimple(){
        ArrayList<String> keysParams = new ArrayList<>(procedureParameters.keySet());
        ArrayList<Object> dataParams = new ArrayList<>(procedureParameters.values());
        ArrayList<Object> outParams  = new ArrayList<>();
        new Executor(){

            @Override
            public void onPreExecute() {

            }

            @Override
            public void doInBackground() {
                Log.d(TAG, "... doInBackground() start running...");
                try {
                    Class.forName(DatabaseHelper.DATABASE_LIBRARY_DRIVER);
                } catch (ClassNotFoundException e) {
                    Log.e("Exception found", TAG + " - cannot setup jdbc class");
                    procedureValue = -1;
                    e.printStackTrace();
                }
                Connection conn = null;
                try {
                    DriverManager.setLoginTimeout(requestTimeoutSeconds);
                    conn = DriverManager.getConnection(databaseHelper.getUrl(),
                            databaseHelper.getUsername(), databaseHelper.getPassword());
                    if(conn == null) return;
                    try {
                        CallableStatement callableStatement = conn.
                                prepareCall(getCallableStatementUrl());

                        if(fullLog){
                            Log.d(TAG, "------ parameters to add ------ ");
                            for (int i=0; i<procedureParameters.size(); i++) {
                                Log.d(TAG, "@" + keysParams.get(i) + ": " + dataParams.get(i));
                            }
                            Log.d(TAG, "------------------------------- ");
                        }
                        if(fullLog) Log.d(TAG, "Applying parameters to stored procedure...");
                        for (int i = 0; i < dataParams.size(); i++) {
                            callableStatement.setObject(keysParams.get(i), dataParams.get(i));
                        }
                        if(fullLog) Log.d(TAG, "Parameters applied to stored procedure...");


                        callableStatement.execute();

                        if(awaitResults){
                            if(fullLog) Log.d(TAG, "Passing ResultSet parameter to abstract method...");
                            if(callableStatement.getMoreResults()){
                                if(fullLog) Log.d(TAG, "callableStatement.getMoreResults(): true");
                                ResultSet resultSet = callableStatement.getResultSet();
                                if(fullLog) Log.d(TAG, "ResultSet object is: " + resultSet);
                                while (resultSet.next()){
                                    handleResultFromStoredProcIfAvailable(resultSet);
                                }
                            }
                            if(fullLog) Log.d(TAG, "Passing ResultSet parameter to abstract method...Done");
                        }

                        callableStatement.close();
                        procedureValue = 1;
                    }catch (SQLException e){
                        Log.e("SQLException found", TAG +
                                " - Cannot perform storedProcedure due " +
                                "to SQLException...inner try-catch block");
                        procedureValue = -1;
                        e.printStackTrace();
                    }

                }
                catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", TAG +
                                " - Cannot perform storedProcedure");
                        e.printStackTrace();
                        procedureValue = -1;
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", TAG +
                                " - Cannot perform storedProcedure");
                        throwable.printStackTrace();
                        procedureValue = -1;
                    }
                }
            }

            @Override
            public void onPostExecute(ExecutorService executor) {
                executor.shutdown();
                handleOnTaskFinished(outParams);
            }
        }.execute();
    }
    /**
     * Execute store procedure with output parameters
     */
    private void executeWithOutputParameters(){
        ArrayList<String>  keysParams       = new ArrayList<>(procedureParameters.keySet());
        ArrayList<Object>  dataParams       = new ArrayList<>(procedureParameters.values());
        ArrayList<String>  keysOutParams    = new ArrayList<>(outParameterStructure.keySet());
        ArrayList<Integer> dataOutParams    = new ArrayList<>(outParameterStructure.values());
        ArrayList<Object>  outParams        = new ArrayList<>();
        new Executor() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void doInBackground() {
                Log.d(TAG, "... doInBackground() start running...");
                try {
                    Class.forName(DatabaseHelper.DATABASE_LIBRARY_DRIVER);
                } catch (ClassNotFoundException e) {
                    Log.e("Exception found", TAG + " - cannot setup jdbc class");
                    e.printStackTrace();
                    procedureValue = -1;
                }
                Connection conn = null;
                try {
                    DriverManager.setLoginTimeout(requestTimeoutSeconds);
                    conn = DriverManager.getConnection(databaseHelper.getUrl(),
                            databaseHelper.getUsername(), databaseHelper.getPassword());
                    if(conn == null) return;
                    try {
                        CallableStatement callableStatement = conn.prepareCall(getCallableStatementUrl());

                        if(fullLog) {
                            Log.d(TAG, "------ parameters to add ------ ");
                            for (int i=0; i<procedureParameters.size(); i++) {
                                Log.d(TAG, "@" + keysParams.get(i) + ": " + dataParams.get(i));
                            }
                            Log.d(TAG, "------------------------------- ");
                        }
                        if(fullLog) Log.d(TAG, "applying parameters to stored procedure...");
                        for (int i = 0; i < dataParams.size(); i++) {
                            callableStatement.setObject(keysParams.get(i), dataParams.get(i));
                        }
                        if(fullLog) Log.d(TAG, "Parameters applied to stored procedure");

                        if(fullLog) {
                            Log.d(TAG, "------ output parameters structure ------ ");
                            for (int i=0; i<outParameterStructure.size(); i++) {
                                Log.d(TAG, "@" + keysOutParams.get(i) + ": " +
                                        dataOutParams.get(i) + " (MS Server SQL type)");
                            }
                            Log.d(TAG, "----------------------------------------- ");
                        }
                        if(fullLog) Log.d(TAG, "Register out parameters to stored procedure...");
                        for (int i = 0; i < dataOutParams.size(); i++) {
                            callableStatement.registerOutParameter(keysOutParams.get(i), dataOutParams.get(i));
                        }
                        if(fullLog) Log.d(TAG, "Register out parameters to stored procedure...Done!");

                        callableStatement.execute();

                        if(awaitResults){
                            if(callableStatement.getMoreResults()){
                                if(fullLog) Log.d(TAG, "Passing ResultSet parameter to abstract method...");
                                ResultSet resultSet = callableStatement.getResultSet();
                                if(fullLog) Log.d(TAG, "ResultSet is: " + resultSet);
                                while (resultSet.next()){
                                    handleResultFromStoredProcIfAvailable(resultSet);
                                }
                                if(fullLog) Log.d(TAG, "Passing ResultSet parameter to abstract method...Done");
                            }
                        }

                        if(fullLog) Log.d(TAG, "Register out parameters to local array...");
                        for (int i = 0; i < dataOutParams.size(); i++) {
                            if(fullLog) Log.d(TAG,
                                    "Register {" + keysOutParams.get(i) + "} at index {" + i +
                                            "} to local array");
                            outParams.add(callableStatement.getObject(keysOutParams.get(i)));
                        }
                        if(fullLog) Log.d(TAG, "Register out parameters to local array...Done");

                        callableStatement.close();
                        procedureValue = 1;
                    }catch (SQLException e){
                        Log.e("SQLException found", TAG +
                                " - Cannot perform storedProcedure due " +
                                "to SQLException...inner try-catch block");
                        e.printStackTrace();
                        procedureValue = -1;
                    }

                }
                catch (Exception e){
                    try {
                        if(conn != null) conn.close();
                        Log.e("Exception found", TAG +
                                " - Cannot perform storedProcedure");
                        e.printStackTrace();
                        procedureValue = -1;
                    } catch (SQLException throwable) {
                        Log.e("SQLException found", TAG +
                                " - Cannot perform storedProcedure");
                        throwable.printStackTrace();
                        procedureValue = -1;
                    }
                }
            }

            @Override
            public void onPostExecute(ExecutorService executor) {
                executor.shutdown();
                handleOnTaskFinished(outParams);
            }
        }.execute();
    }

    /**
     * <p>{@link ResultSet resultSet} object could be null. This is an abstract method and should handle every
     * storedProcedure implementation. As a result, a storedProcedure may not have a {@code SELECT} query or you may call
     * {@code setAwaitResults(false)}.</p>
     * @param resultSet ({@code null}) value OR {@link ResultSet result} of storedProcedure execution.
     * @throws SQLException {@link ResultSet#getString(String)} or any similar function call, can produce SQLException
     */
    public abstract void handleResultFromStoredProcIfAvailable(ResultSet resultSet) throws SQLException;

    /**
     * Function to handle the post storedProcedure execution functionality.
     * @param outParams ({@code null}) value or an {@link ArrayList arrayList} of {@link Object objects}
     *                  containing the result of {@code output} parameters.
     */
    public abstract void handleOnTaskFinished(ArrayList<Object> outParams);
}
