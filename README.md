[![](https://jitpack.io/v/SotirisSapak/MSServerConnector.svg)](https://jitpack.io/#SotirisSapak/MSServerConnector)
# MSServerConnector
Microsoft Server Connection Helper in Android.

## Implementation
This library uses another external library from sourceforge called JDBC. So, you must include this library in your project at app's build.gradle file:

``` gradle
dependencies {
    // ...
    implementation "net.sourceforge.jtds:jtds:1.3.1"
    // this library
    implementation 'com.github.SotirisSapak:MSServerConnector:v.1.0.0-beta'
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

#### Approach #1:
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
#### Approach #2
Or you can use an abstract class called SimpleQueryRequestHelper as below:

``` java
SimpleQueryRequestHelper requestHandler;

// require a DatabaseHelper and a query
requestHandler = new SimpleQueryRequestHelper(databaseHelper, query) {
  @Override
  public void onPreExecuteFunction(){ /* perform action before background task begins */ }
  
  @Override
  public void doInBackgroundFunction(ResultSet resultSet) throws SQLException {
    data = resultSet.getString("column label"); // use resultSet as Approach#1
  }
  
  @Override
  public void onPostExecuteFunction() { /* perform action after background task ends */ )
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
      
      // start the process!
      new SimpleQueryRequestHelper(databaseHelper, query) {
        @Override
        public void onPreExecuteFunction() {
          // just want to add some log information
          // hint: You may want to add a refresh panel or fetching data... alert dialog. It's up to you!
          Log.d("onPre", "onPreExecuteFunction() called");
        }

        @Override
        public void doInBackgroundFunction(ResultSet resultSet) throws SQLException {
          Log.d("onPre", "doInBackgroundFunction() started");
          // create a temporary Point class instance
          Point tempPoint = new Point();
          tempPoint.setX(resultSet.getInt("x"); // table has a column called x
          tempPoint.setY(resultSet.getInt("y"); // table has a column called y
          // important: store data to arrayList in order to fetch all data and not the last one!
          points.add(tempPoint);
        }

        @Override
        public void onPostExecuteFunction() {
          // if you have implemented fetching data alert dialog, dismiss() here!
          Log.d("onPre", "doInBackgroundFunction() finished");
          Log.d("onPost", "onPostExecuteFunction() started");
          if(points.isEmpty()){/* perform action when table has no records */}
          else {
            // show results in Logcat
            for(Point temp: points) Log.d("results", temp.toString());
          }
          Log.d("onPost", "onPostExecuteFunction() finished");
        }
      };        
    }
}

```

