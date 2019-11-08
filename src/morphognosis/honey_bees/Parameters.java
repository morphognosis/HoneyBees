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
       public static int WORLD_WIDTH = 21;
       public static int WORLD_HEIGHT = 21;
       public static int HIVE_RADIUS = 3;
       
       // Note: probabilities are determined for each step.
       
       // Flower parameters.
       public static float FLOWER_SPROUT_PROBABILITY = .01f;
       public static float FLOWER_DEATH_PROBABILITY = .01f;
       public static int FLOWER_NECTAR_CAPACITY = 3;
       public static float FLOWER_NECTAR_PRODUCTION_PROBABILITY = .05f;
       
       // Bee parameters.
       public static int NUM_BEES = 5;
       public static float BEE_FORAGE_TURN_PROBABILITY = .1f;
       public static float BEE_HIVE_TURN_PROBABILITY = .2f;       
       public static float BEE_LEAVE_HIVE_TO_FORAGE_PROBABILITY = .05f;
       public static float BEE_RETURN_TO_HIVE_PROBABILITY = .01f;
       public static int BEE_DANCE_DURATION = 10;
       
       // Save.
       public static void save(DataOutputStream writer) throws IOException
       {
    	      Utility.saveInt(writer, WORLD_WIDTH);
    	      writer.flush();    	   
       }
       
       // Load.
       public static void load(DataInputStream reader) throws IOException
       {
    	   
       }
}
