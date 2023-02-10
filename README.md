# core-sketch

## Introduction

This is the code for our paper "*CORE-Sketch: On Exact Computation of Median Absolute Deviation with Limited Space*". The code is writen in JAVA, in the folder "code\src\main\java".



## Overview

For the subfolders, they all serve as JAVA packages, and the functions are listed as follows:

- benchmark: It contains the exact MAD algorithm No-Sketch and CORE-Sketch in EXACT_MAD.java and CORE_MAD.java respectively. The approximate MAD algorithm based on DD-Sketch is in DD_MAD.java.

- dataset: It gives the methods for generating synthetic datasets in synthetic.java.

- mad: It contains the data structure, CORE-Sketch and DD-Sketch used for computing MAD.

- utils: It contains the methods for file reading & writing, parameter setting, etc.

- experiments (Please make sure that the machine has enough memory (16GB for 1E9 data points). The tested data size and number of test repeated can be changed by modifying variables in code.):
  
  - ParallelExp.java is used for testing the time cost of CORE-SKETCH over parallel degree (Fig. 19). All the detailed test records can be shown by calling the *show_records()* method.
  
  - SpaceLimitExp.java is used for testing the time cost, space cost and relative error over bucket limit $M$ (Fig. 16, 17, 18). What should be noticed is that the content in *main()* should be modified to decide which sketch will be tested.
  
  - ApproxAndDataSizeExp.java is used for testing the time cost, space cost and relative error over data size $|\mathcal{D}|$  (Fig. 13, 14, 15). Similarly, the content in *main()* decides whether all data sizes and sketches will be tested or not. 