package tideengine;

import java.io.File;
import java.io.FileInputStream;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TideUtilities
{
  private final static boolean verbose = false;
  public final static double FEET_2_METERS   = 0.30480061d; // US feet to meters
  public final static double COEFF_FOR_EPOCH = 0.017453292519943289D;
  public final static DecimalFormat DF22 = new DecimalFormat("#0.00");
  public final static DecimalFormat DF22PLUS = new DecimalFormat("#0.00");
  static 
  {
    DF22PLUS.setPositivePrefix("+");  
  }
  
  public static TreeMap<String, StationTreeNode> buildStationTree()
  {
    return buildStationTree(BackEndXMLTideComputer.STATION_FILE);
  }
  public static TreeMap<String, StationTreeNode> buildStationTree(String stationFileName)
  {
    TreeMap<String, StationTreeNode> set = new TreeMap<String, StationTreeNode>();
    
    long before = System.currentTimeMillis();
    StationObserver sf = new StationObserver();
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      
      sf.setTreeToPopulate(set);
      
      InputSource is = new InputSource(new FileInputStream(new File(stationFileName)));
      is.setEncoding("ISO-8859-1");
      saxParser.parse(is, sf);       
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Populating the tree took " + Long.toString(after - before) + " ms");
    
    return set;
  }
  
  public static TreeMap<String, StationTreeNode> buildStationTree(Connection conn) 
  {
    TreeMap<String, StationTreeNode> set = new TreeMap<String, StationTreeNode>();
    String stationQuery = "select name, latitude, longitude, tzoffset, tzname, baseheightvalue, baseheightunit from stations";
    
    long before = System.currentTimeMillis();
    try
    {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(stationQuery);
      while (rs.next())
      {
        TideStation ts = new TideStation();
        ts.setFullName(rs.getString(1));
        ts.setLatitude(rs.getDouble(2));
        ts.setLongitude(rs.getDouble(3));
        ts.setTimeOffset(rs.getString(4));
        ts.setTimeZone(rs.getString(5));
        ts.setBaseHeight(rs.getDouble(6));
        ts.setUnit(rs.getString(7));
        String[] np = ts.getFullName().split(",");
        for (int i=0; i<np.length; i++)
          ts.getNameParts().add(np[(np.length - 1) - i]);
        addStationToTree(ts, set);
      }
      rs.close();
      stmt.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Populating the tree took " + Long.toString(after - before) + " ms");
    
    return set;
  }

  /**
   * Rendering on System.out
   * 
   * @param tree
   * @param level
   */
  public static void renderTree(TreeMap<String, StationTreeNode> tree, int level)
  {
    Set<String> keys = tree.keySet();
    for (String key : keys)
    {
      StationTreeNode stn = tree.get(key);
      for (int i=0; i<(2*level); i++) // Indentation
        System.out.print(" ");
      System.out.println(stn.toString()); // Station name
      if (stn.getSubTree().size() > 0)
        renderTree(stn.getSubTree(), level + 1);
    }
  }

  public static double feetToMeters(double d)
  {
    return d * FEET_2_METERS;  
  }
  public static double metersToFeet(double d)
  {
    return d / FEET_2_METERS;  
  }

  public static double getWaterHeight(Date d, Date jan1st, TideStation ts, ArrayList<Coefficient> constSpeed)
  {
    double value = 0d;
    
    double stationBaseHeight = ts.getBaseHeight();
    long nbSecSinceJan1st = (d.getTime() - jan1st.getTime() ) / 1000L;
    double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
    value = stationBaseHeight;
    for (int i=0; i<constSpeed.size(); i++)
    {
      value += (ts.getHarmonics().get(i).getAmplitude() * Math.cos(constSpeed.get(i).getValue() * timeOffset - ts.getHarmonics().get(i).getEpoch()));
      if (verbose)
        System.out.println("Coefficient:" + ts.getHarmonics().get(i).getName() + " ampl:" + ts.getHarmonics().get(i).getAmplitude() + ", epoch:" + ts.getHarmonics().get(i).getEpoch());
    }
    if (verbose) System.out.println("-----------------------------");
    if (ts.getUnit().indexOf("^2") > -1)
      value = (value >= 0.0D ? Math.sqrt(value): -Math.sqrt(-value));
    
    return value;
  }

  public static double getHarmonicValue(Date d, 
                                        Date jan1st, 
                                        TideStation ts, 
                                        ArrayList<Coefficient> constSpeed, 
                                        int constSpeedIdx)
  {
    double value = 0d;
    
    double stationBaseHeight = ts.getBaseHeight();
    long nbSecSinceJan1st = (d.getTime() - jan1st.getTime()) / 1000L;
    double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
    value = stationBaseHeight;
    value += (ts.getHarmonics().get(constSpeedIdx).getAmplitude() * Math.cos(constSpeed.get(constSpeedIdx).getValue() * timeOffset - ts.getHarmonics().get(constSpeedIdx).getEpoch()));
       
    if (ts.getUnit().indexOf("^2") > -1)
      value = (value >= 0.0D ? Math.sqrt(value): -Math.sqrt(-value));
    
    return value;
  }

  public static double getWaterHeight(TideStation ts, ArrayList<Coefficient> constSpeed, Calendar when) throws Exception
  {
    double wh = 0d;
    if (ts != null)
    {      
      // Calculate min/max, for the graph
      int year = when.get(Calendar.YEAR); 
      // Calc Jan 1st of the current year
      Date jan1st = new GregorianCalendar(year, 0, 1).getTime();
      wh = getWaterHeight(when.getTime(), jan1st, ts, constSpeed);
//    System.out.println("Water Height in " + ts.getFullName() + " on " + now.toString() + " is " + DF22.format(wh) + " " + ts.getUnit());
    }
    else
      if (verbose) System.out.println("Ooch!");    
    
    return wh;
  }
  
  public final static int MIN_POS = 0;
  public final static int MAX_POS = 1;
  
  public static double[] getMinMaxWH(TideStation ts, ArrayList<Coefficient> constSpeed, Calendar when) throws Exception
  {
    double[] minMax = { 0d, 0d };
    if (ts != null)
    {      
      // Calculate min/max, for the graph
      int year = when.get(Calendar.YEAR); 
      // Calc Jan 1st of the current year
      Date jan1st = new GregorianCalendar(year, 0, 1).getTime();
      Date dec31st = new GregorianCalendar(year, 11, 31, 12, 0).getTime(); // 31 Dec, At noon
      double max = -Double.MAX_VALUE;
      double min =  Double.MAX_VALUE;
      Date date = jan1st;
      while (date.before(dec31st))
      {
        double d = getWaterHeight(date, jan1st, ts, constSpeed);
        max = Math.max(max, d);
        min = Math.min(min, d);

        date = new Date(date.getTime() + (7200 * 1000)); // Plus 2 hours
      }
  //  System.out.println("In " + year + ", Min:" + min + ", Max:" + max);
      minMax[MIN_POS] = min;
      minMax[MAX_POS] = max;
    }
    return minMax;
  }

  public static double[] getMinMaxWH(TideStation ts, ArrayList<Coefficient> constSpeed, Calendar from, Calendar to) throws Exception
  {
    double[] minMax = { 0d, 0d };
    if (ts != null)
    {      
      double max = -Double.MAX_VALUE;
      double min =  Double.MAX_VALUE;
      // Calculate min/max, for the graph
      Calendar date = (Calendar)from.clone();
      while (date.getTime().before(to.getTime()))
      {
        // Calc Jan 1st of the current year
        Date jan1st = new GregorianCalendar(date.get(Calendar.YEAR), 0, 1).getTime();
        double d = getWaterHeight(date.getTime(), jan1st, ts, constSpeed);
        max = Math.max(max, d);
        min = Math.min(min, d);

        date.add(Calendar.HOUR, 2); // Plus 2 hours
      }
  //  System.out.println("In " + year + ", Min:" + min + ", Max:" + max);
      minMax[MIN_POS] = min;
      minMax[MAX_POS] = max;
    }
    return minMax;
  }

  public static double getWaterHeightIn(double d, TideStation ts, String unit)
  {
    double val = d;
    if (ts.isCurrentStation())
      throw new RuntimeException(ts.getFullName()+" is a current station. Method getWaterHeightIn applies only to tide stations.");
    if (!unit.equals(ts.getUnit()))
    {
      if (!unit.equals(TideStation.METERS) && !unit.equals(TideStation.FEET))
        throw new RuntimeException("Unsupported unit [" + unit +"]. Only " + TideStation.METERS + " or " + TideStation.FEET + " please.");
      if (unit.equals(TideStation.METERS) && ts.getUnit().equals(TideStation.FEET))
        val *= FEET_2_METERS;
      else
        val /= FEET_2_METERS;
    }
    return val;
  }
  
  private static void addStationToTree(tideengine.TideStation ts, TreeMap<String, TideUtilities.StationTreeNode> currentTree)
  {
    String timeZoneLabel = "";
    try { timeZoneLabel = ts.getTimeZone().substring(0, ts.getTimeZone().indexOf("/")); } catch (Exception ex) { System.err.println(ex.toString() + " for " + ts.getFullName() + " , " + ts.getTimeZone()); }
    StationTreeNode tzstn = currentTree.get(timeZoneLabel);
    if (tzstn == null)
    {
      tzstn = new StationTreeNode(timeZoneLabel);
      currentTree.put(timeZoneLabel, tzstn);
    }
    currentTree = tzstn.getSubTree();
    String timeZoneLabel2 = ts.getTimeZone().substring(ts.getTimeZone().indexOf("/") + 1);      
    tzstn = currentTree.get(timeZoneLabel2);
    if (tzstn == null)
    {
      tzstn = new StationTreeNode(timeZoneLabel2);
      currentTree.put(timeZoneLabel2, tzstn);
    }
    currentTree = tzstn.getSubTree();
    
    StationTreeNode stn = null;
    for (String name : ts.getNameParts())
    {
      stn = currentTree.get(name);
      if (stn == null)
      {
        stn = new StationTreeNode(name);
        stn.setStationType(ts.isCurrentStation()? tideengine.TideUtilities.StationTreeNode.CURRENT_STATION: tideengine.TideUtilities.StationTreeNode.TIDE_STATION);
        currentTree.put(name, stn);
      }
      currentTree = stn.getSubTree();
    }
    stn.setFullStationName(ts.getFullName());
  // currentTree.put(ts.getFullName(), new StationTreeNode(ts.getFullName()));
  }
  
  public static class StationTreeNode implements Comparable
  {
    public final static int TIDE_STATION = 1;
    public final static int CURRENT_STATION = 2;
    
    private String label = "";
    private String fullStationName = null;
    private int stationType = 0;
    private TreeMap<String, StationTreeNode> subTree = new TreeMap<String, StationTreeNode>();
    
    public StationTreeNode(String label)
    {
      this.label = label;
    }
    
    @Override
    public String toString()
    {
      return this.label;
    }

    public int compareTo(Object o)
    {
      return this.label.compareTo(o.toString());
    }

    public TreeMap<String, TideUtilities.StationTreeNode> getSubTree()
    {
      return subTree;
    }

    public void setFullStationName(String fullStationName)
    {
      this.fullStationName = fullStationName;
    }

    public String getFullStationName()
    {
      return fullStationName;
    }

    public void setStationType(int stationType)
    {
      this.stationType = stationType;
    }

    public int getStationType()
    {
      return stationType;
    }
  }

  public static class StationObserver extends DefaultHandler
  {
    private TideStation ts = null;
    
    private boolean foundStation        = false;
    private boolean foundNameCollection = false;

    private TreeMap<String, TideUtilities.StationTreeNode> tree = null;

    public void setTreeToPopulate(TreeMap<String, TideUtilities.StationTreeNode> tree)
    {
      this.tree = tree;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
  //    super.startElement(uri, localName, qName, attributes);
      if (!foundStation && "station".equals(qName))
      {
        String name = attributes.getValue("name");
        foundStation = true;
        ts = new TideStation();
        ts.setFullName(name);
      }
      else if (foundStation)
      {
        if ("name-collection".equals(qName))
        {
          foundNameCollection = true;
        }
        else if ("name-part".equals(qName) && foundNameCollection)
        {
          ts.getNameParts().add(attributes.getValue("name"));
        }
        else if ("position".equals(qName))
        {
          ts.setLatitude(Double.parseDouble(attributes.getValue("latitude")));
          ts.setLongitude(Double.parseDouble(attributes.getValue("longitude")));
        }
        else if ("time-zone".equals(qName))
        {
          ts.setTimeZone(attributes.getValue("name"));
          ts.setTimeOffset(attributes.getValue("offset"));
        }
        else if ("base-height".equals(qName))
        {
          ts.setBaseHeight(Double.parseDouble(attributes.getValue("value")));
          ts.setUnit(attributes.getValue("unit"));
        }
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      if (foundStation && "station".equals(qName))
      {
        foundStation = false;
        addStationToTree(ts, tree);
      }
      else if (foundNameCollection && "name-collection".equals(qName))
      {
        foundNameCollection = false;
      }
    }
  }

      
  public static void main(String[] args)
  {
    TreeMap<String, StationTreeNode> tree = buildStationTree();
    renderTree(tree, 0);    
  }
}