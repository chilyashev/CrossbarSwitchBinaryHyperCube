Simulation model of a static communication network using a binary hypercube architecture
===============================================================================================================
[![Build Status](https://travis-ci.org/chilyashev/CrossbarSwitchBinaryHyperCube.svg?branch=master)](https://travis-ci.org/chilyashev/CrossbarSwitchBinaryHyperCube)

<p align="center">
<img src="doc/icon.png"/>
</p>


This project is a simulation model of the communication network used by an MPP computer with a binary hypercube architecture.

Each node of the MPP contains a router with a crossbar switch commutator. The finished application lets users configure multiple model parameters: node count, transmitted packet size, queue size for each input channel, algorithm used for generating messages, etc. After finishing the simulation, detailed statistics are generated with which the effectiveness of the model’s configuration can be determined.

The step-by-step simulation lets you closely examine packet travel through the network:
![Step by step simulation](doc/screenshots/step-by-step-1.png)

You can also view the current state of a node in the network:
![Step by step simulation -> Node details](doc/screenshots/step-by-step-2.png)

A more detailed explanation  of the model and its underlying theory can be found on page 52 in the writeup for the [Fourth Scientific International Conference of Computer Sciences and Engineering](http://csejournal.cs.tu-varna.bg/cse_journal_1_2016.pdf) (pdf, in Bulgarian with excerpt in English).

Requirements
------------
1. Java 8 should be used for this here beautiful project
2. JavaFX should be installed, but that's not an issue, since it's bundled in Java SE


Building, running, packaging, etc.
----------------------------------
1. With maven
    1. Clone the project
    2. Just `mvn compile package` and if all is configured well, running `java -jar ./target/cbsbh.jar` should start the thing
2. With IDEA
    1. Clone the project
    2. Start IDEA
    3. "Open project"
    4. Navigate to the folder you cloned it
    5. `Ctrl+N`, type `Main`, `<Enter>`, `Ctrl+Shift+F10`
    6. Obviously, don't commit the .iml file

