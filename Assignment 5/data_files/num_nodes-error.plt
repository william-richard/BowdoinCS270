set terminal postscript eps color linewidth 3 "Helvetica" 15

set title "Error vs Number of Hidden Nodes"

set xlabel "Number of Hidden Nodes"
set xrange [0:310]

set ylabel "Error"
set yrange [0:.35]

set output "num_nodes-error.eps"
plot "last_timestep.txt" using ($5):($2) ls 4 ti "Without Bias Node",\
"last_timestep_bias.txt" using ($5):($2) ls 3 ti "With Bias Node"
