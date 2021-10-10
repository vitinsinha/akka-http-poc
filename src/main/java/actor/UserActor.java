package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.FI;
import akka.pattern.PatternsCS;
import model.User;
import model.messages.ActionPerformed;
import model.messages.CreateUserMessage;
import model.messages.GetUserMessage;
import service.UserService;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class UserActor extends AbstractActor {

    private final ActorSystem system;
    private final Executor ec;
    private final UserService userService = new UserService();

    public UserActor(ActorSystem system) {
        this.system = system;
        this.ec = system.dispatchers().lookup("blocking-dispatcher");
    }

    public static Props props(ActorSystem system) {
        return Props.create(UserActor.class, system);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateUserMessage.class, handleCreateUser())
                .match(GetUserMessage.class, handleGetUser())
                .build();
    }

    private FI.UnitApply<CreateUserMessage> handleCreateUser() {
        return createUserMessageMessage -> {
            userService.createUser(createUserMessageMessage.getUser());
            sender().tell(new ActionPerformed(String.format("User %s created.", createUserMessageMessage.getUser()
                    .getName())), getSelf());
        };
    }

    private FI.UnitApply<GetUserMessage> handleGetUser() {
        return getUserMessage -> {
            CompletableFuture<Optional<User>> fut = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return userService.getUser(getUserMessage.getUserId());
            }, ec);
            PatternsCS.pipe(fut, getContext().dispatcher()).to(sender());
        };
    }

}
