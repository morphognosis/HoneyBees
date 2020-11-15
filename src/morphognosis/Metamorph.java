// For conditions of distribution and use, see copyright notice in Morphognosis.java

package morphognosis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

// Metamorph.
public class Metamorph
{
   // Morphognostic.
   public Morphognostic morphognostic;

   // Response.
   public int    response;
   public String responseName;

   // Causal effects.
   public ArrayList<Integer> effectIndexes;

   // Constructors.
   public Metamorph(Morphognostic morphognostic, int response)
   {
      this.morphognostic = morphognostic;
      this.response      = response;
      responseName       = "";
      effectIndexes      = new ArrayList<Integer>();
   }


   public Metamorph(Morphognostic morphognostic, int response, String responseName)
   {
      this.morphognostic = morphognostic;
      this.response      = response;
      this.responseName  = responseName;
      effectIndexes      = new ArrayList<Integer>();
   }


   // Equality test.
   public boolean equals(Metamorph m)
   {
      if (response != m.response)
      {
         return(false);
      }
      if (morphognostic.compare(m.morphognostic) != 0.0f)
      {
         return(false);
      }
      return(true);
   }


   // Save.
   public void save(DataOutputStream output) throws IOException
   {
      morphognostic.save(output);
      Utility.saveInt(output, response);
      Utility.saveString(output, responseName);
      int n = effectIndexes.size();
      Utility.saveInt(output, n);
      for (int i : effectIndexes)
      {
         Utility.saveInt(output, i);
      }
      output.flush();
   }


   // Load.
   public static Metamorph load(DataInputStream input) throws IOException
   {
      Morphognostic morphognostic = Morphognostic.load(input);
      int           response      = Utility.loadInt(input);
      String        responseName  = Utility.loadString(input);
      Metamorph     metamorph     = new Metamorph(morphognostic, response, responseName);
      int           n             = Utility.loadInt(input);

      for (int i = 0; i < n; i++)
      {
         metamorph.effectIndexes.add(Utility.loadInt(input));
      }
      return(metamorph);
   }


   // Print.
   public void print()
   {
      System.out.println("Morphognostic:");
      morphognostic.print();
      System.out.println("Response=" + response);
      System.out.println("ResponseName=" + responseName);
   }
}
