// For conditions of distribution and use, see copyright notice in Main.java

// Honeybee: morphognosis organism.

package morphognosis.honey_bees;

import java.io.BufferedInputStream;
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
import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import morphognosis.Morphognostic.Neighborhood;
import morphognosis.Orientation;
import morphognosis.Utility;

public class HoneyBee
{
   // Properties.
   public int          id;
   public int          x, y, x2, y2, toX, toY;
   public int          orientation, orientation2;
   public boolean      nectarCarry;
   public int          nectarDistanceDisplay;
   public int          nectarX, nectarY;
   public int          nectarDistance;
   public float        returnToHiveProbability;
   public World        world;
   public int          driver;
   public int          driverResponse;
   public SecureRandom random;

   // Sensors.
   public static final int HIVE_PRESENCE_INDEX          = 0;
   public static final int NECTAR_PRESENCE_INDEX        = 1;
   public static final int NECTAR_DANCE_DIRECTION_INDEX = 2;
   public static final int NECTAR_DANCE_DISTANCE_INDEX  = 3;
   public static final int NUM_SENSORS = 4;
   float[] sensors;

   // Response.
   // Initial responses are changing directions (see Orientation).
   public static final int FORWARD                 = Orientation.NUM_ORIENTATIONS;
   public static final int EXTRACT_NECTAR          = FORWARD + 1;
   public static final int DEPOSIT_NECTAR          = EXTRACT_NECTAR + 1;
   public static final int DISPLAY_NECTAR_DISTANCE = DEPOSIT_NECTAR + 1;
   public static final int WAIT          = DISPLAY_NECTAR_DISTANCE + Parameters.BEE_NUM_DISTANCE_VALUES;
   public static final int NUM_RESPONSES = WAIT + 1;
   int response;

   // Maximum distance between equivalent morphognostics.
   public static float EQUIVALENT_MORPHOGNOSTIC_DISTANCE = 0.0f;

   // Morphognostic.
   public Morphognostic morphognostic;

   /*
    * Morphognostic event.
    *
    *      Event values:
    *      {
    *              <hive presence>,
    *              <nectar presence>,
    *              <surplus nectar presence>,
    *              <nectar dance direction>,
    *              <nectar dance distance>,
    *              <orientation>,
    *              <nectar carry status>
    *      }
    *      <orientation>: [<Orientation point true/false> x8]
    *      <nectar distance>: [<distance type true/false> x<BEE_NUM_DISTANCE_VALUES>]
    */

   // Metamorphs.
   public ArrayList<Metamorph> metamorphs;

   // Metamorph neural network.
   public MetamorphNN metamorphNN;

   // NN train.
   public static boolean NNtrain  = true;
   public boolean        resetRun = false;

   // Metamorph dataset file name.
   public static String METAMORPH_DATASET_FILE_BASENAME = "metamorphs";
   public String        metamorphDatasetFilename;

   // Utility constants.
   public int maxDist;
   public int unitDist;

   // Landmarks.
   public boolean[][] landmarkMap;

