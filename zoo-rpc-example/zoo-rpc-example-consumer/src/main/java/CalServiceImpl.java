import com.github.zerowise.example.api.CalService;

/**
 ** @createtime : 2018/10/23 4:52 PM
 **/
public class CalServiceImpl implements CalService {
    @Override
    public int add(int ma, int mb) {
        return ma + mb;
    }
}
