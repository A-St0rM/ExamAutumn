package app.security.interfaces;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public interface ISecurityController {
    Handler login(); // to get a token
    Handler register(); // to get a user
    Handler authenticate(); // to verify roles inside token
    Handler authorize();
    void healthCheck(Context ctx);
}
