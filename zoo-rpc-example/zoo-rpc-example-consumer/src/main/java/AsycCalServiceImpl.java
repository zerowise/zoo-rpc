import com.github.zerowise.example.api.AsycCalService;

import java.util.concurrent.CompletableFuture;

/**
 ** @createtime : 2018/10/24 11:04 AM
 **/
public class AsycCalServiceImpl implements AsycCalService {
    @Override
    public CompletableFuture<Integer> add(int ma, int mb) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return ma + mb;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
