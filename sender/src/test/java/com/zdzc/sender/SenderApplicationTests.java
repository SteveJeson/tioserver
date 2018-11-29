package com.zdzc.sender;

import com.zdzc.sender.util.DateUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.util.Date;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class SenderApplicationTests {

    @Test
    public void contextLoads() throws UnsupportedEncodingException {
        String data = "TRVYP02,460015864927578,89860116851049315573#";
        System.out.println(data.substring(3, 7));
        System.out.println(data.substring(7, 22));
        String body = data.substring(7, data.length()-1);
        String str = new String(body.getBytes(), "UTF-8");
        System.out.println(str);
        String date = DateFormatUtils.format(new Date(DateUtil.getUTCTime()), "yyyyMMddHHmmss");
        System.out.println(date);
        String a = "f";
        try{
            int b = Integer.valueOf(a);
            System.out.println(b);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}
