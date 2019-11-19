# (Hyper) Cubic Grid Searcher
This utility does a Monte Carlo search simulating an agent moving randomly along the edges of a cube from one corner to the opposite corner. It can use multiple cores and can compute the result to a user defined precision.

## Build
Built the utility from source using gradle wrapper. This was tested on Ubuntu 18.04.

```
./gradlew distZip
```

## Test
The tests focus on the validity and numerical stability of the statistical estimators, since this is the greatest source of error. A production application would have more tests.

```
./gradlew test
```

## Run
This is a command line application. To run, first build the package, unzip, cd into the directory, and run the init script. Several parameters can be passed th change the behavior of the app.
```
./gradlew distZip
cd build/distributions
unzip grid-0.0.1.zip
cd grid-0.0.1
./gridsearch.sh
```

By default it starts with three worker threads and fits to an error of 0.001. The following parameters can be adjusted:

```
 Usage: gridsearch [--num-worker-threads num] [--delta num] [--seed num] [--dim num]
```
--num-worker-threads defines the number of workers to use
--delta defines the statistical threshold and determines when to terminate the simulation
--seed is a seed for the random number generator
--dim is the number of dimensions of the cube (this simulation can run on hypercubes of any dimension)

## Executable

This package contains a prebuild package in the root directory in case something goes wrong with the build on another computer, as I only tested on my personal laptop.
