// For conditions of distribution and use, see copyright notice in Main.java

// Honey bee dashboard.

package morphognosis.honey_bees;

import java.awt.BorderLayout;
import java.awt.Checkbox;
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

public class HoneyBeeDashboard extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Components.
   StatusPanel              status;
   DriverPanel              driver;
   MorphognosticDisplay     morphognostic;
   MetamorphOperationsPanel metamorphOperations;

   // Target honey bee.
   HoneyBee bee;

   // World display.
   WorldDisplay worldDisplay;

   // Constructor.
   public HoneyBeeDashboard(HoneyBee bee, WorldDisplay worldDisplay)
   {
      this.bee          = bee;
      this.worldDisplay = worldDisplay;

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
      morphognostic = new MorphognosticDisplay(bee.id, bee.morphognostic);
      basePanel.add(morphognostic);
      metamorphOperations = new MetamorphOperationsPanel();
      basePanel.add(metamorphOperations);
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
      JTextField nectarText;
      JTextField nectarDanceDirectionText;
      JTextField nectarDanceDistanceText;
      JTextField orientationText;
      JTextField nectarCarryText;
      JTextField nectarDistanceDisplayText;
      JTextField responseText;

      // Constructor.
      public StatusPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black),
                      "State"));
         JPanel sensorsPanel = new JPanel();
         sensorsPanel.setLayout(new BoxLayout(sensorsPanel, BoxLayout.Y_AXIS));
         sensorsPanel.setBorder(BorderFactory.createTitledBorder(
                                   BorderFactory.createLineBorder(Color.black),
                                   "Sensors"));
         add(sensorsPanel, BorderLayout.NORTH);
         JPanel hivePanel = new JPanel();
         hivePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(hivePanel);
         hivePanel.add(new JLabel("Hive presence:"));
         hiveText = new JTextField(10);
         hiveText.setEditable(false);
         hivePanel.add(hiveText);
         JPanel nectarPanel = new JPanel();
         nectarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(nectarPanel);
         nectarPanel.add(new JLabel("Nectar presence:"));
         nectarText = new JTextField(10);
         nectarText.setEditable(false);
         nectarPanel.add(nectarText);
         JPanel nectarDanceDirectionPanel = new JPanel();
         nectarDanceDirectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(nectarDanceDirectionPanel);
         nectarDanceDirectionPanel.add(new JLabel("Nectar dance direction:"));
         nectarDanceDirectionText = new JTextField(10);
         nectarDanceDirectionText.setEditable(false);
         nectarDanceDirectionPanel.add(nectarDanceDirectionText);
         JPanel nectarDanceDistancePanel = new JPanel();
         nectarDanceDistancePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(nectarDanceDistancePanel);
         nectarDanceDistancePanel.add(new JLabel("Nectar dance distance:"));
         nectarDanceDistanceText = new JTextField(10);
         nectarDanceDistanceText.setEditable(false);
         nectarDanceDistancePanel.add(nectarDanceDistanceText);
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
                                 "Internal"));
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
         nectarCarryPanel.add(new JLabel("Nectar carry: "));
         nectarCarryText = new JTextField(10);
         nectarCarryText.setEditable(false);
         nectarCarryPanel.add(nectarCarryText);
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
         // Get sensor values.
         float[] sensors = bee.world.getSensors(bee);
         if (sensors[HoneyBee.HIVE_PRESENCE_INDEX] == 1.0f)
         {
            hiveText.setText("true");
         }
         else
         {
            hiveText.setText("false");
         }
         if (sensors[HoneyBee.NECTAR_PRESENCE_INDEX] == 1.0f)
         {
            nectarText.setText("true");
         }
         else
         {
            nectarText.setText("false");
         }
         if (sensors[HoneyBee.NECTAR_DANCE_DIRECTION_INDEX] >= 0.0f)
         {
            nectarDanceDirectionText.setText(Orientation.toName(
                                                (int)sensors[HoneyBee.NECTAR_DANCE_DIRECTION_INDEX]));
         }
         else
         {
            nectarDanceDirectionText.setText("NA");
         }
         if (sensors[HoneyBee.NECTAR_DANCE_DISTANCE_INDEX] == 0.0f)
         {
            nectarDanceDistanceText.setText("long");
         }
         else if (sensors[HoneyBee.NECTAR_DANCE_DISTANCE_INDEX] == 1.0f)
         {
            nectarDanceDistanceText.setText("short");
         }
         else
         {
            nectarDanceDistanceText.setText("NA");
         }

         // Update response.
         responseText.setText(HoneyBee.getResponseName(bee.response));

         // Update state.
         orientationText.setText(Orientation.toName(bee.orientation));
         nectarCarryText.setText(bee.nectarCarry + "");
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
         driverChoice.add("metamorphDB");
         driverChoice.add("metamorphNN");
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
            if (bee.world.driver == Driver.LOCAL_OVERRIDE)
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

   // Metamorph operations panel.
   class MetamorphOperationsPanel extends JPanel implements ActionListener, ItemListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JButton  clearMetamorphsButton;
      JButton  writeMetamorphDatasetButton;
      Checkbox trainNNcheck;

      // Constructor.
      public MetamorphOperationsPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black), "Metamorph operations"));
         JPanel clearMetamorphsPanel = new JPanel();
         clearMetamorphsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(clearMetamorphsPanel, BorderLayout.NORTH);
         clearMetamorphsButton = new JButton("Clear metamorphs");
         clearMetamorphsButton.addActionListener(this);
         clearMetamorphsPanel.add(clearMetamorphsButton);
         JPanel writeMetamorphDatasetPanel = new JPanel();
         writeMetamorphDatasetPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(writeMetamorphDatasetPanel, BorderLayout.CENTER);
         writeMetamorphDatasetButton = new JButton("Write metamorphs to " + bee.metamorphDatasetFilename);
         writeMetamorphDatasetButton.addActionListener(this);
         writeMetamorphDatasetPanel.add(writeMetamorphDatasetButton);
         JPanel trainNNpanel = new JPanel();
         trainNNpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(trainNNpanel, BorderLayout.SOUTH);
         trainNNpanel.add(new JLabel("Train metamorph neural network:"));
         trainNNcheck = new Checkbox();
         trainNNcheck.setState(false);
         trainNNcheck.addItemListener(this);
         trainNNpanel.add(trainNNcheck);
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
            try
            {
               bee.writeMetamorphDataset(bee.metamorphDatasetFilename, false);
            }
            catch (Exception e)
            {
               worldDisplay.controls.messageText.setText("Cannot write metamorph dataset to file " + bee.metamorphDatasetFilename + ": " + e.getMessage());
            }
            return;
         }
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent evt)
      {
         Object source = evt.getSource();

         if (source instanceof Checkbox && ((Checkbox)source == trainNNcheck))
         {
            if (trainNNcheck.getState())
            {
               try
               {
                  worldDisplay.controls.messageText.setText("Training metamorph neural network...");
                  paint(getGraphics());
                  bee.trainMetamorphNN();
                  worldDisplay.controls.messageText.setText("");
               }
               catch (Exception e)
               {
                  worldDisplay.controls.messageText.setText("Cannot train metamorph neural network: " + e.getMessage());
               }
               trainNNcheck.setState(false);
            }
            return;
         }
      }
   }
}
