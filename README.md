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


1. Първо се вика calculateState за всички елементи, след това се вика tick()!


да проследим в лога състоянията на изходния канал след изпращането на целия пакет (защото се опитва да праща null-ове)




TODO(2016-04-12/00:57): Да се разкара използването на B_FIFO_STATUS регистъра. Статуса на опашките да се следи от всяка опашка вътрешно с флаг.
B_FIFO_STATUS маже нещата. Все ощем не съм го усетил точно как ги маже, но знам ,че ги маже. Със състояние в опашката ще е по-чисто.



TODO(2016-04-12/08:23): Предното ТОДО сякаш можем да го игнорираме. Мисля, че го закрепих и без да го правим.
В момента в lolg-a от изходните канали се пращат едни много грантове, до края. We need to get all up in that shit.

TODO(2016-04-12/09:29): Копирам, за да не забравим:  

    не бях в subtle настроение
    първо един арбитър изпраща на много изходни канали рекуест
    по тоя случай после евентуално можеш да ми напомниш да ти покажа какво беше написал и как чупеше всичко
    :Д
    след като прати до всички изходни канали
    тези канали които могат да върнат грант връщат
    сега тука има 2 проблема
    единия е че е възможнo никой да не може да изпрати грант в момента
    и в този случай май се чупи всичкo
    трябва да го предвидим и да го оправим в кода
    втория проблем е
    че арбитърът изпраща рекуест
    до всички изходни канали
    без да се интересува
    дали те са готови да го поемат
    и стават така
    че изходния канал е в състояние 6
    арбитъра му праща рекуест
    рекуеста се добавя там в мапа на изходния канал
    обаче в състояние 6 мапа се зачиства
    и като мине в състояние 1 вече рекуест **йок**










