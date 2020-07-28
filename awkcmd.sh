tail $file
echo
awk ' NR % 3 == 0 { print; } ' $file
echo
awk ' NR % 3 == 0 { print; } ' $file | awk '{total+=$1; count++} END {print total/count }'
echo
awk ' NR % 3 == 1 { print; } ' $file
echo
awk ' NR % 3 == 1 { print; } ' $file | awk '{total+=$1; count++} END {print total/count }'
echo
awk ' NR % 3 == 2 { print; } ' $file
echo
awk ' NR % 3 == 2 { print; } ' $file | awk '{total+=$1; count++} END {print total/count }'

