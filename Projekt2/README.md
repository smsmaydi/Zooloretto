# SoPra Projekt 2

## Wichtige Links

* Aktuelle Informationen zu diesem SoPra: https://sopra.cs.tu-dortmund.de/wiki/sopra/22b/start
* Beispielprojekt Kartenspiel War: https://sopra-gitlab.cs.tu-dortmund.de/internal/bgw-war
* Weitere Links: https://sopra.cs.tu-dortmund.de/wiki/infos/links/

### How does Redo and Undo work ?

- The game state is saved as a linked linst of **Game classes**
- The connection between the nodes of this linked list is determined by a **game action**
- With every game action that changes the state of the game (like changing the attributes of any entity class) a new
  node is added to the end of the list and the `rootService.game` reference is updated to point to the end of the list
- At the beginning there is only a node in this linked list (this is the game state when the game is started, here noted
  as **game0**) (`rootService.game` points to **game0** here)

 ```mermaid
graph LR;
    A(game0)
```

- After a game action like a money action is taken, a new game state is generated and `rootService.game` is updated to
  point to
  **game1** here

 ```mermaid
graph LR;
    A(game0)
    B(game1)
     A-->B;
     B-->A;
```

- After a series of taken actions the linked list looks like this and `rootService.game` now points to **game3**

 ```mermaid
graph LR;
    A(game0)
    B(game1)
    C(game2)
    D(game3)
     A-->B;
     B-->A;
     B-->C;
     C-->B;
     C-->D;
     D-->C;
```

- Now undo and redo is easy. We just update the pointer of `rootService.game` to point to the **previous node** in the
  linked list (in case of undo) or to point to the **next node** in the linked list (in case of redo)
- After each (undo,redo) the refreshables are called to render the state again

#### How to perform a player action ?

- For any player action that changes the game state a new node has to be created in the linked list first. All changes
  are made to the game referenced by that new node
- e.g let's say the current game state looks as follows (`rootService.game` points to **game1**) and an
  action `addTileToDeliveryTruck` needs to be done.

 ```mermaid
graph LR;
    A(game0)
    B(game1)
     A-->B;
     B-->A;
```

- first make sure to create a new node for the game state and to update `rootService.game` pointer and make it point to
  the new node
- then make all changes on that new node

- in this case the current game state looks like this (`rootService.game` points to **game2**)
- and the truck referenced by **game2** has an extra tile (the one which was added)

 ```mermaid
graph LR;
    A(game0)
    B(game1)
    C(game2)
     A-->B;
     B-->A;
     B-->C;
     C-->B;
```

```kotlin

/**
 * - adds the given tile to a delivery truck
 */
fun addTileToTruck(tile: Tile, truck: DeliveryTruck) {

    val game = rootService.game
    checkNotNull(game) { "game is null. cannot add tile to delivery truck" }


    // check if truck is full
    if (truck.tiles.size == MAX_NUM_TILES_PER_TRUCK) {
        throw  Error("cannot add a tile to this delivery truck. it's full")
    }


    // get the truck index in the game
    val truckIndex = game.deliveryTrucks.indexOf(truck)

    if (truckIndex == -1) {
        throw  Error("the given delivery truck does not exist in the game")
    }


    // update the linked list of saved game states (this action creates a new state)
    val newGame = this.createNewGameState(game)
    rootService.game = newGame

    val truckInNewGame = newGame.deliveryTrucks[truckIndex]

    truckInNewGame.tiles.add(tile) // the tile is added to the game referenced by the new node which was created (newGame)

    this.onAllRefreshables { refreshAfterGameStateChanged() }

}

private fun createNewGameState(game: Game): Game {
    // this is the part where a new node is created and added to the end of the linked list
    val newGame = game.clone()
    game.connect(newGame)
    return newGame // the newGame which is returned should be the new rootService.game
}

```

