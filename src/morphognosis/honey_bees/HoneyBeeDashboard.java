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
   StatusPanel          status;
   DriverPanel          driver;
   MorphognosticDisplay morphognostic;
   OperationsPanel      operations;

   // Target honey bee.
   HoneyBee bee;

   // Constructor.
   public HoneyBeeDashboard(HoneyBee bee)
   {
      this.bee = bee;

      setTitle("Honey bee " + bee.id);
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e) { close(); }
                        }
                        );
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
      status = new StatusPanel();
      basePanel.add(status);
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
      // Update status.
      status.update();

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


   // Status panel.
   class StatusPanel extends JPanel
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JTextField hiveText;
      JTextField adjacentFlowerNectarText;
      JTextField adjacentBeeOrientationText;
      JTextField adjacentBeeNectarDistanceText;
      JTextField orientationText;
      JTextField nectarText;
      JTextField nectarDistanceDisplayText;
      JTextField responseText;

      // Constructor.
      public StatusPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black),
                      "Status"));
         JPanel sensorsPanel = new JPanel();
         sensorsPanel.setLayout(new BoxLayout(sensorsPanel, BoxLayout.Y_AXIS));
         sensorsPanel.setBorder(BorderFactory.createTitledBorder(
                                   BorderFactory.createLineBorder(Color.black),
                                   "Sensors"));
         add(sensorsPanel, BorderLayout.NORTH);
         JPanel hivePanel = new JPanel();
         hivePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(hivePanel);
         hivePanel.add(new JLabel("Hive:"));
         hiveText = new JTextField(10);
         hiveText.setEditable(false);
         hivePanel.add(hiveText);
         JPanel adjacentFlowerNectarPanel = new JPanel();
         adjacentFlowerNectarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(adjacentFlowerNectarPanel);
         adjacentFlowerNectarPanel.add(new JLabel("Adjacent flower nectar:"));
         adjacentFlowerNectarText = new JTextField(10);
         adjacentFlowerNectarText.setEditable(false);
         adjacentFlowerNectarPanel.add(adjacentFlowerNectarText);
         JPanel adjacentBeeOrientationPanel = new JPanel();
         adjacentBeeOrientationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(adjacentBeeOrientationPanel);
         adjacentBeeOrientationPanel.add(new JLabel("Adjacent bee orientation:"));
         adjacentBeeOrientationText = new JTextField(10);
         adjacentBeeOrientationText.setEditable(false);
         adjacentBeeOrientationPanel.add(adjacentBeeOrientationText);
         JPanel adjacentBeeNectarDistancePanel = new JPanel();
         adjacentBeeNectarDistancePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(adjacentBeeNectarDistancePanel);
         adjacentBeeNectarDistancePanel.add(new JLabel("Adjacent bee nectar distance:"));
         adjacentBeeNectarDistanceText = new JTextField(10);
         adjacentBeeNectarDistanceText.setEditable(false);
         adjacentBeeNectarDistancePanel.add(adjacentBeeNectarDistanceText);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(responsePanel, BorderLayout.CENTER);
         responsePanel.add(new JLabel("Response:"));
         responseText = new JTextField(25);
         responseText.setEditable(false);
         responsePanel.add(responseText);
         JPanel statePanel = new JPanel();
         statePanel.setLayout(new BoxLayout(statePanel, BoxLayout.Y_AXIS));
         statePanel.setBorder(BorderFactory.createTitledBorder(
                                 BorderFactory.createLineBorder(Color.black),
                                 "State"));
         add(statePanel, BorderLayout.SOUTH);
         JPanel orientationPanel = new JPanel();
         orientationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         statePanel.add(orientationPanel);
         orientationPanel.add(new JLabel("Orientation: "));
         orientationText = new JTextField(10);
         orientationText.setEditable(false);
         orientationPanel.add(orientationText);
         JPanel nectarCarryPanel = new JPanel();
         nectarCarryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         statePanel.add(nectarCarryPanel);
         nectarCarryPanel.add(new JLabel("Nectar: "));
         nectarText = new JTextField(10);
         nectarText.setEditable(false);
         nectarCarryPanel.add(nectarText);
         JPanel nectarDistanceDisplayPanel = new JPanel();
         nectarDistanceDisplayPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         statePanel.add(nectarDistanceDisplayPanel);
         nectarDistanceDisplayPanel.add(new JLabel("Nectar distance display: "));
         nectarDistanceDisplayText = new JTextField(10);
         nectarDistanceDisplayText.setEditable(false);
         nectarDistanceDisplayPanel.add(nectarDistanceDisplayText);
      }


      // Update.
      public void update()
      {
         // Update sensors
         if (bee.sensors[HoneyBee.HIVE_PRESENCE_INDEX] != 0.0f)
         {
            hiveText.setText("true");
         }
         else
         {
            hiveText.setText("false");
         }
         adjacentFlowerNectarText.setText((int)bee.sensors[HoneyBee.ADJACENT_FLOWER_NECTAR_QUANTITY_INDEX] + "");
         if (bee.sensors[HoneyBee.ADJACENT_BEE_ORIENTATION_INDEX] >= 0.0f)
         {
            adjacentBeeOrientationText.setText(Compass.toName((int)bee.sensors[HoneyBee.ADJACENT_BEE_ORIENTATION_INDEX]));
         }
         else
         {
            adjacentBeeOrientationText.setText("NA");
         }
         if (bee.sensors[HoneyBee.ADJACENT_BEE_NECTAR_DISTANCE_INDEX] >= 0.0f)
         {
            adjacentBeeNectarDistanceText.setText((int)bee.sensors[HoneyBee.ADJACENT_BEE_NECTAR_DISTANCE_INDEX] + "");
         }
         else
         {
            adjacentBeeNectarDistanceText.setText("NA");
         }

         // Update response.
         responseText.setText(HoneyBee.getResponseName(bee.response));

         // Update state.
         orientationText.setText(Compass.toName(bee.orientation));
         nectarText.setText(bee.nectarCarry + "");
         if (bee.nectarDistanceDisplay == -1)
         {
            nectarDistanceDisplayText.setText("NA");
         }
         else
         {
            nectarDistanceDisplayText.setText(bee.nectarDistanceDisplay + "");
         }
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
      Choice driverChoice;

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
            if (bee.world.driver == World.DRIVER_TYPE.LOCAL.getValue())
            {
               bee.driver = driverChoice.getSelectedIndex();
            }
            else
            {
               bee.driver = bee.world.driver;
               driverChoice.select(bee.world.driver);
            }
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
