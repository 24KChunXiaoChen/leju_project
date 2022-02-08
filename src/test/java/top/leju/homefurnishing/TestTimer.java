package top.leju.homefurnishing;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TestTimer {

    @Test
    public void test01(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("进行定时操作");
                try { Thread.sleep(1000); } catch (InterruptedException e) {}

            }
        },0,1);
        try { Thread.sleep(5000); } catch (InterruptedException e) {}
        timer.cancel();
        try { Thread.sleep(10000); } catch (InterruptedException e) {}
    }
}
