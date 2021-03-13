#!/bin/bash
javac -classpath "../lib/weka.jar" -d . ../src/de/jannlab/misc/*.java ../src/de/jannlab/data/*.java ../src/morphognosis/*.java ../src/morphognosis/honey_bees/*.java
cp ../res/images/honeybee.png morphognosis/honey_bees
cp ../res/images/flower.png morphognosis/honey_bees
cp ../res/images/nectar.png morphognosis/honey_bees
cp ../res/sounds/bees.wav morphognosis/honey_bees
cp ../src/morphognosis/honey_bees/foraging_rnn.py .
jar cvfm ../bin/honey_bees.jar honey_bees.mf morphognosis de foraging_rnn.py
