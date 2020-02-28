# Test quantities of flowers/bees.
trials=10
echo "Test quantities of flowers/bees"
echo Trials = $trials
numBees=3
numFlowers=3
echo test numBees = $numBees numFlowers = $numFlowers
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./honey_bees.bat -randomSeed $i -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -numBees $numBees -numFlowers $numFlowers
    ./honey_bees.bat -randomSeed $i -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -numBees $numBees -numFlowers $numFlowers
done
numBees=5
numFlowers=5
echo test numBees = $numBees numFlowers = $numFlowers
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./honey_bees.bat -randomSeed $i -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -numBees $numBees -numFlowers $numFlowers
    ./honey_bees.bat -randomSeed $i -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -numBees $numBees -numFlowers $numFlowers
done
numBees=7
numFlowers=7
echo test numBees = $numBees numFlowers = $numFlowers
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./honey_bees.bat -randomSeed $i -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -numBees $numBees -numFlowers $numFlowers
    ./honey_bees.bat -randomSeed $i -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -numBees $numBees -numFlowers $numFlowers
done
