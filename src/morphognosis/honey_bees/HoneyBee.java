// For conditions of distribution and use, see copyright notice in Main.java

// Honeybee: morphognosis organism.

package morphognosis.honey_bees;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Vector;
import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import morphognosis.Morphognostic.Neighborhood;
import morphognosis.Orientation;
import morphognosis.Utility;

public class HoneyBee
{
   // Properties.
   public int          x, y;
   public int          orientation;
   public boolean nectar_carry;
   public World         world;
   public int          driver;
   public int          driverResponse;
   public int          randomSeed;
   public SecureRandom random;

   // Sensors.   
   public static final int HIVE_PRESENCE_INDEX         = 0;
   public static final int ADJACENT_FLOWER_NECTAR_QUANTITY_INDEX       = 1;
   public static final int ADJACENT_BEE_ORIENTATION_INDEX        = 2; 
   public static final int ADJACENT_BEE_NECTAR_DISTANCE_INDEX = 3;
   public static final int NUM_SENSORS             = 4;
   float[] sensors;

   // Response.
   // First responses are turns in compass directions.
   public static final int FORWARD       = Compass.NUM_COMPASS; 
   public static final int EXTRACT_NECTAR        = FORWARD + 1;
   public static final int DEPOSIT_NECTAR         = EXTRACT_NECTAR + 1;
   public static final int DISPLAY_NECTAR_DISTANCE         = DEPOSIT_NECTAR + 1;
   public static final int WAIT          = DISPLAY_NECTAR_DISTANCE + 1;  
   public static final int NUM_RESPONSES = WAIT + 1;
   int response;

   // Metamorph dataset file name.
   public static String METAMORPH_DATASET_FILE_NAME = "metamorphs.csv";
   
   // Maximum distance between equivalent morphognostics.
   public static float EQUIVALENT_MORPHOGNOSTIC_DISTANCE = 0.0f;

   // Current morphognostic.
   public Morphognostic morphognostic;

   // Metamorphs.
   public ArrayList<Metamorph> metamorphs;

   // Navigation.
   public boolean[][] landmarkMap;
   public int         maxEventAge;
   public class Event
   {
	   // Values: 
	   // { <hive presence>, <adjacent flower nectar quantity>, <adjacent bee orientation>, <adjacent bee nectar distance> }
	   // <adjacent bee orientation>: [<compass point>]	   
      public int[] values;
      public int   x;
      public int   y;
      public int   time;
      public Event(int[] values, int x, int y, int time)
      {
         int n = values.length;

         this.values = new int[n];
         for (int i = 0; i < n; i++)
         {
            this.values[i] = values[i];
         }
         this.x    = x;
         this.y    = y;
         this.time = time;
      }
   }
   public Vector<Event> events;
   public int           eventTime;

   // Driver type.
   public enum DRIVER_TYPE
   {
      AUTOPILOT(0),
      METAMORPH_RULES(1),
      MANUAL(2);

      private int value;

      DRIVER_TYPE(int value)
      {
         this.value = value;
      }

      public int getValue()
      {
         return(value);
      }
   }

   // Autopilot response.
   int     state;
   int     radius;
   int     ring;
   int     step;
   boolean smoothStep = true;
   int     spoke;
   int     spokeIndex;
   int     spokeDir;
   class SpokePoint
   {
      int     x, y;
      boolean raise;
      boolean lower;
      SpokePoint(int x, int y)
      {
         this.x = x;
         this.y = y;
      }
   };
   ArrayList<SpokePoint> spokePath;

   // Constructors.
   public HoneyBee(World world, int randomSeed)
   {
      this.world       = world;
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
      init();
      int [] numEventTypes = new int[NUM_SENSORS];
      for (int i = 0, j = NUM_SENSORS - 1; i < j; i++)
      {
         numEventTypes[i] = Nest.MAX_ELEVATION + 1;
      }
      numEventTypes[NUM_SENSORS - 1] = HoneyBee.NUM_RESPONSES;
      morphognostic = new Morphognostic(Orientation.NORTH, numEventTypes);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxEventAge = n.epoch + n.duration - 1;
      metamorphs  = new ArrayList<Metamorph>();
   }


