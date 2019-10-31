Honeybees

Honeybees cooperatively forage using signals to communicate nectar location.
This project simulates this social behavior in a cellular automaton using the Morphognosis model.

World:
1. A hive is centrally located.
2. Flowers are randomly scattered about the vicinity of the hive.
3. Flowers randomly produce nectar.
4. Multiple bees forage for nectar which they bring back to the hive.

Bee capabilities:

Senses:
1. Bee can sense its orientation: N, NE, E, SE, S, SW, W, NW.
2. Bee can sense if it carries nectar.
Relative to its orientation, bee can sense the contents of the cell immediately in front of it:
1. Adjacent bee:
   a. Orientation, or NA.
   b. Distance signal to nectar: none, near, medium, far, or NA.
2. Object: hive, outside, nearest flower distance or NA, adjacent flower with nectar or no nectar.

Responses: wait, forward, turn left, turn right, take nectar, drop nectar, signal nectar distance: near, medium, far.

Prerequesite: Java.

Setup:

1. Clone or download and unzip the code from https://github.com/morphognosis/Honeybees.
2. Optional: Import Eclipse project.
3. Optionally build (since it comes pre-built): click or run the build.bat/build.sh in the work folder to build the code.

Run: 
1. Click on or run the honeybees.bat/honeybees.sh command in the work folder to bring up the display and dashboard.
2. The dashboard is set to the "autopilot" driver, which guides the bees to forage while learning production rules.
3. Reset the environment.
4. Change the driver to "metamorphRules" which will utilize the learned production rules.

Neural network training:
Construct the nest using autopilot, then write out the training dataset using the dashboard.
The dataset can be used with your favorite machine learning tools, e.g. H2Oai (https://www.h2o.ai)

References:

Morphognostic honeybees communicating food location through dance movements:
	Paper: http://tom.portegys.com/research.html#honeybees
	Code: https://github.com/morphognosis/Honeybees

Generating an artificial nest building pufferfish in a cellular automaton through behavior decomposition:
	Paper: http://tom.portegys.com/research.html#pufferfish
	Code: https://github.com/morphognosis/Pufferfish
	
Morphognosis: the shape of knowledge in space and time:
	Paper: http://www.researchgate.net/publication/315112721_Morphognosis_the_shape_of_knowledge_in_space_and_time
	Code: https://github.com/morphognosis/Morphognosis
	
Learning C. elegans locomotion and foraging with a hierarchical space-time cellular automaton:	
	https://www.researchgate.net/publication/326832203_Learning_C_elegans_locomotion_and_foraging_with_a_hierarchical_space-time_cellular_automaton
	
Honeybee information:
https://askabiologist.asu.edu/bee-dance-game/introduction.html