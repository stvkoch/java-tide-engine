package tideengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class BackEndXMLTideComputer
{
  public final static String ARCHIVE_STREAM      = "xml/xml.zip"; 
  public final static String CONSTITUENTS_EMTRY  = "constituents.xml";
  public final static String STATIONS_ENTRY      = "stations.xml";

  private static DOMParser parser = new DOMParser();
  private static boolean verbose  = false;
    
  public static XMLDocument loadDOM(String zipStream, String entryName) throws Exception
  {    
    return loadDOM(getZipInputStream(zipStream, entryName));
  }
    
  public static XMLDocument loadDOM(String resource) throws Exception
  {
    InputStream is = new FileInputStream(new File(resource));
    return loadDOM(is);
  }

  public static XMLDocument loadDOM(InputStream resource) throws Exception 
  {
    XMLDocument doc = null;
    try
    {
      synchronized (parser)
      {
        parser.parse(resource);
        doc = parser.getDocument();
      }
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return doc;
  }
  
  public static List<Coefficient> buildSiteConstSpeed(XMLDocument doc) throws Exception
  {
    List<Coefficient> csal = new ArrayList<Coefficient>();
    String xPath = "//const-speed";
    NodeList nl = doc.selectNodes(xPath);
    for (int i=0; i<nl.getLength(); i++)
    {
      String name = ((XMLElement)nl.item(i)).selectNodes("./coeff-name").item(0).getFirstChild().getNodeValue();
      double value = Double.parseDouble(((XMLElement)nl.item(i)).selectNodes("./coeff-value").item(0).getFirstChild().getNodeValue());
      Coefficient coef = new Coefficient(name, value * TideUtilities.COEFF_FOR_EPOCH);
      csal.add(coef);
    }
    return csal;
  }
  
  public static double getAmplitudeFix(XMLDocument doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      String xPath = "//const-speed[./coeff-name = '" + name + "']/node-factors/factor[./@year='" + Integer.toString(year) + "']";
      NodeList nl = doc.selectNodes(xPath);
      d = Double.parseDouble (((XMLElement)nl.item(0)).getFirstChild().getNodeValue());    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static double getEpochFix(XMLDocument doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      String xPath = "//const-speed[./coeff-name = '" + name + "']/equilibrium-arguments/equilibrium[./@year='" + Integer.toString(year) + "']";
      NodeList nl = doc.selectNodes(xPath);
      d = Double.parseDouble (((XMLElement)nl.item(0)).getFirstChild().getNodeValue()) * TideUtilities.COEFF_FOR_EPOCH;    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static TideStation findTideStation(String stationName, int year, XMLDocument constituents) throws Exception
  {
    return findTideStation(stationName, year, constituents, RegenerateXMLData.getStationFileLocation());
  }

  public static InputStream getZipInputStream(String zipStream, String entryName) throws Exception
  {
    ZipInputStream zip = new ZipInputStream(BackEndXMLTideComputer.class.getResourceAsStream(zipStream));
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
    ZipInputStream zip = new ZipInputStream(BackEndXMLTideComputer.class.getResourceAsStream(zipStream));
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
  
  public static TideStation findTideStation(String stationName, int year, XMLDocument constituents, String zipStream, String entryName) throws Exception
  {
    InputSource is = null;
    try { is = getZipInputSource(zipStream, entryName); } catch (Exception ex) { throw new RuntimeException(ex); }
    return findTideStation(stationName, year, constituents, is);
    
  }

  public static TideStation findTideStation(String stationName, int year, XMLDocument constituents, String stationFile) throws Exception
  {
    InputSource is = new InputSource(new FileInputStream(new File(stationFile)));
    is.setEncoding("ISO-8859-1");
    return findTideStation(stationName, year, constituents, is);
  }

  public static TideStation findTideStation(String stationName, int year, XMLDocument constituents, InputSource is) throws Exception
  {
    TideStation ts = null;
    long before = System.currentTimeMillis();
    StationFinder sf = new StationFinder();
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      
      sf.setStationName(stationName);
//    InputSource is = new InputSource(new FileInputStream(new File(stationFile)));
//    is.setEncoding("ISO-8859-1");
      saxParser.parse(is, sf);
    }
    catch (DoneWithSiteException dwse)
    {
      ts = sf.getTideStation();
//    System.out.println("All right.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Finding the node took " + Long.toString(after - before) + " ms");
    
    // Fix for the given year
//  System.out.println("We are in " + year);
    // Correction to the Harmonics
    for (Harmonic harm : ts.getHarmonics())
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
    if (verbose) System.out.println("Sites coefficients of [" + ts.getFullName() + "] fixed for " + year);
    
    return ts;
  }
  
  public static List<TideStation> getStationData() throws Exception
  {
    long before = System.currentTimeMillis();
    List<TideStation> stationData = new ArrayList<TideStation>();
    StationFinder sf = new StationFinder(stationData);
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();      
      InputSource is = // new InputSource(new FileInputStream(new File(STATION_FILE)));
                       getZipInputSource(BackEndXMLTideComputer.ARCHIVE_STREAM, BackEndXMLTideComputer.STATIONS_ENTRY);
      saxParser.parse(is, sf);       
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Finding all the stations took " + Long.toString(after - before) + " ms");
    
    return stationData;
  }
  
  public static void setVerbose(boolean verbose)
  {
    BackEndXMLTideComputer.verbose = verbose;
  }
  
  public static class StationFinder extends DefaultHandler
  {
    private String stationName = "";
    private TideStation ts = null;
    private List<TideStation> stationArrayList = null;
    
    public void setStationName(String sn)
    {
      this.stationName = sn;
    }
    
    public StationFinder()
    {
    }

    public StationFinder(List<TideStation> al)
    {
      this.stationArrayList = al;
    }
    
    public TideStation getTideStation()
    {
      return ts;
    }

    private boolean foundStation        = false;
    private boolean foundNameCollection = false;
    private boolean foundStationData    = false;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
//    super.startElement(uri, localName, qName, attributes);
      if (!foundStation && "station".equals(qName))
      {
        String name = attributes.getValue("name");
        if (name.contains(this.stationName))
        {
          foundStation = true;
          ts = new TideStation();
          ts.setFullName(name);
        }
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
        else if ("station-data".equals(qName))
        {
          foundStationData = true;
        }
        else if (foundStationData && "harmonic-coeff".equals(qName))
        {
          String name = attributes.getValue("name");
          double amplitude = Double.parseDouble(attributes.getValue("amplitude"));
          double epoch     = Double.parseDouble(attributes.getValue("epoch")) * TideUtilities.COEFF_FOR_EPOCH;
          Harmonic h = new Harmonic(name, amplitude, epoch);
          ts.getHarmonics().add(h);
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
        if (stationArrayList == null)
          throw new DoneWithSiteException("Done with it.");
        else
          stationArrayList.add(ts);
      }
      else if (foundNameCollection && "name-collection".equals(qName))
      {
        foundNameCollection = false;
      }
      else if (foundStationData && "station-data".equals(qName))
      {
        foundStationData = false;
      }
    }
  }
  
  public static class DoneWithSiteException extends SAXException
  {
    public DoneWithSiteException(String s)
    {
      super(s);
    }
  }
}
