package tideengine;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;


public class BackEndSerializedTideComputer
{
  public final static String SER_ARCHIVE_STREAM      = "ser/ser.zip"; 
  public final static String SER_CONSTITUENTS_ENTRY  = "constituents.ser";
  public final static String SER_STATIONS_ENTRY      = "stations.ser";

  public final static String JSON_ARCHIVE_STREAM      = "json/json.zip"; 
  public final static String JSON_CONSTITUENTS_ENTRY  = "constituents.json";
  public final static String JSON_STATIONS_ENTRY      = "stations.json";
  
  public final static int JAVA_SERIALIZATION_FLAVOR = 1;
  public final static int JSON_SERIALIZATION_FLAVOR = 2;
  
  private static int serializationFlavor = JSON_SERIALIZATION_FLAVOR;

  private static boolean verbose  = false;
  private static Gson gson = null;
    
  public static <T> T loadObject(String zipStream, String entryName, Class<T> cl) throws Exception
  {    
    return loadObject(BackEndTideComputer.getZipInputStream(zipStream, entryName), cl);
  }
    
  public static <T> T loadObject(String resource, Class<T> cl) throws Exception
  {
    InputStream is = new FileInputStream(new File(resource));
    return loadObject(is, cl);
  }

  public static <T> T loadObject(InputStream resource, Class<T> cl) throws Exception 
  {
    T tideObject = null;
    try
    {
      if (serializationFlavor == JAVA_SERIALIZATION_FLAVOR)
      {
        ObjectInputStream ois = new ObjectInputStream(resource);
        tideObject = (T)ois.readObject();
        ois.close();
      }
      else if (serializationFlavor == JSON_SERIALIZATION_FLAVOR)
      {
        BufferedReader br = new BufferedReader(new InputStreamReader(resource));
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
        if (gson == null)
          gson = new GsonBuilder().setPrettyPrinting().create();
        tideObject = gson.fromJson(sb.toString(), cl);        
      }
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return tideObject;
  }
  
  public static void setVerbose(boolean verbose)
  {
    BackEndSerializedTideComputer.verbose = verbose;
  }

  public static void setSerializationFlavor(int serializationFlavor)
  {
    BackEndSerializedTideComputer.serializationFlavor = serializationFlavor;
  }
}
