# Ladder Example Mod

- Dependencies:
    - Ladder API jar as libs/ladder-api-1.0-SNAPSHOT.jar
    - Ladder class stubs har as libs/ladder-stubs.jar
    - Ladder Fabric Impl as `io.github.ladder:ladder-impl-fabric:1.0-SNAPSHOT`, in some maven (e.g local)
    - Ladder Forge Impl as `io.github.ladder:ladder-impl-forge:1.0-SNAPSHOT`, in some maven (e.g local)
    
- To run:
    - `gradlew :fabric:runClient` to run the Fabric mod
    - `gradlew :forge:runClient` to run the Forge mod
    - Running those in IDEA works too (as well as debugging!)