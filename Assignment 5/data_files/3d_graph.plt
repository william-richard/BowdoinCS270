set xlabel "Alpha"

set ylabel "Number of Nodes"

set zlabel "Error"

splot "last_timestep.txt" using ($4):($5):($2)
