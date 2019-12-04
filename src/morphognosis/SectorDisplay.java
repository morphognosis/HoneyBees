// For conditions of distribution and use, see copyright notice in Morphognosis.java

package morphognosis;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class SectorDisplay extends JFrame implements Runnable
{
   private static final long serialVersionUID = 0L;

   // Empty cell.
   public static final int   EMPTY_CELL_VALUE = 0;
   public static final Color EMPTY_CELL_COLOR = Color.WHITE;

   // Display.
   MorphognosticDisplay display;

   // Neighborhood and sector.
   int neighborhoodIndex;
   int sectorXindex, sectorYindex;
   Morphognostic.Neighborhood.Sector sector;

   // Display.
   static final int       DISPLAY_UPDATE_DELAY_MS = 50;
   static final Dimension displaySize             = new Dimension(275, 200);
   static final Dimension canvasSize = new Dimension(275, 175);
   Canvas                 canvas;
   Graphics               canvasGraphics;
   Image     image;
   Graphics  imageGraphics;
   Dimension imageSize;
   Thread    displayThread;
   enum DISPLAY_MODE { DENSITIES, LANDMARKS };
   DISPLAY_MODE displayMode;

   // Constructor.
   public SectorDisplay(MorphognosticDisplay display,
                        int neighborhoodIndex, int sectorXindex, int sectorYindex)
   {
      this.display           = display;
      this.neighborhoodIndex = neighborhoodIndex;
      this.sectorXindex      = sectorXindex;
      this.sectorYindex      = sectorYindex;
      sector = display.morphognostic.neighborhoods.get(neighborhoodIndex).sectors[sectorXindex][sectorYindex];

      setTitle("ID=" + display.id + " N=" + neighborhoodIndex +
               " S=[" + sectorXindex + "," + sectorYindex + "]");
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e) { close(); }
                        }
                        );
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());
      canvas = new Canvas();
      canvas.setBounds(0, 0, canvasSize.width, canvasSize.height);
      basePanel.add(canvas, BorderLayout.NORTH);
      JPanel modePanel = new JPanel();
      modePanel.setLayout(new FlowLayout());
      basePanel.add(modePanel, BorderLayout.SOUTH);
      JRadioButton densities = new JRadioButton("Densities", true);
      displayMode = DISPLAY_MODE.DENSITIES;
      densities.addActionListener(new ActionListener()
                                  {
                                     @Override
                                     public void actionPerformed(ActionEvent e)
                                     {
                                        displayMode = DISPLAY_MODE.DENSITIES;
                                     }
                                  }
                                  );
      JRadioButton landmarks = new JRadioButton("Landmarks");
      landmarks.addActionListener(new ActionListener()
                                  {
                                     @Override
                                     public void actionPerformed(ActionEvent e)
                                     {
                                        displayMode = DISPLAY_MODE.LANDMARKS;
                                     }
                                  }
                                  );
      ButtonGroup modeGroup = new ButtonGroup();
      modeGroup.add(densities);
      modeGroup.add(landmarks);
      modePanel.add(densities);
      modePanel.add(landmarks);
      pack();
      setVisible(false);

      // Get canvas image.
      canvasGraphics = canvas.getGraphics();
      image          = createImage(canvasSize.width, canvasSize.height);
      imageGraphics  = image.getGraphics();
      imageSize      = canvasSize;

      // Create display thread.
      displayThread = new Thread(this);
      displayThread.setPriority(Thread.MIN_PRIORITY);
      displayThread.start();
   }


   // Open display.
   void open()
   {
      setVisible(true);
   }


   // Close display.
   void close()
   {
      setVisible(false);
      display.closeDisplay(neighborhoodIndex, sectorXindex, sectorYindex);
   }


   // Run.
   public void run()
   {
      // Display update loop.
      while (Thread.currentThread() == displayThread &&
             !displayThread.isInterrupted())
      {
         updateDisplay();

         try
         {
            Thread.sleep(DISPLAY_UPDATE_DELAY_MS);
         }
         catch (InterruptedException e) {
            break;
         }
      }
   }


   // Update display.
   public void updateDisplay()
   {
      int d, i, j, n, w, h, x, x2, y, y2;

      imageGraphics.setColor(Color.gray);
      imageGraphics.fillRect(0, 0, imageSize.width, imageSize.height);

      if (displayMode == DISPLAY_MODE.DENSITIES)
      {
         // Draw type density histogram.
         n = 0;
         for (d = 0; d < display.morphognostic.eventDimensions; d++)
         {
            n += display.morphognostic.numEventTypes[d];
         }
         w = imageSize.width / n;
         i = x = 0;
         for (d = 0; d < display.morphognostic.eventDimensions; d++)
         {
            for (j = 0; j < display.morphognostic.numEventTypes[d]; j++, i++, x += w)
            {
               imageGraphics.setColor(getEventColor(d, j));
               h = (int)((float)imageSize.height * sector.getTypeDensity(d, j));
               imageGraphics.fillRect(x, imageSize.height - h, w + 1, h);
            }
         }
         imageGraphics.setColor(Color.black);
         for (i = 0, j = n - 1, x = w; i < j; i++, x += w)
         {
            imageGraphics.drawLine(x, 0, x, imageSize.height);
         }
      }
      else
      {
         // Draw landmarks.
         float cellWidth  = (float)imageSize.width / (float)sector.events.length;
         float cellHeight = (float)imageSize.height / (float)sector.events[0].length;
         for (x = 0, x2 = 0; x < sector.events.length;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            for (y = 0, y2 = imageSize.height - (int)cellHeight;
                 y < sector.events[0].length;
                 y++, y2 = (int)(cellHeight * (double)(sector.events[0].length - (y + 1))))
            {
               Color color = getEventColor(0, sector.events[x][y][0]);
               for (d = 0; d < display.morphognostic.eventDimensions; d++)
               {
                  if ((sector.events[x][y][d] != -1) &&
                      (sector.events[x][y][d] != EMPTY_CELL_VALUE))
                  {
                     color = getEventColor(d, sector.events[x][y][d]);
                     break;
                  }
               }
               imageGraphics.setColor(color);
               imageGraphics.fillRect(x2, y2, (int)cellWidth + 1,
                                      (int)cellHeight + 1);
            }
         }
         imageGraphics.setColor(Color.black);
         h = imageSize.height;
         for (x = 1, x2 = (int)cellWidth; x < sector.events.length;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            imageGraphics.drawLine(x2, 0, x2, h);
         }
         w = imageSize.width;
         for (y = 1, y2 = (int)cellHeight; y < sector.events[0].length;
              y++, y2 = (int)(cellHeight * (double)y))
         {
            imageGraphics.drawLine(0, y2, w, y2);
         }
      }
      canvasGraphics.drawImage(image, 0, 0, this);
   }


   // Graduated colors.
   public static boolean[] graduatedColors        = null;
   public static int[]     graduatedColorMaximums = null;

   // Get event color.
   public static Color getEventColor(int dimension, int eventType)
   {
      switch (eventType)
      {
      case -1:
         return(Color.GRAY);

      case EMPTY_CELL_VALUE:
         return(EMPTY_CELL_COLOR);

      default:
         Random random = new Random();
         if ((graduatedColors == null) || !graduatedColors[dimension])
         {
            random.setSeed(((dimension + 3) * 1000) + eventType);
            float r = random.nextFloat();
            float g = random.nextFloat();
            float b = random.nextFloat();
            return(new Color(r, g, b));
         }
         else
         {
            random.setSeed(dimension + 3);
            float s = (float)eventType / (float)graduatedColorMaximums[dimension];
            int   r = 255 - (int)(255.0f * random.nextFloat() * s);
            int   g = 255 - (int)(255.0f * random.nextFloat() * s);
            int   b = 255 - (int)(255.0f * random.nextFloat() * s);
            return(new Color(r, g, b));
         }
      }
   }
}
