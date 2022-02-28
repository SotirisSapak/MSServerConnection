[![](https://jitpack.io/v/SotirisSapak/MSServerConnector.svg)](https://jitpack.io/#SotirisSapak/MSServerConnector)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
# MSServerConnector
Microsoft Server Connection Helper in Android.

## Implementation
#### First of all...
Add 
``` maven { url 'https://jitpack.io' } ``` to ``` settings.gradle ``` of your project:

```gradle
allprojects {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
This library uses another external library from sourceforge called JDBC. So, you must include this library in your project at app's build.gradle file:
``` gradle
dependencies {
    // ...
    implementation "net.sourceforge.jtds:jtds:1.3.1"
    // this library
    implementation 'com.github.SotirisSapak:MSServerConnector:current_version'
    // ...
}
```

##### !! IMPORTANT !!
At AndroidManifest.xml file, add these permissions to let your app have access to the internet (and the library to work properly!!):

``` xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```



## Usage
First of all, you have to initialize all necessary parameters for Microsoft Server Url.
Params: 
| Parameters          | Description                                                                                         |
| -------------       | -------------                                                                                       |
| Ip                  | Ip address of server. If you have an internal internet connection, you can use DESKTOP-XXXXXX info. |
| Port                | Server port. If you don't have any port, leave it empty!                                            |
| db_name             | Database name. Must exist in server                                                                 |
| username            | Username credential                                                                                 |
| password            | Password credential                                                                                 |

### How to init parameters:
By using DatabaseHelper class as below:
``` java
// call this in any Activity
DatabaseHelper databaseHelper = new DatabaseHelper(this);
databaseHelper.setConnectionFields("your ip", "your port", "your db_name", "your username", "your password");

//or by using setter methods
databaseHelper.setIp("your ip");
databaseHelper.setPort("your port");
// etc...
```

### What comes next? 
The Server request. Should use background task for these type of requests.

#### Approach #1 - v.1.0.0-beta01:
Use <b>Executor</b> class to perform this background task. In order to implement this request use Executor class as below:

``` java
Executor executor;
executor = new Executor() {
  @Override 
  public void onPreExecute(){
    // perform your action before background task start
  }
  @Override 
  public void doInBackground(){
    try {
      // DATABASE_LIBRARY_DRIVER is a static param to get MS server external library path
      Class.forName(DatabaseHelper.DATABASE_LIBRARY_DRIVER); 
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    Connection conn = null;
    try 
    {
      DriverManager.setLoginTimeout(4);
      conn = DriverManager.getConnection(databaseHelper.getUrl(),
                databaseHelper.getUsername(), databaseHelper.getPassword());
      Statement statement = conn.createStatement();
      ResultSet resultSet = statement.executeQuery(query);
      if (!resultSet.next()) {
        // cannot find any record in table
        conn.close();
      } else {
        do {
          // get records 
          yourParams = resultSet.getString("column label");
          // or by using column index
          yourParams = resultSet.getString(1);
        } while (resultSet.next());
      }
      
      conn.close();
    }
    catch (Exception e){
        try { if(conn != null) conn.close(); }
        catch (SQLException throwable) { throwable.printStackTrace(); }
        e.printStackTrace();
    }    
  }
  
  @Override
  public void onPostExecute(){
    // perform action when background task is completed!
  }
  
};

executor.execute();

```

From v.1.0.0-beta02 and after, ``` onPostExecute() ``` function is implemented as below:

``` java
@Override
  public void onPostExecute(ExecutorService executor)
    // executor.shutdown(); --- should terminate executor --- 
    // perform action when background task is completed and after executor is terminated!
  }
```
#### Approach #2
Or you can use an abstract class called SelectRequestHelper as below:

``` java

/* perform action before background task begins */
SelectRequestHelper requestHandler;

// require a DatabaseHelper and a query
requestHandler = new SelectRequestHelper(databaseHelper, query, TAG /* not required */) {

  @Override
  public void onBackgroundFunctionality(ResultSet resultSet) throws SQLException {
    data = resultSet.getString("column label"); // use resultSet as Approach#1
  }
  
  @Override
  public void onFinishFunctionality() { /* perform action after background task ends */ ) }
};
```

## Example
Will use Approach #2 to get some points from database. For this example we will not initialize real database params.
#### Requirements:
* Point() object with 2 params: x and y
* An Activity

``` java

/**
 * Example: We have an activity and we want to get some points from MS server.
 * At our project, we have implemented a class called point that has 2 parameters (x, y)
 * At server we have a table that its structure is like:
 *  ------------------
 *  | id |  x  |  y  |
 *  | -- | --- | --- | 
 *  | 0  | 10  |  1  |    -> a point (x: 10, y: 1)
 *  | 1  |  9  | 50  |    -> a point (x: 9 , y: 50)
 *  ------------------
 * Let's start...
 */
public class SampleActivity extends AppCompatActivity {

    private ArrayList<Point> points;  // create an arrayList to hold the database records

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_sample);
      
      // initialize arrayList
      points = new ArrayList<>();
      
      //database helper class
      DatabaseHelper databaseHelper = new DatabaseHelper(this);
      databaseHelper.setConnectionFields(/*do your magic here!*/);  // put your own database parameters
      String query = "select * from POINTS";  // get every record from table called POINTS
      
      // just want to add some log information
      // hint: You may want to add a refresh panel or fetching data... alert dialog. It's up to you!
      // -- start the process! -- 
      new SelectRequestHelper(databaseHelper, query) {

        @Override
        public void onBackgroundFunctionality(ResultSet resultSet) throws SQLException {
          // create a temporary Point class instance
          Point tempPoint = new Point();
          tempPoint.setX(resultSet.getInt("x"); // table has a column called x
          tempPoint.setY(resultSet.getInt("y"); // table has a column called y
          // important: store data to arrayList in order to fetch all data and not the last one!
          points.add(tempPoint);
        }

        @Override
        public void onFinishFunctionality() {
          // if you have implemented fetching data alert dialog, dismiss() here!
          if(points.isEmpty()){/* perform action when table has no records */}
          else {
            // show results in Logcat
            for(Point temp: points) Log.d("results", temp.toString());
          }
        }
      };        
    }
}

```

#### From v.1.0.0-beta03, support for Stored procedure has been added.
How to add this feature to your app:

#### Example:
We have a stored procedure in our MS Server database with this script:

``` sql
create procedure [dbo].[Example]
        -- The flag title
        @title varchar(255),	   
        -- The flag value
        @value bit,		
        -- flags counter
        @count int output		   
as
        -- store the flags counter into our output parameter
        select @count = count(*) from FLAGS	
	-- update title and value columns of all flags
	update flags set title=@title, value=@value
	-- show all flags with the updated values
	select * from FLAGS;
go
```

We want to get the result from ``` @count``` parameter and update the flag parameters by calling the above stored procedure. 

``` java
/**
 * In our activity we must init DatabaseHelper class and call StoredProcedureRequestHelper class.
 */ 
public class SampleActivity extends AppCompatActivity {

    private static String TAG = "Custom-stored-proc-tag";   // Be careful: must contain max 25 characters
    
    private DatabaseHelper databaseHelper;
    private Map<String, Object>  parameters;
    private Map<String, Integer> outputParametersStructure;
    
    private ArrayList<String> resultData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // .. your code
        initComponents();
        setUpDatabaseHelper();
        setUpData();
        // we are ready to create the Stored procedure helper
        StoredProcedureRequestHelper changeFlags = new StoredProcedureRequestHelper
            (databaseHelper, "dbo.Example" , parameters, TAG){
            
            @Override
            public void handleResultFromStoredProcIfAvailable(ResultSet resultSet) throws SQLException {
                // Use this function only if you have a result to wait like a SELECT query.
                // At our example we have a SELECT query.
                // Will use an ArrayList to hold the data.
                resultData.add(resultSet.getString("title"));   // "title" is a column in FLAG table
                // If we have a 'FLAG' table with 4 records, then the resultData arrayList will have 4 values
            }

            @Override
            public void handleOnTaskFinished(ArrayList<Object> outParams) {
                // This method will be called when background task has finished.
                // OutParams arrayList will contain the output parameter values of the Stored procedure.
                // Just print it.
                Toast.makeText(SampleActivity.this, outParams.get(0).toString(), Toast.LENGTH_SHORT).show();    
                // 0 is params index similar with our outputParameterStructure Map.
            }
        
        };
        
        // At this point, if we execute code nothing will happened...
        // We have to call some other functions too!
        changeFlags.setFullLog(true);                                       // Not necessary - get full log of 
                                                                            // what's going on at every step!
        changeFlags.setAwaitResults(true);                                  // Enable result. (from SELECT queries) 
        changeFlags.setHasOutParams(true);                                  // Enable output parameters support.
        changeFlags.setOutParameterStructure(outputParametersStructure);    // Give the output parameters structure
        // and finally...
        changeFlags.execute();                                              // ALWAYS call execute() at the END!
        // Hint: In order to remove these calls, just give more parameters to construtor!
    }
    
    private void initComponents(){
        parameters = new HashMap<>();
        outputParametersStructure = new HashMap<>();
        resultData = new ArrayList<>();
    }
    private void setUpDatabaseHelper(){
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.setConnectionFields(
                "your ip",
                "your port",
                "your database name",
                "your username",
                "your password");
    }
    private void setUpData(){
        // will setup not only parameters, but output parameters structure
        // start with parameters...
        // how to set the parameters? Like that: parameters.put("parameter_title", parameter_value);
        // parameter value could be anything because parameter map support object class and not a String for example.
        parameters.put("TITLE", "FLAG#CUSTOM_TITLE");   // VARCHAR VALUE
        parameters.put("VALUE", 1);                     // BIT VALUE
        // -------------------------------------------------------------
        // setup output parameter structure as below:
        // outputParamsStructure.put("output parameter name", type_of_value); // type value should be from java.sql.Types class.
        outputParametersStructure.put("count", Types.INTEGER); 
    }
}
```

If ``` changeFlags.setFullLog(true); ``` then the Log result will be like this:

``` 
D/Custom-stored-proc-tag: ... doInBackground() start running...
D/Custom-stored-proc-tag: ------ parameters to add ------ 
D/Custom-stored-proc-tag: @TITLE: FLAG#CUSTOM_TITLE
D/Custom-stored-proc-tag: @VALUE: 1
D/Custom-stored-proc-tag: ------------------------------- 
D/Custom-stored-proc-tag: applying parameters to stored procedure...
D/Custom-stored-proc-tag: Parameters applied to stored procedure
D/Custom-stored-proc-tag: ------ output parameters structure ------ 
D/Custom-stored-proc-tag: @count: 4 (MS Server SQL type)
D/Custom-stored-proc-tag: ----------------------------------------- 
D/Custom-stored-proc-tag: Register out parameters to stored procedure...
D/Custom-stored-proc-tag: Register out parameters to stored procedure...Done!
D/Custom-stored-proc-tag: Passing ResultSet parameter to abstract method...
D/Custom-stored-proc-tag: ResultSet is: net.sourceforge.jtds.jdbc.JtdsResultSet@2780b4e
D/Custom-stored-proc-tag: Passing ResultSet parameter to abstract method...Done
D/Custom-stored-proc-tag: Register out parameters to local array...
D/Custom-stored-proc-tag: Register {count} at index {0} to local array
D/Custom-stored-proc-tag: Register out parameters to local array...Done
``` 

