# Test quantities of hidden neurons.
trials=10
echo "Test quantities of hidden neurons"
echo Trials = $trials
hiddenNeurons="25"
echo test hiddenNeurons = $hiddenNeurons
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./honey_bees.sh -randomSeed $i -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
    ./honey_bees.sh -randomSeed $i -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
done
hiddenNeurons="100"
echo test hiddenNeurons = $hiddenNeurons
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./honey_bees.sh -randomSeed $i -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
    ./honey_bees.sh -randomSeed $i -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
done

