// For conditions of distribution and use, see copyright notice in Main.java

// Metamorph machine learning.

package morphognosis.honey_bees;

import java.util.ArrayList;
import morphognosis.Metamorph;

public class MetamorphML
{
   public static final int NEURAL_NETWORK = 0;
   public static final int DECISION_TREE  = 1;
   public int              type;

   // Constructor.
   public MetamorphML(int type)
   {
      this.type = type;
   }


   // Train metamorphs.
   public void train(ArrayList<Metamorph> metamorphs)
   {
   }


   // Predict response.
   public int respond()
   {
      return(0);
   }


   /*
    * // Initialize metamorph Weka neural network.
    * public void initMetamorphWekaNN(Morphognostic morphognostic)
    * {
    * metamorphWekaNNattributeNames = new FastVector();
    * for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
    * {
    *    int n = morphognostic.neighborhoods.get(i).sectors.length;
    *    for (int x = 0; x < n; x++)
    *    {
    *       for (int y = 0; y < n; y++)
    *       {
    *          for (int d = 0; d < morphognostic.eventDimensions; d++)
    *          {
    *             for (int j = 0; j < morphognostic.numEventTypes[d]; j++)
    *             {
    *                metamorphWekaNNattributeNames.addElement(new Attribute(i + "-" + x + "-" + y + "-" + d + "-" + j));
    *             }
    *          }
    *       }
    *    }
    * }
    * FastVector responseVals = new FastVector();
    * for (int i = 0; i < NUM_RESPONSES; i++)
    * {
    *    responseVals.addElement(i + "");
    * }
    * headMetamorphWekaNNattributeNames.addElement(new Attribute("type", responseVals));
    * headMetamorphWekaInstances = new Instances("head_metamorphs", headMetamorphWekaNNattributeNames, 0);
    * headMetamorphWekaNN        = new MultilayerPerceptron();
    * }
    *
    *
    * // Initialize body metamorph Weka neural network.
    * public void initBodyMetamorphWekaNN(Morphognostic morphognostic)
    * {
    * bodyMetamorphWekaNNattributeNames = new FastVector();
    * for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
    * {
    *    int n = morphognostic.neighborhoods.get(i).sectors.length;
    *    for (int x = 0; x < n; x++)
    *    {
    *       for (int y = 0; y < n; y++)
    *       {
    *          for (int d = 0; d < morphognostic.eventDimensions; d++)
    *          {
    *             for (int j = 0; j < morphognostic.numEventTypes[d]; j++)
    *             {
    *                bodyMetamorphWekaNNattributeNames.addElement(new Attribute(i + "-" + x + "-" + y + "-" + d + "-" + j));
    *             }
    *          }
    *       }
    *    }
    * }
    * FastVector responseVals = new FastVector();
    * for (int i = 0; i < NUM_RESPONSES; i++)
    * {
    *    responseVals.addElement(i + "");
    * }
    * bodyMetamorphWekaNNattributeNames.addElement(new Attribute("type", responseVals));
    * bodyMetamorphWekaInstances = new Instances("body_metamorphs", bodyMetamorphWekaNNattributeNames, 0);
    * bodyMetamorphWekaNN        = new MultilayerPerceptron();
    * }
    *
    *
    * // Create and train head metamorph neural network.
    * public void createHeadMetamorphWekaNN() throws Exception
    * {
    * // Create instances.
    * headMetamorphWekaInstances = new Instances("head_metamorphs", headMetamorphWekaNNattributeNames, 0);
    * for (List<Metamorph> metamorphList : headMetamorphs.values())
    * {
    *    for (Metamorph m : metamorphList)
    *    {
    *       headMetamorphWekaInstances.add(createInstance(headMetamorphWekaInstances, m));
    *    }
    * }
    * headMetamorphWekaInstances.setClassIndex(headMetamorphWekaInstances.numAttributes() - 1);
    *
    * // Create and train the neural network.
    * MultilayerPerceptron mlp = new MultilayerPerceptron();
    * headMetamorphWekaNN = mlp;
    * mlp.setLearningRate(0.1);
    * mlp.setMomentum(0.2);
    * mlp.setTrainingTime(2000);
    * mlp.setHiddenLayers("20");
    * mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 20"));
    * mlp.buildClassifier(headMetamorphWekaInstances);
    *
    * // Save training instances?
    * if (saveMetamorphWekaInstances)
    * {
    *    ArffSaver saver = new ArffSaver();
    *    saver.setInstances(headMetamorphWekaInstances);
    *    saver.setFile(new File("headMetamorphWekaInstances.arff"));
    *    saver.writeBatch();
    * }
    *
    * // Save networks?
    * if (saveMetamorphWekaNN)
    * {
    *    Debug.saveToFile("headMetamorphWekaNN.dat", mlp);
    * }
    *
    * // Evaluate the network.
    * if (evaluateMetamorphWekaNN)
    * {
    *    Evaluation eval = new Evaluation(headMetamorphWekaInstances);
    *    eval.evaluateModel(mlp, headMetamorphWekaInstances);
    *    System.out.println("Error rate=" + eval.errorRate());
    *    System.out.println(eval.toSummaryString());
    * }
    * }
    *
    *
    * // Create and train body metamorph neural network.
    * public void createBodyMetamorphWekaNN() throws Exception
    * {
    * // Create instances.
    * bodyMetamorphWekaInstances = new Instances("body_metamorphs", bodyMetamorphWekaNNattributeNames, 0);
    * for (List<Metamorph> metamorphList : bodyMetamorphs.values())
    * {
    *    for (Metamorph m : metamorphList)
    *    {
    *       bodyMetamorphWekaInstances.add(createInstance(bodyMetamorphWekaInstances, m));
    *    }
    * }
    * bodyMetamorphWekaInstances.setClassIndex(bodyMetamorphWekaInstances.numAttributes() - 1);
    *
    * // Create and train the neural network.
    * MultilayerPerceptron mlp = new MultilayerPerceptron();
    * bodyMetamorphWekaNN = mlp;
    * mlp.setLearningRate(0.1);
    * mlp.setMomentum(0.2);
    * mlp.setTrainingTime(2000);
    * mlp.setHiddenLayers("20");
    * mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 20"));
    * mlp.buildClassifier(bodyMetamorphWekaInstances);
    *
    * // Save training instances?
    * if (saveMetamorphWekaInstances)
    * {
    *    ArffSaver saver = new ArffSaver();
    *    saver.setInstances(bodyMetamorphWekaInstances);
    *    saver.setFile(new File("bodyMetamorphWekaInstances.arff"));
    *    saver.writeBatch();
    * }
    *
    * // Save networks?
    * if (saveMetamorphWekaNN)
    * {
    *    Debug.saveToFile("bodyMetamorphWekaNN.dat", mlp);
    * }
    *
    * // Evaluate the network.
    * if (evaluateMetamorphWekaNN)
    * {
    *    Evaluation eval = new Evaluation(bodyMetamorphWekaInstances);
    *    eval.evaluateModel(mlp, bodyMetamorphWekaInstances);
    *    System.out.println("Error rate=" + eval.errorRate());
    *    System.out.println(eval.toSummaryString());
    * }
    * }
    *
    *
    * // Create metamorph Weka NN instance.
    * Instance createInstance(Instances instances, Metamorph m)
    * {
    * double[]  attrValues = new double[instances.numAttributes()];
    * int a = 0;
    * for (int i = 0; i < m.morphognostic.NUM_NEIGHBORHOODS; i++)
    * {
    *    int n = m.morphognostic.neighborhoods.get(i).sectors.length;
    *    for (int x = 0; x < n; x++)
    *    {
    *       for (int y = 0; y < n; y++)
    *       {
    *          Morphognostic.Neighborhood.Sector s = m.morphognostic.neighborhoods.get(i).sectors[x][y];
    *          for (int d = 0; d < m.morphognostic.eventDimensions; d++)
    *          {
    *             for (int j = 0; j < s.typeDensities[d].length; j++)
    *             {
    *                attrValues[a] = s.typeDensities[d][j];
    *                a++;
    *             }
    *          }
    *       }
    *    }
    * }
    * attrValues[a] = instances.attribute(a).indexOfValue(m.response + "");
    * a++;
    * return(new Instance(1.0, attrValues));
    * }
    *
    *
    * // Save head metamorph neural network training dataset.
    * public void saveHeadMetamorphNNtrainingData() throws Exception
    * {
    * FileOutputStream output;
    *
    * try
    * {
    *    output = new FileOutputStream(new File(HEAD_NN_DATASET_SAVE_FILE_NAME));
    * }
    * catch (Exception e)
    * {
    *    throw new IOException("Cannot open output file " + HEAD_NN_DATASET_SAVE_FILE_NAME + ":" + e.getMessage());
    * }
    * PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)));
    * boolean     header = true;
    * for (List<Metamorph> metamorphList : headMetamorphs.values())
    * {
    *    for (Metamorph m : metamorphList)
    *    {
    *       String csv = morphognostic2csv(m.morphognostic);
    *       if (m.responseName.isEmpty())
    *       {
    *          csv += ("," + m.response);
    *       }
    *       else
    *       {
    *          csv += ("," + m.responseName);
    *       }
    *       if (header)
    *       {
    *          header = false;
    *          int    j    = csv.split(",").length - 1;
    *          String csv2 = "";
    *          for (int i = 0; i < j; i++)
    *          {
    *             csv2 += ("c" + i + ",");
    *          }
    *          csv2 += "response";
    *          writer.println(csv2);
    *       }
    *       writer.println(csv);
    *    }
    * }
    * writer.flush();
    * output.close();
    * }
    */
}
