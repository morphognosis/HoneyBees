// For conditions of distribution and use, see copyright notice in Main.java

// Foraging RNN.
// Test the ability of an RNN to track relative location, a skill necessary to return to hive.
// There are 9 possible responses: 8 orientations plus a forward movement.
// Classification: the orientation to the initial location.

package morphognosis.honey_bees;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.LSTMGenerator;
import de.jannlab.training.GradientDescent;
import morphognosis.Orientation;
import de.jannlab.misc.TimeCounter;
import de.jannlab.tools.ClassificationValidator;

public class ForagingRNN
{
   // Parameters.
   public static double LEARNING_RATE       = 0.001;
   public static double MOMENTUM            = 0.9;
   public static int    NUM_HIDDEN_NEURONS  = 5;
   public static int    NUM_HIDDEN_LAYERS   = 1;
   public static float  TURN_PROBABILITY    = 0.5f;
   public static int    NUM_TRAIN           = 1;
   public static int    NUM_TEST            = 1;
   public static int    MIN_SEQUENCE_LENGTH = 5;
   public static int    MAX_SEQUENCE_LENGTH = 15;
   public static int    EPOCHS      = 200;
   public static int    RANDOM_SEED = 4517;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java morphognosis.honey_bees.ForagingRNN\n" +
      "     [-learningRate <float> (default=" + LEARNING_RATE + ")]\n" +
      "     [-momentum <float> (default=" + MOMENTUM + ")]\n" +
      "     [-numHiddenNeurons <quantity> (default=" + NUM_HIDDEN_NEURONS + ")]\n" +
      "     [-numHiddenLayers <quantity> (default=" + NUM_HIDDEN_LAYERS + ")]\n" +
      "     [-genHoneyBeeData (generate data from honey bee foraging)]\n" +
      "     [-numTrain <quantity> (default=" + NUM_TRAIN + ")]\n" +
      "     [-numTest <quantity> (default=" + NUM_TEST + ")]\n" +
      "     [-minSequenceLength <quantity> (default=" + MIN_SEQUENCE_LENGTH + ")]\n" +
      "     [-maxSequenceLength <quantity> (default=" + MAX_SEQUENCE_LENGTH + ")]\n" +
      "     [-turnProbability <probability> (default=" + TURN_PROBABILITY + ")]\n" +
      "     [-epochs <quantity> (default=" + EPOCHS + ")]\n" +
      "     [-randomSeed <random number seed> (default=" + RANDOM_SEED + ")]";

   private static TimeCounter TC  = new TimeCounter();
   private static Random      rnd = new Random(RANDOM_SEED);

   // Sample is a sequence of responses, where each response is an orientation (x8) or a forward movement.
   public static Sample generateSample(World world, boolean verbose)
   {
      HoneyBee bee = world.bees[0];

      while (bee.response != HoneyBee.DEPOSIT_NECTAR)
      {
         world.step();
      }
      bee.orientation = Orientation.NORTH;
      int cx = bee.x;
      int cy = bee.y;
      relocateFlower(world);
      ArrayList<double[]> sequence = new ArrayList<double[]>();
      while (true)
      {
         world.step();
         if (bee.response == HoneyBee.EXTRACT_NECTAR) { break; }
         double[] v      = new double[9];
         v[bee.response] = 1.0;
         if (verbose)
         {
            for (int j = 0; j < 9; j++)
            {
               System.out.print(v[j] + " ");
            }
            System.out.println();
         }
         sequence.add(v);
      }
      if (verbose)
      {
         System.out.println("sequence length=" + sequence.size());
      }
      double[] data = new double[sequence.size() * 9];
      int i = 0;
      for (double[] d : sequence)
      {
         for (int j = 0; j < 9; j++)
         {
            data[i] = d[j];
            i++;
         }
      }
      double[] target = getTarget(bee.x - cx, bee.y - cy);
      if (verbose)
      {
         System.out.println("target:");
         for (int j = 0; j < 9; j++)
         {
            System.out.print(target[j] + " ");
         }
         System.out.println();
      }
      return(new Sample(data, target, 9, sequence.size(), 9, 1));
   }


