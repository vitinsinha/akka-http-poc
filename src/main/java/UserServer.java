import actor.UserActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import model.User;
import model.messages.ActionPerformed;
import model.messages.CreateUserMessage;
import model.messages.GetUserMessage;
import scala.concurrent.duration.Duration;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.server.PathMatchers.longSegment;
import static akka.http.javadsl.server.PathMatchers.segment;

class UserServer extends HttpApp {

    final Timeout timeout = new Timeout(Duration.create(1, TimeUnit.MINUTES));
    final Duration duration = Duration.create(1, TimeUnit.MINUTES);
    private final ActorRef userActor;
    private final ActorSystem system;
    private final ObjectMapper mapper = new ObjectMapper();

    UserServer(ActorRef userActor, ActorSystem system) {
        this.userActor = userActor;
        this.system = system;
    }

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("userServer");
        Config conf = ConfigFactory.load("application.conf");
        ActorRef userActor = system.actorOf(UserActor.props(system), "userActor");
        UserServer server = new UserServer(userActor, system);
        server.startServer("localhost", 8080, system);
    }

    @Override
    public Route routes() {
        return path("users", this::postUser)
                .orElse(path(segment("users").slash(longSegment()), this::getUser));
    }

    private Route getUser(Long id) {
        return get(() -> withRequestTimeout(duration, () -> {
            CompletionStage<Optional<User>> user = PatternsCS.ask(userActor, new GetUserMessage(id), timeout)
                    .thenCompose(o -> (CompletionStage<Optional<User>>) o);

            return onSuccess(() -> user, performed -> {
                if (performed.isPresent())
                    return complete(StatusCodes.OK, performed.get(), Jackson.marshaller());
                else
                    return complete(StatusCodes.NOT_FOUND);
            });
        }));
    }

    private Route postUser() {
        return post(() -> entity(Jackson.unmarshaller(User.class), user -> {
            CompletionStage<ActionPerformed> userCreated = PatternsCS.ask(userActor, new CreateUserMessage(user), timeout)
                    .thenApply(obj -> (ActionPerformed) obj);

            return onSuccess(() -> userCreated, performed -> complete(StatusCodes.CREATED, performed, Jackson.marshaller()));
        }));
    }
}