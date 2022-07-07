# Telegram bot skeleton  

This is the minimum required functionality for building simple and complex telegram bots

## Getting started
### Installation
The application doesn't require the installation of an additional environment and running like a usual Spring Boot application.  

Environment variables must be set before installation

| Variable name  | Value                                       |
|----------------|---------------------------------------------|
| TG_BOT_NAME    | Choose a name for your bot                  |
| TG_BOT_TOKEN   | Get a bot token in a telegram bot BotFather |

### Adding new telegram handlers
The main necessary functionality in order to process user requests and switch between handlers is already implemented in `TelegramBot`  

To add a new handler, create a new class in the `handler` package. This class must implement the `FlowHandler` interface and is 
annotated as a `@Service`

```java
@Service
public class FirstStepHandler implements FlowHandler {
    @Override
    public CheckPoint getCheckPoint() {
        return CheckPoint.FIRST_STEP;
    }

    @Override
    public Message greeting(final User user) {
        return Message.builder()
                .content(new Message.Content(String.format("Hi %s", user.getFirstName())))
                .keyboard(new Message.Keyboard(List.of(List.of("Go to second step")), KeyboardType.AUTO))
                .build();
    }

    @Override
    public Result handle(final TelegramContext context, final User user) {
        if (!context.getMessage().getValue().equals("Go to second step")) {
            // show the error message and stay on the same step
            return new Result(getCheckPoint(), new Message(new Message.Content("Incorrect input")), false);
        }

        // redirect to next step
        return new Result(CheckPoint.SECOND_STEP);
    }
}
```