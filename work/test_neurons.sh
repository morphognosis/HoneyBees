# Test quantities of hidden neurons.
trials=10
echo "Test quantities of hidden neurons"
echo Trials = $trials
hiddenNeurons="25"
echo test hiddenNeurons = $hiddenNeurons
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./honey_bees.bat -randomSeed $i -constantNectar -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
    ./honey_bees.bat -randomSeed $i -constantNectar -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -NNhiddenLayers $hiddenNeurons
done

