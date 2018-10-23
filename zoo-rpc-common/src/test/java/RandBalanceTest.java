import com.github.zerowise.rpc.common.Weightable;
import com.github.zerowise.rpc.lb.LoadBalancer;
import com.github.zerowise.rpc.lb.RandomLoadBalancer;
import org.junit.Test;

import java.util.Arrays;

/**
 ** @createtime : 2018/10/23 4:26 PM
 **/
public class RandBalanceTest {

    @Test
    public void lb() {
        LoadBalancer<IntWeightable> weightableLoadBalancer = new RandomLoadBalancer<>();
        weightableLoadBalancer.updateWeightable(Arrays.asList(new IntWeightable(4), new IntWeightable(5), new IntWeightable(30), new IntWeightable(1)));
        for (int i = 0; i < 200; i++) {
            IntWeightable intWeightable = weightableLoadBalancer.select();
            intWeightable.cnt++;
        }

        weightableLoadBalancer.getWeigthables().forEach(intWeightable -> System.out.println(intWeightable.w + ":" + intWeightable.cnt));

    }

    class IntWeightable implements Weightable {
        public IntWeightable(int w) {
            this.w = w;
        }

        public int w;

        public int cnt;

        @Override
        public int weight() {
            return w;
        }
    }
}
