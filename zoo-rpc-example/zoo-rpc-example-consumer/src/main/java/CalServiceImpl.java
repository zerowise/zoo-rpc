import com.github.zerowise.example.api.CalService;
import com.github.zerowise.rpc.handler.RpcService;

/**
 ** @createtime : 2018/10/23 4:52 PM
 **/

@RpcService
public class CalServiceImpl implements CalService {
    @Override
    public int add(int ma, int mb) {
        return ma + mb;
    }
}
