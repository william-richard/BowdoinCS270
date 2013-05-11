set terminal postscript eps color linewidth 3 "Helvetica" 15

set title "Error vs Learning Rate"

set xlabel "Learning Rate"
set xrange [0:0.11]

set ylabel "Error"
set yrange [0:.35]

set output "learning_rate-error.eps"
plot "last_timestep.txt" using ($4):($2) ls 4 ti "Without Bias Node",\
"last_timestep_bias.txt" using ($4):($2) ls 3 ti "With Bias Node"
