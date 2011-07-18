package tideengine;

import java.io.File;
import java.io.FileInputStream;

import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Utilities
{
  private final static boolean verbose = true;
  
  public static TreeMap<String, StationTreeNode> buildStationTree()
  {
    return buildStationTree(BackEndTideComputer.STATION_FILE);
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
    
  public static void main(String[] args)
  {
    TreeMap<String, StationTreeNode> tree = buildStationTree();
    renderTree(tree, 0);    
  }
  
  public static class StationTreeNode
    implements Comparable
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

    public TreeMap<String, Utilities.StationTreeNode> getSubTree()
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
    private BackEndTideComputer.TideStation ts = null;
    
    private boolean foundStation        = false;
    private boolean foundNameCollection = false;

    private TreeMap<String, Utilities.StationTreeNode> tree = null;

    private void addStationToTree(BackEndTideComputer.TideStation ts)
    {
      TreeMap<String, Utilities.StationTreeNode> currentTree = tree;
      String timeZoneLabel = ts.getTimeZone().substring(0, ts.getTimeZone().indexOf("/"));      
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
          stn.setStationType(ts.isCurrentStation()?Utilities.StationTreeNode.CURRENT_STATION:Utilities.StationTreeNode.TIDE_STATION);
          currentTree.put(name, stn);
        }
        currentTree = stn.getSubTree();
      }
      stn.setFullStationName(ts.getFullName());
   // currentTree.put(ts.getFullName(), new StationTreeNode(ts.getFullName()));      
    }

    public void setTreeToPopulate(TreeMap<String, Utilities.StationTreeNode> tree)
    {
      this.tree = tree;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
  //  super.startElement(uri, localName, qName, attributes);
      if (!foundStation && "station".equals(qName))
      {
        String name = attributes.getValue("name");
        foundStation = true;
        ts = new BackEndTideComputer.TideStation();
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
        addStationToTree(ts);
      }
      else if (foundNameCollection && "name-collection".equals(qName))
      {
        foundNameCollection = false;
      }
    }
  }
}