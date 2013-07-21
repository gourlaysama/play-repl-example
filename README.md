# a Scala REPL embedded in a Play app

An example of a *very* simple client that interfaces (through WebSockets) with a REPL running within a Play application. The REPL has access to the application state and can be used to change stuff.

## Running

 - Compile and `stage` (or `dist`) the project and then run it:

   ```sh
play stage
./target/start
```

   WARNING: the play application has to run in Prod mode (no `play run`), or the auto-reloading will prevent the REPL from working (too much classloader magic)

 - Browse to [http://localhost:9000/](http://localhost:9000/) and [http://localhost:9000/repl](http://localhost:9000/repl) for the REPL (with a modern browser).

 - Try changing some application state by typing in the REPL:

   ```scala
controllers.Application.someState = "hacked from REPL!"
```

  and then reloading the index page to see the change.

## The interesting bits

The REPL is instanciated and configured in `/app/REPL.scala`.
