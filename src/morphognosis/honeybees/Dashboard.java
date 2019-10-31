// For conditions of distribution and use, see copyright notice in Main.java

// Honeybees dashboard.

package morphognosis.honeybees;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import morphognosis.MorphognosticDisplay;
import morphognosis.Orientation;

public class Dashboard extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Components.
   SensorsResponsePanel sensorsResponse;
   DriverPanel          driver;
   MorphognosticDisplay morphognostic;
   OperationsPanel      operations;

   // Targets.
   Pufferfish  pufferfish;
   NestDisplay nestDisplay;

   // Constructor.
   public Dashboard(Pufferfish pufferfish, NestDisplay nestDisplay)
   {
      this.pufferfish  = pufferfish;
      this.nestDisplay = nestDisplay;

      setTitle("Pufferfish");
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e) { close(); }
                        }
                        );
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
      sensorsResponse = new SensorsResponsePanel();
      basePanel.add(sensorsResponse);
      driver = new DriverPanel();
      basePanel.add(driver);
      morphognostic = new MorphognosticDisplay(0, pufferfish.morphognostic);
      basePanel.add(morphognostic);
      operations = new OperationsPanel();
      basePanel.add(operations);
      pack();
      setLocation();
      setVisible(false);
      update();
   }


   void setLocation()
   {
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int       w   = getSize().width;
      int       h   = getSize().height;
      int       x   = (int)((float)(dim.width - w) * 0.9f);
      int       y   = (dim.height - h) / 2;

      setLocation(x, y);
   }


   // Update dashboard.
   void update()
   {
      // Update elevations.
      int[] elevations = getElevations();
      String elevationsString = "";
      for (int i = 0, j = Pufferfish.NUM_SENSORS - 1, k = j - 1; i < j; i++)
      {
         elevationsString += elevations[i];
         if (i < k)
         {
            elevationsString += ",";
         }
      }
      setElevations(elevationsString);

      // Update previous response.
      setPreviousResponse(Pufferfish.getResponseName(Main.previousResponse));

      // Update response.
      setResponse(Pufferfish.getResponseName(pufferfish.response));

      // Update driver choice.
      setDriverChoice(pufferfish.driver);
   }


   // Get elevations.
   int[] getElevations()
   {
      int x, y, width, height;

      int[] elevations = new int[Pufferfish.NUM_SENSORS - 1];

      width  = nestDisplay.nest.size.width;
      height = nestDisplay.nest.size.height;

      // Initialize elevations.
      for (int i = 0, j = Pufferfish.NUM_SENSORS - 1; i < j; i++)
      {
         x = pufferfish.x;
         y = pufferfish.y;
         switch (i)
         {
         case 0:
            switch (pufferfish.orientation)
            {
            case Orientation.NORTH:
               x--;
               if (x < 0) { x += width; }
               y = ((y + 1) % height);
               break;

            case Orientation.EAST:
               x = ((x + 1) % width);
               y = ((y + 1) % height);
               break;

            case Orientation.SOUTH:
               x = ((x + 1) % width);
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.WEST:
               x--;
               if (x < 0) { x += width; }
               y--;
               if (y < 0) { y += height; }
               break;
            }
            break;

         case 1:
            switch (pufferfish.orientation)
            {
            case Orientation.NORTH:
               y = ((y + 1) % height);
               break;

            case Orientation.EAST:
               x = ((x + 1) % width);
               break;

            case Orientation.SOUTH:
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.WEST:
               x--;
               if (x < 0) { x += width; }
               break;
            }
            break;

         case 2:
            switch (pufferfish.orientation)
            {
            case Orientation.NORTH:
               x = ((x + 1) % width);
               y = ((y + 1) % height);
               break;

            case Orientation.EAST:
               x = ((x + 1) % width);
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.SOUTH:
               x--;
               if (x < 0) { x += width; }
               y--;
               if (y < 0) { y += height; }
               break;

            case Orientation.WEST:
               x--;
               if (x < 0) { x += width; }
               y = ((y + 1) % height);
               break;
            }
            break;
         }
         elevations[i] = nestDisplay.nest.cells[x][y][Nest.ELEVATION_CELL_INDEX];
      }
      return(elevations);
   }


   // Open the dashboard.
   void open()
   {
      setVisible(true);
   }


   // Close the dashboard.
   void close()
   {
      morphognostic.close();
      setVisible(false);
   }


   // Set elevations display.
   void setElevations(String elevationsString)
   {
      if (Pufferfish.IGNORE_ELEVATION_SENSOR_VALUES)
      {
         sensorsResponse.elevationsText.setText("ignored");
      }
      else
      {
         sensorsResponse.elevationsText.setText(elevationsString);
      }
   }


   // Set previous response display.
   void setPreviousResponse(String previousResponseString)
   {
      sensorsResponse.previousResponseText.setText(previousResponseString);
   }


   // Set response display.
   void setResponse(String responseString)
   {
      sensorsResponse.responseText.setText(responseString);
   }


   // Sensors/Response panel.
   class SensorsResponsePanel extends JPanel
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JTextField elevationsText;
      JTextField previousResponseText;
      JTextField responseText;

      // Constructor.
      public SensorsResponsePanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black),
                      "Sensors/Response"));
         JPanel sensorsPanel = new JPanel();
         sensorsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(sensorsPanel, BorderLayout.NORTH);
         sensorsPanel.add(new JLabel("Elevations:"));
         elevationsText = new JTextField(20);
         elevationsText.setEditable(false);
         sensorsPanel.add(elevationsText);
         sensorsPanel.add(new JLabel("Previous response:"));
         previousResponseText = new JTextField(10);
         previousResponseText.setEditable(false);
         sensorsPanel.add(previousResponseText);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(responsePanel, BorderLayout.SOUTH);
         responsePanel.add(new JLabel("Response:"));
         responseText = new JTextField(10);
         responseText.setEditable(false);
         responsePanel.add(responseText);
      }
   }

   // Get driver choice.
   int getDriverChoice()
   {
      return(driver.driverChoice.getSelectedIndex());
   }


   // Set driver choice.
   void setDriverChoice(int driverChoice)
   {
      driver.driverChoice.select(driverChoice);
   }


   // Driver panel.
   class DriverPanel extends JPanel implements ItemListener, ActionListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      Choice  driverChoice;
      JButton forwardButton;
      JButton turnLeftButton;
      JButton turnRightButton;
      JButton smoothSurfaceButton;
      JButton raiseSurfaceButton;
      JButton lowerSurfaceButton;

      // Constructor.
      public DriverPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black), "Driver"));
         JPanel driverPanel = new JPanel();
         driverPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(driverPanel, BorderLayout.NORTH);
         driverChoice = new Choice();
         driverPanel.add(driverChoice);
         driverChoice.add("autopilot");
         driverChoice.add("metamorphRules");
         driverChoice.add("manual");
         driverChoice.addItemListener(this);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout());
         add(responsePanel, BorderLayout.CENTER);
         forwardButton = new JButton("Forward");
         forwardButton.addActionListener(this);
         responsePanel.add(forwardButton);
         turnLeftButton = new JButton("Turn left");
         turnLeftButton.addActionListener(this);
         responsePanel.add(turnLeftButton);
         turnRightButton = new JButton("Turn right");
         turnRightButton.addActionListener(this);
         responsePanel.add(turnRightButton);
         smoothSurfaceButton = new JButton("Smooth");
         smoothSurfaceButton.addActionListener(this);
         responsePanel.add(smoothSurfaceButton);
         raiseSurfaceButton = new JButton("Raise");
         raiseSurfaceButton.addActionListener(this);
         responsePanel.add(raiseSurfaceButton);
         lowerSurfaceButton = new JButton("Lower");
         lowerSurfaceButton.addActionListener(this);
         responsePanel.add(lowerSurfaceButton);
         JPanel datasetpanel = new JPanel();
         datasetpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(datasetpanel, BorderLayout.SOUTH);
         setManualResponseButtons(false);
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent evt)
      {
         Object source = evt.getSource();

         if (source instanceof Choice && ((Choice)source == driverChoice))
         {
            pufferfish.driver = driverChoice.getSelectedIndex();

            if (pufferfish.driver == Pufferfish.DRIVER_TYPE.MANUAL.getValue())
            {
               setManualResponseButtons(true);
               return;
            }
            else
            {
               setManualResponseButtons(false);
            }

            if (pufferfish.driver == Pufferfish.DRIVER_TYPE.AUTOPILOT.getValue())
            {
               // Fresh start for autopilot.
               pufferfish.reset();

               // Update elevations.
               int[] elevations = getElevations();
               String elevationsString = "";
               for (int i = 0, j = Pufferfish.NUM_SENSORS - 1, k = j - 1; i < j; i++)
               {
                  elevationsString += elevations[i];
                  if (i < k)
                  {
                     elevationsString += ",";
                  }
               }
               setElevations(elevationsString);

               // Update previous response.
               Main.previousResponse = Pufferfish.WAIT;
               setPreviousResponse(Pufferfish.getResponseName(Main.previousResponse));

               // Update response.
               setResponse(Pufferfish.getResponseName(pufferfish.response));
               return;
            }
         }
      }


      void setManualResponseButtons(boolean enabled)
      {
         forwardButton.setEnabled(enabled);
         turnRightButton.setEnabled(enabled);
         turnLeftButton.setEnabled(enabled);
         smoothSurfaceButton.setEnabled(enabled);
         raiseSurfaceButton.setEnabled(enabled);
         lowerSurfaceButton.setEnabled(enabled);
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         if ((JButton)evt.getSource() == forwardButton)
         {
            pufferfish.driverResponse = Pufferfish.FORWARD;
            return;
         }

         if ((JButton)evt.getSource() == turnLeftButton)
         {
            pufferfish.driverResponse = Pufferfish.TURN_LEFT;
            return;
         }

         if ((JButton)evt.getSource() == turnRightButton)
         {
            pufferfish.driverResponse = Pufferfish.TURN_RIGHT;
            return;
         }

         if ((JButton)evt.getSource() == smoothSurfaceButton)
         {
            pufferfish.driverResponse = Pufferfish.SMOOTH;
            return;
         }

         if ((JButton)evt.getSource() == raiseSurfaceButton)
         {
            pufferfish.driverResponse = Pufferfish.RAISE;
            return;
         }

         if ((JButton)evt.getSource() == lowerSurfaceButton)
         {
            pufferfish.driverResponse = Pufferfish.LOWER;
            return;
         }
      }
   }

   // Operations panel.
   class OperationsPanel extends JPanel implements ActionListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JButton clearMetamorphsButton;
      JButton writeMetamorphDatasetButton;

      // Constructor.
      public OperationsPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black), "Operations"));
         JPanel operationspanel = new JPanel();
         operationspanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(operationspanel, BorderLayout.CENTER);
         clearMetamorphsButton = new JButton("Clear metamorph rules");
         clearMetamorphsButton.addActionListener(this);
         operationspanel.add(clearMetamorphsButton);
         writeMetamorphDatasetButton = new JButton("Write metamorph dataset to " + Pufferfish.DATASET_FILE_NAME);
         writeMetamorphDatasetButton.addActionListener(this);
         operationspanel.add(writeMetamorphDatasetButton);
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         if ((JButton)evt.getSource() == clearMetamorphsButton)
         {
            pufferfish.metamorphs.clear();
            return;
         }

         if ((JButton)evt.getSource() == writeMetamorphDatasetButton)
         {
            try {
               pufferfish.writeMetamorphDataset();
            }
            catch (Exception e) {
               System.err.println("Cannot write metamorph dataset to file " + Pufferfish.DATASET_FILE_NAME + ": " + e.getMessage());
            }
            return;
         }
      }
   }
}
