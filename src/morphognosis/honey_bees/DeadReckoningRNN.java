// For conditions of distribution and use, see copyright notice in Main.java

// Dead reckoning RNN.
// Test the ability of an RNN to track relative location, a skill necessary to return to hive.
// There are 9 possible responses: 8 orientations plus a forward movement.
// Classification: the orientation to the initial location.

package morphognosis.honey_bees;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Random;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;

public class DeadReckoningRNN
{
   // Parameters.
   public static float   TURN_PROBABILITY    = 0.5f;
   public static int     NUM_TRAIN           = 1;
   public static int     NUM_TEST            = 1;
   public static int     MIN_SEQUENCE_LENGTH = 5;
   public static int     MAX_SEQUENCE_LENGTH = 15;
   public static int     RANDOM_SEED         = 4517;
   public static boolean VERBOSE             = false;

   // Dataset.
   public static String pythonDatasetFilename = "foraging_dataset.py";
   public static int    padToSequenceLength   = -1;

   // RNN parameters.
   public static int NUM_NEURONS = 128;
   public static int NUM_EPOCHS  = 1000;

   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java morphognosis.honey_bees.DeadReckoningRNN\n" +
      "     [-numTrain <quantity> (default=" + NUM_TRAIN + ")]\n" +
      "     [-numTest <quantity> (default=" + NUM_TEST + ")]\n" +
      "     [-minSequenceLength <quantity> (default=" + MIN_SEQUENCE_LENGTH + ")]\n" +
      "     [-maxSequenceLength <quantity> (default=" + MAX_SEQUENCE_LENGTH + ")]\n" +
      "     [-turnProbability <probability> (default=" + TURN_PROBABILITY + ")]\n" +
      "     [-numNeurons <quantity> (default=" + NUM_NEURONS + ")]\n" +
      "     [-numEpochs <quantity> (default=" + NUM_EPOCHS + ")]\n" +
      "     [-randomSeed <random number seed> (default=" + RANDOM_SEED + ")]\n" +
      "     [-verbose (default=" + VERBOSE + ")]";

   private static Random random = new Random();

   private static int finalTrainTarget = -1;
   private static int finalTestTarget  = -1;
   private static int currentTarget    = -1;

