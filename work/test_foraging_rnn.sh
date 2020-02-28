# Test foraging RNN.
trials=10
echo "Test foraging RNN"
echo Trials = $trials
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./foraging_rnn.sh -randomSeed $i -numTrain 20 -numTest 5
done

