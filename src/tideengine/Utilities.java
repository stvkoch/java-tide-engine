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
    TreeMap<String, StationTreeNode> set = new TreeMap<String, StationTreeNode>();
    
    long before = System.currentTimeMillis();
    StationObserver sf = new StationObserver();
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      
      sf.setTreeToPopulate(set);
      
      InputSource is = new InputSource(new FileInputStream(new File(BackEndTideComputer.STATION_FILE)));
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
      for (int i=0; i<(2*level); i++)
        System.out.print(" ");
      System.out.println(stn.toString());
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
    private String label = "";
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
      for (String name : ts.getNameParts())
      {
        StationTreeNode stn = currentTree.get(name);
        if (stn == null)
        {
          stn = new StationTreeNode(name);
          currentTree.put(name, stn);
        }
        currentTree = stn.getSubTree();
      }
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
  //    super.startElement(uri, localName, qName, attributes);
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