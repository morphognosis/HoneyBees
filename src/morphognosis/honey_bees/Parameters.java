// For conditions of distribution and use, see copyright notice in Main.java

// Parameters.

package morphognosis.honey_bees;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import morphognosis.Morphognostic;
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
   public static int   FLOWER_NECTAR_CAPACITY               = 3;
   public static float FLOWER_NECTAR_PRODUCTION_PROBABILITY = .01f;

   // Bee parameters.
   public static int   NUM_BEES             = 5;
   public static float BEE_TURN_PROBABILITY = .2f;
   public static float BEE_RETURN_TO_HIVE_PROBABILITY_INCREMENT = .01f;
   public static int   BEE_NUM_DISTANCE_VALUES             = 3;
   public static int   BEE_NECTAR_DISPLAY_DURATION         = 10;
   public static float BEE_DEADLOCK_PREVENTION_PROBABILITY = .001f;

   // Morphognosis parameters.
   public static int NUM_NEIGHBORHOODS = 3;  // Morphognostic.DEFAULT_NUM_NEIGHBORHOODS + 1;
   public static int NEIGHBORHOOD_INITIAL_DIMENSION    = Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION;
   public static int NEIGHBORHOOD_DIMENSION_STRIDE     = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE;
   public static int NEIGHBORHOOD_DIMENSION_MULTIPLIER = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER;
   public static int EPOCH_INTERVAL_STRIDE             = 3;
   public static int EPOCH_INTERVAL_MULTIPLIER         = Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER;

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
      Utility.saveInt(writer, BEE_NUM_DISTANCE_VALUES);
      Utility.saveInt(writer, BEE_NECTAR_DISPLAY_DURATION);
      Utility.saveFloat(writer, BEE_DEADLOCK_PREVENTION_PROBABILITY);
      Utility.saveInt(writer, NUM_NEIGHBORHOODS);
      Utility.saveInt(writer, NEIGHBORHOOD_INITIAL_DIMENSION);
      Utility.saveInt(writer, NEIGHBORHOOD_DIMENSION_STRIDE);
      Utility.saveInt(writer, NEIGHBORHOOD_DIMENSION_MULTIPLIER);
      Utility.saveInt(writer, EPOCH_INTERVAL_STRIDE);
      Utility.saveInt(writer, EPOCH_INTERVAL_MULTIPLIER);
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
      BEE_NUM_DISTANCE_VALUES             = Utility.loadInt(reader);
      BEE_NECTAR_DISPLAY_DURATION         = Utility.loadInt(reader);
      BEE_DEADLOCK_PREVENTION_PROBABILITY = Utility.loadFloat(reader);
      NUM_NEIGHBORHOODS = Utility.loadInt(reader);
      NEIGHBORHOOD_INITIAL_DIMENSION    = Utility.loadInt(reader);
      NEIGHBORHOOD_DIMENSION_STRIDE     = Utility.loadInt(reader);
      NEIGHBORHOOD_DIMENSION_MULTIPLIER = Utility.loadInt(reader);
      EPOCH_INTERVAL_STRIDE             = Utility.loadInt(reader);
      EPOCH_INTERVAL_MULTIPLIER         = Utility.loadInt(reader);
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
      System.out.println("BEE_NUM_DISTANCE_VALUES = " + BEE_NUM_DISTANCE_VALUES);
      System.out.println("BEE_DANCE_DURATION = " + BEE_NECTAR_DISPLAY_DURATION);
      System.out.println("BEE_DEADLOCK_PREVENTION_PROBABILITY = " + BEE_DEADLOCK_PREVENTION_PROBABILITY);
      System.out.println("NUM_NEIGHBORHOODS = " + NUM_NEIGHBORHOODS);
      System.out.println("NEIGHBORHOOD_INITIAL_DIMENSION = " + NEIGHBORHOOD_INITIAL_DIMENSION);
      System.out.println("NEIGHBORHOOD_DIMENSION_STRIDE = " + NEIGHBORHOOD_DIMENSION_STRIDE);
      System.out.println("NEIGHBORHOOD_DIMENSION_MULTIPLIER = " + NEIGHBORHOOD_DIMENSION_MULTIPLIER);
      System.out.println("EPOCH_INTERVAL_STRIDE = " + EPOCH_INTERVAL_STRIDE);
      System.out.println("EPOCH_INTERVAL_MULTIPLIER = " + EPOCH_INTERVAL_MULTIPLIER);
   }
}
