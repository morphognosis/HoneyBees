// For conditions of distribution and use, see copyright notice in Main.java

// Honey bee dashboard.

package morphognosis.honey_bees;

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

public class HoneyBeeDashboard extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Components.
   SensorsResponsePanel sensorsResponse;
   DriverPanel          driver;
   MorphognosticDisplay morphognostic;
   OperationsPanel      operations;

   // Target honey bee.
   HoneyBee  bee;

   // Constructor.
   public HoneyBeeDashboard(HoneyBee bee)
   {
	   this.bee = bee;

      setTitle("Honey bee");
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
      morphognostic = new MorphognosticDisplay(0, bee.morphognostic);
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
      // Update sensors.
      String sensorsString = "hive=" + bee.sensors[HoneyBee.HIVE_PRESENCE_INDEX];
      sensorsString += " adjacent flower nectar=" + bee.sensors[HoneyBee.ADJACENT_FLOWER_NECTAR_QUANTITY_INDEX];
      sensorsString += " adjacent bee: orientation=" + bee.sensors[HoneyBee.ADJACENT_BEE_ORIENTATION_INDEX];
      sensorsString += ", nectar distance=" + bee.sensors[HoneyBee.ADJACENT_BEE_NECTAR_DISTANCE_INDEX];
      //TODO Notes: add nectar carry and break out each sensor in its own box.
      setSensors(sensorsString);

      // Update response.
      setResponse(HoneyBee.getResponseName(bee.response));

      // Update driver choice.
      setDriverChoice(bee.driver);
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
   
   // Set sensors display.
   void setSensors(String sensorsString)
   {
      sensorsResponse.sensorsText.setText(sensorsString);
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
      JTextField sensorsText;
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
         sensorsPanel.add(new JLabel("Sensors:"));
         sensorsText = new JTextField(30);
         sensorsText.setEditable(false);
         sensorsPanel.add(sensorsText);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(responsePanel, BorderLayout.SOUTH);
         responsePanel.add(new JLabel("Response:"));
         responseText = new JTextField(15);
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
   class DriverPanel extends JPanel implements ItemListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      Choice  driverChoice;

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
         driverChoice.add("metamorphs");
         driverChoice.addItemListener(this);
         JPanel datasetpanel = new JPanel();
         datasetpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(datasetpanel, BorderLayout.SOUTH);
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent evt)
      {
         Object source = evt.getSource();

         if (source instanceof Choice && ((Choice)source == driverChoice))
         {
            bee.driver = driverChoice.getSelectedIndex();

            if (bee.driver == HoneyBee.DRIVER_TYPE.AUTOPILOT.getValue())
            {
               // Fresh start for autopilot.
               bee.reset();

               // Update sensors.
               String sensorsString = "hive=" + bee.sensors[HoneyBee.HIVE_PRESENCE_INDEX];
               sensorsString += " adjacent flower nectar=" + bee.sensors[HoneyBee.ADJACENT_FLOWER_NECTAR_QUANTITY_INDEX];
               sensorsString += " adjacent bee: orientation=" + bee.sensors[HoneyBee.ADJACENT_BEE_ORIENTATION_INDEX];
               sensorsString += ", nectar distance=" + bee.sensors[HoneyBee.ADJACENT_BEE_NECTAR_DISTANCE_INDEX];
               setSensors(sensorsString);
               
               // Update response.
               setResponse(HoneyBee.getResponseName(bee.response));
               return;
            }
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
         writeMetamorphDatasetButton = new JButton("Write metamorph dataset to " + HoneyBee.METAMORPH_DATASET_FILE_NAME);
         writeMetamorphDatasetButton.addActionListener(this);
         operationspanel.add(writeMetamorphDatasetButton);
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         if ((JButton)evt.getSource() == clearMetamorphsButton)
         {
            bee.metamorphs.clear();
            return;
         }

         if ((JButton)evt.getSource() == writeMetamorphDatasetButton)
         {
            try {
               bee.writeMetamorphDataset(false);
            }
            catch (Exception e) {
               System.err.println("Cannot write metamorph dataset to file " + HoneyBee.METAMORPH_DATASET_FILE_NAME + ": " + e.getMessage());
            }
            return;
         }
      }
   }
}