   public HoneyBee(World world, int randomSeed,
                     int NUM_NEIGHBORHOODS,
                     int NEIGHBORHOOD_INITIAL_DIMENSION,
                     int NEIGHBORHOOD_DIMENSION_STRIDE,
                     int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                     int EPOCH_INTERVAL_STRIDE,
                     int EPOCH_INTERVAL_MULTIPLIER)
   {
      this.world       = world;
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
      init();
      int [] numEventTypes = new int[NUM_SENSORS];
      for (int i = 0, j = NUM_SENSORS - 1; i < j; i++)
      {
         numEventTypes[i] = Nest.MAX_ELEVATION + 1;
      }
      numEventTypes[NUM_SENSORS - 1] = HoneyBee.NUM_RESPONSES;
      morphognostic = new Morphognostic(Orientation.NORTH, numEventTypes,
                                        NUM_NEIGHBORHOODS,
                                        NEIGHBORHOOD_INITIAL_DIMENSION,
                                        NEIGHBORHOOD_DIMENSION_STRIDE,
                                        NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                        EPOCH_INTERVAL_STRIDE,
                                        EPOCH_INTERVAL_MULTIPLIER);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxEventAge = n.epoch + n.duration - 1;
      metamorphs  = new ArrayList<Metamorph>();
   }


   // Initialize.
   void init()
   {
      x           = x2 = nest.size.width / 2;
      y           = y2 = nest.size.height / 2;
      orientation = orientation2 = Orientation.NORTH;
      sensors     = new float[NUM_SENSORS];
      for (int n = 0; n < NUM_SENSORS; n++)
      {
         sensors[n] = 0.0f;
      }
      response       = WAIT;
      driver         = DRIVER_TYPE.AUTOPILOT.getValue();
      driverResponse = WAIT;
      landmarkMap    = new boolean[nest.size.width][nest.size.height];
      for (int i = 0; i < nest.size.width; i++)
      {
         for (int j = 0; j < nest.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      events    = new Vector<Event>();
      eventTime = 0;
      initAutopilot();
   }


   // Reset state.
   void reset()
   {
      random.setSeed(randomSeed);
      x           = x2;
      y           = y2;
      orientation = orientation2;
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response       = WAIT;
      driverResponse = WAIT;
      for (int i = 0; i < nest.size.width; i++)
      {
         for (int j = 0; j < nest.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      events.clear();
      morphognostic.clear();
      initAutopilot();
   }


   // Save pufferfish to file.
   public void save(String filename) throws IOException
   {
      DataOutputStream writer;

      try
      {
         writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filename))));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      save(writer);
      writer.close();
   }


   // Save pufferfish.
   public void save(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, x);
      Utility.saveInt(writer, y);
      Utility.saveInt(writer, orientation);
      Utility.saveInt(writer, x2);
      Utility.saveInt(writer, y2);
      Utility.saveInt(writer, orientation2);
      morphognostic.save(writer);
      Utility.saveInt(writer, maxEventAge);
      Utility.saveInt(writer, metamorphs.size());
      for (Metamorph m : metamorphs)
      {
         m.save(writer);
      }
      Utility.saveFloat(writer, EQUIVALENT_MORPHOGNOSTIC_DISTANCE);
      writer.flush();
   }


   // Load pufferfish from file.
   public void load(String filename) throws IOException
   {
      FileInputStream input;

      try
      {
         input = new FileInputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(input);
      input.close();
   }


   // Load pufferfish.
   public void load(DataInputStream reader) throws IOException
   {
      // Load the properties.
      x             = Utility.loadInt(reader);
      y             = Utility.loadInt(reader);
      orientation   = Utility.loadInt(reader);
      x2            = Utility.loadInt(reader);
      y2            = Utility.loadInt(reader);
      orientation2  = Utility.loadInt(reader);
      morphognostic = Morphognostic.load(reader);
      maxEventAge   = Utility.loadInt(reader);
      metamorphs.clear();
      int n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         metamorphs.add(Metamorph.load(reader));
      }
      EQUIVALENT_MORPHOGNOSTIC_DISTANCE = Utility.loadFloat(reader);
      initAutopilot();
   }


