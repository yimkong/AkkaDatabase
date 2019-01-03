import com.akkademo.JClient;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * author yg
 * description
 * date 2018/12/29
 */
public class JClientIntegrationTest {
    JClient jClient = new JClient("127.0.0.1:2552");

    @Test
    public void itShouldSetRecord() throws ExecutionException, InterruptedException {
        jClient.set("123", 123);
        Integer result = (Integer) ((CompletableFuture) jClient.get("123")).get();
        assert (result == 123);
    }
}
