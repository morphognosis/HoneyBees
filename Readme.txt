Honey bees nectar foraging.

Honey bees cooperatively forage using dancing behavior to communicate nectar direction and distance to other bees.
This project simulates this social behavior in a cellular automaton using the Morphognosis model.

World:
1. A hive is centrally located.
2. Flowers are randomly scattered about the vicinity of the hive.
3. Flowers randomly produce nectar.
4. Multiple bees forage for nectar which they bring back to the hive.

Bee capabilities:

Senses:
Hive presence.
Nectar presence.
In hive bee nectar signal: Orientation and distance to nectar.

Internal state:
Orientation.
Carrying nectar.

Responses: wait, forward, turn N, NE, E, SE, S, SW, W, NW, extract nectar, deposit nectar, display nectar distance.

Setup:

1. Clone or download and unzip the code from https://github.com/morphognosis/HoneyBees.
2. Optional: Import Eclipse project.
3. Optionally build (it comes pre-built): click or run the build.bat/build.sh in the work folder to build the code.

Run: 
1. Click on or run the honey_bees.bat/honey_bees.sh command in the work folder to bring up the display and dashboard.
2. The dashboard is set to the "autopilot" driver, which guides the bees to forage while accumulating metamorph rules.
3. Reset the environment.
4. Change the driver to "metamorphDB" which will utilize the metamorphs.

Neural network training:
Forage using the autopilot driver to creaete training dataset, then train using the dashboard.
The dataset can also be written out and used with your favorite machine learning tools, e.g. H2Oai (https://www.h2o.ai)

References:

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