   // Sample input is a sequence of responses, where each response is an orientation (x8) or a forward movement.
   // Sample target is direction to origin from destination.
   public static Sample generateSample(final int length)
   {
      double[] data   = new double[length * 10];
      double[] target = new double[length * 9];

      int x           = 0;
      int y           = 0;
      int orientation = 0;
      if (VERBOSE) { System.out.println("data:"); }
      for (int i = 0; i < length; i++)
      {
         int response = 8;
         if (random.nextFloat() < TURN_PROBABILITY)
         {
            response = random.nextInt(8);
         }
         data[(i * 10) + response] = 1.0;
         if (VERBOSE)
         {
            for (int j = 0; j < 9; j++)
            {
               System.out.print(data[(i * 10) + j] + " ");
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

         currentTarget = getTarget(x, y);
         target[(i * 9) + currentTarget] = 1.0;
         if (VERBOSE)
         {
            for (int j = 0; j < 9; j++)
            {
               System.out.print(target[(i * 9) + j] + " ");
            }
            System.out.println();
         }
      }

      if (padToSequenceLength < length)
      {
         padToSequenceLength = length;
      }

      return(new Sample(data, target, 10, length, 9, length));
   }


   // Generate target from position.
   public static int getTarget(int x, int y)
   {
      if (x > 0)
      {
         if (y > 0)
         {
            return(1);
         }
         else if (y < 0)
         {
            return(3);
         }
         else
         {
            return(2);
         }
      }
      else if (x < 0)
      {
         if (y > 0)
         {
            return(7);
         }
         else if (y < 0)
         {
            return(5);
         }
         else
         {
            return(6);
         }
      }
      else
      {
         if (y > 0)
         {
            return(0);
         }
         else if (y < 0)
         {
            return(4);
         }
         else
         {
            return(8);
         }
      }
   }


   public static SampleSet generate(int n)
   {
      SampleSet set = new SampleSet();

      for (int i = 0; i < n; i++)
      {
         int length = random.nextInt((MAX_SEQUENCE_LENGTH - MIN_SEQUENCE_LENGTH + 1)) + MIN_SEQUENCE_LENGTH;
         set.add(generateSample(length));
      }
      return(set);
   }


   // Export Python dataset.
   public static void exportPythonDataset(SampleSet trainSet, SampleSet testSet)
   {
      if (trainSet.size() == 0)
      {
         System.err.println("Cannot export python dataset: no data");
         return;
      }
      String oldlinesep = System.getProperty("line.separator");
      System.setProperty("line.separator", "\n");
      FileOutputStream datasetOutput = null;
      PrintWriter      datasetWriter = null;
      try
      {
         datasetOutput = new FileOutputStream(new File(pythonDatasetFilename));
         datasetWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(datasetOutput)));
         boolean first = true;
         for (Sample s : trainSet)
         {
            double[] inputData = s.getInput();
            if (first)
            {
               first = false;
               int n = s.getInputLength();
               datasetWriter.println("X_train_shape = [" + NUM_TRAIN + "," + n + "," + 10 + "]");
               datasetWriter.print("X_train_seq = [");
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  if (i == 0)
                  {
                     datasetWriter.print(inputData[i] + "");
                  }
                  else
                  {
                     datasetWriter.print("," + inputData[i]);
                  }
               }
            }
            else
            {
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  datasetWriter.print("," + inputData[i]);
               }
            }
         }
         datasetWriter.println("]");
         first = true;
         for (Sample s : testSet)
         {
            double[] inputData = s.getInput();
            if (first)
            {
               first = false;
               int n = s.getInputLength();
               datasetWriter.println("X_test_shape = [" + NUM_TEST + "," + n + "," + 10 + "]");
               datasetWriter.print("X_test_seq = [");
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  if (i == 0)
                  {
                     datasetWriter.print(inputData[i] + "");
                  }
                  else
                  {
                     datasetWriter.print("," + inputData[i]);
                  }
               }
            }
            else
            {
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  datasetWriter.print("," + inputData[i]);
               }
            }
         }
         datasetWriter.println("]");
         first = true;
         for (Sample s : trainSet)
         {
            double[] targetData = s.getTarget();
            if (first)
            {
               first = false;
               int n = s.getTargetLength();
               datasetWriter.println("y_shape = [" + NUM_TRAIN + "," + n + "," + 9 + "]");
               datasetWriter.print("y_seq = [");
               for (int i = 0, j = targetData.length; i < j; i++)
               {
                  if (i == 0)
                  {
                     datasetWriter.print(targetData[i] + "");
                  }
                  else
                  {
                     datasetWriter.print("," + targetData[i]);
                  }
               }
            }
            else
            {
               for (int i = 0, j = targetData.length; i < j; i++)
               {
                  datasetWriter.print("," + targetData[i]);
               }
            }
         }
         datasetWriter.println("]");
      }
      catch (Exception e)
      {
         System.err.println("Cannot open dataset file " + pythonDatasetFilename + ":" + e.getMessage());
      }
      if (datasetWriter != null)
      {
         datasetWriter.flush();
         try {
            datasetOutput.close();
         }
         catch (IOException e) {}
      }
      System.setProperty("line.separator", oldlinesep);
   }


   public static void main(String[] args) throws IOException
   {
      for (int i = 0; i < args.length; i++)
      {
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
            continue;
         }
         if (args[i].equals("-numNeurons"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_NEURONS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_NEURONS < 0)
            {
               System.err.println("Invalid numNeurons option");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numEpochs"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numEpochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            try
            {
               NUM_EPOCHS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numEpochs option");
               System.err.println(Usage);
               System.exit(1);
            }
            if (NUM_EPOCHS < 0)
            {
               System.err.println("Invalid numEpochs option");
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
      if (MIN_SEQUENCE_LENGTH > MAX_SEQUENCE_LENGTH)
      {
         System.err.println("minSequenceLength cannot be greater than maxSequenceLength");
         System.exit(1);
      }

      //
      // generate train and test data.
      //
      Parameters.NUM_FLOWERS = 1;
      Parameters.NUM_BEES    = 1;
      Parameters.FLOWER_SURPLUS_NECTAR_PROBABILITY = 0.0f;
      random.setSeed(RANDOM_SEED);
      SampleSet trainset;
      SampleSet testset;
      if (VERBOSE) { System.out.println("Training samples:"); }
      trainset         = generate(NUM_TRAIN);
      finalTrainTarget = currentTarget;
      if (VERBOSE) { System.out.println("Testing samples:"); }
      testset         = generate(NUM_TEST);
      finalTestTarget = currentTarget;

      // Pad sequences to max size.
      if (padToSequenceLength != -1)
      {
         SampleSet sampleSet = new SampleSet();
         for (Sample s : trainset)
         {
            int n = s.getInputLength();
            if (n < padToSequenceLength)
            {
               double[] inputData2  = new double[padToSequenceLength * 10];
               double[] targetData2 = new double[padToSequenceLength * 9];
               double[] inputData   = s.getInput();
               double[] targetData  = s.getTarget();
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  inputData2[i] = inputData[i];
               }
               for (int i = 0, j = targetData.length; i < j; i++)
               {
                  targetData2[i] = targetData[i];
               }
               int i = inputData.length;
               int t = targetData.length;
               for (int j = 0, k = padToSequenceLength - n; j < k; j++)
               {
                  double[] input = new double[10];
                  input[9]       = 1.0;
                  for (int p = 0; p < 10; p++, i++)
                  {
                     inputData2[i] = input[p];
                  }
                  double[] target          = new double[9];
                  target[finalTrainTarget] = 1.0;
                  for (int p = 0; p < 9; p++, t++)
                  {
                     targetData2[t] = target[p];
                  }
               }
               Sample s2 = new Sample(inputData2, targetData2, 10, padToSequenceLength,
                                      9, padToSequenceLength);
               sampleSet.add(s2);
            }
            else
            {
               sampleSet.add(s);
            }
         }
         trainset = sampleSet;

         sampleSet = new SampleSet();
         for (Sample s : testset)
         {
            int n = s.getInputLength();
            if (n < padToSequenceLength)
            {
               double[] inputData2  = new double[padToSequenceLength * 10];
               double[] targetData2 = new double[padToSequenceLength * 9];
               double[] inputData   = s.getInput();
               double[] targetData  = s.getTarget();
               for (int i = 0, j = inputData.length; i < j; i++)
               {
                  inputData2[i] = inputData[i];
               }
               for (int i = 0, j = targetData.length; i < j; i++)
               {
                  targetData2[i] = targetData[i];
               }
               int i = inputData.length;
               int t = targetData.length;
               for (int j = 0, k = padToSequenceLength - n; j < k; j++)
               {
                  double[] input = new double[10];
                  input[9]       = 1.0;
                  for (int p = 0; p < 10; p++, i++)
                  {
                     inputData2[i] = input[p];
                  }
                  double[] target         = new double[9];
                  target[finalTestTarget] = 1.0;
                  for (int p = 0; p < 9; p++, t++)
                  {
                     targetData2[t] = target[p];
                  }
               }
               Sample s2 = new Sample(inputData2, targetData2, 10, padToSequenceLength,
                                      9, padToSequenceLength);
               sampleSet.add(s2);
            }
            else
            {
               sampleSet.add(s);
            }
         }
         testset = sampleSet;
      }

      // Export Python dataset.
      exportPythonDataset(trainset, testset);

      // Run RNN.
      try
      {
         InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("foraging_rnn.py");
         if (in == null)
         {
            System.err.println("Cannot access foraging_rnn.py");
            System.exit(1);
         }
         File             pythonScript = new File("foraging_rnn.py");
         FileOutputStream out          = new FileOutputStream(pythonScript);
         byte[] buffer = new byte[1024];
         int bytesRead;
         while ((bytesRead = in.read(buffer)) != -1)
         {
            out.write(buffer, 0, bytesRead);
         }
         out.close();
      }
      catch (Exception e)
      {
         System.err.println("Cannot create foraging_rnn.py");
         System.exit(1);
      }
      ProcessBuilder processBuilder = new ProcessBuilder("python", "foraging_rnn.py",
                                                         "-n", (NUM_NEURONS + ""), "-e", (NUM_EPOCHS + ""));
      processBuilder.inheritIO();
      Process process = processBuilder.start();
      try
      {
         process.waitFor();
      }
      catch (InterruptedException e) {}
   }
}
