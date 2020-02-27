# Test hive size.
trials=10
echo "Test hive size"
echo Trials = $trials
hiveRadius=2
echo test hiveRadius = #hiveRadius
for (( i=0; i< $trials; ++i)); do
    echo trial = $i
    ./honey_bees.bat -randomSeed $i -constantNectar -saveNN nn.dat -steps 20000 -display false -printCollectedNectar -hiveRadius $hiveRadius
    ./honey_bees.bat -randomSeed $i -constantNectar -driver metamorphNN -loadNN nn.dat -steps 20000 -display false -printCollectedNectar -hiveRadius $hiveRadius
done

