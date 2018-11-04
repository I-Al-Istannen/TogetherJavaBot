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

Some mayb Junit5 might join that list…

## Contribute

You are welcome to raise issues or suggestions in the issue tracker here.
PRs are welcome too :)

### Writing a new command
1. Make a new class in the `org.togetherjava.command.commands` package.
  All classes in that package are automatically registered at runtime.
2. Make sure the class *has a constructor that takes no arguments*!
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
5. For more infomation on adding commands, refer to the documentation for Brigardier or ask :)


## TODO
* Role assignment via reactions
* Rep? Maybe give/remove via a given emoji?