   // Constructor.
   public HoneyBee(int id, World world, SecureRandom random)
   {
      this.id = id;
      metamorphDatasetFilename = METAMORPH_DATASET_FILE_BASENAME + "_" + id + ".csv";
      this.world  = world;
      this.random = random;

      // Initialize bee.
      for (int i = 0; i < 20; i++)
      {
         int dx = random.nextInt(Parameters.HIVE_RADIUS);
         if (random.nextBoolean()) { dx = -dx; }
         int dy = random.nextInt(Parameters.HIVE_RADIUS);
         if (random.nextBoolean()) { dy = -dy; }
         x = x2 = (Parameters.WORLD_WIDTH / 2) + dx;
         y = y2 = (Parameters.WORLD_HEIGHT / 2) + dy;
         if (world.cells[x][y].hive && (world.cells[x][y].bee == null))
         {
            world.cells[x][y].bee = this;
            break;
         }
         if (i == 19)
         {
            System.err.println("Cannot place bee in world");
            System.exit(1);
         }
      }
      orientation           = orientation2 = random.nextInt(Orientation.NUM_ORIENTATIONS);
      nectarCarry           = false;
      nectarDistanceDisplay = -1;
      nectarX                 = nectarY = -1;
      nectarDistance          = -1;
      returnToHiveProbability = 0.0f;
      sensors                 = new float[NUM_SENSORS];
      for (int n = 0; n < NUM_SENSORS; n++)
      {
         sensors[n] = 0.0f;
      }
      response = WAIT;

      // Initialize Morphognostic.
      int numImmediateEvents =
         1 +                                          // <nectar presence>
         Orientation.NUM_ORIENTATIONS +               // <nectar dance direction>
         Parameters.BEE_NUM_DISTANCE_VALUES +         // <nectar dance distance>
         Orientation.NUM_ORIENTATIONS +               // <orientation>
         1;                                           // <nectar carry status>
      int numEventTypes =
         1 +                                          // <hive presence.
         1;                                           // <surplus nectar presence>
      int [] eventTypes = new int[numEventTypes];
      for (int i = 0; i < eventTypes.length; i++)
      {
         eventTypes[i] = 1;
      }
      morphognostic = new Morphognostic(Orientation.NORTH, numImmediateEvents, eventTypes,
                                        Parameters.WORLD_WIDTH, Parameters.WORLD_HEIGHT,
                                        Parameters.NUM_NEIGHBORHOODS,
                                        Parameters.NEIGHBORHOOD_INITIAL_DIMENSION,
                                        Parameters.NEIGHBORHOOD_DIMENSION_STRIDE,
                                        Parameters.NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                        Parameters.EPOCH_INTERVAL_STRIDE,
                                        Parameters.EPOCH_INTERVAL_MULTIPLIER,
                                        Parameters.BINARY_VALUE_AGGREGATION);

      // Create metamorphs.
      metamorphs = new ArrayList<Metamorph>();

      // Utility values.
      maxDist  = Math.max(Parameters.WORLD_WIDTH, Parameters.WORLD_HEIGHT) / 2;
      unitDist = maxDist / Parameters.BEE_NUM_DISTANCE_VALUES;

      // Initialize landmarks.
      landmarkMap = new boolean[Parameters.WORLD_WIDTH][Parameters.WORLD_HEIGHT];
      for (int i = 0; i < Parameters.WORLD_WIDTH; i++)
      {
         for (int j = 0; j < Parameters.WORLD_HEIGHT; j++)
         {
            landmarkMap[i][j] = false;
         }
      }

      // Initialize driver.
      driver         = Driver.AUTOPILOT;
      driverResponse = WAIT;
   }