   // Sensor/response cycle.
   public int cycle(float[] sensors)
   {
      // Update morphognostic.
      int[] values = new int[NUM_SENSORS];
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         this.sensors[i] = sensors[i];
         values[i]       = (int)sensors[i];
      }
      events.add(new Event(values, x, y, eventTime));
      if ((eventTime - events.get(0).time) > maxEventAge)
      {
         events.remove(0);
      }
      int w = nest.size.width;
      int h = nest.size.height;
      int a = maxEventAge + 1;
      int morphEvents[][][][] = new int[w][h][NUM_SENSORS][a];
      for (int x2 = 0; x2 < w; x2++)
      {
         for (int y2 = 0; y2 < h; y2++)
         {
            for (int n = 0; n < NUM_SENSORS; n++)
            {
               for (int t = 0; t < a; t++)
               {
                  morphEvents[x2][y2][n][t] = -1;
               }
            }
         }
      }
      for (Event e : events)
      {
         for (int n = 0; n < NUM_SENSORS; n++)
         {
            morphEvents[e.x][e.y][n][eventTime - e.time] = e.values[n];
         }
      }
      morphognostic.update(morphEvents, x, y);

      // Respond.
      if (driver == DRIVER_TYPE.METAMORPH_RULES.getValue())
      {
         metamorphRulesResponse();
      }
      else if (driver == DRIVER_TYPE.AUTOPILOT.getValue())
      {
         autoPilotResponse();
      }
      else
      {
         response = driverResponse;
      }

      // Update metamorphs.
      Metamorph metamorph = new Metamorph(morphognostic.clone(), response);
      boolean   found     = false;

      for (Metamorph m : metamorphs)
      {
         for (int i = 0; i < Orientation.NUM_ORIENTATIONS; i++)
         {
            metamorph.morphognostic.orientation = i;
            if (m.morphognostic.compare(metamorph.morphognostic) <=
                EQUIVALENT_MORPHOGNOSTIC_DISTANCE)
            {
               found = true;
               break;
            }
         }
         if (found) { break; }
      }
      metamorph.morphognostic.orientation = Orientation.NORTH;
      if (!found)
      {
         metamorphs.add(metamorph);
      }

