##A model of a router for NUMA computers which use a static binary hypercube communication network.

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
3. Seriousify comments.
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

    G:
       1. Routing algorithms


    M:
       1. Packet generator
       2. interface

    :
       1. Buffer count per channel = ?
       2. Initializing



Transport Mask да се променя във всеки изходен канал, за да нямаме връщане назад.

