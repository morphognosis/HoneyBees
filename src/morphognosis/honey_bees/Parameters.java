// For conditions of distribution and use, see copyright notice in Main.java

// Parameters.

package morphognosis.honey_bees;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import morphognosis.Utility;

public class Parameters
{
   // Dimensions.
   public static int WORLD_WIDTH  = 21;
   public static int WORLD_HEIGHT = 21;
   public static int HIVE_RADIUS  = 3;

   // Note: probabilities are applied for each step.

   // Flower parameters.
   public static float FLOWER_SPROUT_PROBABILITY            = .01f;
   public static int   FLOWER_NECTAR_CAPACITY               = 2;
   public static float FLOWER_NECTAR_PRODUCTION_PROBABILITY = 0.0f;
   public static int   FLOWER_RADIUS = 10;

   // Bee parameters.
   public static int   NUM_BEES             = 1;
   public static float BEE_TURN_PROBABILITY = .2f;
   public static float BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT = .01f;

   // Morphognosis parameters.
   public static int     NUM_NEIGHBORHOODS        = 4;
   public static int[][] NEIGHBORHOOD_DIMENSIONS  = { { 3, 1 }, { 3, 1 }, { 3, 1 }, { 3, 1 } };
   public static int[]   NEIGHBORHOOD_DURATIONS   = { 1, (int)((float)FLOWER_RADIUS * .7f), FLOWER_RADIUS, 75 };
   public static boolean BINARY_VALUE_AGGREGATION = true;

   // Metamorph neural network parameters.
   public static double NN_LEARNING_RATE = 0.1;
   public static double NN_MOMENTUM      = 0.2;
   public static String NN_HIDDEN_LAYERS = "20,20";
   public static int    NN_TRAINING_TIME = 10000;

   // Save.
   public static void save(DataOutputStream writer) throws IOException
   {
      Utility.saveInt(writer, WORLD_WIDTH);
      Utility.saveInt(writer, WORLD_HEIGHT);
      Utility.saveInt(writer, HIVE_RADIUS);
      Utility.saveFloat(writer, FLOWER_SPROUT_PROBABILITY);
      Utility.saveInt(writer, FLOWER_NECTAR_CAPACITY);
      Utility.saveFloat(writer, FLOWER_NECTAR_PRODUCTION_PROBABILITY);
      Utility.saveInt(writer, NUM_BEES);
      Utility.saveFloat(writer, BEE_TURN_PROBABILITY);
      Utility.saveFloat(writer, BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT);
      Utility.saveInt(writer, NUM_NEIGHBORHOODS);
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Utility.saveInt(writer, NEIGHBORHOOD_DIMENSIONS[i][0]);
         Utility.saveInt(writer, NEIGHBORHOOD_DIMENSIONS[i][1]);
      }
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Utility.saveInt(writer, NEIGHBORHOOD_DURATIONS[i]);
      }
      int v = 0;
      if (BINARY_VALUE_AGGREGATION)
      {
         v = 1;
      }
      Utility.saveInt(writer, v);
      Utility.saveDouble(writer, NN_LEARNING_RATE);
      Utility.saveDouble(writer, NN_MOMENTUM);
      Utility.saveString(writer, NN_HIDDEN_LAYERS);
      Utility.saveInt(writer, NN_TRAINING_TIME);
      writer.flush();
   }


   // Load.
   public static void load(DataInputStream reader) throws IOException
   {
      WORLD_WIDTH  = Utility.loadInt(reader);
      WORLD_HEIGHT = Utility.loadInt(reader);
      HIVE_RADIUS  = Utility.loadInt(reader);
      FLOWER_SPROUT_PROBABILITY            = Utility.loadFloat(reader);
      FLOWER_NECTAR_CAPACITY               = Utility.loadInt(reader);
      FLOWER_NECTAR_PRODUCTION_PROBABILITY = Utility.loadFloat(reader);
      NUM_BEES             = Utility.loadInt(reader);
      BEE_TURN_PROBABILITY = Utility.loadFloat(reader);
      BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT = Utility.loadFloat(reader);
      NUM_NEIGHBORHOODS       = Utility.loadInt(reader);
      NEIGHBORHOOD_DIMENSIONS = new int[NUM_NEIGHBORHOODS][2];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         NEIGHBORHOOD_DIMENSIONS[i][0] = Utility.loadInt(reader);
         NEIGHBORHOOD_DIMENSIONS[i][1] = Utility.loadInt(reader);
      }
      NEIGHBORHOOD_DURATIONS = new int[NUM_NEIGHBORHOODS];
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         NEIGHBORHOOD_DURATIONS[i] = Utility.loadInt(reader);
      }
      int v = Utility.loadInt(reader);
      if (v == 1)
      {
         BINARY_VALUE_AGGREGATION = true;
      }
      else
      {
         BINARY_VALUE_AGGREGATION = false;
      }
      NN_LEARNING_RATE = Utility.loadDouble(reader);
      NN_MOMENTUM      = Utility.loadDouble(reader);
      NN_HIDDEN_LAYERS = Utility.loadString(reader);
      NN_TRAINING_TIME = Utility.loadInt(reader);
   }


   // Print.
   public static void print()
   {
      System.out.println("WORLD_WIDTH = " + WORLD_WIDTH);
      System.out.println("WORLD_HEIGHT = " + WORLD_HEIGHT);
      System.out.println("HIVE_RADIUS = " + HIVE_RADIUS);
      System.out.println("FLOWER_SPROUT_PROBABILITY = " + FLOWER_SPROUT_PROBABILITY);
      System.out.println("FLOWER_NECTAR_CAPACITY = " + FLOWER_NECTAR_CAPACITY);
      System.out.println("FLOWER_NECTAR_PRODUCTION_PROBABILITY = " + FLOWER_NECTAR_PRODUCTION_PROBABILITY);
      System.out.println("NUM_BEES = " + NUM_BEES);
      System.out.println("BEE_TURN_PROBABILITY = " + BEE_TURN_PROBABILITY);
      System.out.println("BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT = " + BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT);
      System.out.println("NUM_NEIGHBORHOODS = " + NUM_NEIGHBORHOODS);
      System.out.print("NEIGHBORHOOD_DIMENSIONS (element: { <neighborhood dimension>, <sector dimension> })={");
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         System.out.print("{" + NEIGHBORHOOD_DIMENSIONS[i][0] + "," + NEIGHBORHOOD_DIMENSIONS[i][1] + "}");
         if (i < NUM_NEIGHBORHOODS - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.print("NEIGHBORHOOD_DURATIONS={");
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         System.out.print(NEIGHBORHOOD_DURATIONS[i] + "");
         if (i < NUM_NEIGHBORHOODS - 1)
         {
            System.out.print(",");
         }
      }
      System.out.println("}");
      System.out.println("BINARY_VALUE_AGGREGATION = " + BINARY_VALUE_AGGREGATION);
      System.out.println("NN_LEARNING_RATE = " + NN_LEARNING_RATE);
      System.out.println("NN_MOMENTUM = " + NN_MOMENTUM);
      System.out.println("NN_HIDDEN_LAYERS = " + NN_HIDDEN_LAYERS);
      System.out.println("NN_TRAINING_TIME = " + NN_TRAINING_TIME);
   }
}