      eventTime++;
      return(response);
   }


   // Get metamorph rules response.
   void metamorphRulesResponse()
   {
      response = WAIT;
      Metamorph metamorph = null;
      float     d         = 0.0f;
      float     d2;
      for (Metamorph m : metamorphs)
      {
         for (int i = 0; i < Orientation.NUM_ORIENTATIONS; i++)
         {
            morphognostic.orientation = i;
            d2 = morphognostic.compare(m.morphognostic);
            if ((metamorph == null) || (d2 < d))
            {
               d         = d2;
               metamorph = m;
            }
            else
            {
               if (d2 == d)
               {
                  if (random.nextBoolean())
                  {
                     d         = d2;
                     metamorph = m;
                  }
               }
            }
         }
      }
      morphognostic.orientation = Orientation.NORTH;
      if (metamorph != null)
      {
         response = metamorph.response;
      }
   }


   // Initialize autopilot.
   public void initAutopilot()
   {
      int w = nest.size.width;
      int h = nest.size.height;

      state  = 0;
      radius = Nest.CENTER_RADIUS - 1;
      int r = Math.abs(w - x - 1);
      if (r < radius)
      {
         radius = r;
      }
      if (y < radius)
      {
         radius = y;
      }
      r = Math.abs(h - y - 1);
      if (r < radius)
      {
         radius = r;
      }
      ring        = 0;
      step        = 0;
      smoothStep  = true;
      orientation = Orientation.WEST;
      spoke       = 0;
      spokeIndex  = 0;
      spokeDir    = 0;
      spokePath   = null;
   }


   // Autopilot response.
   void autoPilotResponse()
   {
      response = WAIT;
      if (state == 0)
      {
         int steps = ring * 2;
         if (smoothStep)
         {
            smoothStep = false;
            response   = SMOOTH;
         }
         else
         {
            smoothStep = true;
            if (radius == 0)
            {
               switch (orientation)
               {
               case Orientation.WEST:
                  response = TURN_LEFT;
                  break;

               case Orientation.SOUTH:
                  response = TURN_LEFT;
                  break;

               case Orientation.EAST:
                  response = TURN_LEFT;
                  break;

               case Orientation.NORTH:
                  state = 1;
                  genSpokePath();
                  response = TURN_LEFT;
                  break;
               }
            }
            else
            {
               switch (orientation)
               {
               case Orientation.WEST:
                  if (step == (steps + 1))
                  {
                     if (ring < radius)
                     {
                        response = TURN_LEFT;
                        ring++;
                        step = 1;
                     }
                  }
                  else
                  {
                     response = FORWARD;
                     step++;
                     if ((ring == radius) && (step > steps))
                     {
                        state = 1;
                        genSpokePath();
                     }
                  }
                  break;

               case Orientation.SOUTH:
                  if (step == steps)
                  {
                     response = TURN_LEFT;
                     step     = 0;
                  }
                  else
                  {
                     response = FORWARD;
                     step++;
                  }
                  break;

               case Orientation.EAST:
                  if (step == steps)
                  {
                     response = TURN_LEFT;
                     step     = 0;
                  }
                  else
                  {
                     response = FORWARD;
                     step++;
                  }
                  break;

               case Orientation.NORTH:
                  if (step == steps)
                  {
                     response = TURN_LEFT;
                     step     = 0;
                  }
                  else
                  {
                     response = FORWARD;
                     step++;
                  }
                  break;
               }
            }
         }
      }
      else
      {
         if (spoke == Nest.NUM_SPOKES) { return; }
         SpokePoint p = spokePath.get(spokeIndex);
         if ((x == p.x) && (y == p.y))
         {
            if (p.raise)
            {
               response = RAISE;
               p.raise  = false;
               return;
            }
            else if (p.lower)
            {
               response = LOWER;
               p.lower  = false;
               return;
            }
            else
            {
               if (spokeDir == 0)
               {
                  if (spokeIndex == (spokePath.size() - 1))
                  {
                     spokeIndex--;
                     spokeDir = 1;
                  }
                  else
                  {
                     spokeIndex++;
                  }
               }
               else
               {
                  if (spokeIndex > 0)
                  {
                     spokeIndex--;
                  }
                  else
                  {
                     spoke++;
                     if (spoke == Nest.NUM_SPOKES) { return; }
                     spokeDir   = 0;
                     spokeIndex = 1;
                     genSpokePath();
                  }
               }
            }
         }
         p = spokePath.get(spokeIndex);
         switch (orientation)
         {
         case Orientation.WEST:
            if (p.x < x)
            {
               response = FORWARD;
            }
            else if (p.x > x)
            {
               response = TURN_LEFT;
            }
            else if (p.y > y)
            {
               response = TURN_RIGHT;
            }
            else if (p.y < y)
            {
               response = TURN_LEFT;
            }
            break;

         case Orientation.SOUTH:
            if (p.y < y)
            {
               response = FORWARD;
            }
            else if (p.y > y)
            {
               response = TURN_LEFT;
            }
            else if (p.x > x)
            {
               response = TURN_LEFT;
            }
            else if (p.x < x)
            {
               response = TURN_RIGHT;
            }
            break;

         case Orientation.EAST:
            if (p.x > x)
            {
               response = FORWARD;
            }
            else if (p.x < x)
            {
               response = TURN_LEFT;
            }
            else if (p.y > y)
            {
               response = TURN_LEFT;
            }
            else if (p.y < y)
            {
               response = TURN_RIGHT;
            }
            break;

         case Orientation.NORTH:
            if (p.y > y)
            {
               response = FORWARD;
            }
            else if (p.y < y)
            {
               response = TURN_LEFT;
            }
            else if (p.x > x)
            {
               response = TURN_RIGHT;
            }
            else if (p.x < x)
            {
               response = TURN_LEFT;
            }
            break;
         }
      }
   }


   // Generate spoke path.
   void genSpokePath()
   {
      spokePath = new ArrayList<SpokePoint>();
      float  angle = (360.0f / (float)Nest.NUM_SPOKES) * (float)spoke;
      float  vx    = (float)Math.cos(Math.toRadians(angle + 90.0f));
      float  vy    = (float)Math.sin(Math.toRadians(angle + 90.0f));
      int    cx    = nest.size.width / 2;
      int    cy    = nest.size.height / 2;
      double d     = (double)(Nest.CENTER_RADIUS + Nest.SPOKE_LENGTH);
      for (int i = 0; ; i++)
      {
         int        px = (int)(vx * (float)i) + cx;
         int        py = (int)(vy * (float)i) + cy;
         SpokePoint p  = new SpokePoint(px, py);
         if (spokePath.size() > 0)
         {
            SpokePoint p2 = spokePath.get(spokePath.size() - 1);
            if ((p2.x == p.x) && (p2.y == p.y)) { continue; }
         }
         spokePath.add(p);
         double dx = p.x - cx;
         double dy = p.y - cy;
         if (Math.sqrt((dx * dx) + (dy * dy)) >= d) { break; }
      }
      int a = Nest.SPOKE_RIPPLE_LENGTH / 2;
      int b = spokePath.size() - 1;
      for (int i = 0; i < 4 && a > 0 && b >= 0; i++)
      {
         for (int j = 0; j < a && b >= 0; j++)
         {
            SpokePoint p = spokePath.get(b);
            b--;
            if ((i % 2) == 0)
            {
               p.raise = true;
            }
            else
            {
               p.lower = true;
            }
         }
      }
      spokeIndex = 0;
      spokeDir   = 0;
   }


   // Random movement.
   void randomMovement()
   {
      switch (random.nextInt(3))
      {
      case 0:
         response = FORWARD;
         return;

      case 1:
         response = TURN_LEFT;
         return;

      case 2:
         response = TURN_RIGHT;
         return;
      }
   }


   // Write metamporph dataset.
   public void writeMetamorphDataset() throws Exception
   {
      FileOutputStream output;

      try
      {
         output = new FileOutputStream(new File(DATASET_FILE_NAME));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + DATASET_FILE_NAME + ":" + e.getMessage());
      }
      PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
      for (Metamorph m : metamorphs)
      {
         writer.println(morphognostic2csv(m.morphognostic) + "," +
                        Pufferfish.getResponseName(m.response));
      }
      writer.flush();
      output.close();
   }


   // Flatten morphognostic to csv string.
   public String morphognostic2csv(Morphognostic morphognostic)
   {
      String  output    = "";
      boolean skipComma = true;
      int     dx        = 0;

      if (Pufferfish.IGNORE_ELEVATION_SENSOR_VALUES)
      {
         dx = 3;
      }
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood neighborhood = morphognostic.neighborhoods.get(i);
         float[][][] densities = neighborhood.rectifySectorTypeDensities();
         int n = neighborhood.sectors.length;
         for (int j = 0, j2 = n * n; j < j2; j++)
         {
            for (int d = dx, d2 = morphognostic.eventDimensions; d < d2; d++)
            {
               for (int k = 0, k2 = morphognostic.numEventTypes[d]; k < k2; k++)
               {
                  if (skipComma)
                  {
                     skipComma = false;
                  }
                  else
                  {
                     output += ",";
                  }
                  output += (densities[j][d][k] + "");
               }
            }
         }
      }
      return(output);
   }


   // Response value from name.
   public static int getResponseValue(String name)
   {
      if (name.equals("wait"))
      {
         return(WAIT);
      }
      if (name.equals("forward"))
      {
         return(FORWARD);
      }
      if (name.equals("turn left"))
      {
         return(TURN_LEFT);
      }
      if (name.equals("turn right"))
      {
         return(TURN_RIGHT);
      }
      if (name.equals("smooth surface"))
      {
         return(SMOOTH);
      }
      if (name.equals("raise surface"))
      {
         return(RAISE);
      }
      if (name.equals("lower surface"))
      {
         return(LOWER);
      }
      return(-1);
   }


   // Get response name.
   public static String getResponseName(int response)
   {
      switch (response)
      {
      case Pufferfish.WAIT:
         return("wait");

      case Pufferfish.FORWARD:
         return("forward");

      case Pufferfish.TURN_LEFT:
         return("turn left");

      case Pufferfish.TURN_RIGHT:
         return("turn right");

      case Pufferfish.SMOOTH:
         return("smooth surface");

      case Pufferfish.RAISE:
         return("raise surface");

      case Pufferfish.LOWER:
         return("lower surface");
      }
      return("unknown");
   }
}
