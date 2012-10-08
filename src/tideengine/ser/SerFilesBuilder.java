package tideengine.ser;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tideengine.BackEndXMLTideComputer;
import tideengine.Constituents;
import tideengine.Stations;


public class SerFilesBuilder
{
  public static void main(String[] args) throws Exception
  {
    Constituents constituentsObj = null;
    Stations stationsObj = null;
    
    if (args.length == 0 || "MAKE".equalsIgnoreCase(args[0]))
    {
      constituentsObj = buildConstituents();
      stationsObj = buildStations();

      long before = System.currentTimeMillis();
      try
      {
        FileOutputStream fout = new FileOutputStream("constituents.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(constituentsObj);
        oos.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      long after = System.currentTimeMillis();
      System.out.println("Constituents Objects serialized in " + Long.toString(after - before) + " ms.");  

      
      before = System.currentTimeMillis();
      try
      {
        FileOutputStream fout = new FileOutputStream("stations.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(stationsObj);
        oos.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      after = System.currentTimeMillis();
      System.out.println("Stations Objects serialized in " + Long.toString(after - before) + " ms.");  
    }
    else if ("LOAD".equalsIgnoreCase(args[0]))
    {
      long before = System.currentTimeMillis();
      try
      {
        FileInputStream fin = new FileInputStream("constituents.ser");
        ObjectInputStream ois = new ObjectInputStream(fin);
        constituentsObj = (Constituents) ois.readObject();
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
        stationsObj = (Stations) ois.readObject();
        ois.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }      
      after = System.currentTimeMillis();
      System.out.println("Stations deserialized in " + Long.toString(after - before) + " ms.");    }
  }
  
  public static Constituents buildConstituents() throws Exception
  {
    return BackEndXMLTideComputer.buildConstituents();
  }

  public static Stations buildStations() throws Exception
  {
    return BackEndXMLTideComputer.getTideStations();
  }
}
