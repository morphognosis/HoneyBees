# Honey bees nectar foraging.

Honey bees cooperatively forage using dancing behavior to communicate nectar direction and distance to other bees.
This project simulates this social behavior in a cellular automaton using the Morphognosis model.

<img src="http://tom.portegys.com/research/morphognosis/waggledance.jpg" width="200" height="200" />
Honey bee dance

## World.

1. A hive is centrally located.
2. Flowers are randomly scattered about the vicinity of the hive.
3. Flowers randomly produce nectar.
4. Multiple bees forage for nectar which they bring back to the hive.

## Bee capabilities.

Senses:
* Hive presence.
* Nectar presence.
* In-hive bee nectar signal: Orientation and distance to nectar.

Internal state:
* Orientation.
* Carrying nectar.

Responses: wait, forward, turn N, NE, E, SE, S, SW, W, NW, extract nectar, deposit nectar, display nectar distance.

## Requirements.

Java 1.8 or later.

## Setup.

1. Clone or download and unzip the code from https://github.com/morphognosis/HoneyBees.
2. Optional: Import Eclipse project.
3. Build: click or run the build.bat/build.sh in the work folder to build the code.

## Run.

1. Click on or run the honey_bees.bat/honey_bees.sh command in the work folder to bring up the display.
2. The driver is set to "autopilot", which guides the bees to forage while accumulating metamorph rules.
3. Reset the environment.
4. Change the driver to "metamorphDB" which will utilize the metamorphs.

<pre>
Usage:
  New run:
    java morphognosis.honey_bees.Main
      [-steps <steps> | -display <true | false> (default=true)]
      World parameters:
        [-worldDimensions <width> <height> (default=21 21)]
        [-hiveRadius <radius> (default=3)]
      Flower parameters:
        [-numFlowers <quantity> (default=3)]
        [-flowerNectarRegenerationTime <steps> (default=10)]
        [-flowerSurplusNectarProbability <probability> (default=0.5)]
      Honey bee parameters:
        [-numBees <quantity> (default=3)]
        [-beeTurnProbability <probability> (default=0.2)]
        [-beeReturnToHiveProbabilityIncrement <probability> (default=0.01)]
      Metamorph Weka neural network parameters:
        [-NNlearningRate <quantity> (default=0.1)]
        [-NNmomentum <quantity> (default=0.2)]
        [-NNhiddenLayers <quantity> (default="100")]
        [-NNtrainingTime <quantity> (default=5000)]
     [-driver <autopilot | metamorphDB | metamorphNN | local_override> (honey bees driver: default=autopilot)]
     [-randomSeed <random number seed> (default=4517)]
     [-printCollectedNectar]
     [-save <file name>]
     [-saveNN <metamorph neural network model file name>]
     [-loadNN <metamorph neural network model file name>]
     [-writeMetamorphDataset [<file name>] (write metamorph dataset file, default=metamorphs.csv)]
  Resume run:
    java morphognosis.honey_bees.Main
      -load <file name>
     [-steps <steps> | -display (default)]
     [-driver autopilot | metamorphDB | metamorphNN | local_override>
        (default=autopilot)]
     [-randomSeed <random number seed>]
     [-printCollectedNectar]
     [-save <file name>]
     [-saveNN <metamorph neural network model file name>]
     [-loadNN <metamorph neural network model file name>]
     [-writeMetamorphDataset [<file name>] (write metamorph dataset file, default=metamorphs.csv)]
  Print parameters:
    java morphognosis.honey_bees.Main -printParameters
  Version:
    java morphognosis.honey_bees.Main -version
Exit codes:
  0=success
  1=error
</pre>

## Other commands in work folder.

1. foraging_parameter_optimize.bat/.sh: Optimize parameters to favor foraging cooperation.
2. foraging_rnn.bat/.sh: Test the ability of an RNN to track relative location, a skill necessary to return to hive.  

## Neural network training.

Forage using the autopilot driver to create training dataset, then train using the controls.
The dataset can also be written out and used with your favorite machine learning tools.

## References

Morphognostic honey bees communicating food location through dance movements:
	Paper: http://tom.portegys.com/research.html#honey_bees
	Code: https://github.com/morphognosis/HoneyBees

Generating an artificial nest building pufferfish in a cellular automaton through behavior decomposition:
	Paper: http://tom.portegys.com/research.html#pufferfish
	Code: https://github.com/morphognosis/Pufferfish
	
Morphognosis: the shape of knowledge in space and time:
	Paper: http://www.researchgate.net/publication/315112721_Morphognosis_the_shape_of_knowledge_in_space_and_time
	Code: https://github.com/morphognosis/Morphognosis
	
Learning C. elegans locomotion and foraging with a hierarchical space-time cellular automaton:	
	https://www.researchgate.net/publication/326832203_Learning_C_elegans_locomotion_and_foraging_with_a_hierarchical_space-time_cellular_automaton
	
Honey bee dance information:
https://askabiologist.asu.edu/bee-dance-game/introduction.html