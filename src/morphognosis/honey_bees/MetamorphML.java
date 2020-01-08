// For conditions of distribution and use, see copyright notice in Main.java

// Metamorph machine learning.

package morphognosis.honey_bees;

import java.util.ArrayList;
import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class MetamorphML
{
   // ML type.
   public static final int NEURAL_NETWORK = 0;
   public static final int DECISION_TREE  = 1;
   public int              type;

   // Dataset attributes.
   Instances metamorphAttributes;
   int       numAttributes;

   // Neural network model.
   public MultilayerPerceptron metamorphNN;

   // Constructor.
   public MetamorphML(int type)
   {
      this.type = type;
   }


   // Train metamorphs.
   public void train(ArrayList<Metamorph> metamorphs)
   {
      if (metamorphs.size() == 0) { return; }

      // Create metamorph training dataset.
      Morphognostic        morphognostic  = metamorphs.get(0).morphognostic;
      ArrayList<Attribute> attributeNames = new ArrayList<Attribute>();
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         int n = morphognostic.neighborhoods.get(i).sectors.length;
         for (int x = 0; x < n; x++)
         {
            for (int y = 0; y < n; y++)
            {
               for (int d = 0; d < morphognostic.eventDimensions; d++)
               {
                  for (int j = 0; j < morphognostic.numEventTypes[d]; j++)
                  {
                     attributeNames.add(new Attribute(i + "-" + x + "-" + y + "-" + d + "-" + j));
                  }
               }
            }
         }
      }
      attributeNames.add(new Attribute("response"));
      metamorphAttributes = new Instances("metamorph_attributes", attributeNames, 0);
      Instances metamorphInstances = new Instances("metamorphs", attributeNames, 0);
      numAttributes = attributeNames.size();
      for (Metamorph metamorph : metamorphs)
      {
         metamorphInstances.add(createInstance(metamorph.morphognostic, metamorph.response));
      }
      metamorphInstances.setClassIndex(numAttributes - 1);

      // Neural network?
      if (type == NEURAL_NETWORK)
      {
         trainNN(metamorphInstances);
      }
      else
      {
         trainDT(metamorphInstances);
      }
   }


   // Create instance.
   public Instance createInstance(Morphognostic morphognostic, int response)
   {
      Instance instance = new DenseInstance(numAttributes);
      int      a        = 0;

      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         int n = morphognostic.neighborhoods.get(i).sectors.length;
         for (int x = 0; x < n; x++)
         {
            for (int y = 0; y < n; y++)
            {
               Morphognostic.Neighborhood.Sector s = morphognostic.neighborhoods.get(i).sectors[x][y];
               for (int d = 0; d < morphognostic.eventDimensions; d++)
               {
                  for (int j = 0; j < s.typeDensities[d].length; j++)
                  {
                     instance.setValue(a, (double)s.typeDensities[d][j]);
                     a++;
                  }
               }
            }
         }
      }
      instance.setValue(a, (double)response);
      return(instance);
   }


   // Train neural network.
   public void trainNN(Instances metamorphInstances)
   {
      // Create model.
      metamorphNN = new MultilayerPerceptron();
      metamorphNN.setLearningRate(0.1);
      metamorphNN.setMomentum(0.2);
      metamorphNN.setTrainingTime(2000);
      metamorphNN.setHiddenLayers("20");
      try
      {
         metamorphNN.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 20"));
      }
      catch (Exception e)
      {
         System.err.println("Cannot create neural network model: " + e.getMessage());
         e.printStackTrace();
      }

      // Train model.
      try
      {
         metamorphNN.buildClassifier(metamorphInstances);
      }
      catch (Exception e)
      {
         System.err.println("Cannot train neural network model: " + e.getMessage());
         e.printStackTrace();
      }

      // Evaluate model.
      try
      {
         Evaluation eval = new Evaluation(metamorphInstances);
         eval.evaluateModel(metamorphNN, metamorphInstances);
         System.out.println("Error rate=" + eval.errorRate());
         System.out.println(eval.toSummaryString());
      }
      catch (Exception e)
      {
         System.err.println("Cannot evaluate neural network model: " + e.getMessage());
         e.printStackTrace();
      }
   }


   // Train decision tree.
   public void trainDT(Instances metamorphInstances)
   {
      //TODO.
   }


   // Predict response.
   public int respond(Morphognostic morphognostic)
   {
      if (type == NEURAL_NETWORK)
      {
         return(respondNN(morphognostic));
      }
      else
      {
         return(respondDT(morphognostic));
      }
   }


   // Predict neural network response.
   public int respondNN(Morphognostic morphognostic)
   {
      Instance morphognosticInstance = createInstance(morphognostic, 0);
      double   response = 0.0;

      try
      {
         response = metamorphNN.distributionForInstance(morphognosticInstance)[0];
      }
      catch (Exception e)
      {
         System.err.println("Cannot get response from neural network: " + e.getMessage());
         e.printStackTrace();
      }
      return((int)response);
   }


   // Predict decision tree response.
   public int respondDT(Morphognostic morphognostic)
   {
      //TODO.
      return(0);
   }
}
