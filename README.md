##A model of a router for NUMA computers which use a static binary hypercube communication network.

[![Build Status](https://travis-ci.org/chilyashev/CrossbarSwitchBinaryHyperCube.svg?branch=master)](https://travis-ci.org/chilyashev/CrossbarSwitchBinaryHyperCube)

**TODO**: A Nice description


###Requirements
1. Java 7 should be used for this here beautiful project
2. JavaFX should be installed, but that's not an issue, since it's bundled in Java SE
3. ???
4. Profit


###TODO
1. Finish the damn thing
    1. The model
        - Runs in a separate thread, of course
    2. Data collection
    3. Data visualisation
    4. More goodies
    5. Error showing
2. Document it
3. Seriousify comments and documentation.
4. Tests?


###Building, Running, Packaging, etc

1. With maven
    1. clone the project
    2. Just `mvn compile package` and if all is configured well, running `java -jar ./target/cbsbh.jar` should start the thing
2. With IDEA
    1. clone the project
    2. Start IDEA
    3. "Open project"
    4. Navigate to the folder you cloned it
    5. `Ctrl+N`, type `Main`, `<Enter>`, `Ctrl+Shift+F10`
    6. Obviously, don't commit the .iml file



#TODO

->А->
A.hasSignal(...)

->B->
A.hasOutputSignal(...)


да разберем откъде идва CNT_EQU и как се издава.
Най-вероятно идва от FIFO опашката, когато се предаде и последния флит.


# План за действие.


1. Инициализация *check*
    1. Input/Output channels *check*
        1. RRA *check*
        2. FIFOQueues *check*
            1. Arbiters (lil' arby guys) *check*
    2. SMPNode *check*
    3. MPPNetwork *check*
2. Генериране на тестови данни
    0. Бернули.
    1. Съобщения
        1. По 1 пакет
        2. По 2 пакета
        3. По 4 пакета
        4. По 3 пакета
        5. По 5 пакета
    2. DMA-тата!
        1. Да се случва нещо, когато нещо се получи напълно
3. Статистика
    1. Graphics


4. Да се провери дали всички сигнали се зануляват, когато трябва.


- Изчисляване на всички сигнали, които зависят от сигнали на други чаркове (PACK_WAIT например)
        - WR_IN_FIFO

5. Да видим защо FIFO_BUSY не се сваля никога и как, по дяволите, входният канал ще си ходи из състоянията, след като всичките му опашки са винаги заети

6. Изпращането на съобщения **трябва** да се направи.











Рутирането се маже. В RRA grant-а не работи правилно.
























