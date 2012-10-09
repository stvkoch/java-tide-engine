package tideengine;


import coreutilities.sql.SQLUtil;
import coreutilities.sql.SQLiteUtil;

import java.io.File;

import java.io.InputStream;

import java.security.AccessControlException;

import java.sql.Connection;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xml.sax.InputSource;


/**
 * Access method agnostic front end.
 * Calls the right methods, depending on the chosen option (XML, SQL, JAVA, json, etc)
 */
public class BackEndTideComputer
{
  private static String dbLocation = "all-db";
  static
  {
    try { dbLocation = System.getProperty("db.location", ".." + File.separator + "all-db"); }
    catch (Exception ex)
    {
      System.err.println("Are you an Applet? " + ex.getLocalizedMessage() + " (OK).");
    }
  }
  private static String sqliteDb = "sqlite" + File.separator + "tidedb";
  
  public static final int XML_OPTION             = 0; // Using SAX only
  public static final int SQL_OPTION             = 1; // hSQL DB
  public static final int SQLITE_OPTION          = 2;
  public static final int JAVA_SERIALIZED_OPTION = 3;
  public static final int JSON_SERIALIZED_OPTION = 4;
  
  private static int CHOSEN_OPTION = XML_OPTION;

  private static Connection conn = null;
  private static Constituents constituentsObject = null;
  private static Stations stationsObject = null;
  
  private static boolean verbose = false;
  
  public static Stations getStations()
  {
    return stationsObject;
  }
  
  public static Constituents getConstituents()
  {
    return constituentsObject;
  }
  
