package tideengine;


import coreutilities.sql.SQLUtil;
import coreutilities.sql.SQLiteUtil;

import java.io.File;

import java.security.AccessControlException;

import java.sql.Connection;

import java.util.List;
import java.util.TreeMap;

import oracle.xml.parser.v2.XMLDocument;

import tideengine.serialized.Constituents;
import tideengine.serialized.Stations;


/**
 * Access method agnostic front end.
 * Calls the right methods, depending on the chosen option (XML or SQL)
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
  
  public static final int XML_OPTION        = 0;
  public static final int SQL_OPTION        = 1;
  public static final int SQLITE_OPTION     = 2;
  public static final int SERIALIZED_OPTION = 3;
  
  private static int CHOSEN_OPTION = XML_OPTION;

  private static XMLDocument constituents = null;  
  private static Connection conn = null;
  private static Constituents constituentsObject = null;
  private static Stations stationsObject = null;
  
  public static void connect(int option) throws Exception
  {
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
//      constituents = BackEndXMLTideComputer.loadDOM(constituentFileLocation);
        constituents = BackEndXMLTideComputer.loadDOM(BackEndXMLTideComputer.ARCHIVE_STREAM, BackEndXMLTideComputer.CONSTITUENTS_ENTRY);
        break;
      case SERIALIZED_OPTION:
        constituentsObject = (Constituents)BackEndSerializedTideComputer.loadObject(BackEndSerializedTideComputer.ARCHIVE_STREAM, BackEndSerializedTideComputer.CONSTITUENTS_ENTRY);
        stationsObject = (Stations)BackEndSerializedTideComputer.loadObject(BackEndSerializedTideComputer.ARCHIVE_STREAM, BackEndSerializedTideComputer.STATIONS_ENTRY);
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
      case XML_OPTION:
        constSpeed = BackEndXMLTideComputer.buildSiteConstSpeed(constituents);
        break;
      case SQL_OPTION:
      case SQLITE_OPTION:
        constSpeed = BackEndSQLTideComputer.buildSiteConstSpeed(conn);
        break;
      case SERIALIZED_OPTION:
        constSpeed = BackEndSerializedTideComputer.buildSiteConstSpeed(constituentsObject);
        break;
    }
    return constSpeed;
  }
  
  public static double getAmplitudeFix(int year, String name) throws Exception
  {
    double d = 0;
    switch (CHOSEN_OPTION)
    {
      case XML_OPTION:
        d = BackEndXMLTideComputer.getAmplitudeFix(constituents, year, name);
        break;
      case SQL_OPTION:
      case SQLITE_OPTION:
        d = BackEndSQLTideComputer.getAmplitudeFix(conn, year, name);
        break;
      case SERIALIZED_OPTION:
        d = BackEndSerializedTideComputer.getAmplitudeFix(constituentsObject, year, name);
        break;
    }
    return d;
  }
  
  public static double getEpochFix(int year, String name) throws Exception
  {
    double d = 0;
    switch (CHOSEN_OPTION)
    {
      case XML_OPTION:
        d = BackEndXMLTideComputer.getEpochFix(constituents, year, name);
        break;
      case SQL_OPTION:
      case SQLITE_OPTION:
        d = BackEndSQLTideComputer.getEpochFix(conn, year, name);
        break;
      case SERIALIZED_OPTION:
        d = BackEndSerializedTideComputer.getEpochFix(constituentsObject, year, name);
        break;
    }
    return d;
  }
  
  public static TideStation findTideStation(String stationName, int year) throws Exception
  {
    TideStation ts = null;
    switch (CHOSEN_OPTION)
    {
      case XML_OPTION:
        ts = BackEndXMLTideComputer.findTideStation(stationName, year, constituents, BackEndXMLTideComputer.ARCHIVE_STREAM, BackEndXMLTideComputer.STATIONS_ENTRY);
        break;
      case SQL_OPTION:
      case SQLITE_OPTION:
        ts = BackEndSQLTideComputer.findTideStation(stationName, year, conn);
        break;
      case SERIALIZED_OPTION:
        ts = BackEndSerializedTideComputer.findTideStation(stationName, year, constituentsObject, stationsObject);
        break;
    }
    return ts;
  }
  
  public static List<TideStation> getStationData() throws Exception
  {
    List<TideStation> alts = null;
    switch (CHOSEN_OPTION)
    {
      case XML_OPTION:
        alts = BackEndXMLTideComputer.getStationData();
        break;
      case SQL_OPTION:
      case SQLITE_OPTION:
        alts = BackEndSQLTideComputer.getStationData(conn);
        break;
      case SERIALIZED_OPTION:
        alts = BackEndSerializedTideComputer.getStationData(stationsObject);
        break;
    }
    return alts;
  }
  
  public static TreeMap<String, TideUtilities.StationTreeNode> buildStationTree()
  {
    TreeMap<String, TideUtilities.StationTreeNode> st = null;
  
    switch (CHOSEN_OPTION)
    {
      case XML_OPTION:
//      st = TideUtilities.buildStationTree(stationFileLocation);
        try
        {
          st = TideUtilities.buildStationTree(BackEndXMLTideComputer.getZipInputSource(BackEndXMLTideComputer.ARCHIVE_STREAM, BackEndXMLTideComputer.STATIONS_ENTRY));
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
        break;
      case SQL_OPTION:
      case SQLITE_OPTION:
        st = TideUtilities.buildStationTree(conn);
        break;
      case SERIALIZED_OPTION:
        st = TideUtilities.buildStationTree(stationsObject);
        break;
    }      
    return st;
  }
  
  public static void setVerbose(boolean v)
  {
    switch (CHOSEN_OPTION)
    {
      case SQL_OPTION:
      case SQLITE_OPTION:
        BackEndSQLTideComputer.setVerbose(v);
        break;
      case XML_OPTION:
        BackEndXMLTideComputer.setVerbose(v);
        break;
      case SERIALIZED_OPTION:
        BackEndSerializedTideComputer.setVerbose(v);
        break;
    }
  }

  public static void setDbLocation(String dbLocation)
  {
    BackEndTideComputer.dbLocation = dbLocation;
  }
}
