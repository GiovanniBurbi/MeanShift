# MeanShift
> Parallel computing project using Java and Java Threads

## Prerequisites
* Java 8
* Maven 3.8.2

## Brief introduction
For this project we implemented the **Mean Shift**, a non-parametric clustering algorithm based on kernel-density estimation that has a **quadratic computational complexity**.
We have exploited the **embarrassingly parallel structure** of the algorithm to improve its performance using **Java and its parallel mechanisms**.
We tried to efficiently use the cache using a certain data organization in memory called **Structure of Array** and we showed the difference of using the **Runnable and Callable interfaces** to implement the threads.
To perform the experiments the **mean shift clustering** has been used to perform image processing, more precisely **image segmentation**.

<div align="center">
<div>
<img src="mean-shift/src/main/resources/images/benzina400x300.jpg" width="35%"/>
<img src="images/result.jpg" width="35%""/>
</div>
</div>

                                         
For more details on the development process of the project and the tools used see the [Final Report]().
            

## Experiments setup
To obtain the results shown in the Final Report has been used a machine with:
* Intel© Core™ i7-8750H CPU @ 2.20GHz with 6 cores / 12 threads
                                         
## Getting started
### Using IDE
* Clone this repository in your IDE. **Eclipse** has been used for this project.
* Set **flags** in the header of "App.java" to select the implementation of the algorithm that will run.
* To **run the Maven build**
  * Run the root directory of the project as Maven build with goals:
    `clean verify`
* To **run the Java application**
   * In your IDE open the file "App.java" and run it as Java application.