  public static void connect(int option) throws Exception
  {
    long before = 0L, after = 0L;
    CHOSEN_OPTION = option;
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
        if (dbLocation.startsWith("//"))
          conn = SQLUtil.getServerConnection(dbLocation, "tides", "tides"); // like //localhost:1234/tides
        else
          conn = SQLUtil.getConnection(dbLocation, "TIDES", "tides", "tides");
        break;
      case SQLITE_OPTION:
        try
        {
          conn = SQLiteUtil.getConnection(sqliteDb);
          if (conn == null)
          {
            System.err.println("** Cannot connect");
            throw new RuntimeException("SQLite connection cannot be obtained");
          }
          else
            System.out.println("** Connection to SQLite OK **"); 
        }
        catch (AccessControlException iaex)
        {
          System.err.println("** Cannot connect");
          System.err.println(iaex.getLocalizedMessage());
          // iaex.printStackTrace();
          if (conn == null)
          {
            System.err.println("** Cannot connect");
            throw new RuntimeException("SQLite connection cannot be obtained");
          }
        }
        break;
      case XML_OPTION:
        BackEndXMLTideComputer.setVerbose(verbose);
        if (verbose)
          before = System.currentTimeMillis();
        constituentsObject = BackEndXMLTideComputer.buildConstituents(); // Uses SAX
        stationsObject = BackEndXMLTideComputer.getTideStations();       // Uses SAX
        if (verbose)
        {
          after = System.currentTimeMillis();
          System.out.println("Objects loaded in " + Long.toString(after - before) + " ms");
        }
        break;
      case JAVA_SERIALIZED_OPTION:
        BackEndSerializedTideComputer.setVerbose(verbose);
        if (verbose)
          before = System.currentTimeMillis();
        BackEndSerializedTideComputer.setSerializationFlavor(BackEndSerializedTideComputer.JAVA_SERIALIZATION_FLAVOR);
        constituentsObject = BackEndSerializedTideComputer.loadObject(BackEndSerializedTideComputer.SER_ARCHIVE_STREAM, BackEndSerializedTideComputer.SER_CONSTITUENTS_ENTRY, Constituents.class);
        stationsObject = BackEndSerializedTideComputer.loadObject(BackEndSerializedTideComputer.SER_ARCHIVE_STREAM, BackEndSerializedTideComputer.SER_STATIONS_ENTRY, Stations.class);
        if (verbose)
        {
          after = System.currentTimeMillis();
          System.out.println("Objects loaded in " + Long.toString(after - before) + " ms");
        }
        break;
      case JSON_SERIALIZED_OPTION:
        BackEndSerializedTideComputer.setVerbose(verbose);
        if (verbose)
          before = System.currentTimeMillis();
        BackEndSerializedTideComputer.setSerializationFlavor(BackEndSerializedTideComputer.JSON_SERIALIZATION_FLAVOR);        
        constituentsObject = BackEndSerializedTideComputer.loadObject(BackEndSerializedTideComputer.JSON_ARCHIVE_STREAM, BackEndSerializedTideComputer.JSON_CONSTITUENTS_ENTRY, Constituents.class);
        boolean v2 = false;
        if (!v2) 
          stationsObject = BackEndSerializedTideComputer.loadObject(BackEndSerializedTideComputer.JSON_ARCHIVE_STREAM, BackEndSerializedTideComputer.JSON_STATIONS_ENTRY, Stations.class);
        else
        {
          DecimalFormat DF = new DecimalFormat("00000");
          int nbStation = 1;
          stationsObject = new Stations();
          String radical = "station_";
          String suffix = ".json";
          boolean go = true;
          while (go)
          {
            String resourceName = radical + DF.format(nbStation++) + suffix;
            System.out.println(resourceName);
            try
            {
              TideStation station = BackEndSerializedTideComputer.loadObject(BackEndSerializedTideComputer.JSON_ARCHIVE_STREAM, resourceName, TideStation.class);
              stationsObject.getStations().put(station.getFullName(), station);
            }
            catch (Exception ex)
            {
              go = false;
              ex.printStackTrace();
            }
          }
        }
        if (verbose)
        {
          after = System.currentTimeMillis();
          System.out.println("Objects loaded in " + Long.toString(after - before) + " ms");
        }
        break;
    }
  }
  
  public static void disconnect() throws Exception
  {
    if (CHOSEN_OPTION == SQL_OPTION || CHOSEN_OPTION == SQLITE_OPTION)
      conn.close();
  }
  
  public static List<Coefficient> buildSiteConstSpeed() throws Exception
  {
    List<Coefficient> constSpeed = null;
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        constSpeed = BackEndSQLTideComputer.buildSiteConstSpeed(conn);
        break;
      case XML_OPTION:
      case JAVA_SERIALIZED_OPTION:
      case JSON_SERIALIZED_OPTION:
        constSpeed = buildSiteConstSpeed(constituentsObject);
        break;
    }
    return constSpeed;
  }
  
  public static double getAmplitudeFix(int year, String name) throws Exception
  {
    double d = 0;
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        d = BackEndSQLTideComputer.getAmplitudeFix(conn, year, name);
        break;
      case XML_OPTION:
      case JAVA_SERIALIZED_OPTION:
      case JSON_SERIALIZED_OPTION:
        d = getAmplitudeFix(constituentsObject, year, name);
        break;
    }
    return d;
  }
  
  public static double getEpochFix(int year, String name) throws Exception
  {
    double d = 0;
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        d = BackEndSQLTideComputer.getEpochFix(conn, year, name);
        break;
      case XML_OPTION:
      case JAVA_SERIALIZED_OPTION:
      case JSON_SERIALIZED_OPTION:
        d = getEpochFix(constituentsObject, year, name);
        break;
    }
    return d;
  }
  
  public static TideStation findTideStation(String stationName, int year) throws Exception
  {
    TideStation ts = null;
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        ts = BackEndSQLTideComputer.findTideStation(stationName, year, conn);
        break;
      case XML_OPTION:
      case JAVA_SERIALIZED_OPTION:
      case JSON_SERIALIZED_OPTION:
        ts = findTideStation(stationName, year, constituentsObject, stationsObject);
        break;
    }
    return ts;
  }
  
  public static List<TideStation> getStationData() throws Exception
  {
    List<TideStation> alts = null;
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        alts = BackEndSQLTideComputer.getStationData(conn);
        break;
      case XML_OPTION:
      case JAVA_SERIALIZED_OPTION:
      case JSON_SERIALIZED_OPTION:
        alts = getStationData(stationsObject);
        break;
    }
    return alts;
  }
  
  public static TreeMap<String, TideUtilities.StationTreeNode> buildStationTree()
  {
    TreeMap<String, TideUtilities.StationTreeNode> st = null;
  
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        st = TideUtilities.buildStationTree(conn);
        break;
      case XML_OPTION:
      case JAVA_SERIALIZED_OPTION:
      case JSON_SERIALIZED_OPTION:
        st = TideUtilities.buildStationTree(stationsObject);
        break;
    }      
    return st;
  }
  
  public static InputStream getZipInputStream(String zipStream, String entryName) throws Exception
  {
    ZipInputStream zip = new ZipInputStream(BackEndSerializedTideComputer.class.getResourceAsStream(zipStream));
    InputStream is = null;
    boolean go = true;
    while (go)
    {
      ZipEntry ze = zip.getNextEntry();
      if (ze == null)
        go = false;
      else
      {
        if (ze.getName().equals(entryName))
        {
          is = zip;
          go = false;
        }
      }
    }
    if (is == null)
    {
      throw new RuntimeException("Entry " + entryName + " not found in " + zipStream.toString());
    }
    return is;    
  }
  
  public static InputSource getZipInputSource(String zipStream, String entryName) throws Exception
  {
    ZipInputStream zip = new ZipInputStream(BackEndSerializedTideComputer.class.getResourceAsStream(zipStream));
    InputSource is = null;
    boolean go = true;
    while (go)
    {
      ZipEntry ze = zip.getNextEntry();
      if (ze == null)
        go = false;
      else
      {
        if (ze.getName().equals(entryName))
        {
          is = new InputSource(zip);
          is.setEncoding("ISO-8859-1");
          go = false;
        }
      }
    }
    if (is == null)
    {
      throw new RuntimeException("Entry " + entryName + " not found in " + zipStream.toString());
    }
    return is;    
  }
  
  public static List<Coefficient> buildSiteConstSpeed(Constituents doc) throws Exception
  {
    List<Coefficient> csal = new ArrayList<Coefficient>();
    Map<String, Constituents.ConstSpeed> csm = doc.getConstSpeedMap();
    Set<String> keys = csm.keySet();    
    for (String k : keys)
    {
      Constituents.ConstSpeed cs = csm.get(k);
      Coefficient coef = new Coefficient(cs.getCoeffName(), cs.getCoeffValue() * TideUtilities.COEFF_FOR_EPOCH);
      csal.add(coef);
    }
    return csal;
  }
  
  public static double getAmplitudeFix(Constituents doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
      double f = cs.getFactors().get(year);
      d = f;    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static double getEpochFix(Constituents doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
      double f = cs.getEquilibrium().get(year);
      d = f * TideUtilities.COEFF_FOR_EPOCH;    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static TideStation findTideStation(String stationName, int year, Constituents constituents, Stations stations) throws Exception
  {
    long before = System.currentTimeMillis();
    TideStation station = stations.getStations().get(stationName);
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Finding the node took " + Long.toString(after - before) + " ms");
    
    // Fix for the given year
  //  System.out.println("We are in " + year);
    // Correction to the Harmonics
    for (Harmonic harm : station.getHarmonics())
    {
      String name = harm.getName();
      if (!"x".equals(name))
      {
        double amplitudeFix = getAmplitudeFix(constituents, year, name);
        double epochFix     = getEpochFix(constituents, year, name);
        
        harm.setAmplitude(harm.getAmplitude() * amplitudeFix);
        harm.setEpoch(harm.getEpoch() - epochFix);
      }
    }
    if (verbose) System.out.println("Sites coefficients of [" + station.getFullName() + "] fixed for " + year);
    
    return station;
  }
  
  public static List<TideStation> getStationData(Stations stations) throws Exception
  {
    long before = System.currentTimeMillis();
    List<TideStation> stationData = new ArrayList<TideStation>();
    Set<String> keys = stations.getStations().keySet();
    for (String k : keys)
    {
      try
      {
        stationData.add(stations.getStations().get(k));
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Finding all the stations took " + Long.toString(after - before) + " ms");
    
    return stationData;
  }
  
  public static void setVerbose(boolean v)
  {
    verbose = v;
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        BackEndSQLTideComputer.setVerbose(v);
        break;
      case XML_OPTION:
        BackEndXMLTideComputer.setVerbose(v);
        break;
      case JAVA_SERIALIZED_OPTION:
      case JSON_SERIALIZED_OPTION:
        BackEndSerializedTideComputer.setVerbose(v);
        break;
    }
  }

  public static void setDbLocation(String dbLocation)
  {
    BackEndTideComputer.dbLocation = dbLocation;
  }
}
