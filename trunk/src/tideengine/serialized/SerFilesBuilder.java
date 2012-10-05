package tideengine.serialized;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.TimeZone;
import java.util.Vector;

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

import tideengine.BackEndXMLTideComputer;


public class SerFilesBuilder
{
  public static void main(String[] args) throws Exception
  {
    if (args.length == 0 || "MAKE".equalsIgnoreCase(args[0]))
    {
      buildConstituents();
      buildStations();
    }
    else if ("LOAD".equalsIgnoreCase(args[0]))
    {
      long before = System.currentTimeMillis();
      try
      {
        FileInputStream fin = new FileInputStream("constituents.ser");
        ObjectInputStream ois = new ObjectInputStream(fin);
        Constituents constituentsObj = (Constituents) ois.readObject();
        ois.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      long after = System.currentTimeMillis();
      System.out.println("Constituents deserialized in " + Long.toString(after - before) + " ms.");
      before = System.currentTimeMillis();
      try
      {
        FileInputStream fin = new FileInputStream("stations.ser");
        ObjectInputStream ois = new ObjectInputStream(fin);
        Stations stationsObj = (Stations) ois.readObject();
        ois.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }      
      after = System.currentTimeMillis();
      System.out.println("Stations deserialized in " + Long.toString(after - before) + " ms.");    }
  }
  
  private static void buildConstituents() throws Exception
  {
    long before = System.currentTimeMillis();
    long start = before;    
    XMLDocument constituents = BackEndXMLTideComputer.loadDOM(BackEndXMLTideComputer.ARCHIVE_STREAM, BackEndXMLTideComputer.CONSTITUENTS_ENTRY);
    long after = System.currentTimeMillis();
    System.out.println("Constituents Parsed in " + Long.toString(after - before) + " ms.");
    before = System.currentTimeMillis();
    Constituents constObj = new Constituents();
    NodeList speedConstList = constituents.selectNodes("/constituents/speed-constituents/const-speed");
//  System.out.println("We have " + speedConstList.getLength() + " speed constituent(s).");
    for (int i=0; i<speedConstList.getLength(); i++)
    {
      XMLElement constSpeed = (XMLElement)speedConstList.item(i);
      String name = constSpeed.selectNodes("./coeff-name").item(0).getTextContent();
      Float val = Float.parseFloat(constSpeed.selectNodes("./coeff-value").item(0).getTextContent());
      Constituents.ConstSpeed cs = new Constituents.ConstSpeed(i+1, name, val);
      constObj.getConstSpeedMap().put(name, cs);
      NodeList equ = constSpeed.selectNodes("./equilibrium-arguments/equilibrium");
//    System.out.println("-- We have " + equ.getLength() + " equilibrium(s).");
      for (int e=0; e<equ.getLength(); e++)
      {
        XMLElement equilibrium = (XMLElement)equ.item(e);
        int year = Integer.parseInt(equilibrium.getAttribute("year"));
        float value = Float.parseFloat(equilibrium.getTextContent());
        cs.putEquilibrium(year, value);
      }
      
      NodeList fac = constSpeed.selectNodes("./node-factors/factor");
//    System.out.println("-- We have " + fac.getLength() + " factors(s).");
      for (int f=0; f<fac.getLength(); f++)
      {
        XMLElement factor = (XMLElement)fac.item(f);
        int year = Integer.parseInt(factor.getAttribute("year"));
        float value = Float.parseFloat(factor.getTextContent());
        cs.putFactor(year, value);
      }
    }
    after = System.currentTimeMillis();
    System.out.println("Constituents Objects built in " + Long.toString(after - before) + " ms.");  
  
    before = System.currentTimeMillis();
    try
    {
      FileOutputStream fout = new FileOutputStream("constituents.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(constObj);
      oos.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    after = System.currentTimeMillis();
    System.out.println("Constituents Objects serialized in " + Long.toString(after - before) + " ms.");  
    System.out.println("Constituents Over all: " + Long.toString(after - start) + " ms.");  
  }

  private static void buildStations() throws Exception
  {
    long before = System.currentTimeMillis();
    long start = before;    
    XMLDocument stationsDocument = BackEndXMLTideComputer.loadDOM(BackEndXMLTideComputer.ARCHIVE_STREAM, BackEndXMLTideComputer.STATIONS_ENTRY);
    long after = System.currentTimeMillis();
    System.out.println("Stations Parsed in " + Long.toString(after - before) + " ms.");
    before = System.currentTimeMillis();
    Stations stationObj = new Stations();
    NodeList stationList = stationsDocument.selectNodes("/stations/station");
  //  System.out.println("We have " + speedConstList.getLength() + " speed constituent(s).");
    for (int i=0; i<stationList.getLength(); i++)
    {
      XMLElement station = (XMLElement)stationList.item(i);
      String name = station.getAttribute("name");
      Vector<String> names = new Vector<String>();
      NodeList nameNL = station.selectNodes("./name-collection/name-part");
      for (int n=0; n<nameNL.getLength(); n++)
        names.addElement(((XMLElement)nameNL.item(n)).getAttribute("name"));
      XMLElement pos = (XMLElement)station.selectNodes("./position").item(0);
      Stations.StationPosition position = new Stations.StationPosition(Double.parseDouble(pos.getAttribute("latitude")), Double.parseDouble(pos.getAttribute("longitude")));
      TimeZone tz = TimeZone.getTimeZone(((XMLElement)station.selectNodes("./time-zone").item(0)).getAttribute("name"));
      XMLElement height = (XMLElement)station.selectNodes("./base-height").item(0);
      Stations.BaseHeight bh = new Stations.BaseHeight(Double.parseDouble(height.getAttribute("value")), height.getAttribute("unit"));
      Stations.Station tideStation = new Stations.Station(name, names, position, tz, bh);
      NodeList coeff = station.selectNodes("./station-data/harmonic-coeff");
      for (int c=0; c<coeff.getLength(); c++)
      {
        XMLElement ch = (XMLElement)coeff.item(c);
        Stations.HarmonicCoeff hc = new Stations.HarmonicCoeff(c+1, ch.getAttribute("name"), Float.parseFloat(ch.getAttribute("amplitude")), Float.parseFloat(ch.getAttribute("epoch")));
        tideStation.getHarmonicCoeffList().put(ch.getAttribute("name"), hc);
      }
      stationObj.getStations().put(name, tideStation);
    }
    after = System.currentTimeMillis();
    System.out.println("Stations Objects built in " + Long.toString(after - before) + " ms.");  
  
    before = System.currentTimeMillis();
    try
    {
      FileOutputStream fout = new FileOutputStream("stations.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(stationObj);
      oos.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    after = System.currentTimeMillis();
    System.out.println("Stations Objects serialized in " + Long.toString(after - before) + " ms.");  
    System.out.println("Stations Over all: " + Long.toString(after - start) + " ms.");  
  }
}
