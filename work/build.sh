#!/bin/bash
javac -classpath ../lib/morphognosis.jar -d . ../src/morphognosis/honey_bees/*.java
jar cvfm ../bin/honey_bees.jar honey_bees.mf morphognosis