   // Reset.
   void reset()
   {
      x                       = x2;
      y                       = y2;
      orientation             = orientation2;
      nectarCarry             = false;
      nectarDistanceDisplay   = -1;
      nectarX                 = nectarY = -1;
      nectarDistance          = -1;
      returnToHiveProbability = 0.0f;
      world.cells[x][y].bee   = this;
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response = WAIT;
      morphognostic.clear();
      for (int i = 0; i < Parameters.WORLD_WIDTH; i++)
      {
         for (int j = 0; j < Parameters.WORLD_HEIGHT; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      driverResponse = WAIT;
   }


   // Save bee to file.
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


   // Save bee.
   public void save(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, id);
      Utility.saveInt(writer, x);
      Utility.saveInt(writer, y);
      Utility.saveInt(writer, orientation);
      Utility.saveInt(writer, x2);
      Utility.saveInt(writer, y2);
      Utility.saveInt(writer, orientation2);
      if (nectarCarry)
      {
         Utility.saveInt(writer, 1);
      }
      else
      {
         Utility.saveInt(writer, 0);
      }
      Utility.saveInt(writer, nectarDistanceDisplay);
      Utility.saveInt(writer, nectarX);
      Utility.saveInt(writer, nectarY);
      Utility.saveInt(writer, nectarDistance);
      Utility.saveFloat(writer, returnToHiveProbability);
      morphognostic.save(writer);
      Utility.saveInt(writer, metamorphs.size());
      for (Metamorph m : metamorphs)
      {
         m.save(writer);
      }
      Utility.saveFloat(writer, EQUIVALENT_MORPHOGNOSTIC_DISTANCE);
      Utility.saveInt(writer, driver);
      Utility.saveInt(writer, driverResponse);
      writer.flush();
   }


   // Load bee from file.
   public void load(String filename) throws IOException
   {
      DataInputStream reader;

      try
      {
         reader = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filename))));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(reader);
      reader.close();
   }


   // Load bee.
   public void load(DataInputStream reader) throws IOException
   {
      id           = Utility.loadInt(reader);
      x            = Utility.loadInt(reader);
      y            = Utility.loadInt(reader);
      orientation  = Utility.loadInt(reader);
      x2           = Utility.loadInt(reader);
      y2           = Utility.loadInt(reader);
      orientation2 = Utility.loadInt(reader);
      if (Utility.loadInt(reader) == 1)
      {
         nectarCarry = true;
      }
      else
      {
         nectarCarry = false;
      }
      nectarDistanceDisplay = Utility.loadInt(reader);
      nectarX                 = Utility.loadInt(reader);
      nectarY                 = Utility.loadInt(reader);
      nectarDistance          = Utility.loadInt(reader);
      returnToHiveProbability = Utility.loadFloat(reader);
      morphognostic           = Morphognostic.load(reader);
      metamorphs.clear();
      int n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         metamorphs.add(Metamorph.load(reader));
      }
      EQUIVALENT_MORPHOGNOSTIC_DISTANCE = Utility.loadFloat(reader);
      driver                = Utility.loadInt(reader);
      driverResponse        = Utility.loadInt(reader);
      world.cells[x][y].bee = this;
   }


   // Sense/response cycle.
   public int cycle(float[] sensors)
   {
      if (NNtrain)
      {
         if (resetRun)
         {
            resetRun = false;
            for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
            {
               for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
               {
                  if (world.cells[x][y].flower != null)
                  {
                     world.cells[x][y].flower.nectar = 0;
                  }
               }
            }
            morphognostic.clear();
         }
      }
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         this.sensors[i] = sensors[i];
      }

      // Update morphognostic.
      int[] immediateEvents = new int[morphognostic.numImmediateEvents];
      immediateEvents[0]    = 0;
      int surplusNectar = 0;
      if (nectarCarry)
      {
         // Surplus nectar?
         if ((int)sensors[NECTAR_PRESENCE_INDEX] == 1.0f)
         {
            surplusNectar = 1;
         }
      }
      else
      {
         // Nectar present?
         if ((int)sensors[NECTAR_PRESENCE_INDEX] == 1.0f)
         {
            immediateEvents[0] = 1;
         }
      }
      if (sensors[NECTAR_DANCE_DIRECTION_INDEX] != -1)
      {
         immediateEvents[1 + (int)sensors[NECTAR_DANCE_DIRECTION_INDEX]] = 1;
      }
      if (sensors[NECTAR_DANCE_DISTANCE_INDEX] != -1)
      {
         immediateEvents[1 + Orientation.NUM_ORIENTATIONS + (int)sensors[NECTAR_DANCE_DISTANCE_INDEX]] = 1;
      }
      immediateEvents[1 + Orientation.NUM_ORIENTATIONS + Parameters.BEE_NUM_DISTANCE_VALUES + orientation] = 1;
      if (nectarCarry)
      {
         immediateEvents[1 + Orientation.NUM_ORIENTATIONS + Parameters.BEE_NUM_DISTANCE_VALUES + Orientation.NUM_ORIENTATIONS] = 1;
      }
      int[] eventValues = new int[morphognostic.eventDimensions];
      eventValues[0]    = (int)sensors[HIVE_PRESENCE_INDEX];
      eventValues[1]    = surplusNectar;
      morphognostic.update(immediateEvents, eventValues, x, y);

      // Respond.
      boolean validMetamorph = false;
      switch (driver)
      {
      case Driver.AUTOPILOT:
         validMetamorph = autopilotResponse();
         break;

      case Driver.METAMORPH_DB:
         metamorphDBresponse();
         break;

      case Driver.METAMORPH_NN:
         metamorphNNresponse();
         break;

      default:
         response = driverResponse;
         break;
      }

      // Update metamorphs.
      if (validMetamorph)
      {
         Metamorph metamorph = new Metamorph(morphognostic.clone(), response, getResponseName(response));
         metamorph.morphognostic.orientation = Orientation.NORTH;
         boolean found = false;
         for (Metamorph m : metamorphs)
         {
            if (m.morphognostic.compare(metamorph.morphognostic) <=
                EQUIVALENT_MORPHOGNOSTIC_DISTANCE)
            {
               found = true;
               break;
            }
         }
         if (!found)
         {
            metamorphs.add(metamorph);
         }
      }

      return(response);
   }


   // Autopilot response.
   // Returns: true if handling nectar; false if randomly foraging.
   boolean autopilotResponse()
   {
      int width  = Parameters.WORLD_WIDTH;
      int height = Parameters.WORLD_HEIGHT;

      // If in hive clear probability to return to hive.
      if (sensors[HIVE_PRESENCE_INDEX] == 1.0f)
      {
         returnToHiveProbability = 0.0f;
      }

      // Turn at edge of world.
      if ((toX < 0) || (toX >= width) || (toY < 0) || (toY >= height))
      {
         nectarDistance = -1;
         response       = random.nextInt(Orientation.NUM_ORIENTATIONS);
         return(false);
      }

      // Carrying nectar?
      if (nectarCarry)
      {
         // In hive?
         if (sensors[HIVE_PRESENCE_INDEX] == 1.0f)
         {
            // Deposit nectar.
            response = DEPOSIT_NECTAR;
            if (NNtrain)
            {
               if (nectarX == -1)
               {
                  resetRun = true;
               }
            }
         }
         else
         {
            // Sense surplus nectar to dance about?
            if (sensors[NECTAR_PRESENCE_INDEX] == 1.0f)
            {
               // Remember nectar location for later display.
               nectarX = x;
               nectarY = y;
            }

            // Continue to hive.
            if (random.nextFloat() < Parameters.BEE_DEADLOCK_PREVENTION_PROBABILITY)
            {
               // Abandon nectar to avoid deadlock.
               response = DEPOSIT_NECTAR;
            }
            else
            {
               response = moveTo(width / 2, height / 2);
            }
         }
         return(true);
      }

      // Not carrying nectar.

      // In hive?
      if (sensors[HIVE_PRESENCE_INDEX] == 1.0f)
      {
         // Surplus nectar detected?
         if (nectarX != -1)
         {
            // Orient toward nectar?
            int o = orientToward(nectarX, nectarY);
            if (o != orientation)
            {
               response = o;
               return(true);
            }

            // Dance display of nectar direction and distance to bees in hive.
            int d = (int)Math.sqrt((double)((nectarX - x) * (nectarX - x)) +
                                   (double)((nectarY - y) * (nectarY - y)));
            d = d / unitDist;
            if (d >= Parameters.BEE_NUM_DISTANCE_VALUES)
            {
               d = Parameters.BEE_NUM_DISTANCE_VALUES - 1;
            }
            nectarX        = nectarY = -1;
            nectarDistance = -1;
            response       = DISPLAY_NECTAR_DISTANCE + d;
            if (NNtrain)
            {
               resetRun = true;
            }
            return(true);
         }

         // Sense nectar dance?
         if (sensors[NECTAR_DANCE_DIRECTION_INDEX] != -1.0f)
         {
            nectarDistance = ((int)(sensors[NECTAR_DANCE_DISTANCE_INDEX]) + 1) * unitDist;
            response       = (int)sensors[NECTAR_DANCE_DIRECTION_INDEX];
            return(true);
         }
      }
      else
      {
         // Found nectar to extract?
         if (NNtrain)
         {
            if (world.cells[x][y].flower != null)
            {
               if (random.nextBoolean())
               {
                  world.cells[x][y].flower.nectar = 2;
               }
               else
               {
                  world.cells[x][y].flower.nectar = 1;
               }
               sensors[NECTAR_PRESENCE_INDEX] = 1.0f;
            }
         }
         if (sensors[NECTAR_PRESENCE_INDEX] == 1.0f)
         {
            nectarDistance = -1;
            nectarX        = nectarY = -1;
            response       = EXTRACT_NECTAR;
            return(true);
         }
      }

      // Heading to nectar?
      if (nectarDistance != -1)
      {
         if (random.nextFloat() < Parameters.BEE_DEADLOCK_PREVENTION_PROBABILITY)
         {
            // Abandon nectar to avoid deadlock.
            nectarDistance = -1;
            response       = random.nextInt(Orientation.NUM_ORIENTATIONS);
         }
         else
         {
            nectarDistance--;
            response = FORWARD;
         }
         return(true);
      }

      // Return to hive?
      if (sensors[HIVE_PRESENCE_INDEX] == 0.0f)
      {
         if (random.nextFloat() < returnToHiveProbability)
         {
            response = moveTo(width / 2, height / 2);
            return(false);
         }
         else
         {
            // Increase tendency to return.
            returnToHiveProbability += Parameters.BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT;
            if (returnToHiveProbability > 1.0f)
            {
               returnToHiveProbability = 1.0f;
            }
         }
      }

      // Continue foraging.
      if (random.nextFloat() < Parameters.BEE_TURN_PROBABILITY)
      {
         response = random.nextInt(Orientation.NUM_ORIENTATIONS);
      }
      else
      {
         response = FORWARD;
      }
      return(false);
   }


   // Get response that moves to destination cell.
   // Return -1 if at destination.
   public int moveTo(int dX, int dY)
   {
      // Facing destination?
      if ((dX == x) && (dY == y))
      {
         return(-1);
      }

      if (dX < x)
      {
         if (dY < y)
         {
            if (orientation == Orientation.SOUTHWEST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.SOUTHWEST);
            }
         }
         else if (dY > y)
         {
            if (orientation == Orientation.NORTHWEST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.NORTHWEST);
            }
         }
         else
         {
            if (orientation == Orientation.WEST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.WEST);
            }
         }
      }
      else if (dX > x)
      {
         if (dY < y)
         {
            if (orientation == Orientation.SOUTHEAST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.SOUTHEAST);
            }
         }
         else if (dY > y)
         {
            if (orientation == Orientation.NORTHEAST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.NORTHEAST);
            }
         }
         else
         {
            if (orientation == Orientation.EAST)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.EAST);
            }
         }
      }
      else
      {
         if (dY < y)
         {
            if (orientation == Orientation.SOUTH)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.SOUTH);
            }
         }
         else if (dY > y)
         {
            if (orientation == Orientation.NORTH)
            {
               return(FORWARD);
            }
            else
            {
               return(Orientation.NORTH);
            }
         }
         else
         {
            return(FORWARD);
         }
      }
   }


   // Get response that orients toward destination cell.
   public int orientToward(int dX, int dY)
   {
      float mX = (float)Math.abs(dX - x);
      float mY = (float)Math.abs(dY - y);
      float r;

      if (dX < x)
      {
         if (dY < y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.WEST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.SOUTH);
            }
            else
            {
               return(Orientation.SOUTHWEST);
            }
         }
         else if (dY > y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.WEST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.NORTH);
            }
            else
            {
               return(Orientation.NORTHWEST);
            }
         }
         else
         {
            return(Orientation.WEST);
         }
      }
      else if (dX > x)
      {
         if (dY < y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.EAST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.SOUTH);
            }
            else
            {
               return(Orientation.SOUTHEAST);
            }
         }
         else if (dY > y)
         {
            r = mY / mX;
            if (r < 0.5f)
            {
               return(Orientation.EAST);
            }
            else if (r > 2.0f)
            {
               return(Orientation.NORTH);
            }
            else
            {
               return(Orientation.NORTHEAST);
            }
         }
         else
         {
            return(Orientation.EAST);
         }
      }
      else
      {
         if (dY < y)
         {
            return(Orientation.SOUTH);
         }
         else if (dY > y)
         {
            return(Orientation.NORTH);
         }
         else
         {
            // Co-located.
            return(WAIT);
         }
      }
   }


   // Get metamorph DB response.
   void metamorphDBresponse()
   {
      // Handling nectar?
      int previousResponse = response;

      if (autopilotResponse())
      {
         Metamorph metamorph = null;
         float     d         = 0.0f;
         float     d2;
         for (Metamorph m : metamorphs)
         {
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
         if (metamorph != null)
         {
            response = metamorph.response;
         }
         else
         {
            response = WAIT;
         }

         // Check for hung nectar display.
         if ((previousResponse >= DISPLAY_NECTAR_DISTANCE) && (previousResponse < WAIT))
         {
            if ((response >= DISPLAY_NECTAR_DISTANCE) && (response < WAIT))
            {
               nectarX        = nectarY = -1;
               nectarDistance = -1;
            }
         }
      }
   }


   // Get metamorph neural network response.
   void metamorphNNresponse()
   {
      // Handling nectar?
      int previousResponse = response;

      if (autopilotResponse())
      {
         if (metamorphNN != null)
         {
            // Lost hive?
            if (!world.cells[x][y].hive && !checkMorphognosticEvent(morphognostic, 0, false))
            {
               // Use auto pilot response.
               return;
            }

            // Check for hung nectar display.
            if ((previousResponse >= DISPLAY_NECTAR_DISTANCE) && (previousResponse < WAIT))
            {
               if ((response >= DISPLAY_NECTAR_DISTANCE) && (response < WAIT))
               {
                  nectarX        = nectarY = -1;
                  nectarDistance = -1;
               }
            }
         }
         else
         {
            System.err.println("Must train metamorph neural network");
            response = WAIT;
         }
      }
   }


   // Train metamorph neural network.
   public void trainMetamorphNN()
   {
      metamorphNN = new MetamorphNN(random);
      metamorphNN.train(metamorphs);
   }


   // Write metamporph dataset.
   public void writeMetamorphDataset(String filename, boolean append) throws Exception
   {
      FileOutputStream output;

      try
      {
         output = new FileOutputStream(new File(filename), append);
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
      for (Metamorph m : metamorphs)
      {
         writer.println(morphognostic2csv(m.morphognostic) + "," + m.response);
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
      if (name.equals("turn north"))
      {
         return(Orientation.NORTH);
      }
      if (name.equals("turn northeast"))
      {
         return(Orientation.NORTHEAST);
      }
      if (name.equals("turn east"))
      {
         return(Orientation.EAST);
      }
      if (name.equals("turn southeast"))
      {
         return(Orientation.SOUTHEAST);
      }
      if (name.equals("turn south"))
      {
         return(Orientation.SOUTH);
      }
      if (name.equals("turn southwest"))
      {
         return(Orientation.SOUTHWEST);
      }
      if (name.equals("turn west"))
      {
         return(Orientation.WEST);
      }
      if (name.equals("turn northwest"))
      {
         return(Orientation.NORTHWEST);
      }
      if (name.equals("move forward"))
      {
         return(FORWARD);
      }
      if (name.equals("extract nectar"))
      {
         return(EXTRACT_NECTAR);
      }
      if (name.equals("deposit nectar"))
      {
         return(DEPOSIT_NECTAR);
      }
      if (name.equals("display nectar distance"))
      {
         return(DISPLAY_NECTAR_DISTANCE);
      }
      if (name.equals("wait"))
      {
         return(WAIT);
      }
      return(-1);
   }


   // Get response name.
   public static String getResponseName(int response)
   {
      switch (response)
      {
      case Orientation.NORTH:
         return("turn north");

      case Orientation.NORTHEAST:
         return("turn northeast");

      case Orientation.EAST:
         return("turn east");

      case Orientation.SOUTHEAST:
         return("turn southeast");

      case Orientation.SOUTH:
         return("turn south");

      case Orientation.SOUTHWEST:
         return("turn southwest");

      case Orientation.WEST:
         return("turn west");

      case Orientation.NORTHWEST:
         return("turn northwest");

      case FORWARD:
         return("move forward");

      case EXTRACT_NECTAR:
         return("extract nectar");

      case DEPOSIT_NECTAR:
         return("deposit nectar");
      }

      if ((response >= DISPLAY_NECTAR_DISTANCE) && (response < WAIT))
      {
         return("display nectar distance " + (response - DISPLAY_NECTAR_DISTANCE));
      }

      if (response == WAIT)
      {
         return("wait");
      }

      return("unknown");
   }


   // Check for event in morphognostic.
   public static boolean checkMorphognosticEvent(Morphognostic morphognostic, int valueIndex)
   {
      return(checkMorphognosticEvent(morphognostic, valueIndex, false));
   }


   public static boolean checkMorphognosticEvent(Morphognostic morphognostic,
                                                 int valueIndex, boolean verbose)
   {
      float c = 0.0f;

      int centerXY = Parameters.NEIGHBORHOOD_INITIAL_DIMENSION / 2;

      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood n = morphognostic.neighborhoods.get(i);
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               if ((x == centerXY) && (y == centerXY)) { continue; }
               Neighborhood.Sector s = n.sectors[x][y];
               c += s.typeDensities[valueIndex][0];
               if (verbose && (s.typeDensities[valueIndex][0] > 0.0f))
               {
                  System.out.println("checkMorphognosticFeaturePresence, valueIndex=" + valueIndex +
                                     ",neighborhood=" + i + ",duration=" + n.duration + ",sector=" + x + "/" + y +
                                     ",density=" + s.typeDensities[0][0]);
               }
            }
         }
      }
      if (c == 0.0f)
      {
         if (verbose)
         {
            System.out.println("checkMorphognosticFeaturePresence fails for valueIndex=" + valueIndex);
         }
         return(false);
      }
      else
      {
         return(true);
      }
   }
}
