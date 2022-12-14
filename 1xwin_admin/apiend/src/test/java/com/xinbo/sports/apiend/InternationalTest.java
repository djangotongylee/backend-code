package com.xinbo.sports.apiend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.apiend.aop.annotation.FieldAnnotation;
import com.xinbo.sports.apiend.io.EncryptDto;
import com.xinbo.sports.dao.generator.po.Promotions;
import com.xinbo.sports.dao.generator.service.PromotionsService;
import com.xinbo.sports.service.base.ActivityDescription;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.I18nUtils;
import com.xinbo.sports.utils.SpringUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author: wells
 * @date: 2020/7/7
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
public class InternationalTest {
    @Autowired
    private ActivityDescription activityDescription;

    @Test
    void translate() {
        //  var msg = I18nUtils.getLocaleMessage("HongKong  odds");
    }

    @Test
    void testJson() {
        String str = "{\n" +
                "\t\"code\": 0,\n" +
                "\t\"data\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"codeZh\": \"????????????\",\n" +
                "\t\t\t\"id\": 1,\n" +
                "\t\t\t\"promotionsResDtoList\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"category\": 1,\n" +
                "\t\t\t\t\t\"endedAt\": 1585991266,\n" +
                "\t\t\t\t\t\"id\": 1,\n" +
                "\t\t\t\t\t\"startedAt\": 1585991266\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"msg\": \"\"\n" +
                "}";
        var jsonObject = JSONObject.parseObject(str);
        var json = analysisJson(jsonObject);
        System.out.println("json=" + json);
    }

    public Object analysisJson(Object reqObject) {
        if (reqObject instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) reqObject;
            for (int i = 0; i < jsonArray.size(); i++) {
                analysisJson(jsonArray.get(i));
            }
        }
        if (reqObject instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) reqObject;
            Iterator<String> it = jsonObject.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Object object = jsonObject.get(key);
                //????????????????????????
                if (object instanceof JSONArray) {
                    JSONArray objArray = (JSONArray) object;
                    analysisJson(objArray);
                }
                //??????key????????????json??????
                else if (object instanceof JSONObject) {
                    analysisJson((JSONObject) object);
                }
                //??????key????????????
                else {
                    jsonObject.put(key, I18nUtils.getLocaleMessage(object.toString()));
                }
            }
        }
        return reqObject;
    }

    @Test
    void encrypt() {
        ApplicationContext context = SpringUtils.getApplicationContext();
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
        BeanDefinitionRegistry beanDefinitionRegistry = (DefaultListableBeanFactory) configurableContext.getBeanFactory();
        Class<EncryptDto> encryptDtoClass = EncryptDto.class;
        boolean b = encryptDtoClass.isAnnotationPresent(FieldAnnotation.class);
        if (b) {
            FieldAnnotation annotation = encryptDtoClass.getAnnotation(FieldAnnotation.class);
            //????????????
            boolean createFlag = annotation.createFlag();
            if (createFlag) {
                // get the BeanDefinitionBuilder
                BeanDefinitionBuilder beanDefinitionBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition("com.xinbo.sports.apiend.io.EncryptDto");
                // get the BeanDefinition
                BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
                // register the bean
                beanDefinitionRegistry.registerBeanDefinition("encryptDto", beanDefinition);
                EncryptDto dto = (EncryptDto) SpringUtils.getBean("encryptDto");
                String name = dto.getName();
                System.out.println("name=" + name);
            }
        }
    }

    @Autowired
    private PromotionsService promotionsServiceImpl;

    @Test
    void testActivity() {
        var list = promotionsServiceImpl.list();
        try {
            var langList = List.of("en", "th", "zh", "vi");
            for (Promotions x : list) {
                if (x.getId() != 6 && x.getId() != 9 && x.getId() != 12 && x.getId() != 13) {
                    JSONObject jsonObject = new JSONObject();
                    for (String lang : langList) {
                        // var description = activityDescription.getDescription(x.getId(), lang);
                        var path = "/Users/mac/Downloads/i18n/" + lang + "/promotions_" + x.getId() + ".txt";
                        File file = new File(path);
                        BufferedReader proBuffer = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                        String line = "";
                        var stringBuilder = new StringBuilder();
                        while ((line = proBuffer.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        var reStr = stringBuilder.toString();
                        if (Strings.isNotEmpty(reStr)) {
                            jsonObject.put(lang, reStr);
                        }
                    }
                    Promotions promotions = new Promotions();
                    promotions.setId(x.getId());
                    promotions.setDescript(jsonObject.toJSONString());
                    promotionsServiceImpl.updateById(promotions);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        //activityDescription.getStaticDescription(1);
        //activityDescription.getFileContent(1);
        // activityDescription.changeValueByPropertyName(
        //  "/i18n/messages_en_US.properties","????????????","Sports11");
    }

    @Test
    void testInstant() {
        var time1 = Instant.now().getEpochSecond();
        var time2 = DateNewUtils.now();
        var diff = time1 - time2;
        System.out.println("diff=" + diff);
    }
}