   public static void relocateFlower(World world)
   {
      for (int x = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            world.cells[x][y].flower = null;
         }
      }
      double cx = Parameters.WORLD_WIDTH / 2.0;
      double cy = Parameters.WORLD_HEIGHT / 2.0;
      for (int i = 0; i < Parameters.NUM_FLOWERS; i++)
      {
         for (int j = 0; j < 100; j++)
         {
            int x = rnd.nextInt(Parameters.WORLD_WIDTH);
            int y = rnd.nextInt(Parameters.WORLD_HEIGHT);
            if (Math.sqrt(((double)y - cy) * ((double)y - cy) + ((double)x - cx) * (
                             (double)x - cx)) <= (double)Parameters.FLOWER_RANGE)
            {
               Cell cell = world.cells[x][y];
               if (!cell.hive && (cell.bee == null))
               {
                  Flower flower = new Flower();
                  cell.flower = flower;
                  if ((Parameters.FLOWER_NECTAR_CAPACITY > 0) &&
                      (rnd.nextFloat() < Parameters.FLOWER_NECTAR_PRODUCTION_PROBABILITY))
                  {
                     flower.nectar = 1;
                  }
                  break;
               }
            }
            if (j == 99)
            {
               System.err.println("Cannot place flower in world");
               System.exit(1);
            }
         }
      }
   }


   public static Sample generateSample(final int length, boolean verbose)
   {
      double[] data = new double[length * 9];
      //
      int x           = 0;
      int y           = 0;
      int orientation = 0;
      if (verbose) { System.out.println("data:"); }
      for (int i = 0; i < length; i++)
      {
         int response = 8;
         if (rnd.nextFloat() < TURN_PROBABILITY)
         {
            response = rnd.nextInt(8);
         }
         data[(i * 9) + response] = 1.0;
         if (verbose)
         {
            for (int j = 0; j < 9; j++)
            {
               System.out.print(data[(i * 9) + j] + " ");
            }
            System.out.println();
         }
         if (response < 8)
         {
            orientation = response;
         }
         else
         {
            switch (orientation)
            {
            case 0:
               y++;
               break;

            case 1:
               x++;
               y++;
               break;

            case 2:
               x++;
               break;

            case 3:
               x++;
               y--;
               break;

            case 4:
               y--;
               break;

            case 5:
               x--;
               y--;
               break;

            case 6:
               x--;
               break;

            case 7:
               x--;
               y++;
               break;
            }
         }
      }
      //
      double[] target = getTarget(x, y);
      if (verbose)
      {
         System.out.println("target:");
         for (int j = 0; j < 9; j++)
         {
            System.out.print(target[j] + " ");
         }
         System.out.println();
      }
      return(new Sample(data, target, 9, length, 9, 1));
   }


   // Generate target from position.
   public static double[] getTarget(int x, int y)
   {
      double[] target = new double[9];
      if (x > 0)
      {
         if (y > 0)
         {
            target[1] = 1.0;
         }
         else if (y < 0)
         {
            target[3] = 1.0;
         }
         else
         {
            target[2] = 1.0;
         }
      }
      else if (x < 0)
      {
         if (y > 0)
         {
            target[7] = 1.0;
         }
         else if (y < 0)
         {
            target[5] = 1.0;
         }
         else
         {
            target[6] = 1.0;
         }
      }
      else
      {
         if (y > 0)
         {
            target[0] = 1.0;
         }
         else if (y < 0)
         {
            target[4] = 1.0;
         }
         else
         {
            target[8] = 1.0;
         }
      }
      return(target);
   }


   public static SampleSet generate(int n, World world)
   {
      SampleSet set = new SampleSet();

      //
      for (int i = 0; i < n; i++)
      {
         set.add(generateSample(world, true));
      }
      //
      return(set);
   }


   public static SampleSet generate(int n)
   {
      SampleSet set = new SampleSet();

      //
      for (int i = 0; i < n; i++)
      {
         int length = rnd.nextInt((MAX_SEQUENCE_LENGTH - MIN_SEQUENCE_LENGTH + 1)) + MIN_SEQUENCE_LENGTH;
         set.add(generateSample(length, true));
      }
      //
      return(set);
   }


   public static Net LSTM(final int in, final int hid, int layers, final int out)
   {
      //
      LSTMGenerator gen = new LSTMGenerator();

      gen.inputLayer(in);
      for (int i = 0; i < layers; i++)
      {
         gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.TANH, true);
      }
      gen.outputLayer(out, CellType.TANH);
      //
      return(gen.generate());
   }


   public static void main(String[] args) throws IOException
   {
      boolean genHoneyBeeData = false;
      boolean gotParm         = false;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-learningRate"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid learningRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               LEARNING_RATE = Double.parseDouble(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid learningRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((LEARNING_RATE < 0.0) || (LEARNING_RATE > 1.0))
            {
               System.err.println("Invalid learningRate option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-momentum"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid momentum option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MOMENTUM = Double.parseDouble(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid momentum option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MOMENTUM < 0.0)
            {
               System.err.println("Invalid momentum option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numHiddenNeurons"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_HIDDEN_NEURONS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_HIDDEN_NEURONS < 0)
            {
               System.err.println("Invalid numHiddenNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numHiddenLayers"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numHiddenLayers option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_HIDDEN_LAYERS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numHiddenLayers option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_HIDDEN_LAYERS < 0)
            {
               System.err.println("Invalid numHiddenLayers option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-genHoneyBeeData"))
         {
            genHoneyBeeData = true;
            continue;
         }
         if (args[i].equals("-numTrain"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numTrain option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_TRAIN = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numTrain option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_TRAIN < 0)
            {
               System.err.println("Invalid numTrain option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numTest"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numTest option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_TEST = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numTest option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_TEST < 0)
            {
               System.err.println("Invalid numTest option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-minSequenceLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid minSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MIN_SEQUENCE_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid minSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MIN_SEQUENCE_LENGTH < 0)
            {
               System.err.println("Invalid minSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-maxSequenceLength"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               MAX_SEQUENCE_LENGTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (MAX_SEQUENCE_LENGTH < 0)
            {
               System.err.println("Invalid maxSequenceLength option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-turnProbability"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid turnProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               TURN_PROBABILITY = Float.parseFloat(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid turnProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            if ((TURN_PROBABILITY < 0.0) || (TURN_PROBABILITY > 1.0))
            {
               System.err.println("Invalid turnProbability option");
               System.err.println(Usage);
               System.exit(1);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-epochs"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               EPOCHS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (EPOCHS < 0)
            {
               System.err.println("Invalid epochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               RANDOM_SEED = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }
      if (genHoneyBeeData && gotParm)
      {
         System.err.println("Unnecessary options used with genHoneyBeeData option");
         System.exit(1);
      }
      if (MIN_SEQUENCE_LENGTH > MAX_SEQUENCE_LENGTH)
      {
         System.err.println("minSequenceLength cannot be greater than maxSequenceLength");
         System.exit(1);
      }

      //
      // generate train and test data.
      //
      Parameters.NUM_FLOWERS     = 1;
      Parameters.NUM_BEES        = 1;
      HoneyBee.constantNectar    = true;
      HoneyBee.minConstantNectar = 1;
      HoneyBee.maxConstantNectar = 1;
      World world = null;
      try
      {
         world = new World(RANDOM_SEED);
      }
      catch (Exception e)
      {
         System.err.println("Cannot initialize world: " + e.getMessage());
         System.exit(1);
      }
      SampleSet trainset;
      SampleSet testset;
      if (genHoneyBeeData)
      {
         System.out.println("Training samples:");
         trainset = generate(NUM_TRAIN, world);
         System.out.println("Testing samples:");
         testset = generate(NUM_TEST, world);
      }
      else
      {
         System.out.println("Training samples:");
         trainset = generate(NUM_TRAIN);
         System.out.println("Testing samples:");
         testset = generate(NUM_TEST);
      }
      //
      // build network.
      //
      Net net    = LSTM(9, NUM_HIDDEN_NEURONS, NUM_HIDDEN_LAYERS, 9);
      int length = Math.max(trainset.maxSequenceLength(), testset.maxSequenceLength());

      net.rebuffer(length);
      net.initializeWeights();
      //
      // setup network.
      //
      final int maxlength = Math.max(trainset.maxSequenceLength(), testset.maxSequenceLength());
      net.initializeWeights(rnd);
      net.rebuffer(maxlength);
      //
      // setup trainer.
      //
      GradientDescent trainer = new GradientDescent();
      trainer.setNet(net);
      trainer.setRnd(rnd);
      trainer.setPermute(true);
      trainer.setTrainingSet(trainset);
      trainer.setLearningRate(LEARNING_RATE);
      trainer.setMomentum(MOMENTUM);
      trainer.setEpochs(EPOCHS);
      //
      TC.reset();
      //
      // perform training.
      //
      trainer.train();
      //
      System.out.println(
         "training time: " +
         TC.valueMilliDouble() +
         " ms."
         );
      //
      // evaluate learning success.
      //
      ClassificationValidator f = new ClassificationValidator(net);
      int correct = 0;
      int setSize = trainset.size();
      for (int i = 0; i < setSize; i++)
      {
         Sample s = trainset.get(i);
         double[] results = f.apply(s);
         int    maxidx = -1;
         double maxval = -1.0;
         for (int j = 0; j < results.length; j++)
         {
            if ((maxidx == -1) || (maxval < results[j]))
            {
               maxidx = j;
               maxval = results[j];
            }
         }
         double[] t = s.getTarget();
         int targetidx = 0;
         for ( ; targetidx < t.length; targetidx++)
         {
            if (t[targetidx] == 1.0) { break; }
         }
         if (targetidx == maxidx) { correct++; }
      }
      System.out.print("trainset results: correct/size=" + correct + "/" + setSize);
      if (setSize > 0)
      {
         System.out.print(" (" + ((double)correct / (double)setSize) * 100.0 + "%)");
      }
      System.out.println();
      //double ratio = f.ratio();
      //System.out.println("trainset classification result: " + ratio * 100.0 + "%.");
      f       = new ClassificationValidator(net);
      correct = 0;
      setSize = testset.size();
      for (int i = 0; i < setSize; i++)
      {
         Sample s = testset.get(i);
         double[] results = f.apply(s);
         int    maxidx = -1;
         double maxval = -1.0;
         for (int j = 0; j < results.length; j++)
         {
            if ((maxidx == -1) || (maxval < results[j]))
            {
               maxidx = j;
               maxval = results[j];
            }
         }
         double[] t = s.getTarget();
         int targetidx = 0;
         for ( ; targetidx < t.length; targetidx++)
         {
            if (t[targetidx] == 1.0) { break; }
         }
         if (targetidx == maxidx) { correct++; }
      }
      System.out.print("testset results: correct/size=" + correct + "/" + setSize);
      if (setSize > 0)
      {
         System.out.print(" (" + ((double)correct / (double)setSize) * 100.0 + "%)");
      }
      System.out.println();
      //ratio = f.ratio();
      //System.out.println("testset classification result: " + ratio * 100.0 + "%.");
   }
}
