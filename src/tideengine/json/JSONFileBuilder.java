package tideengine.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import java.text.DecimalFormat;

import java.util.Set;

import tideengine.TideStation;

import tideengine.Constituents;
import tideengine.ser.SerFilesBuilder;
import tideengine.Stations;


public class JSONFileBuilder
{
  public static void main(String[] args) throws Exception
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    DecimalFormat DF = new DecimalFormat("00000");

    Constituents constituentsObj = null;
    Stations stationsObj = null;
    
    if (args.length == 0 || "MAKE".equalsIgnoreCase(args[0]))
    {
      constituentsObj = SerFilesBuilder.buildConstituents();
      stationsObj = SerFilesBuilder.buildStations();
      System.out.println("Objects are ready, serializing.");
      long before = System.currentTimeMillis();
      try
      {
        String constJsonStr = gson.toJson(constituentsObj);
        BufferedWriter bw = new BufferedWriter(new FileWriter("constituents.json"));
        bw.write(constJsonStr);
        bw.close();
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
        boolean v2 = false;
        if (!v2)
        {
          String constJsonStr = gson.toJson(stationsObj);
          BufferedWriter bw = new BufferedWriter(new FileWriter("stations.json"));
          bw.write(constJsonStr);
          bw.close();
        }
        else // v2
        {
          // TODO Write in the zip
          Set<String> keys = stationsObj.getStations().keySet();
          int nbStation = 1;
          for (String k : keys)
          {
            TideStation station = stationsObj.getStations().get(k);
            String constJsonStr = gson.toJson(station);
            BufferedWriter bw = new BufferedWriter(new FileWriter("station_" + DF.format(nbStation++) + ".json"));
            bw.write(constJsonStr);
            bw.close();
          }
        }
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
        BufferedReader br = new BufferedReader(new FileReader("constituents.json"));
        StringBuffer sb = new StringBuffer();
        String line = "";
        boolean go = true;
        while (go)
        {
          line = br.readLine();
          if (line == null)
            go = false;
          else
            sb.append(line);
        }
        br.close();
        constituentsObj = gson.fromJson(sb.toString(), Constituents.class);        
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
        BufferedReader br = new BufferedReader(new FileReader("stations.json"));
        StringBuffer sb = new StringBuffer();
        String line = "";
        boolean go = true;
        while (go)
        {
          line = br.readLine();
          if (line == null)
            go = false;
          else
            sb.append(line);
        }
        br.close();
        stationsObj = gson.fromJson(sb.toString(), Stations.class);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }      
      after = System.currentTimeMillis();
      System.out.println("Stations deserialized in " + Long.toString(after - before) + " ms.");    }
  }
  
}
