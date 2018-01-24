# City Yandere Carcassonne Web Application

This is the web version of [City Yandere Carcassonne](https://github.com/AndreasMReumschuessel/de.htwg.cityyanderecarcassonne/).
From the named repository was a .jar file built and added as a library in this project.
It uses the [Play Framework](https://www.playframework.com/) to run as a web application.

### Requirements
- Scala Built Tool (sbt) installed
- or Intellij IDEA with Scala plugin

### How to build
#### Console

1. Clone the git repository:
```
git clone https://github.com/AndreasMReumschuessel/de.htwg.cityyanderecarcassonne.web.git
```
2. Compile the web application:
```
sbt compile
```
3. Run it:
```
sbt run
```
4. Open `http://localhost:9000` in your webbrowser.

You can do point 2 and 3 in one step by just using `sbt run`. It will compile when you open the page, so don't worry if it takes some time loading the page when opening it for the first time.

#### Intellij IDEA
1. Import using Github in "Checkout from Version Control".
2. Import project from external model as "SBT" project.
3. Wait for idea to finish.
4. Create run configuration with "Run -> Edit Configurations...".
5. Add "SBT Task".
6. Enter `run` in the "Tasks:" text input.
7. Run you configuration.
8. Open `http://localhost:9000` in your webbrowser. The first load will take some time since it is compiling.

### Future plans
- Implement Silhouette with SSO
- Implement Chat with (maybe Polymer) and Websockets
