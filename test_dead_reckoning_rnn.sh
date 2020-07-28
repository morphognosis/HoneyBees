# Test dead reckoning RNN.
trials=10
echo "Test dead reckoning RNN"
echo Trials = $trials
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./dead_reckoning_rnn.sh -randomSeed $i -numTrain 20 -numTest 5 -verbose
done

