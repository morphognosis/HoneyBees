# Test quantities of hidden neurons.
trials=10
echo "Test quantities of hidden neurons"
echo Trials = $trials
hiddenNeurons="25"
echo test hiddenNeurons = $hiddenNeurons
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    randomSeed=`expr $i + 1`
    ./honey_bees.sh -randomSeed $randomSeed -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
    ./honey_bees.sh -randomSeed $randomSeed -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
done
hiddenNeurons="100"
echo test hiddenNeurons = $hiddenNeurons
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    randomSeed=`expr $i + 1`    
    ./honey_bees.sh -randomSeed $randomSeed -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
    ./honey_bees.sh -randomSeed $randomSeed -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
done

