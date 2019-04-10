## About

This is a small bot intended for use on the Together Java discord server.

## Functions

We will see whether it evolves, but it will likely only handle smaller tasks for now.

## Technologies

* JDA
  * Discord API
* TOML4J
  * A Java implementation of the TOML config language
* Brigardier
  * Mojang's Minecraft command parser
* Logback and SLF4J
  * Logging
* Maven
  * Build system

Some day Junit5 might join that list…

## Contribute

You are welcome to raise issues or suggestions in the issue tracker here.
PRs are welcome too :)

### Setting up a local dev environment
1. You should probably clone this repository via `git clone https://github.com/I-Al-Istannen/TogetherJavaBot.git`
2. Enter the cloned directory and set up your IDE of choice
3. Copy the default bot config found in `src/main/resources` to another place and edit it to your heart's content.
  If you copy it to a file called `realized.toml` in the root of this project it is already covered by an ignore rule.
    * Edit the run configuration for the `ApplicationEntry` class to add an environment variable called `TJ_CONFIG_PATH` which contains the path to the config file.
    * **OR** pass the path to the config as the first command line argument
4. Set your bot token in the config
5. Run the `org.togetherjava.ApplicationEntry` class

#### Build instructions
This project uses `Maven` as its build system.
You can generate a jar file by running `mvn clean package` while inside the root of the project.
The jar file can then be found in the `target` folder.

### Writing a new command
1. Make a new class in the `org.togetherjava.command.commands` package.
  All classes in that package are automatically registered at runtime.
2. Make sure the class *has a constructor that takes no arguments* or takes a single `Toml` instance (the bot config).
3. Implement the mandated method:
   ```java
   public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
   ```
4. Build your command in there. Here is the echo command, which might serve as an example:
   ```java
   @Override
   public LiteralCommandNode<CommandSource> getCommand(CommandDispatcher<CommandSource> dispatcher) {
     return CommandGenericHelper.literal("echo")
         .then(
             CommandGenericHelper.argument("message", StringArgumentType.greedyString())
                 .executes(context -> {
                   CommandSource source = context.getSource();
                   String argument = context.getArgument("message", String.class);
 
                   source.getMessageSender()
                       .sendMessage(SimpleMessage.information(argument), source.getChannel());
                   return 0;
                 })
         )
         .build();
   }
   ```
5. For more information on adding commands, refer to the documentation for Brigardier or ask :)


## TODO
* Role assignment via reactions
* Rep? Maybe give/remove via a given emoji?
