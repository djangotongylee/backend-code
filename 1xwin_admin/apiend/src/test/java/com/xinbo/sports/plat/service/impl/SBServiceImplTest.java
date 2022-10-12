package com.xinbo.sports.plat.service.impl;

import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.utils.TextUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.AccessType;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class SBServiceImplTest {
    @Autowired
    private IBCServiceImpl shaBaSportsServiceImpl;

    @Test
    void login() {
        var dto = PlatFactoryParams.PlatLoginReqDto.builder()
                .lang("zh")
                .username("wells0002")
                .build();
        //shaBaSportsServiceImpl.login(dto);
//        var transferDto = PlatFactoryParams.PlatCoinTransferReqDto.builder()
//                .coin(BigDecimal.valueOf(100))
//                .username("wells0002")
//                .orderId(TextUtils.generateRandomString(16))
//                .build();
        //shaBaSportsServiceImpl.coinUp(transferDto);
        //shaBaSportsServiceImpl.coinDown(transferDto);
        //var balance = PlatFactoryParams.PlatQueryBalanceReqDto.builder().username("wells0002").build();
        // shaBaSportsServiceImpl.queryBalance(balance);
        shaBaSportsServiceImpl.pullBetsLips();
    }

    @Test
    void service()
            throws ServletException, IOException {
        String vendor_id = "r8a5yrndrp";
        String operatorId = "XBSP";
        String username = "XBSP_test008";
        String vendor_member_id = "XBSP_test008";
        String oddstype = "1";
        String currency = "20";
        String maxtransfer = "500";
        String mintransfer = "50";

        String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
        data += "&" + URLEncoder.encode("vendor_id", "UTF-8") + "=" + vendor_id;
        data += "&" + URLEncoder.encode("operatorId", "UTF-8") + "=" + operatorId;
        data += "&" + URLEncoder.encode("vendor_member_id", "UTF-8") + "=" + vendor_member_id;
        data += "&" + URLEncoder.encode("oddstype", "UTF-8") + "=" + oddstype;
        data += "&" + URLEncoder.encode("currency", "UTF-8") + "=" + currency;
        data += "&" + URLEncoder.encode("maxtransfer", "UTF-8") + "=" + maxtransfer;
        data += "&" + URLEncoder.encode("mintransfer", "UTF-8") + "=" + mintransfer;
        System.out.println(data);
        String apiUrl = "http://tsa.ig128.com/api/CreateMember";
        byte[] postData = data.getBytes(StandardCharsets.UTF_8);
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("X-Requested-With", "X-Api-Client");
        conn.setRequestProperty("X-Api-Call", "X-Api-Client");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        os.write(postData);
        os.flush();
        os.close();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("response>>>" + conn.getResponseMessage());
            System.out.println("rsp code:" + conn.getResponseCode());
            String result = String.valueOf(sb);
            System.out.println("rsp:" + result);

        } else {
            System.out.println("response>>>" + conn.getResponseMessage());
            System.out.println("rsp code:" + conn.getResponseCode());
        }
    }

    @Test
    @SneakyThrows
    void getBet() {
        String version_key = "30309219";
        String data = URLEncoder.encode("version_key", "UTF-8") + "=" + URLEncoder.encode(version_key, "UTF-8");
        data += "&" + URLEncoder.encode("vendor_id", "UTF-8") + "=" + "r8a5yrndrp";
        String apiUrl = "http://tsa.ig128.com/api/GetBetDetail";
        byte[] postData = data.getBytes(StandardCharsets.UTF_8);
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("X-Requested-With", "X-Api-Client");
        conn.setRequestProperty("X-Api-Call", "X-Api-Client");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        os.write(postData);
        os.flush();
        os.close();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("response>>>" + conn.getResponseMessage());
            System.out.println("rsp code:" + conn.getResponseCode());
            String result = String.valueOf(sb);
            System.out.println("rsp:" + result);

        } else {
            System.out.println("response>>>" + conn.getResponseMessage());
            System.out.println("rsp code:" + conn.getResponseCode());
        }
    }
}