// For conditions of distribution and use, see copyright notice in Main.java

// Foraging RNN.
// Test the ability of an RNN to forage when allowed to sense its relative location
// to the hive upon discovering nectar. The RNN must return to the hive and deposit nectar.
// If surplus nectar was sensed, it must also perform an appropriate dance and move toward
// the nectar.
//
// Input:
// <nectar presence><nectar carry><orientation><25 (5x5) hive presence>
// Target:
// <response>

package morphognosis.honey_bees;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
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
   public static double  LEARNING_RATE      = 0.001;
   public static double  MOMENTUM           = 0.9;
   public static int     NUM_HIDDEN_NEURONS = 5;
   public static int     NUM_HIDDEN_LAYERS  = 1;
   public static int     EPOCHS             = 200;
   public static int     RANDOM_SEED        = 4517;
   public static boolean VERBOSE            = false;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java morphognosis.honey_bees.ForagingRNN\n" +
      "     [-numFlowers <quantity> (default=" + Parameters.NUM_FLOWERS + ")]\n" +
      "     [-learningRate <float> (default=" + LEARNING_RATE + ")]\n" +
      "     [-momentum <float> (default=" + MOMENTUM + ")]\n" +
      "     [-numHiddenNeurons <quantity> (default=" + NUM_HIDDEN_NEURONS + ")]\n" +
      "     [-numHiddenLayers <quantity> (default=" + NUM_HIDDEN_LAYERS + ")]\n" +
      "     [-epochs <quantity> (default=" + EPOCHS + ")]\n" +
      "     [-randomSeed <random number seed> (default=" + RANDOM_SEED + ")]\n" +
      "     [-exportDataset <filename>]\n" +
      "     [-verbose (default=" + VERBOSE + ")]";

   // NN input size.
   public static final int NN_INPUT_SIZE = 35;

   // Dataset file name.
   private static String           DATASET_FILENAME = null;
   private static FileOutputStream datasetOutput    = null;
   private static PrintWriter      datasetWriter    = null;

   private static TimeCounter  TC  = new TimeCounter();
   private static SecureRandom rnd = new SecureRandom();

   private static int     maxSequenceSize = -1;
   private static boolean noDanceLearn    = false;

   // Sample input is a sequence of sensor values, target is a sequence of responses.
   public static Sample generateSample(World world, int flower, int orientation, boolean surplusNectar)
   {
      world.reset();
      HoneyBee bee = world.bees[0];

      if (VERBOSE)
      {
         System.out.println("generate sequence flower=" + flower + ", " +
                            "orientation=" + orientation + ", surplus nectar=" + surplusNectar + ":");
      }

      // Remove other flowers.
      int fx = -1;
      int fy = -1;
      for (int x = 0, i = 0; x < Parameters.WORLD_WIDTH; x++)
      {
         for (int y = 0; y < Parameters.WORLD_HEIGHT; y++)
         {
            if (world.cells[x][y].flower != null)
            {
               if (i == flower)
               {
                  fx = x;
                  fy = y;
                  world.cells[fx][fy].flower.nectar = true;
               }
               else
               {
                  world.cells[x][y].flower = null;
               }
               i++;
            }
         }
      }

      // Forage to flower.
      while (world.cells[bee.x][bee.y].flower == null)
      {
         world.step();
      }
      bee.orientation = orientation;
      ArrayList<double[]> inputSeq  = new ArrayList<double[]>();
      ArrayList<double[]> targetSeq = new ArrayList<double[]>();
      bee.handlingNectar = true;
      boolean first        = true;
      int     extractCount = 0;
      while (bee.handlingNectar)
      {
         if ((maxSequenceSize != -1) && (inputSeq.size() >= maxSequenceSize)) { break; }
         double[] input = new double[NN_INPUT_SIZE];
         String nectarPresence = "false";
         if (world.cells[bee.x][bee.y].flower != null)
         {
            if (world.cells[bee.x][bee.y].flower.nectar)
            {
               input[0]       = 1.0;
               nectarPresence = "true";
            }
         }
         String nectarCarry = "false";
         if (bee.nectarCarry)
         {
            input[1]    = 1.0;
            nectarCarry = "true";
         }
         int o = bee.orientation;
         input[2 + bee.orientation] = 1.0;
         int hx = -1;
         int hy = -1;
         int bx = bee.x;
         int by = bee.y;
         if (first || world.cells[bee.x][bee.y].hive)
         {
            first = false;
            bx    = bee.x;
            hx    = bee.x / 4;
            if (hx > 4) { hx = 4; }
            hx = 4 - hx;
            by = bee.y;
            hy = bee.y / 4;
            if (hy > 4) { hy = 4; }
            hy = 4 - hy;
            input[10 + (hy * 5) + hx] = 1.0;
         }
         world.step();
         if (bee.response == HoneyBee.EXTRACT_NECTAR) { extractCount++; }
         if (bee.handlingNectar && (extractCount < 2))
         {
            if (VERBOSE)
            {
               System.out.println("input: ");
               System.out.println("nectar presence [0]: " + input[0] + " (" + nectarPresence + ")");
               System.out.println("nectar carry [1]: " + input[1] + " (" + nectarCarry + ")");
               System.out.print("orientation [2-9]: ");
               for (int j = 2; j < 10; j++)
               {
                  System.out.print(input[j] + " ");
               }
               System.out.println("(" + Orientation.toName(o) + ")");
               System.out.println("hive presence [10-34]:");
               for (int i = 30, j = NN_INPUT_SIZE, k = 0; k < 5; j -= 5, i = j - 5, k++)
               {
                  for ( ; i < j; i++)
                  {
                     System.out.print(input[i] + " ");
                  }
                  System.out.println();
               }
               if (hx != -1)
               {
                  System.out.println("(bee=[" + bx + "," + by + "]->[" + (bx / 4) + "," +
                                     (by / 4) + "], hive=[" + hx + "," + hy + "], index=" +
                                     (10 + (hy * 5) + hx + ")"));
               }
            }
            inputSeq.add(input);
            world.cells[fx][fy].flower.nectar = surplusNectar;
            double[] target      = new double[HoneyBee.NUM_RESPONSES];
            target[bee.response] = 1.0;
            if (VERBOSE)
            {
               System.out.println("target: ");
               System.out.print("response: ");
               for (int j = 0; j < HoneyBee.NUM_RESPONSES; j++)
               {
                  System.out.print(target[j] + " ");
               }
               System.out.println("(" + HoneyBee.getResponseName(bee.response) + ")");
            }
            targetSeq.add(target);
         }
      }
      if (VERBOSE)
      {
         System.out.println("sequence length=" + inputSeq.size());
      }
      double[] inputData = new double[inputSeq.size() * NN_INPUT_SIZE];
      int i = 0;
      for (double[] d : inputSeq)
      {
         for (int j = 0; j < NN_INPUT_SIZE; j++)
         {
            inputData[i] = d[j];
            i++;
         }
      }
      if (datasetWriter != null)
      {
         datasetWriter.print("input:\n");
         for (int j = 0; j < inputData.length; j++)
         {
            datasetWriter.print(inputData[j] + "");
            if (j < inputData.length - 1)
            {
               datasetWriter.print(",");
            }
         }
         datasetWriter.print("\n");
      }
      double[] targetData = new double[targetSeq.size() * HoneyBee.NUM_RESPONSES];
      i = 0;
      for (double[] d : targetSeq)
      {
         for (int j = 0; j < HoneyBee.NUM_RESPONSES; j++)
         {
            targetData[i] = d[j];
            i++;
         }
      }
      if (datasetWriter != null)
      {
         datasetWriter.print("target:\n");
         for (int j = 0; j < targetData.length; j++)
         {
            datasetWriter.print(targetData[j] + "");
            if (j < targetData.length - 1)
            {
               datasetWriter.print(",");
            }
         }
         datasetWriter.print("\n");
      }
      return(new Sample(inputData, targetData, NN_INPUT_SIZE, inputSeq.size(),
                        HoneyBee.NUM_RESPONSES, targetSeq.size()));
   }


   // Generate training data.
   public static SampleSet generateTrainingData(World world)
   {
      SampleSet set = new SampleSet();

      if (DATASET_FILENAME != null)
      {
         try
         {
            datasetOutput = new FileOutputStream(new File(DATASET_FILENAME));
         }
         catch (Exception e)
         {
            System.err.println("Cannot open dataset file " + DATASET_FILENAME + ":" + e.getMessage());
         }
         if (datasetOutput != null)
         {
            datasetWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(datasetOutput)));
            int n = 1;
            if (!noDanceLearn) { n++; }
            datasetWriter.write("training set length=" + (Parameters.NUM_FLOWERS * Orientation.NUM_ORIENTATIONS * n) +
                                ", input size=" + NN_INPUT_SIZE + ", target size=" + HoneyBee.NUM_RESPONSES + "\n");
         }
      }

      for (int i = 0; i < Parameters.NUM_FLOWERS; i++)
      {
         for (int j = 0; j < Orientation.NUM_ORIENTATIONS; j++)
         {
            set.add(generateSample(world, i, j, false));
            if (!noDanceLearn) { set.add(generateSample(world, i, j, true)); }
         }
      }
      if (VERBOSE)
      {
         System.out.println("training set size=" + set.size());
      }

      if (datasetWriter != null)
      {
         datasetWriter.flush();
         try {
            datasetOutput.close();
         }
         catch (IOException e) {}
      }

      return(set);
   }


   public static Net LSTM(final int in, final int hid, int layers, final int out)
   {
      LSTMGenerator gen = new LSTMGenerator();

      gen.inputLayer(in);
      for (int i = 0; i < layers; i++)
      {
         gen.hiddenLayer(hid, CellType.SIGMOID, CellType.TANH, CellType.TANH, true);
      }
      gen.outputLayer(out, CellType.TANH);
      return(gen.generate());
   }


   public static void main(String[] args) throws IOException
   {
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-numFlowers"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numFlowers option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               Parameters.NUM_FLOWERS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numFlowers option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (Parameters.NUM_FLOWERS < 0)
            {
               System.err.println("Invalid numFlowers option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
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
         if (args[i].equals("-exportDataset"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid exportDataset option");
               System.err.println(Usage);
               System.exit(1);
            }
            DATASET_FILENAME = args[i];
            continue;
         }
         if (args[i].equals("-maxSequenceSize"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxSequenceSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               maxSequenceSize = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxSequenceSize option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-noDanceLearn"))
         {
            noDanceLearn = true;
            continue;
         }
         if (args[i].equals("-verbose"))
         {
            VERBOSE = true;
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

      //
      // generate train data.
      //
      Parameters.WORLD_WIDTH  = 21;
      Parameters.WORLD_HEIGHT = 21;
      Parameters.HIVE_RADIUS  = 3;
      Parameters.NUM_BEES     = 1;
      Parameters.FLOWER_SURPLUS_NECTAR_PROBABILITY = 0.0f;
      rnd.setSeed(RANDOM_SEED);
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
      if (VERBOSE) { System.out.println("Training data:"); }
      SampleSet trainset = generateTrainingData(world);

      //
      // build network.
      //
      Net net       = LSTM(NN_INPUT_SIZE, NUM_HIDDEN_NEURONS, NUM_HIDDEN_LAYERS, HoneyBee.NUM_RESPONSES);
      int maxlength = trainset.maxSequenceLength();
      net.rebuffer(maxlength);
      net.initializeWeights();
      //
      // setup network.
      //
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
      System.out.println("training time: " + TC.valueMilliDouble() + " ms.");
      //
      // evaluate learning success.
      //
      ClassificationValidator f = new ClassificationValidator(net);
      int correctResponses      = 0;
      int totalResponses        = 0;
      int correctForages        = 0;
      int totalForages          = 0;
      int setSize = trainset.size();
      System.out.println("number of forages=" + setSize);
      System.out.println("training results:");
      for (int i = 0; i < setSize; i++)
      {
         System.out.println("forage=" + i);
         boolean forageCorrect = true;
         Sample  s             = trainset.get(i);
         int     seqLength     = s.getInputLength();
         double[] inputData  = s.getInput();
         double[] targetData = s.getTarget();
         for (int j = 0; j < seqLength; j++)
         {
            System.out.println("step=" + (j + 1));
            Sample s2 = new Sample(inputData, targetData, NN_INPUT_SIZE, j + 1,
                                   HoneyBee.NUM_RESPONSES, j + 1);
            double[] prediction = f.apply(s2);
            System.out.print("prediction: ");
            int    predictionIdx = -1;
            double maxval        = -1.0;
            for (int k = 0; k < prediction.length; k++)
            {
               System.out.printf("%.2f ", prediction[k]);
               if ((predictionIdx == -1) || (maxval < prediction[k]))
               {
                  predictionIdx = k;
                  maxval        = prediction[k];
               }
            }
            System.out.println();
            System.out.print("target: ");
            int targetIdx = -1;
            for (int k = 0, q = j * HoneyBee.NUM_RESPONSES; k < HoneyBee.NUM_RESPONSES; k++)
            {
               System.out.print(targetData[q + k] + " ");
               if (targetData[q + k] == 1.0)
               {
                  targetIdx = k;
               }
            }
            System.out.println();
            totalResponses++;
            if (targetIdx == predictionIdx)
            {
               correctResponses++;
            }
            else
            {
               forageCorrect = false;
            }
         }
         if (forageCorrect)
         {
            correctForages++;
         }
         totalForages++;
      }
      System.out.print("correct/total responses=" + correctResponses + "/" + totalResponses);
      if (totalResponses > 0)
      {
         System.out.printf(" (%.2f", ((double)correctResponses / (double)totalResponses) * 100.0);
         System.out.println("%)");
      }
      System.out.print("correct/total forages=" + correctForages + "/" + totalForages);
      if (totalForages > 0)
      {
         System.out.printf(" (%.2f", ((double)correctForages / (double)totalForages) * 100.0);
         System.out.println("%)");
      }
      //double ratio = f.ratio();
      //System.out.println("classification results= " + ratio * 100.0 + "%.");
   }
}
